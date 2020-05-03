package ru.melod1n.vk.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import butterknife.ButterKnife
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import ru.melod1n.vk.R
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.common.FragmentSwitcher
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.current.BaseActivity
import ru.melod1n.vk.database.MemoryCache
import ru.melod1n.vk.fragment.FragmentConversations
import ru.melod1n.vk.fragment.FragmentFriends
import ru.melod1n.vk.fragment.FragmentLogin
import ru.melod1n.vk.fragment.FragmentSettings
import ru.melod1n.vk.service.LongPollService
import ru.melod1n.vk.util.AndroidUtils
import ru.melod1n.vk.util.ViewUtils
import java.util.*


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, TaskManager.OnEventListener {

    private var toggleDrawable: DrawerArrowDrawable? = null
    private var toggleClick: View.OnClickListener? = null

    private val fragmentConversations = FragmentConversations()
    private val fragmentFriends = FragmentFriends()
    private val fragmentSettings = FragmentSettings()
    private val fragmentLogin = FragmentLogin()

    private var selectedId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)

        prepareToolbar()
        prepareNavigationView()
        prepareDrawerToggle()
        checkExtraData()
        checkLogin(savedInstanceState)

        TaskManager.addOnEventListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        TaskManager.removeOnEventListener(this)
    }

    override fun onNewEvent(event: EventInfo<*>) {
        when (event.key) {
            EventInfo.USER_UPDATE -> {
                prepareNavigationHeader(MemoryCache.getUser(UserConfig.userId) ?: VKUser())
            }
        }
    }

    private fun prepareDrawerToggle() {
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)

        toggleDrawable = DrawerArrowDrawable(this)
        toggleDrawable!!.color = AppGlobal.colorAccent

        toggle.drawerArrowDrawable = toggleDrawable!!

        drawerLayout.addDrawerListener(toggle)

        toggle.isDrawerSlideAnimationEnabled = false
        toggle.syncState()

        toggleClick = View.OnClickListener { drawerLayout!!.openDrawer(navigationView!!) }
    }

    private fun prepareToolbar() {
        setSupportActionBar(toolbar)

        toolbar.setOnLongClickListener {
            val currentFragment = FragmentSwitcher.getCurrentFragment(supportFragmentManager)
            if (currentFragment is FragmentLogin) {
                AlertDialog.Builder(this).apply {
                    setTitle(R.string.custom_data)

                    val userId = AppCompatEditText(this@MainActivity).apply {
                        inputType = InputType.TYPE_CLASS_NUMBER
                        setHint(R.string.user_id)
                        maxLines = 1
                    }

                    val token = AppCompatEditText(this@MainActivity).apply {
                        setHint(R.string.token)
                        maxLines = 1
                    }

                    val layout = LinearLayout(this@MainActivity).also {
                        it.orientation = LinearLayout.VERTICAL
                        it.addView(userId)
                        it.addView(token)
                    }

                    setView(layout)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        val id = userId.text.toString().toInt()
                        val accessToken = token.text.toString()

                        if (id < 1 || TextUtils.isEmpty(accessToken)) return@setPositiveButton

                        UserConfig.apply {
                            this.userId = userId.text.toString().toInt()
                            this.token = token.text.toString()
                        }.save()

                        finish()
                        startActivity(Intent(this@MainActivity, MainActivity::class.java))
                    }

                    setCancelable(false)
                    setNegativeButton(android.R.string.cancel, null)
                }.show()
            }

            false
        }
    }

    private fun prepareNavigationView() {
        navigationView.layoutParams?.width = AppGlobal.screenWidth - AppGlobal.screenWidth / 6

        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun checkExtraData() {
        if (intent.hasExtra("token")) {
            val token = intent.getStringExtra("token")
            val userId = intent.getIntExtra("user_id", -1)

            UserConfig.token = token
            UserConfig.userId = userId
            UserConfig.save()

        } else if (intent.hasExtra("open_login")) {
            openLoginScreen()
        }
    }

    private fun checkLogin(savedInstanceState: Bundle?) {
        if (UserConfig.isLoggedIn) {
            loadProfileInfo()
            startLongPoll()

            if (savedInstanceState == null) {
                selectedId = R.id.navigationConversations
                navigationView!!.setCheckedItem(selectedId)
                openConversationsScreen()
            }
        } else {
            if (!intent.hasExtra("open_login"))
                openStartScreen()
        }
    }

    private fun startLongPoll() {
        startService(Intent(this, LongPollService::class.java))
    }

    private fun openStartScreen() {
        finish()
        startActivity(Intent(this, StartActivity::class.java))
        overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit)
    }

    private fun openLoginScreen() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar.navigationIcon = null
        FragmentSwitcher.instance.switchFragment(this, fragmentLogin)
    }

    private fun openConversationsScreen() {
        FragmentSwitcher.instance.switchFragment(this, fragmentConversations)
    }

    private fun openFriendsScreen() {
        FragmentSwitcher.instance.switchFragment(this, fragmentFriends)
    }

    private fun loadProfileInfo() {
        if (AndroidUtils.hasConnection()) {
            TaskManager.loadUser(UserConfig.userId)
        }

        val user = MemoryCache.getUser(UserConfig.userId) ?: return

        prepareNavigationHeader(user)
    }

    private fun prepareNavigationHeader(user: VKUser) {
        ViewUtils.prepareNavigationHeader(navigationView, user)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FROM_DRAWER && resultCode == Activity.RESULT_OK) {
            val itemId = data?.getIntExtra("item_id", -1) ?: -1
            if (itemId == -1) return
            switchFragment(itemId)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        switchFragment(item.itemId)
        return true
    }

    private fun switchFragment(itemId: Int) {
        var valid = true

        when (itemId) {
            R.id.navigationConversations -> openConversationsScreen()
            R.id.navigationFriends -> openFriendsScreen()
            R.id.navigationSettings -> FragmentSwitcher.instance.switchFragment(this, fragmentSettings)
            else -> valid = false
        }

        if (!valid) return

        if (selectedId != itemId) {
            selectedId = itemId
            navigationView!!.setCheckedItem(selectedId)
        }

        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }
    }

    fun setNavigationIcon(drawable: Drawable?) {
        toolbar!!.navigationIcon = drawable ?: toggleDrawable
    }

    fun setNavigationClick(listener: View.OnClickListener?) {
        toolbar!!.setNavigationOnClickListener(listener ?: toggleClick)
    }

    override fun onBackPressed() {
        val currentFragment = FragmentSwitcher.getCurrentFragment(supportFragmentManager)
        if (currentFragment != null && currentFragment.javaClass == FragmentSettings::class.java && (currentFragment as FragmentSettings).onBackPressed()) {
            super.onBackPressed()
        } else {
            if (drawerLayout!!.isDrawerOpen(navigationView!!)) {
                drawerLayout!!.closeDrawer(navigationView!!)
            } else {
                if (currentFragment != null && currentFragment.javaClass != FragmentSettings::class.java) super.onBackPressed()
            }
        }
    }

    companion object {
        const val REQUEST_CODE_FROM_DRAWER = 1
        var viewedDialogs = ArrayList<Int>()
    }
}