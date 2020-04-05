package ru.melod1n.vk.mvp.presenter

import android.util.Log
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.VKException
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.contract.BaseContract.Presenter
import ru.melod1n.vk.mvp.model.MessagesRepository
import ru.melod1n.vk.util.ArrayUtil
import java.util.*


class MessagesPresenter(private val view: BaseContract.View<VKMessage>) : Presenter<VKMessage> {
    private val repository: BaseContract.Repository<VKMessage>
    private var loadedValues: ArrayList<VKMessage>? = null
    private var cachedValues: ArrayList<VKMessage>? = null

    override fun readyForLoading() {
        view.showNoInternetView(false)
        view.showNoItemsView(false)
        view.showRefreshLayout(false)
        view.hideErrorView()
    }

    override fun requestCachedValues(id: Int, offset: Int, count: Int) {
        readyForLoading()
        cachedValues = repository.loadCachedValues(id, offset, count)
        valuesLoaded(offset, cachedValues!!, true)
    }

    override fun requestValues(id: Int, offset: Int, count: Int) {
        readyForLoading()
        repository.loadValues(id, offset, count, object : OnResponseListener<VKMessage> {
            override fun onSuccess(models: ArrayList<VKMessage>) {
                loadedValues = models
                valuesLoaded(offset, loadedValues!!, false)
            }

            override fun onError(e: Exception) {
                valuesErrorLoading(e)
            }
        })
    }

    override fun valuesLoading() {
        view.showProgressBar(true)
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

    override fun valuesLoaded(offset: Int, values: ArrayList<VKMessage>, isCache: Boolean) {
        view.hideErrorView()
        view.showNoItemsView(false)
        view.showRefreshLayout(false)
        view.showProgressBar(false)
        view.showNoItemsView(ArrayUtil.isEmpty(values))
        view.loadValuesIntoList(offset, values, isCache)
    }

    override fun clearList() {
        view.clearList()
    }

    companion object {
        private const val TAG = "ConversationsPresenter"
    }

    init {
        repository = MessagesRepository()
        Log.d(TAG, "constructor")
    }
}