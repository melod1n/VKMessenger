package ru.melod1n.vk.mvp.model

import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.VKApi.SuccessCallback
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.util.ArrayUtil

class ConversationsRepository : BaseContract.Repository<VKConversation>() {
    override fun loadCachedValues(id: Int, offset: Int, count: Int): ArrayList<VKConversation> {
        val conversations = ArrayUtil.cut(CacheStorage.getConversations(count), offset, count)
        val dialogs = ArrayList<VKConversation>(conversations.size)

        conversations.sortWith(Comparator { o1: VKConversation, o2: VKConversation ->
            val m1 = CacheStorage.getMessage(o1.lastMessageId)
            val m2 = CacheStorage.getMessage(o2.lastMessageId)

            if (m1 == null || m2 == null) return@Comparator 0

            val x = m1.date
            val y = m2.date

            y - x
//            if (x > y) -1 else if (x == y) 1 else 0
        })

        for (i in conversations.indices) {
            val conversation = conversations[i].apply {
                lastMessage = CacheStorage.getMessage(lastMessageId)
            }

            dialogs.add(conversation)
        }
        return dialogs
    }

    override fun loadValues(id: Int, offset: Int, count: Int, listener: OnResponseListener<VKConversation>) {
        TaskManager.execute {
            try {
                val models = VKApi.messages()
                        .conversations
                        .filter("all")
                        .extended(true)
                        .fields(VKUser.DEFAULT_FIELDS)
                        .offset(offset).count(count)
                        .execute(VKConversation::class.java)!!

                insertDataInDatabase(models)

                AppGlobal.handler.post(SuccessCallback(listener, models))
            } catch (e: Exception) {
                e.printStackTrace()
                AppGlobal.handler.post(VKApi.ErrorCallback(listener, e))
            }
        }
    }

    override fun insertDataInDatabase(models: ArrayList<VKConversation>) {
        val messages = ArrayList<VKMessage>(models.size)

        for (conversation in models) {
            messages.add(conversation.lastMessage!!)
        }

        CacheStorage.insertMessages(messages)
        CacheStorage.insertConversations(models)
        CacheStorage.insertUsers(models[0].profiles)
        CacheStorage.insertGroups(models[0].groups)
    }
}