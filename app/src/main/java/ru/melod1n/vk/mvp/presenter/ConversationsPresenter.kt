package ru.melod1n.vk.mvp.presenter

import android.util.Log
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.VKException
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.model.ConversationsRepository
import ru.melod1n.vk.util.ArrayUtil

class ConversationsPresenter(private val view: BaseContract.View<VKConversation>) : BaseContract.Presenter<VKConversation> {

    private val repository: BaseContract.Repository<VKConversation>
    private var loadedValues: ArrayList<VKConversation>? = null
    private var cachedValues: ArrayList<VKConversation>? = null

    override fun readyForLoading() {
        view.showNoInternetView(false)
        view.showNoItemsView(false)
        view.showProgressBar(false)
        view.hideErrorView()
    }

    override fun requestCachedValues(id: Int, offset: Int, count: Int) {
        readyForLoading()
        cachedValues = repository.loadCachedValues(0, offset, count)

        valuesLoaded(offset, cachedValues!!, true)
    }

    override fun requestValues(id: Int, offset: Int, count: Int) {
        readyForLoading()
        repository.loadValues(id, offset, count, object : OnResponseListener<VKConversation> {
            override fun onSuccess(models: ArrayList<VKConversation>) {
                loadedValues = models
                valuesLoaded(offset, loadedValues!!, false)
            }

            override fun onError(e: Exception) {
                valuesErrorLoading(e)
            }
        })
    }

    override fun valuesLoading() {
        view.showRefreshLayout(true)
    }

    override fun valuesErrorLoading(e: Exception) {
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

    override fun valuesLoaded(offset: Int, values: ArrayList<VKConversation>, isCache: Boolean) {
        view.hideErrorView()
        view.showNoItemsView(false)
        view.showRefreshLayout(false)
        view.showProgressBar(false)
        view.showNoItemsView(ArrayUtil.isEmpty(values))
        view.loadValuesIntoList(offset, values, isCache)
    }

    override fun requestClearList() {
        view.clearList()
    }

    companion object {
        private const val TAG = "ConversationsPresenter"
    }

    init {
        repository = ConversationsRepository()
        Log.d(TAG, "Constructor")
    }
}