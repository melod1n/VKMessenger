package ru.melod1n.vk.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import butterknife.ButterKnife
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import ru.melod1n.vk.R
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.FragmentSwitcher
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.current.BaseActivity
import ru.melod1n.vk.database.CacheStorage.insertUsers
import ru.melod1n.vk.database.MemoryCache.getUser
import ru.melod1n.vk.fragment.FragmentConversations
import ru.melod1n.vk.fragment.FragmentLogin
import ru.melod1n.vk.fragment.FragmentSettings
import ru.melod1n.vk.service.LongPollService
import ru.melod1n.vk.util.ViewUtils.prepareNavigationHeader
import java.util.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var toggleDrawable: DrawerArrowDrawable? = null
    private var toggleClick: View.OnClickListener? = null
    private val fragmentConversations = FragmentConversations(R.string.navigation_conversations)
    private val fragmentSettings = FragmentSettings()
    private val fragmentLogin = FragmentLogin(R.string.fragment_login)
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
    }

    private fun prepareDrawerToggle() {
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        toggleDrawable = DrawerArrowDrawable(this)
        toggleDrawable!!.color = AppGlobal.colorAccent

        toggle.drawerArrowDrawable = toggleDrawable!!

        drawerLayout!!.addDrawerListener(toggle)

        toggle.isDrawerSlideAnimationEnabled = false
        toggle.syncState()

        toggleClick = View.OnClickListener { drawerLayout!!.openDrawer(navigationView!!) }
    }

    private fun prepareToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun prepareNavigationView() {
        navigationView!!.setNavigationItemSelectedListener(this)
        prepareNavigationHeader(navigationView!!, null)
    }

    private fun checkExtraData() {
        if (intent.hasExtra("token")) {
            val token = intent.getStringExtra("token")
            val userId = intent.getIntExtra("user_id", -1)
            UserConfig.setToken(token)
            UserConfig.setUserId(userId)
            UserConfig.save()
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
            openLoginScreen()
        }
    }

    private fun startLongPoll() {
        startService(Intent(this, LongPollService::class.java))
    }

    private fun openLoginScreen() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar.navigationIcon = null
        FragmentSwitcher.instance.switchFragment(this, fragmentLogin)
    }

    private fun openConversationsScreen() {
        FragmentSwitcher.instance.switchFragment(this, fragmentConversations)
    }

    private fun loadProfileInfo() {
        TaskManager.execute {
            VKApi.users().get().fields(VKUser.DEFAULT_FIELDS).execute(VKUser::class.java, object : OnResponseListener<VKUser> {
                override fun onSuccess(models: ArrayList<VKUser>) {
                    val profileUser = models[0]
                    insertUsers(profileUser.asList())
                    prepareNavigationHeader(navigationView!!, profileUser)
                }

                override fun onError(e: Exception) {
                    val cachedUser = getUser(UserConfig.getUserId())
                    if (cachedUser != null) {
                        prepareNavigationHeader(navigationView!!, cachedUser)
                    } else {
                        Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
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