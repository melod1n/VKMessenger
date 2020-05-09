package ru.melod1n.vk.fragment

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
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
import ru.melod1n.vk.adapter.diffutil.ConversationCallback
import ru.melod1n.vk.api.VKException
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.util.VKUtil
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.common.TimeManager
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.current.BaseFragment
import ru.melod1n.vk.database.MemoryCache.getGroup
import ru.melod1n.vk.database.MemoryCache.getUser
import ru.melod1n.vk.oldmvp.contract.BaseContract
import ru.melod1n.vk.oldmvp.presenter.ConversationsPresenter
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

    internal lateinit var presenter: ConversationsPresenter

    override fun onResume() {
        super.onResume()

        Log.d(tag, "onResume")

        requireActivity().setTitle(R.string.navigation_conversations)
    }

    override fun onRefresh() {
        loadValues()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(tag, "onCreateView")
        return inflater.inflate(R.layout.fragment_conversations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(tag, "onViewCreated, savedInstanceState ${if (savedInstanceState == null) "== null" else "!= null"}")

        prepareRefreshLayout()
        prepareRecyclerView()

        prepareNoItemsView()
        prepareNoInternetView()
        prepareListeners()

        presenter = ConversationsPresenter(this)

        loadCachedValues()

        loadValues()

        TimeManager.addOnMinuteChangeListener(this)

//        (requireActivity() as MainActivity).getToolbar().setOnClickListener {
//            loadValues()
////            adapter ?: return@setOnClickListener
////
////            val list = ArrayList(adapter!!.values)
////
////            adapter!!.updateList(list.reversed())
//        }
    }

    private fun prepareRefreshLayout() {
        swipeRefreshLayout.setColorSchemeColors(AppGlobal.colorAccent)
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    private fun prepareRecyclerView() {
        val manager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        decoration.setDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.divider)))

        recyclerView.setHasFixedSize(true)

        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(decoration)

        recyclerView.layoutManager = manager
    }

    private fun loadCachedValues() {
        presenter.requestCachedValues(0, 0, CONVERSATIONS_COUNT)

//        if (AndroidUtils.hasConnection()) {
//            loadValues()
//        }
    }

    private fun loadValues() {
        if (AndroidUtils.hasConnection()) {
            if (adapter != null && !adapter!!.isEmpty()) {
                presenter.prepareForLoading()
            } else {
                showRefreshLayout(true)
            }

            presenter.requestValues(0, 0, CONVERSATIONS_COUNT)
        } else {
            showRefreshLayout(false)

            if (adapter != null && adapter!!.isEmpty()) {
                showNoInternetView(true)
            } else {
                //showNoInternetSnackbar
            }

        }
    }

    private fun prepareListeners() {
        FragmentSettings.addOnEventListener(this)
    }

    override fun onNewEvent(event: EventInfo<*>) {

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

    override fun onItemClick(position: Int) {
        openChat(position)
    }

    override fun onMinuteChange(currentMinute: Int) {
        requireActivity().runOnUiThread {
            adapter ?: return@runOnUiThread

            adapter!!.notifyItemRangeChanged(0, adapter!!.itemCount, ConversationCallback.DATE)
        }
    }

    override fun prepareNoInternetView() {
        noInternetUpdate.setOnClickListener {
            loadValues()

            if (!AndroidUtils.hasConnection()) {
                Snackbar.make(noInternetView, R.string.no_connection, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun prepareNoItemsView() {
        noItemsRefresh.setOnClickListener {
            loadValues()

            if (!AndroidUtils.hasConnection())
                showNoInternetView(true)
        }

        noItemsText.text = getString(R.string.conversations_is_empty)
    }

    override fun prepareErrorView() {

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
        if (visible) {
            clearList()

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

    override fun showErrorView(e: Exception?) {
        if (e is VKException) {
            //...
        } else {
            //...
        }

        if (!AndroidUtils.hasConnection()) {
            presenter.requestCachedValues(0, 0, CONVERSATIONS_COUNT)
        }
    }

    override fun showRefreshLayout(visible: Boolean) {
        swipeRefreshLayout.isRefreshing = visible
    }

    override fun showProgressBar(visible: Boolean) {
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun insertValues(id: Int, offset: Int, count: Int, values: ArrayList<VKConversation>, isCache: Boolean) {
        Log.d(tag, "loadValuesIntoList: $offset, ${values.size}, isCache: $isCache")

        if (adapter == null) {
            adapter = ConversationAdapter(this, values).also { it.onItemClickListener = this }
            recyclerView.adapter = adapter
            return
        }


            if (recyclerView.adapter == null) {
                recyclerView.adapter = adapter
            }

            if (offset != 0) {
                val list = ArrayList(adapter!!.values)
                list.addAll(values)
                adapter!!.updateList(list)
                return
            }

            adapter!!.updateList(values)
    }

    override fun clearList() {
        adapter?.let {
            it.clear()
            it.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        FragmentSettings.removeOnEventListener(this)
        super.onDestroy()

        Log.d(tag, "onDestroy")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter?.destroy()
        presenter.destroy()

        Log.d(tag, "onDestroyView")
        TimeManager.removeOnMinuteChangeListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(tag, "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(tag, "onActivityCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart")
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(tag, "onDetach")
    }
}