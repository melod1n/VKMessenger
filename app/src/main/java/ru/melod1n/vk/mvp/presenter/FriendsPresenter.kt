package ru.melod1n.vk.mvp.presenter

import android.util.Log
import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.VKException
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.contract.FriendsContract
import ru.melod1n.vk.mvp.model.FriendsRepository
import ru.melod1n.vk.util.ArrayUtil
import java.util.*

class FriendsPresenter(private val view: BaseContract.View<VKUser>) : FriendsContract.Presenter<VKUser> {

    companion object {
        private const val TAG = "ConversationsPresenter"
    }

    private val repository: FriendsContract.Repository<VKUser>
    private var loadedValues: ArrayList<VKUser>? = null
    private var cachedValues: ArrayList<VKUser>? = null

    init {
        repository = FriendsRepository()
        Log.d(TAG, "Constructor")
    }

    override fun readyForLoading() {
        view.showNoInternetView(false)
        view.showNoItemsView(false)
        view.showProgressBar(false)
        view.hideErrorView()
    }


    override fun onRequestLoadCachedValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean) {
        readyForLoading()
        cachedValues = repository.loadCachedValues(0, offset, count, onlyOnline)

        onValuesLoaded(offset, cachedValues!!, true)
    }

    override fun onRequestLoadValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean) {
        readyForLoading()
        repository.loadValues(id, offset, count, onlyOnline, object : VKApi.OnResponseListener<VKUser> {
            override fun onSuccess(models: ArrayList<VKUser>) {
                loadedValues = models
                onValuesLoaded(offset, loadedValues!!, false)
            }

            override fun onError(e: Exception) {
                onValuesErrorLoading(e)
            }
        })
    }

    override fun onValuesLoading() {
        view.showRefreshLayout(true)
    }

    override fun onValuesErrorLoading(e: Exception) {
        view.clearList()
        view.showProgressBar(false)
        view.showNoItemsView(false)
        view.showRefreshLayout(false)
        if (e is VKException) {
            view.showErrorView(e.toString(), e.message)
        } else {
            view.showErrorView(e.toString(), Log.getStackTraceString(e))
        }
        Log.d(TAG, "onValuesErrorLoading: " + e.toString() + ": " + Log.getStackTraceString(e))
    }

    override fun onValuesLoaded(offset: Int, values: ArrayList<VKUser>, isCache: Boolean) {
        view.hideErrorView()
        view.showNoItemsView(false)
        view.showRefreshLayout(false)
        view.showProgressBar(false)
        view.showNoItemsView(ArrayUtil.isEmpty(values))
        view.loadValuesIntoList(offset, values, isCache)
    }

    override fun onRequestClearList() {
        view.clearList()
    }

}