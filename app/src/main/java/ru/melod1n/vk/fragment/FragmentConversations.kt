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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.recycler_view.*
import ru.melod1n.vk.R
import ru.melod1n.vk.activity.MainActivity
import ru.melod1n.vk.activity.MessagesActivity
import ru.melod1n.vk.adapter.conversations.ConversationAdapter
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.current.BaseFragment
import ru.melod1n.vk.database.MemoryCache.getGroup
import ru.melod1n.vk.database.MemoryCache.getUser
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.presenter.ConversationsPresenter
import ru.melod1n.vk.util.AndroidUtils

class FragmentConversations : BaseFragment, BaseContract.View<VKConversation>, OnRefreshListener, BaseAdapter.OnItemClickListener {

    companion object {
        const val CONVERSATIONS_COUNT = 30
        const val TAG = "FragmentConversations"
    }

    private var adapter: ConversationAdapter? = null
    private lateinit var presenter: ConversationsPresenter

    constructor(titleRes: Int) : super(titleRes)
    constructor()

    override fun onRefresh() {
        presenter.onValuesLoading()
        presenter.onRequestClearList()
        presenter.onRequestLoadValues(0, 0, CONVERSATIONS_COUNT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_conversations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        presenter = ConversationsPresenter(this)

        prepareRefreshLayout()
        prepareRecyclerView()

        if (AndroidUtils.hasConnection()) {
            onRefresh()
        } else {
            presenter.onRequestLoadCachedValues(0, 0, CONVERSATIONS_COUNT)
        }
    }

    private fun openChat(position: Int) {
        val conversation = adapter!!.getItem(position)

        val peerUser = getUser(conversation.id)
        val peerGroup = getGroup(conversation.id)
        val data = Bundle()

        data.putSerializable(MessagesActivity.TAG_EXTRA_CONVERSATION, conversation)
        data.putString(MessagesActivity.TAG_EXTRA_TITLE, adapter!!.getTitle(conversation, peerUser, peerGroup))
        data.putString(MessagesActivity.TAG_EXTRA_AVATAR, adapter!!.getAvatar(conversation, peerUser, peerGroup))

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

    override fun onItemClick(position: Int) {
        openChat(position)
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
            presenter.onRequestLoadCachedValues(0, 0, CONVERSATIONS_COUNT)
        }
    }

    override fun hideErrorView() {
        Log.d(TAG, "hideErrorView")
    }

    override fun showRefreshLayout(visible: Boolean) {
        Log.d(TAG, "showRefreshLayout: $visible")
        swipeRefreshLayout.isRefreshing = visible
    }

    override fun showProgressBar(visible: Boolean) {
        Log.d(TAG, "showProgressBar: $visible")
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun loadValuesIntoList(offset: Int, values: ArrayList<VKConversation>) {
        Log.d(TAG, "loadValuesIntoList: $offset, ${values.size}")

        if (values.isEmpty()) return

        if (adapter == null) {
            adapter = ConversationAdapter(this, values).also { it.setOnItemClickListener(this) }
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
                notifyItemRangeChanged(0, adapter!!.itemCount, 0)
            }
            return
        }

        adapter!!.changeItems(values)
        adapter!!.notifyItemRangeChanged(0, adapter!!.itemCount, 0)
    }

    override fun clearList() {
        Log.d(TAG, "clearList")
        if (adapter == null) return
        adapter!!.clear()
        adapter!!.notifyDataSetChanged()
    }

    override fun onDetach() {
        if (adapter != null) adapter!!.destroy()
        super.onDetach()
    }
}