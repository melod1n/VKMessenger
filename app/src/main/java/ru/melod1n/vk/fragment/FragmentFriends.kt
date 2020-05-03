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
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.no_internet_view.*
import kotlinx.android.synthetic.main.no_items_view.*
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
import ru.melod1n.vk.util.AndroidUtils.hasConnection

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
        loadValues()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prepareRefreshLayout()
        prepareRecyclerView()

        prepareNoItemsView()
        prepareNoInternetView()
        prepareListeners()

        presenter = FriendsPresenter(this)

        loadCachedData()

        loadValues()
    }


    override fun onDestroy() {
        FragmentSettings.removeOnEventListener(this)
        super.onDestroy()
    }

    override fun onDetach() {
        if (adapter != null) adapter!!.onDestroy()
        super.onDetach()
    }


    private fun prepareListeners() {
        FragmentSettings.addOnEventListener(this)
    }

    override fun onNewEvent(event: EventInfo<*>) {

    }

    private fun loadValues() {
        if (hasConnection()) {
            if (adapter != null && !adapter!!.isEmpty()) {
                presenter.prepareForLoading();
            } else {
                showRefreshLayout(true);
            }

            presenter.requestValues(0, 0, FragmentConversations.CONVERSATIONS_COUNT)
        } else {
            showNoInternetView(true);
            showRefreshLayout(false);
        }
    }

    private fun loadCachedData() {
        presenter.requestCachedValues(0, 0, FRIENDS_COUNT, false)
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

    override fun onItemClick(position: Int) {
        openChat(position)
    }

    override fun prepareNoInternetView() {
        noInternetUpdate.setOnClickListener {
            loadValues()

            if (!hasConnection()) {
                Snackbar.make(noInternetView, R.string.no_connection, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun prepareNoItemsView() {
        noItemsRefresh.setOnClickListener {
            loadValues()

            if (!hasConnection())
                showNoInternetView(true)
        }

        noItemsText.text = getString(R.string.friends_is_empty)
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

    override fun showErrorView(e: Exception?) {
        if (!hasConnection()) {
            presenter.requestCachedValues(0, 0, FragmentConversations.CONVERSATIONS_COUNT, false)
        }
    }

    override fun showRefreshLayout(visible: Boolean) {
        swipeRefreshLayout.isRefreshing = visible
    }

    override fun showProgressBar(visible: Boolean) {
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun insertValues(id: Int, offset: Int, count: Int, values: ArrayList<VKUser>, isCache: Boolean) {
        Log.d(TAG, "loadValuesIntoList: $offset, ${values.size}, isCache: $isCache")

        if (isCache && values.isEmpty() && !hasConnection()) {
            showNoInternetView(true)
            return

        }

        if (adapter == null) {
            adapter = FriendAdapter(requireContext(), values).also { it.onItemClickListener = this }
            recyclerView.adapter = adapter
            return
        }

//        val newList = ArrayList(adapter!!.values)

        if (recyclerView.adapter == null) {
            recyclerView.adapter = adapter
        }

        if (offset != 0) {
            adapter!!.addAll(values)

            adapter!!.notifyDataSetChanged()
//            adapter!!.updateList(newList)
            return
        }

        adapter!!.values = values

        adapter!!.notifyDataSetChanged()
    }

    override fun clearList() {
        if (adapter == null) return

        adapter!!.clear()
        adapter!!.notifyDataSetChanged()
    }
}