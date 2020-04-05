package ru.melod1n.vk.mvp.contract

import ru.melod1n.vk.api.VKApi.OnResponseListener

open class BaseContract {
    interface View<T> {
        fun showNoItemsView(visible: Boolean)
        fun showNoInternetView(visible: Boolean)
        fun showErrorView(errorTitle: String, errorDescription: String)
        fun hideErrorView()
        fun showRefreshLayout(visible: Boolean)
        fun showProgressBar(visible: Boolean)
        fun loadValuesIntoList(offset: Int, values: ArrayList<T>, isCache: Boolean)
        fun checkListIsEmpty(values: ArrayList<T>)
        fun hasConnection(): Boolean
        fun clearList()
    }

    interface Presenter<T> {
        fun readyForLoading()
        fun requestCachedValues(id: Int, offset: Int, count: Int)
        fun requestValues(id: Int, offset: Int, count: Int)
        fun valuesLoading()
        fun valuesErrorLoading(e: Exception)
        fun valuesLoaded(offset: Int, values: ArrayList<T>, isCache: Boolean)
        fun clearList()
    }

    abstract class Repository<T> {
        abstract fun loadCachedValues(id: Int, offset: Int, count: Int): ArrayList<T>
        abstract fun loadValues(id: Int, offset: Int, count: Int, listener: OnResponseListener<T>)
        abstract fun insertDataInDatabase(models: ArrayList<T>)
    }
}