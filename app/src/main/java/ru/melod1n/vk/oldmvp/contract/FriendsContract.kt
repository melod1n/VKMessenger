package ru.melod1n.vk.oldmvp.contract

import ru.melod1n.vk.api.VKApi

class FriendsContract : BaseContract() {

    abstract class Presenter<T>(view: View<T>) : BaseContract.Presenter<T>(view) {

        abstract override val repository: Repository<T>

        fun requestValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean){
            repository.loadValues(id, offset, count, onlyOnline, object : VKApi.OnResponseListener<T> {
                override fun onSuccess(models: ArrayList<T>) {
                    onValuesLoaded(id, offset, count, models, false)
                }

                override fun onError(e: Exception) {
                    onValuesError(e)
                }
            })
        }

        fun requestCachedValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean) {
            val cachedValues = repository.loadCachedValues(id, offset, count, onlyOnline)

            onValuesLoaded(id, offset, count, cachedValues, true)
        }

    }

    abstract class Repository<T> : BaseContract.Repository<T>() {
        abstract fun loadCachedValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean): ArrayList<T>

        abstract fun loadValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean, listener: VKApi.OnResponseListener<T>)
    }
}