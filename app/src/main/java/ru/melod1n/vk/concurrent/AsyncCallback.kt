package ru.melod1n.vk.concurrent

import android.app.Activity
import java.lang.ref.WeakReference

abstract class AsyncCallback(activity: Activity?) : Runnable {
    private val reference: WeakReference<Activity?>?
    @Throws(Exception::class)
    abstract fun ready()

    abstract fun done()
    abstract fun error(e: Exception?)
    override fun run() {
        try {
            ready()
        } catch (e: Exception) {
            e.printStackTrace()
            if (reference?.get() != null) {
                reference.get()!!.runOnUiThread { error(e) }
            }
            return
        }
        if (reference?.get() != null) {
            reference.get()!!.runOnUiThread { done() }
        }
    }

    init {
        reference = WeakReference(activity)
    }
}