package ru.melod1n.vk.concurrent

import ru.melod1n.vk.common.AppGlobal

abstract class TryCallback : Runnable {
    @Throws(Exception::class)
    abstract fun ready()

    abstract fun done()
    abstract fun error(e: Exception?)
    override fun run() {
        try {
            ready()
        } catch (e: Exception) {
            e.printStackTrace()
            AppGlobal.handler.post { error(e) }
            return
        }
        AppGlobal.handler.post { done() }
    }
}