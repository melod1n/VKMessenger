package ru.melod1n.vk.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.no_internet_view.*
import kotlinx.android.synthetic.main.no_items_view.*
import kotlinx.android.synthetic.main.recycler_view.*
import ru.melod1n.vk.R
import ru.melod1n.vk.adapter.MessageAdapter
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.api.model.VKModel
import ru.melod1n.vk.api.util.VKUtil
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.current.BaseActivity
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.database.MemoryCache
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.contract.BaseContract.Presenter
import ru.melod1n.vk.mvp.presenter.MessagesPresenter
import ru.melod1n.vk.util.AndroidUtils
import kotlin.random.Random

class MessagesActivity : BaseActivity(),
        BaseContract.View<VKMessage>,
        BaseAdapter.OnItemClickListener,
        TaskManager.OnEventListener {

    companion object {
        const val TAG = "MessagesActivity"

        const val MESSAGES_COUNT = 30

        const val TAG_EXTRA_CONVERSATION = "dialog"
        const val TAG_EXTRA_TITLE = "title"
        const val TAG_EXTRA_AVATAR = "avatar"
        const val TAG_ID = "id"
    }

    private var isEdit = false

    private var fabState = FabState.VOICE

    private enum class FabState {
        VOICE, SEND, EDIT, DELETE, BLOCKED
    }

    private var conversation: VKConversation? = null

    private var title = ""
    private var avatar = ""

    private var lastMessageText = ""
    private var attachments = ArrayList<VKModel>()

    private var peerId = 0

    private lateinit var presenter: Presenter<VKMessage>

    private var adapter: MessageAdapter? = null

    private var loadedId = false

    override fun onDestroy() {
        super.onDestroy()

        adapter?.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        initExtraData()

        prepareToolbar()
        prepareRefreshLayout()
        prepareRecyclerView()
        prepareEditText()
        prepareNoItemsView()
        prepareNoInternetView()

        presenter = MessagesPresenter(this)
        (presenter as MessagesPresenter).readyForLoading()

        if (conversation == null) {
            loadConversation()
            return
        }

        initData()
    }

    private fun refreshData() {
        if (hasConnection()) {
            presenter.readyForLoading()
            presenter.requestValues(peerId, 0, MESSAGES_COUNT)
        } else {
            showNoInternetView(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_messages, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.messagesRefresh -> refreshData()
        }

        return super.onOptionsItemSelected(item)
    }

    fun getRecyclerView(): RecyclerView {
        return recyclerView
    }

    private fun prepareNoItemsView() {
        noItemsRefresh.setOnClickListener {
            if (hasConnection()) {
                refreshData()
            } else {
                showNoInternetView(true)
            }
        }

        noItemsText.text = getString(R.string.dialog_is_empty)
    }

    private fun prepareNoInternetView() {
        noInternetUpdate.setOnClickListener {
            if (hasConnection()) {
                refreshData()
            } else {
                Snackbar.make(noInternetView, R.string.no_connection, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadConversation() {
        TaskManager.loadConversation(peerId, object : VKApi.OnResponseListener<VKConversation> {
            override fun onSuccess(models: ArrayList<VKConversation>) {
                conversation = models[0]
                initData()
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun initData() {
        checkAllowedWriting()

        val viewedDialogs = MainActivity.viewedDialogs

        presenter.requestCachedValues(peerId, 0, MESSAGES_COUNT)

        if (adapter?.values?.size == 1) {
            showProgressBar(true)
        }

        if (hasConnection() && !viewedDialogs.contains(peerId)) {
            chatInfo.setText(R.string.loading)
            viewedDialogs.add(peerId)

            refreshData()
        } else {
            showProgressBar(false)
        }

        refreshFabStyle()
    }

    private fun checkAllowedWriting() {
        if (conversation!!.isGroupChannel) {
            chatPanel.visibility = View.GONE
            return
        }

        chatPanel.visibility = View.VISIBLE

        if (conversation!!.isAllowed) {
            fabState = FabState.VOICE
            chatSend.imageTintList = ColorStateList.valueOf(AppGlobal.colorAccent)
            chatMessage.isEnabled = true
            chatPanel.setBackgroundResource(R.drawable.chat_panel_background)
        } else {
            fabState = FabState.BLOCKED
            chatSend.imageTintList = ColorStateList.valueOf(Color.WHITE)
            chatMessage.isEnabled = false
            chatMessage.setHintTextColor(Color.WHITE)
            chatMessage.setHint(R.string.no_access)
            chatPanel.setBackgroundResource(R.drawable.chat_panel_background_blocked)
        }
    }

    private fun prepareEditText() {
        chatMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                fabState = if (count == 0) {
                    if (isEdit) {
                        FabState.DELETE
                    } else {
                        FabState.VOICE
                    }
                } else {
                    if (isEdit) {
                        FabState.EDIT
                    } else {
                        FabState.SEND
                    }
                }

                refreshFabStyle()
            }
        })
    }

    override fun onNewEvent(event: EventInfo<*>) {
        when (event.key) {
            EventInfo.USER_UPDATE -> {
                setChatInfoText()
            }
        }
    }

    private fun refreshFabStyle() {
        chatSend.isClickable = true
        when (fabState) {
            FabState.VOICE -> {
                chatSend.apply {
                    setImageResource(R.drawable.ic_mic)
                    setOnClickListener {
                        showVoiceTip()
                    }
                    setOnLongClickListener {
                        recordVoice()
                        true
                    }
                }
            }
            FabState.SEND -> {
                chatSend.apply {
                    setImageResource(R.drawable.ic_send)

                    setOnClickListener {
                        sendMessage(chatMessage.text.toString(), attachments)
                    }

                    setOnLongClickListener {
                        sendMessage(chatMessage.text.toString(), attachments, false)
                        true
                    }
                }
            }
            FabState.EDIT -> {
                chatSend.apply {
                    setImageResource(R.drawable.ic_done)

                    setOnClickListener {
                        //editMessage()
                    }

                    setOnLongClickListener {
                        performClick()
                        true
                    }
                }

            }
            FabState.DELETE -> {
                chatSend.apply {
                    setImageResource(R.drawable.ic_trash_outline)

                    chatSend.setOnClickListener {
                        //deleteMessage
                    }

                    chatSend.setOnLongClickListener {
                        performClick()
                        true
                    }
                }
            }
            FabState.BLOCKED -> {
                chatSend.apply {
                    isClickable = false
                    setImageResource(R.drawable.ic_lock)
                }
            }
        }
    }

    private fun recordVoice() {
        Toast.makeText(this, "типо записывается войс (нет)", Toast.LENGTH_LONG).show()
    }

    private fun showVoiceTip() {
        Toast.makeText(this, R.string.voice_record_tip, Toast.LENGTH_LONG).show()
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

    private fun prepareToolbar() {
        setSupportActionBar(toolbar)

        val placeholder = TextDrawable
                .builder()
                .buildRound(if (title.isEmpty()) "" else title.substring(0, 1), AppGlobal.colorAccent)

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
//        toolbar.setNavigationOnClickListener { onBackPressed() }
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
        conversation = intent.getSerializableExtra(TAG_EXTRA_CONVERSATION) as VKConversation?

        peerId = intent.getIntExtra(TAG_ID, -1)
        title = intent.getStringExtra(TAG_EXTRA_TITLE) ?: ""
        avatar = intent.getStringExtra(TAG_EXTRA_AVATAR) ?: ""

        if (conversation == VKConversation() && hasConnection()) {
            Thread(Runnable {
                try {
                    val conversation = VKApi.messages()
                            .conversationsById
                            .peerIds(peerId)
                            .extended(true)
                            .execute(VKConversation::class.java) ?: ArrayList()

                    if (conversation.isNullOrEmpty()) {
                        return@Runnable
                    }

                    this@MessagesActivity.conversation = conversation[0]

                    val user = VKUtil.searchUser(peerId)
                    val group = VKUtil.searchGroup(peerId)

                    if (user != null) {
                        title = user.toString()
                        avatar = user.photo200!!
                    } else if (group != null) {
                        title = group.name!!
                        avatar = group.photo200!!
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }).start()
        }
    }

    private fun sendMessage(text: String = "", attachments: ArrayList<VKModel>? = null, scrollToBottom: Boolean = true) {
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

        checkListIsEmpty(adapter!!.values)

        if (scrollToBottom)
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

    override fun showNoItemsView(visible: Boolean) {
        if (visible) {
            noItemsView.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(250).start()
            }
        } else {
            noItemsView.apply {
                alpha = 1f
                animate().alpha(0f).setDuration(250).withEndAction { visibility = View.GONE }.start()
            }
        }
    }

    override fun showNoInternetView(visible: Boolean) {
        if (visible) clearList()

        if (visible) {
            noInternetView.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(250).start()
            }
        } else {
            noInternetView.apply {
                alpha = 1f
                animate().alpha(0f).setDuration(250).withEndAction { visibility = View.GONE }.start()
            }
        }
    }

    override fun showErrorView(errorTitle: String, errorDescription: String) {
        Log.d(TAG, "showErrorView: $errorTitle: $errorDescription")
        if (!hasConnection()) {
            presenter.requestCachedValues(0, 0, MESSAGES_COUNT)
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

        setChatInfoText()

        VKUtil.sortMessagesByDate(values, false)
        VKUtil.prepareList(values)

        if (adapter == null) {
            adapter = MessageAdapter(this, values, conversation!!).also {
                it.onItemClickListener = this
            }

            recyclerView.adapter = adapter
            return
        }

        if (recyclerView.adapter == null) {
            recyclerView.adapter = adapter
        }

        if (offset > 0) {
            adapter!!.addAll(values)
            adapter!!.notifyItemRangeInserted(offset, values.size)
            return
        }

        adapter!!.values = values
        adapter!!.notifyDataSetChanged()

        if (offset == 0) {
            recyclerView.smoothScrollToPosition(adapter!!.itemCount + 1)
        }
    }

    private fun getChatInfo(): String? {
        return when (conversation!!.type) {
            VKConversation.TYPE_CHAT -> {
                if (conversation!!.isGroupChannel) {
                    getString(R.string.group_channel_members, conversation!!.membersCount)
                } else {
                    getString(R.string.chat_members, conversation!!.membersCount)
                }
            }
            VKConversation.TYPE_USER -> {
                getUserOnline()
            }
            else -> null
        }
    }

    private fun getUserOnline(): String? {
        val user = MemoryCache.getUser(conversation!!.id)

        if (!loadedId) {
            loadedId = true
            TaskManager.loadUser(conversation!!.id)
        }

        return if (user == null) {
            null
        } else {
            VKUtil.getUserOnline(user)
        }
    }

    override fun checkListIsEmpty(values: ArrayList<VKMessage>) {
        showNoItemsView(values.isEmpty())
    }

    override fun hasConnection(): Boolean {
        return AndroidUtils.hasConnection()
    }

    override fun clearList() {
        if (adapter == null) return

        adapter!!.clear()
        adapter!!.notifyDataSetChanged()
    }

    override fun onItemClick(position: Int) {
        val message = adapter!!.getItem(position)

        Toast.makeText(this, message.text, Toast.LENGTH_SHORT).show()
    }
}