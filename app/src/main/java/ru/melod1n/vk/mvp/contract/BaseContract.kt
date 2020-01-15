package ru.melod1n.vk.mvp.contract

import ru.melod1n.vk.api.VKApi.OnResponseListener
import java.util.*

class BaseContract {
    interface View<T> {
        fun showNoItemsView(visible: Boolean)
        fun showNoInternetView(visible: Boolean)
        fun showErrorView(errorTitle: String, errorDescription: String)
        fun hideErrorView()
        fun showRefreshLayout(visible: Boolean)
        fun showProgressBar(visible: Boolean)
        fun loadValuesIntoList(offset: Int, values: ArrayList<T>)
        fun clearList()
    }

    interface Presenter<T> {
        fun readyForLoading()
        fun onRequestLoadCachedValues(id: Int, offset: Int, count: Int)
        fun onRequestLoadValues(id: Int, offset: Int, count: Int)
        fun onValuesLoading()
        fun onValuesErrorLoading(e: Exception)
        fun onValuesLoaded(offset: Int, values: ArrayList<T>)
        fun onRequestClearList()
    }

    abstract class Repository<T> {
        abstract fun loadCachedValues(id: Int, offset: Int, count: Int): ArrayList<T>
        abstract fun loadValues(id: Int, offset: Int, count: Int, listener: OnResponseListener<T>)
        abstract fun insertDataInDatabase(models: ArrayList<T>)
    }
}