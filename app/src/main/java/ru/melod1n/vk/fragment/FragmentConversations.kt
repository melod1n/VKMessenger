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
import kotlinx.android.synthetic.main.no_items_view.*
import kotlinx.android.synthetic.main.recycler_view.*
import ru.melod1n.vk.R
import ru.melod1n.vk.activity.MainActivity
import ru.melod1n.vk.activity.MessagesActivity
import ru.melod1n.vk.adapter.ConversationAdapter
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.util.VKUtil
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.common.TimeManager
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.current.BaseFragment
import ru.melod1n.vk.database.MemoryCache.getGroup
import ru.melod1n.vk.database.MemoryCache.getUser
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.presenter.ConversationsPresenter
import ru.melod1n.vk.util.AndroidUtils

class FragmentConversations : BaseFragment(),
        BaseContract.View<VKConversation>,
        SwipeRefreshLayout.OnRefreshListener,
        BaseAdapter.OnItemClickListener,
        FragmentSettings.OnEventListener,
        TimeManager.OnMinuteChangeListener {

    companion object {
        const val CONVERSATIONS_COUNT = 30
        const val TAG = "FragmentConversations"
    }

    private var adapter: ConversationAdapter? = null
    private lateinit var presenter: ConversationsPresenter

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
        prepareRefreshLayout()
        prepareRecyclerView()

        prepareNoItemsView()
        prepareNoInternetView()
        prepareListeners()

        presenter = ConversationsPresenter(this)

        loadCachedData()

        refreshData()

        TimeManager.addOnMinuteChangeListener(this)
    }

    override fun onMinuteChange(currentMinute: Int) {
        requireActivity().runOnUiThread {
//            adapter?.updateData()
        }
    }

    private fun prepareNoItemsView() {
        noItemsRefresh.setOnClickListener {
            refreshData()

            if (!hasConnection())
                showNoInternetView(true)
        }
        noItemsText.text = getString(R.string.list_is_empty)
    }

    private fun prepareNoInternetView() {
        noInternetUpdate.setOnClickListener {
            refreshData()

            if (!hasConnection()) {
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
        if (hasConnection()) {
            presenter.readyForLoading()
            presenter.requestValues(0, 0, CONVERSATIONS_COUNT)
        } else {
            if (adapter!!.isEmpty())
                showNoInternetView(true)

            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadCachedData() {
        presenter.requestCachedValues(0, 0, CONVERSATIONS_COUNT)
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
        if (!AndroidUtils.hasConnection()) {
            presenter.requestCachedValues(0, 0, CONVERSATIONS_COUNT)
        }
    }

    override fun hideErrorView() {
        Log.d(TAG, "hideErrorView")
    }

    override fun showRefreshLayout(visible: Boolean) {
        swipeRefreshLayout.isRefreshing = visible
    }

    override fun showProgressBar(visible: Boolean) {
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun loadValuesIntoList(offset: Int, values: ArrayList<VKConversation>, isCache: Boolean) {
        Log.d(TAG, "loadValuesIntoList: $offset, ${values.size}, isCache: $isCache")

        if (isCache && values.isEmpty() && !hasConnection()) {
            showNoInternetView(true)
            return
        }

        if (adapter == null) {
            adapter = ConversationAdapter(this, values).also { it.onItemClickListener = this }
            recyclerView.adapter = adapter
            return
        }

        val newList = ArrayList(adapter!!.values)

        if (recyclerView.adapter == null) {
            recyclerView.adapter = adapter
        }

        if (offset != 0) {
            newList.addAll(values)

            adapter!!.updateList(newList)
            return
        }

        adapter!!.values = values

        adapter!!.notifyDataSetChanged()
    }

    override fun checkListIsEmpty(values: ArrayList<VKConversation>) {
        showNoItemsView(values.isEmpty())
    }

    override fun hasConnection(): Boolean {
        return AndroidUtils.hasConnection()
    }

    override fun clearList() {
        if (adapter == null) return

        val values = ArrayList<VKConversation>()

        adapter!!.updateList(values)
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