package ru.melod1n.vk.mvp.contract

import ru.melod1n.vk.api.VKApi

class FriendsContract : BaseContract() {

    interface Presenter<T> {
        fun readyForLoading()
        fun onRequestLoadCachedValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean)
        fun onRequestLoadValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean)
        fun onValuesLoading()
        fun onValuesErrorLoading(e: Exception)
        fun onValuesLoaded(offset: Int, values: ArrayList<T>, isCache: Boolean)
        fun onRequestClearList()
    }

    abstract class Repository<T> {
        abstract fun loadCachedValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean): ArrayList<T>
        abstract fun loadValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean, listener: VKApi.OnResponseListener<T>)
        abstract fun insertDataInDatabase(models: java.util.ArrayList<T>)
    }
}