package ru.melod1n.vk.mvp.repository

import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.VKApi.SuccessCallback
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.api.util.VKUtil
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.database.DatabaseHelper
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.util.ArrayUtil

class ConversationsRepository : BaseContract.Repository<VKConversation>() {

    override fun loadCachedValues(id: Int, offset: Int, count: Int): ArrayList<VKConversation> {
        val conversations = ArrayUtil.cut(CacheStorage.getConversations(count), offset, count)

        val dialogs = ArrayList<VKConversation>(conversations.size)

        VKUtil.sortConversationsByDate(dialogs, true)

        for (i in conversations.indices) {
            val conversation = conversations[i].apply {
                lastMessage = CacheStorage.getMessage(lastMessageId) ?: return@apply
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

                if (models.isEmpty()) {
                    CacheStorage.delete(DatabaseHelper.TABLE_CONVERSATIONS)
                    CacheStorage.delete(DatabaseHelper.TABLE_MESSAGES)
                } else if (offset == 0) {
                    CacheStorage.delete(DatabaseHelper.TABLE_CONVERSATIONS)
                }

                cacheValues(models)

                AppGlobal.handler.post(SuccessCallback(listener, models))
            } catch (e: Exception) {
                e.printStackTrace()
                AppGlobal.handler.post(VKApi.ErrorCallback(listener, e))
            }
        }
    }

    override fun cacheValues(values: ArrayList<VKConversation>) {
        val messages = ArrayList<VKMessage>(values.size)

        for (conversation in values) {
            messages.add(conversation.lastMessage)
        }

        CacheStorage.insertMessages(messages)
        CacheStorage.insertConversations(values)
        CacheStorage.insertUsers(VKConversation.profiles)
        CacheStorage.insertGroups(VKConversation.groups)
    }

}