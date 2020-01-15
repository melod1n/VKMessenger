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

    override fun onRequestLoadCachedValues(id: Int, offset: Int, count: Int) {
        readyForLoading()
        cachedValues = repository.loadCachedValues(id, offset, count)
        onValuesLoaded(offset, cachedValues!!)
    }

    override fun onRequestLoadValues(id: Int, offset: Int, count: Int) {
        readyForLoading()
        repository.loadValues(id, offset, count, object : OnResponseListener<VKMessage> {
            override fun onSuccess(models: ArrayList<VKMessage>) {
                loadedValues = models
                onValuesLoaded(offset, loadedValues!!)
            }

            override fun onError(e: Exception) {
                onValuesErrorLoading(e)
            }
        })
    }

    override fun onValuesLoading() {
        view.showProgressBar(true)
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

    override fun onValuesLoaded(offset: Int, values: ArrayList<VKMessage>) {
        view.hideErrorView()
        view.showNoItemsView(false)
        view.showRefreshLayout(false)
        view.showProgressBar(false)
        view.showNoItemsView(ArrayUtil.isEmpty(values))
        view.loadValuesIntoList(offset, values)
    }

    override fun onRequestClearList() {
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