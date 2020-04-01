package ru.melod1n.vk.fragment

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.no_internet_view.*
import kotlinx.android.synthetic.main.recycler_view.*
import ru.melod1n.vk.R
import ru.melod1n.vk.activity.MainActivity
import ru.melod1n.vk.activity.MessagesActivity
import ru.melod1n.vk.adapter.ConversationAdapter
import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.api.util.VKUtil
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.common.TimeManager
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.current.BaseFragment
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.database.MemoryCache.getGroup
import ru.melod1n.vk.database.MemoryCache.getUser
import ru.melod1n.vk.util.AndroidUtils
import ru.melod1n.vk.util.ArrayUtil

class FragmentConversations : BaseFragment(),
//        BaseContract.View<VKConversation>,
        SwipeRefreshLayout.OnRefreshListener,
        BaseAdapter.OnItemClickListener,
        FragmentSettings.OnEventListener,
        TimeManager.OnMinuteChangeListener {

    companion object {
        const val CONVERSATIONS_COUNT = 30
        const val TAG = "FragmentConversations"
    }

    private var adapter: ConversationAdapter? = null
//    private lateinit var presenter: ConversationsPresenter

    override fun onResume() {
        super.onResume()

        requireActivity().setTitle(R.string.navigation_conversations)
    }

    override fun onRefresh() {
        refreshData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_conversations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        presenter = ConversationsPresenter(this)

        prepareRefreshLayout()
        prepareRecyclerView()

        prepareNoInternetView()
        prepareListeners()

        loadCachedData()

        if (AndroidUtils.hasConnection()) {
            refreshData()
        }

        TimeManager.addOnMinuteChangeListener(this)
    }

    override fun onMinuteChange(currentMinute: Int) {
        requireActivity().runOnUiThread {
//            adapter?.updateData()
        }
    }

    private fun prepareNoInternetView() {
        noInternetUpdate.setOnClickListener {
            if (AndroidUtils.hasConnection()) {
                refreshData()
            } else {
                Snackbar.make(noInternetView, R.string.no_connection, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareListeners() {
        FragmentSettings.addOnEventListener(this)
    }

    override fun onNewEvent(event: EventInfo<*>) {

    }

    private fun refreshData() {
        if (AndroidUtils.hasConnection()) {
            loadValues(0, 0, CONVERSATIONS_COUNT, object : VKApi.OnResponseListener<VKConversation> {
                override fun onSuccess(models: ArrayList<VKConversation>) {
                    loadValuesIntoList(0, models, false)
                }

                override fun onError(e: Exception) {

                }

            })
//            presenter.readyForLoading()
//            presenter.requestValues(0, 0, CONVERSATIONS_COUNT)
        } else {
            showNoInternetView(true)
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadCachedData() {
//        presenter.requestCachedValues(0, 0, CONVERSATIONS_COUNT)
    }

    fun loadCachedValues(id: Int, offset: Int, count: Int): ArrayList<VKConversation> {
        val conversations = ArrayUtil.cut(CacheStorage.getConversations(count), offset, count)

        val dialogs = ArrayList<VKConversation>(conversations.size)

        conversations.sortWith(Comparator { o1: VKConversation, o2: VKConversation ->

            val m1 = CacheStorage.getMessage(o1.lastMessageId)
            val m2 = CacheStorage.getMessage(o2.lastMessageId)

            if (m1 == null || m2 == null) return@Comparator 0

            val x = m1.date
            val y = m2.date

            y - x
//            if (x > y) -1 else if (x == y) 1 else 0
        })

        for (i in conversations.indices) {
            val conversation = conversations[i].apply {
                lastMessage = CacheStorage.getMessage(lastMessageId)
            }

            dialogs.add(conversation)
        }
        return dialogs
    }

    fun loadValues(id: Int, offset: Int, count: Int, listener: VKApi.OnResponseListener<VKConversation>) {
        TaskManager.execute {
            try {
                val models = VKApi.messages()
                        .conversations
                        .filter("all")
                        .extended(true)
                        .fields(VKUser.DEFAULT_FIELDS)
                        .offset(offset).count(count)
                        .execute(VKConversation::class.java) ?: ArrayList()

//                insertDataInDatabase(models)

                AppGlobal.handler.post(VKApi.SuccessCallback(listener, models))
            } catch (e: Exception) {
                e.printStackTrace()
                AppGlobal.handler.post(VKApi.ErrorCallback(listener, e))
            }
        }
    }

    private fun openChat(position: Int) {
        val conversation = adapter!!.getItem(position)

        val peerUser = getUser(conversation.id)
        val peerGroup = getGroup(conversation.id)

        val data = Bundle().apply {
            putInt(MessagesActivity.TAG_ID, conversation.id)
            putSerializable(MessagesActivity.TAG_EXTRA_CONVERSATION, conversation)
            putString(MessagesActivity.TAG_EXTRA_TITLE, VKUtil.getTitle(conversation, peerUser, peerGroup))
            putString(MessagesActivity.TAG_EXTRA_AVATAR, VKUtil.getAvatar(conversation, peerUser, peerGroup))
        }

        requireActivity().startActivityForResult(Intent(requireContext(), MessagesActivity::class.java).putExtras(data), MainActivity.REQUEST_CODE_FROM_DRAWER)
    }

    private fun prepareRefreshLayout() {
        swipeRefreshLayout.apply {
            setColorSchemeColors(AppGlobal.colorAccent)
            setOnRefreshListener(this@FragmentConversations)
        }
    }

    private fun prepareRecyclerView() {
        val manager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        decoration.setDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.divider)))

        recyclerView.addItemDecoration(decoration)
        recyclerView.layoutManager = manager
    }

    fun getRecyclerView(): RecyclerView {
        return recyclerView
    }

    override fun onItemClick(position: Int) {
        openChat(position)
    }

    fun showNoItemsView(visible: Boolean) {
        Log.d(TAG, "showNoItemsView: $visible")
    }

    fun showNoInternetView(visible: Boolean) {
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
//
//        noInternetView.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun showErrorView(errorTitle: String, errorDescription: String) {
        Log.d(TAG, "showErrorView: $errorTitle: $errorDescription")
        if (!AndroidUtils.hasConnection()) {
//            presenter.requestCachedValues(0, 0, CONVERSATIONS_COUNT)
        }
    }

    fun hideErrorView() {
        Log.d(TAG, "hideErrorView")
    }

    fun showRefreshLayout(visible: Boolean) {
        swipeRefreshLayout.isRefreshing = visible
    }

    fun showProgressBar(visible: Boolean) {
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun loadValuesIntoList(offset: Int, values: ArrayList<VKConversation>, isCache: Boolean) {
        Log.d(TAG, "loadValuesIntoList: $offset, ${values.size}, isCache: $isCache")

        if (isCache && values.isEmpty() && !AndroidUtils.hasConnection()) {
            showNoInternetView(true)
            return
        }

        if (values.isEmpty()) return

        if (adapter == null) {
            adapter = ConversationAdapter(this, values).also { it.onItemClickListener = this }
            recyclerView.adapter = adapter
            return
        }

        if (recyclerView.adapter == null) {
            recyclerView.adapter = adapter
        }

        if (offset != 0) {
            adapter!!.apply {
                addAll(values)
                notifyItemRangeInserted(offset, values.size)
            }
            return
        }

        adapter!!.values = values
        adapter!!.notifyDataSetChanged()
    }

    fun clearList() {
        Log.d(TAG, "clearList")

        if (adapter == null) return

        adapter!!.clear()
        adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        FragmentSettings.removeOnEventListener(this)
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter?.onDestroy()

        TimeManager.removeOnMinuteChangeListener(this)
    }
}