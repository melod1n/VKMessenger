package ru.melod1n.vk.api

import android.util.Log
import org.json.JSONArray
import ru.melod1n.vk.api.model.VKConversation.Companion.isChatId
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.database.CacheStorage
import java.util.*

@Suppress("UNCHECKED_CAST")
class VKLongPollParser {

    companion object {

        fun parse(updates: JSONArray) {
            if (updates.length() == 0) {
                return
            }
            for (i in 0 until updates.length()) {
                val item = updates.optJSONArray(i)
                when (item.optInt(0)) {
                    2 -> messageSetFlags(item)
                    3 -> messageClearFlags(item)
                    4 -> messageEvent(item)
                    5 -> messageEdit(item)
                }
            }
        }

        private const val TAG = "VKLongPollParser"

        private val listeners = ArrayList<OnEventListener>()
        private val messagesListeners: MutableList<OnMessagesListener> = ArrayList()

        fun addOnEventListener(onEventListener: OnEventListener) {
            listeners.add(onEventListener)
        }

        fun removeOnEventListener(onEventListener: OnEventListener?) {
            listeners.remove(onEventListener)
        }

        fun addOnMessagesListener(onMessagesListener: OnMessagesListener) {
            messagesListeners.add(onMessagesListener)
        }

        fun removeOnMessagesListener(onMessagesListener: OnMessagesListener?) {
            messagesListeners.remove(onMessagesListener)
        }

        private fun messageEvent(item: JSONArray) {
            val message = VKMessage.parse(item)
            TaskManager.loadMessage(message.id)

            if (isChatId(message.peerId)) {
                TaskManager.loadConversation(message.peerId)
            }

            val info = EventInfo(EventInfo.MESSAGE_NEW, message)

            CacheStorage.insertMessage(message)

            sendEvent(info)
            sendMessageEvent(info)
        }

        private fun messageEdit(item: JSONArray) {
            val message = VKMessage.parse(item)
            val info = EventInfo(EventInfo.MESSAGE_EDIT, message)

            if (CacheStorage.getMessage(message.id) != null)
                CacheStorage.updateMessage(message)

            sendEvent(info)
            sendMessageEvent(info)
        }

        private fun messageDelete(item: JSONArray) {
            val messageId = item.optInt(1)
            val peerId = item.optInt(3)
            val info = EventInfo(EventInfo.MESSAGE_DELETE, arrayOf(messageId, peerId))

            if (CacheStorage.getMessage(messageId) != null)
                CacheStorage.deleteMessage(messageId)

            sendEvent(info)
            sendMessageEvent(info)
        }

        private fun messageRestored(item: JSONArray) {
            val message = VKMessage.parse(item)
            val info = EventInfo(EventInfo.MESSAGE_RESTORE, message)

            CacheStorage.insertMessage(message)

            sendEvent(info)
            sendMessageEvent(info)
        }

        private fun messageRead(item: JSONArray) {
            val messageId = item.optInt(1)
            val peerId = item.optInt(3)
            val info = EventInfo(EventInfo.MESSAGE_READ, arrayOf(messageId, peerId))

            val message = CacheStorage.getMessage(messageId)

            if (message != null) {
                CacheStorage.updateMessage(message.apply { isRead = true })
            }

            sendEvent(info)
            sendMessageEvent(info)
        }

        private fun messageClearFlags(item: JSONArray) {
            val id = item.optInt(1)
            val flags = item.optInt(2)
            if (VKMessage.hasFlag(flags, "cancel_spam")) {
                Log.i(TAG, "Message with id $id: Not spam")
            }
            if (VKMessage.hasFlag(flags, "deleted")) {
                messageRestored(item)
            }
            if (VKMessage.hasFlag(flags, "important")) {
                Log.i(TAG, "Message with id $id: Not Important")
            }
            if (VKMessage.hasFlag(flags, "unread")) {
                messageRead(item)
            }
        }

        private fun messageSetFlags(item: JSONArray) {
            val id = item.optInt(1)
            val flags = item.optInt(2)
            if (VKMessage.hasFlag(flags, "delete_for_all")) {
                messageDelete(item)
            }
            if (VKMessage.hasFlag(flags, "deleted")) {
                messageDelete(item)
            }
            if (VKMessage.hasFlag(flags, "spam")) {
                Log.i(TAG, "Message with id $id: Spam")
            }
            if (VKMessage.hasFlag(flags, "important")) {
                Log.i(TAG, "Message with id $id: Important")
            }
        }

        private fun sendEvent(info: EventInfo<*>) {
            AppGlobal.handler.post {
                for (listener in listeners) {
                    listener.onEvent(info)
                }
            }
        }

        private fun sendMessageEvent(info: EventInfo<*>) {
            for (listener in messagesListeners) {
                AppGlobal.handler.post {
                    when (info.key) {
                        EventInfo.MESSAGE_NEW -> listener.onNewMessage(info.data as VKMessage)
                        EventInfo.MESSAGE_EDIT -> listener.onEditMessage(info.data as VKMessage)
                        EventInfo.MESSAGE_READ -> {
                            val data = info.data as Array<Int>
                            val peerId = data[1]
                            val messageId = data[0]
                            listener.onReadMessage(messageId, peerId)
                        }
                        EventInfo.MESSAGE_DELETE -> {
                            val data = info.data as Array<Int>
                            val peerId = data[1]
                            val messageId = data[0]
                            listener.onDeleteMessage(messageId, peerId)
                        }
                        EventInfo.MESSAGE_RESTORE -> listener.onRestoredMessage(info.data as VKMessage)
                    }
                }
            }
        }

    }


    interface OnEventListener {
        fun onEvent(info: EventInfo<*>)
    }

    interface OnMessagesListener {
        fun onNewMessage(message: VKMessage)
        fun onEditMessage(message: VKMessage)
        fun onReadMessage(messageId: Int, peerId: Int)
        fun onDeleteMessage(messageId: Int, peerId: Int)
        fun onRestoredMessage(message: VKMessage)
    }

}