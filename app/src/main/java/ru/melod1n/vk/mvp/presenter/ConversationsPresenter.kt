package ru.melod1n.vk.mvp.presenter

import android.util.Log
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.VKException
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.contract.BaseContract.Presenter
import ru.melod1n.vk.mvp.model.ConversationsRepository
import ru.melod1n.vk.util.ArrayUtil

class ConversationsPresenter(private val view: BaseContract.View<VKConversation>) : Presenter<VKConversation> {

    private val repository: BaseContract.Repository<VKConversation>
    private var loadedValues: ArrayList<VKConversation>? = null
    private var cachedValues: ArrayList<VKConversation>? = null

    override fun readyForLoading() {
        view.showNoInternetView(false)
        view.showNoItemsView(false)
        view.showProgressBar(false)
        view.hideErrorView()
    }

    override fun onRequestLoadCachedValues(id: Int, offset: Int, count: Int) {
        readyForLoading()
        cachedValues = repository.loadCachedValues(0, offset, count)

        onValuesLoaded(offset, cachedValues!!, true)
    }

    override fun onRequestLoadValues(id: Int, offset: Int, count: Int) {
        readyForLoading()
        repository.loadValues(id, offset, count, object : OnResponseListener<VKConversation> {
            override fun onSuccess(models: ArrayList<VKConversation>) {
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

    override fun onValuesLoaded(offset: Int, values: ArrayList<VKConversation>, isCache: Boolean) {
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

    companion object {
        private const val TAG = "ConversationsPresenter"
    }

    init {
        repository = ConversationsRepository()
        Log.d(TAG, "Constructor")
    }
}