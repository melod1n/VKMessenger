package ru.melod1n.vk.oldmvp.contract

import android.util.Log
import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.VKException
import java.util.*
import kotlin.collections.ArrayList


open class BaseContract {

    interface View<T> {
        fun prepareNoInternetView()

        fun prepareNoItemsView()

        fun prepareErrorView()

        fun showNoItemsView(visible: Boolean)

        fun showNoInternetView(visible: Boolean)

        fun showErrorView(e: Exception?)

        fun showRefreshLayout(visible: Boolean)

        fun showProgressBar(visible: Boolean)

        fun insertValues(id: Int = 0, offset: Int = 0, count: Int, values: ArrayList<T>, isCache: Boolean)

        fun clearList()
    }

    abstract class Presenter<T>(private var view: View<T>?) {

        abstract val tag: String

        abstract val repository: Repository<T>

        abstract var loadedValues: ArrayList<T>
        abstract var cachedValues: ArrayList<T>

        private var timer = Timer()

        fun prepareForLoading() {
            view?.showNoInternetView(false)
            view?.showNoItemsView(false)
            view?.showProgressBar(false)
            view?.showErrorView(null)
        }

        fun showList() {
            view?.showProgressBar(false)
            view?.showRefreshLayout(false)
        }

        fun checkListIsEmpty(isCache: Boolean) {
            view?.showNoItemsView((isCache && cachedValues.isEmpty()) || (!isCache && loadedValues.isEmpty()))
        }

        fun checkListIsEmpty(values: ArrayList<T>) {
            view?.showNoItemsView(values.isEmpty())
        }

        fun requestValues(id: Int = 0, offset: Int = 0, count: Int) {
            repository.loadValues(id, offset, count, object : VKApi.OnResponseListener<T> {
                override fun onSuccess(models: ArrayList<T>) {
                    onValuesLoaded(id, offset, count, models, false)
                }

                override fun onError(e: Exception) {
                    onValuesError(e)
                }
            })
        }

        fun requestCachedValues(id: Int = 0, offset: Int = 0, count: Int) {
            val cachedValues = repository.loadCachedValues(id, offset, count)

            onValuesLoaded(id, offset, count, cachedValues, true)
        }

        fun onValuesLoaded(id: Int = 0, offset: Int = 0, count: Int, values: ArrayList<T>, isCache: Boolean) {
            showList()

            if (isCache) {
                cachedValues = values
            } else {
                loadedValues = values
            }

            view?.insertValues(id, offset, count, values, isCache)
            checkListIsEmpty(isCache)
        }

        fun onValuesError(e: Exception) {
            showList()

            loadedValues.clear()

            view?.showErrorView(e)
        }

        private fun waitMinute() {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    Log.d(tag, "1 minute pass")

                    view?.showErrorView(VKException("", "", -1))
                }
            }, 60 * 1000)
        }

        fun destroy() {
            view = null
        }
    }

    abstract class Repository<T> {
        abstract fun loadValues(id: Int = 0, offset: Int = 0, count: Int, listener: VKApi.OnResponseListener<T>)

        abstract fun loadCachedValues(id: Int = 0, offset: Int = 0, count: Int): ArrayList<T>

        abstract fun cacheValues(values: ArrayList<T>)
    }
}