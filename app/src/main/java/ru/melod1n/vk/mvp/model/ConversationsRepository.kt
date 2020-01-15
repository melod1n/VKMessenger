package ru.melod1n.vk.mvp.model

import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.VKApi.SuccessCallback
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.database.CacheStorage.getConversations
import ru.melod1n.vk.database.CacheStorage.getMessageByPeerId
import ru.melod1n.vk.database.CacheStorage.insertConversations
import ru.melod1n.vk.database.CacheStorage.insertGroups
import ru.melod1n.vk.database.CacheStorage.insertMessages
import ru.melod1n.vk.database.CacheStorage.insertUsers
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.util.ArrayUtil
import java.util.*
import kotlin.collections.ArrayList

class ConversationsRepository : BaseContract.Repository<VKConversation>() {
    override fun loadCachedValues(id: Int, offset: Int, count: Int): ArrayList<VKConversation> {
        val conversations = ArrayUtil.manipulate(getConversations(count), offset, count)
        val dialogs = ArrayList<VKConversation>(conversations.size)

        conversations.sortWith(Comparator { o1: VKConversation, o2: VKConversation ->
            val m1 = getMessageByPeerId(o1.id)
            val m2 = getMessageByPeerId(o2.id)

            if (m1 == null || m2 == null) return@Comparator 0

            val x = m1.date
            val y = m2.date

            y - x
        })

        for (i in conversations.indices) {
            val conversation = conversations[i].apply {
                lastMessage = getMessageByPeerId(id)
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

        insertMessages(messages)
        insertConversations(models)
        insertUsers(models[0].profiles)
        insertGroups(models[0].groups)
    }
}