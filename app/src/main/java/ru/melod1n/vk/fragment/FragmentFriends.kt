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
import ru.melod1n.library.mvp.base.MvpConstants
import ru.melod1n.library.mvp.base.MvpFields
import ru.melod1n.vk.mvp.presenter.FriendsPresenter
import ru.melod1n.vk.mvp.view.FriendsView
import ru.melod1n.vk.util.AndroidUtils

class FragmentFriends : BaseFragment(), FriendsView, SwipeRefreshLayout.OnRefreshListener, BaseAdapter.OnItemClickListener, FragmentSettings.OnEventListener {

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

        loadCachedValues()

        loadValues()
    }

    override fun onDestroy() {
        FragmentSettings.removeOnEventListener(this)
        super.onDestroy()
    }

    override fun onDetach() {
        if (adapter != null) adapter!!.destroy()
        super.onDetach()
    }


    private fun prepareListeners() {
        FragmentSettings.addOnEventListener(this)
    }

    override fun onNewEvent(event: EventInfo<*>) {

    }

    private fun loadValues() {
        if (AndroidUtils.hasConnection()) {
            if (adapter != null && !adapter!!.isEmpty()) {
                presenter.prepareForLoading()
            } else {
                startRefreshing()
            }

            presenter.requestLoadValues(MvpFields().apply {
                put(MvpConstants.ID, 0)
                put(MvpConstants.OFFSET, 0)
                put(MvpConstants.COUNT, FRIENDS_COUNT)
                put(MvpConstants.FROM_CACHE, false)
                put(FriendsPresenter.ONLY_ONLINE, false)
            })
        } else {
            showNoInternetView()
            stopRefreshing()
        }
    }

    private fun loadCachedValues() {
        presenter.requestCachedData(MvpFields().apply {
            put(MvpConstants.ID, 0)
            put(MvpConstants.OFFSET, 0)
            put(MvpConstants.COUNT, FRIENDS_COUNT)
            put(MvpConstants.FROM_CACHE, true)
            put(FriendsPresenter.ONLY_ONLINE, false)
        })
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

            if (!AndroidUtils.hasConnection()) {
                Snackbar.make(noInternetView, R.string.no_connection, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun prepareNoItemsView() {
        noItemsRefresh.setOnClickListener {
            loadValues()

            if (!AndroidUtils.hasConnection())
                showNoInternetView()
        }

        noItemsText.text = getString(R.string.friends_is_empty)
    }

    override fun prepareErrorView() {

    }


    override fun showNoInternetView() {
        clearList()

        noInternetView.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(250).start()
        }
    }

    override fun hideNoInternetView() {
        noInternetView.apply {
            alpha = 1f
            animate().alpha(0f).setDuration(250).withEndAction { visibility = View.GONE }.start()
        }
    }

    override fun showNoItemsView() {
        noItemsView.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(250).start()
        }
    }

    override fun hideNoItemsView() {
        noItemsView.apply {
            alpha = 1f
            animate().alpha(0f).setDuration(250).withEndAction { visibility = View.GONE }.start()
        }
    }

    override fun showErrorView(e: Exception?) {

    }

    override fun hideErrorView() {

    }

    override fun startRefreshing() {
        swipeRefreshLayout.isRefreshing = true
    }

    override fun stopRefreshing() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    override fun insertValues(fields: MvpFields, values: java.util.ArrayList<VKUser>) {
        val offset = fields.getInt(MvpConstants.OFFSET)
        val fromCache = fields.getBoolean(MvpConstants.FROM_CACHE)

        Log.d(tag, "loadValuesIntoList: $offset, ${values.size}, fromCache: $fromCache")


        if (adapter == null) {
            adapter = FriendAdapter(requireContext(), values).also { it.onItemClickListener = this }
            recyclerView.adapter = adapter
            return
        }

        val list = ArrayList(adapter!!.values)

        if (recyclerView.adapter == null) {
            recyclerView.adapter = adapter
        }

        if (offset > 0) {
            list.addAll(values)

            adapter!!.updateList(list)
            return
        }

        adapter!!.updateList(values)
    }


    override fun clearList() {
        if (adapter == null) return

        adapter!!.clear()
        adapter!!.notifyDataSetChanged()
    }
}