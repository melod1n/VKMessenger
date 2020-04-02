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
import ru.melod1n.vk.adapter.FriendAdapter
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.current.BaseFragment
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.presenter.FriendsPresenter
import ru.melod1n.vk.util.AndroidUtils
import java.util.*

class FragmentFriends : BaseFragment(), BaseContract.View<VKUser>, SwipeRefreshLayout.OnRefreshListener, BaseAdapter.OnItemClickListener, FragmentSettings.OnEventListener {

    companion object {
        const val FRIENDS_COUNT = 30
        const val TAG = "FragmentFriends"
    }

    private var adapter: FriendAdapter? = null
    private lateinit var presenter: FriendsPresenter

    override fun onResume() {
        super.onResume()

        requireActivity().setTitle(R.string.navigation_friends)
    }

    override fun onRefresh() {
        refreshData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prepareRefreshLayout()
        prepareRecyclerView()

        prepareNoInternetView()
        prepareListeners()

        presenter = FriendsPresenter(this)

        loadCachedData()

        if (AndroidUtils.hasConnection()) {
            refreshData()
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
        when (event.key) {
            EventInfo.CONVERSATIONS_REFRESH -> {
                //TODO
            }
        }
    }

    private fun refreshData() {
        if (AndroidUtils.hasConnection()) {
            showNoInternetView(false)
            presenter.onValuesLoading()
            presenter.onRequestLoadValues(0, 0, FRIENDS_COUNT, false)
        } else {
            showNoInternetView(true)
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadCachedData() {
        presenter.onRequestLoadCachedValues(0, 0, FRIENDS_COUNT, false)
    }

    private fun openChat(position: Int) {
        val user = adapter!!.getItem(position)


        val data = Bundle().apply {
            putInt(MessagesActivity.TAG_ID, user.id)
            putString(MessagesActivity.TAG_EXTRA_TITLE, user.toString())
            putString(MessagesActivity.TAG_EXTRA_AVATAR, user.photo200)
        }

        requireActivity().startActivityForResult(Intent(requireContext(), MessagesActivity::class.java).putExtras(data), MainActivity.REQUEST_CODE_FROM_DRAWER)
    }

    private fun prepareRefreshLayout() {
        swipeRefreshLayout.apply {
            setColorSchemeColors(AppGlobal.colorAccent)
            setOnRefreshListener(this@FragmentFriends)
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
        Log.d(TAG, "showNoItemsView: $visible")
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
            presenter.onRequestLoadCachedValues(0, 0, FragmentConversations.CONVERSATIONS_COUNT, false)
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

    override fun loadValuesIntoList(offset: Int, values: ArrayList<VKUser>, isCache: Boolean) {
        Log.d(TAG, "loadValuesIntoList: $offset, ${values.size}, isCache: $isCache")

        if (isCache && values.isEmpty() && !AndroidUtils.hasConnection()) {
            showNoInternetView(true)
            return

        }

        if (values.isEmpty()) return

        if (adapter == null) {
            adapter = FriendAdapter(requireContext(), values).also { it.onItemClickListener = this }
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

    override fun clearList() {
        Log.d(TAG, "clearList")
        if (adapter == null) return
        adapter!!.clear()
        adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        FragmentSettings.removeOnEventListener(this)
        super.onDestroy()
    }

    override fun onDetach() {
        if (adapter != null) adapter!!.onDestroy()
        super.onDetach()
    }

}