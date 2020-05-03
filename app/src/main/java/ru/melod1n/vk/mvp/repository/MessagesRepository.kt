package ru.melod1n.vk.mvp.repository

import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.VKApi.SuccessCallback
import ru.melod1n.vk.api.model.VKGroup
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.util.ArrayUtil

class MessagesRepository : BaseContract.Repository<VKMessage>() {

    override fun loadCachedValues(id: Int, offset: Int, count: Int): ArrayList<VKMessage> {
        val messages = CacheStorage.getMessages(id)
        return ArrayUtil.cut(messages, offset, count)
    }

    override fun loadValues(id: Int, offset: Int, count: Int, listener: OnResponseListener<VKMessage>) {
        TaskManager.execute(Runnable {
            try {
                val models = VKApi.messages()
                        .history
                        .peerId(id)
                        .rev(0)
                        .extended(true)
                        .fields(VKUser.DEFAULT_FIELDS + "," + VKGroup.DEFAULT_FIELDS)
                        .offset(offset)
                        .count(count)
                        .execute(VKMessage::class.java) ?: ArrayList()

                cacheValues(models)

                AppGlobal.handler.post(SuccessCallback(listener, models))
            } catch (e: Exception) {
                e.printStackTrace()
                AppGlobal.handler.post(VKApi.ErrorCallback(listener, e))
            }
        })
    }

    override fun cacheValues( values: ArrayList<VKMessage>) {
        CacheStorage.insertMessages(values)
    }
}