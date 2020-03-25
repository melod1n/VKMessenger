package ru.melod1n.vk.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.recycler_view.*
import ru.melod1n.vk.R
import ru.melod1n.vk.adapter.MessageAdapter
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.api.util.VKUtil
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.database.MemoryCache
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.contract.BaseContract.Presenter
import ru.melod1n.vk.mvp.presenter.MessagesPresenter
import ru.melod1n.vk.util.AndroidUtils
import ru.melod1n.vk.util.ViewUtils
import kotlin.random.Random

class MessagesActivity : AppCompatActivity(), BaseContract.View<VKMessage>, BaseAdapter.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener, TaskManager.OnEventListener {

    companion object {
        const val TAG = "MessagesActivity"

        const val MESSAGES_COUNT = 100

        const val TAG_EXTRA_CONVERSATION = "dialog"
        const val TAG_EXTRA_TITLE = "title"
        const val TAG_EXTRA_AVATAR = "avatar"
    }

    private lateinit var conversation: VKConversation

    private var title: String? = null
    private var avatar: String? = null

    private var lastMessageText = ""

    private var peerId = 0

    private var presenter: Presenter<VKMessage>? = null

    private var adapter: MessageAdapter? = null

    private var loadedId = false

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
            chatInfo.setText(R.string.loading)
            viewedDialogs.add(peerId)
            onLoad()
        } else {
            onLoad()
//            presenter!!.onRequestLoadCachedValues(peerId, 0, MESSAGES_COUNT)
        }
    }

    override fun onNewEvent(event: EventInfo<*>) {
        when (event.key) {
            EventInfo.USER_UPDATE -> {
                setChatInfoText()
            }
        }
    }

    private fun setChatInfoText() {
        val info = getChatInfo()
        chatInfo.apply {
            if (info.isNullOrEmpty())
                visibility = View.GONE
            else {
                visibility = View.VISIBLE
                text = info
            }
        }
    }

    private fun prepareNavigationView() {
        ViewUtils.prepareNavigationHeader(navigationView, null)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.navigationConversations)
    }

    private fun prepareToolbar() {
        setSupportActionBar(toolbar)

        val placeholder = TextDrawable
                .builder()
                .buildRound(if (title!!.isEmpty()) "" else title!!.substring(0, 1), AppGlobal.colorAccent)

        chatAvatar.setImageDrawable(placeholder)

        try {
            Picasso.get().load(avatar).placeholder(placeholder).into(chatAvatar)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        chatTitle.text = title

        supportActionBar?.setDisplayShowTitleEnabled(false)
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

        lastMessageText = text

        val message = VKMessage().apply {
            this.date = (System.currentTimeMillis() / 1000).toInt()
            this.text = text
            this.isOut = true
            this.peerId = this@MessagesActivity.peerId
            this.fromId = UserConfig.userId
            this.randomId = Random.nextInt()
        }

        chatMessage.setText("")

        adapter!!.add(message)
        adapter!!.notifyDataSetChanged()

        recyclerView.smoothScrollToPosition(adapter!!.itemCount - 1)

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

        VKUtil.sortMessagesByDate(values, false)
        VKUtil.prepareList(values)

        setChatInfoText()

        if (adapter == null) {
            adapter = MessageAdapter(this, values).also {
                it.onItemClickListener = this
            }

//            adapter!!.addFooter(generateEmptyView())

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

    private fun getChatInfo(): String? {
        return when (conversation.type) {
            VKConversation.TYPE_CHAT -> {
                if (conversation.isGroupChannel) {
                    getString(R.string.group_channel_members, conversation.membersCount)
                } else {
                    getString(R.string.chat_members, conversation.membersCount)
                }
            }
            VKConversation.TYPE_USER -> {
                getUserOnline()
            }
            else -> null
        }
    }

    private fun getUserOnline(): String? {
        val user = MemoryCache.getUser(conversation.id)

        if (!loadedId) {
            loadedId = true
            TaskManager.loadUser(conversation.id)
        }

        return if (user == null) {
            null
        } else {
            VKUtil.getUserOnline(user)
        }
    }

//    private fun generateEmptyView(): View {
//        return View(this).apply {
//            isFocusable = false
//            isClickable = false
//            isEnabled = false
//            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtils.px(96F))
//        }
//    }

    class TimeStamp(var string: String = "") : VKMessage()

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