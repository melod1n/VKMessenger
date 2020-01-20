package ru.melod1n.vk.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.recycler_view.*
import kotlinx.android.synthetic.main.toolbar.*
import ru.melod1n.vk.R
import ru.melod1n.vk.adapter.MessageAdapter
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.contract.BaseContract.Presenter
import ru.melod1n.vk.mvp.presenter.MessagesPresenter
import ru.melod1n.vk.util.AndroidUtils
import ru.melod1n.vk.util.ViewUtils
import kotlin.random.Random

class MessagesActivity : AppCompatActivity(), BaseContract.View<VKMessage>, BaseAdapter.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val TAG = "MessagesActivity"

        const val MESSAGES_COUNT = 30

        const val TAG_EXTRA_CONVERSATION = "dialog"
        const val TAG_EXTRA_TITLE = "title"
        const val TAG_EXTRA_AVATAR = "avatar"
    }

    private lateinit var conversation: VKConversation

    private var title: String? = null
    private var avatar: String? = null

    private var peerId = 0

    private var presenter: Presenter<VKMessage>? = null

    private var adapter: MessageAdapter? = null

    private fun onLoad() {
        presenter!!.onValuesLoading()
        presenter!!.onRequestClearList()
        presenter!!.onRequestLoadValues(peerId, 0, MESSAGES_COUNT)
    }

    override fun onDestroy() {
        super.onDestroy()

        adapter?.onDestroy()
        presenter = null

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        presenter = MessagesPresenter(this)

        initExtraData()

        prepareNavigationView()
        prepareToolbar()
        prepareRefreshLayout()
        prepareRecyclerView()
        prepareActionButton()

        val viewedDialogs = MainActivity.viewedDialogs
        if (AndroidUtils.hasConnection() && !viewedDialogs.contains(peerId)) {
            viewedDialogs.add(peerId)
            onLoad()
        } else {
            presenter!!.onRequestLoadCachedValues(peerId, 0, MESSAGES_COUNT)
        }
    }

    private fun prepareNavigationView() {
        ViewUtils.prepareNavigationHeader(navigationView, null)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.navigationConversations)
    }

    private fun prepareToolbar() {
        setSupportActionBar(toolbar)
        setTitle(title)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.navigationIcon?.setTint(AppGlobal.colorAccent)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun prepareActionButton() {
        chatSend.setOnClickListener {
            val message = chatMessage.text.toString().trim()
            if (message.isEmpty()) {
                return@setOnClickListener
            } else {
                sendMessage(message)
            }
        }
    }

    private fun prepareRefreshLayout() {
        swipeRefreshLayout.isEnabled = false
    }

    private fun prepareRecyclerView() {
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        layoutManager.stackFromEnd = true

        recyclerView.layoutManager = layoutManager
    }

    private fun initExtraData() {
        conversation = intent.getSerializableExtra(TAG_EXTRA_CONVERSATION) as VKConversation
        title = intent.getStringExtra(TAG_EXTRA_TITLE)
        avatar = intent.getStringExtra(TAG_EXTRA_AVATAR)

        peerId = conversation.id
    }

    private fun sendMessage(text: String) {
        adapter ?: return

        val message = VKMessage().apply {
            this.date = (System.currentTimeMillis() / 1000).toInt()
            this.text = text
            this.isOut = true
            this.peerId = this@MessagesActivity.peerId
            this.fromId = UserConfig.userId
            this.randomId = Random.nextInt()
        }

        adapter!!.notifyItemInserted(adapter!!.itemCount - 1)
        adapter!!.add(message)

        recyclerView.smoothScrollToPosition(adapter!!.realItemCount - 1)

        TaskManager.execute {
            VKApi.messages().send()
                    .peerId(peerId)
                    .message(text)
                    .randomId(message.randomId)
                    .execute(Int::class.java, object : VKApi.OnResponseListener<Int> {
                        override fun onSuccess(models: ArrayList<Int>) {
                            message.id = models[0]

                            CacheStorage.insertMessage(message)
                        }

                        override fun onError(e: Exception) {
                            e.printStackTrace()
                        }
                    })
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigationConversations,
            R.id.navigationSettings,
            R.id.navigationFriends,
            R.id.navigationImportant,
            R.id.navigationSearch -> {
                setResult(Activity.RESULT_OK, Intent().apply { putExtra("item_id", item.itemId) })
                finish()

                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            else -> false
        }
    }

    override fun showNoItemsView(visible: Boolean) {
        Log.d(TAG, "showNoItemsView: $visible")
    }

    override fun showNoInternetView(visible: Boolean) {
        Log.d(TAG, "showNoInternetView: $visible")
    }

    override fun showErrorView(errorTitle: String, errorDescription: String) {
        Log.d(TAG, "showErrorView: $errorTitle: $errorDescription")
        if (!AndroidUtils.hasConnection()) {
            presenter!!.onRequestLoadCachedValues(0, 0, MESSAGES_COUNT)
        }
    }

    override fun hideErrorView() {
        Log.d(TAG, "hideErrorView")
    }

    override fun showRefreshLayout(visible: Boolean) {}

    override fun showProgressBar(visible: Boolean) {
        findViewById<ProgressBar>(R.id.progressBar).visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun loadValuesIntoList(offset: Int, values: ArrayList<VKMessage>, isCache: Boolean) {
        Log.d(TAG, "loadValuesIntoList: " + offset + ", " + values.size)
        if (values.isEmpty()) return

        values.reverse()

        if (adapter == null) {
            adapter = MessageAdapter(this, values).also {
                it.onItemClickListener = this
            }

            adapter!!.addFooter(generateEmptyView())

            recyclerView!!.adapter = adapter
            return
        }

        if (recyclerView!!.adapter == null) {
            recyclerView!!.adapter = adapter
        }

        if (offset == 0) {
            recyclerView!!.scrollToPosition(adapter!!.itemCount - 1)
        }

        if (offset > 0) {
            adapter!!.addAll(values)
            adapter!!.notifyItemRangeInserted(offset, values.size)
            return
        }

        adapter!!.values = values
        adapter!!.notifyDataSetChanged()
    }

    private fun generateEmptyView(): View {
        return View(this).apply {
            isFocusable = false
            isClickable = false
            isEnabled = false
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtils.px(96f))
        }
    }

    override fun clearList() {
        Log.d(TAG, "clearList")
        if (adapter == null) return
        adapter!!.clear()
        adapter!!.notifyDataSetChanged()
    }

    override fun onItemClick(position: Int) {
        val message = adapter!!.getItem(position)

        Toast.makeText(this, message.text, Toast.LENGTH_SHORT).show()
    }
}