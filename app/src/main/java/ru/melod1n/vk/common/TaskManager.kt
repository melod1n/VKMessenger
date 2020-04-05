package ru.melod1n.vk.common

import android.util.Log
import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.method.MethodSetter
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.model.VKGroup
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.concurrent.LowThread
import ru.melod1n.vk.database.CacheStorage
import java.util.*

object TaskManager {
    private const val TAG = "TaskManager"
    private val currentTasksIds = ArrayList<Int>()

    private val listeners = ArrayList<OnEventListener>()

    fun addOnEventListener(onEventListener: OnEventListener) {
        listeners.add(onEventListener)
    }

    fun removeOnEventListener(onEventListener: OnEventListener?) {
        listeners.remove(onEventListener)
    }

    fun execute(runnable: Runnable) {
        LowThread(runnable).start()
    }

    fun execute(runnable: () -> Unit) {
        LowThread(runnable).start()
    }

    private fun <T> addProcedure(methodSetter: MethodSetter, className: Class<T>, pushInfo: EventInfo<*>?, onResponseListener: OnResponseListener<T>?) {
        execute {
            methodSetter.execute(className, object : OnResponseListener<T> {
                override fun onSuccess(models: ArrayList<T>) {
                    onResponseListener?.onSuccess(models)
                    if (pushInfo != null) {
                        sendEvent(pushInfo)
                    }
                }

                override fun onError(e: Exception) {
                    onResponseListener?.onError(e)
                }
            })
        }
    }

    private fun sendEvent(info: EventInfo<*>) {
        AppGlobal.handler.post {
            for (listener in listeners) {
                listener.onNewEvent(info)
            }
        }
    }

    fun loadUser(userId: Int) {
        Log.i(TAG, "loadUser: $userId")

        if (currentTasksIds.contains(userId)) return
        currentTasksIds.add(userId)

        val setter = VKApi.users().get().userId(userId).fields(VKUser.DEFAULT_FIELDS)

        addProcedure(setter, VKUser::class.java, EventInfo<Any>(EventInfo.USER_UPDATE, userId), object : OnResponseListener<VKUser> {
            override fun onSuccess(models: ArrayList<VKUser>) {
                currentTasksIds.remove(userId)
                CacheStorage.insertUsers(models)
            }

            override fun onError(e: Exception) {
                Log.w(TAG, "User not loaded. Stack: " + Log.getStackTraceString(e))
            }
        })
    }

    fun loadGroup(groupId: Int) {
        Log.i(TAG, "loadGroup: $groupId")

        if (currentTasksIds.contains(groupId)) return
        currentTasksIds.add(groupId)

        val setter = VKApi.groups().byId.groupId(groupId).fields(VKGroup.DEFAULT_FIELDS)

        addProcedure(setter, VKGroup::class.java, EventInfo<Any>(EventInfo.GROUP_UPDATE, groupId), object : OnResponseListener<VKGroup> {
            override fun onSuccess(models: ArrayList<VKGroup>) {
                currentTasksIds.remove(groupId)
                CacheStorage.insertGroups(models)
            }

            override fun onError(e: Exception) {
                Log.w(TAG, "Group not loaded. Stack: " + Log.getStackTraceString(e))
            }
        })
    }

    fun loadMessage(messageId: Int, listener: OnResponseListener<VKMessage>? = null) {
        Log.i(TAG, "loadMessage: $messageId")

        if (currentTasksIds.contains(messageId)) return

        currentTasksIds.add(messageId)

        val setter = VKApi.messages().byId.messageIds(messageId).extended(true).fields(VKUser.DEFAULT_FIELDS + "," + VKGroup.DEFAULT_FIELDS)

        addProcedure(setter, VKMessage::class.java, EventInfo<Any>(EventInfo.MESSAGE_UPDATE, messageId), object : OnResponseListener<VKMessage> {
            override fun onSuccess(models: ArrayList<VKMessage>) {
                currentTasksIds.remove(messageId)

                if (CacheStorage.getMessage(models[0].id) == null) {
                    CacheStorage.insertMessages(models)
                } else {
                    CacheStorage.updateMessages(models)
                }

                listener?.onSuccess(models)
            }

            override fun onError(e: Exception) {
                Log.w(TAG, "Message not loaded. Stack: " + Log.getStackTraceString(e))
                listener?.onError(e)
            }
        })
    }

    fun loadConversation(peerId: Int, listener: OnResponseListener<VKConversation>? = null) {
        Log.i(TAG, "loadConversation: $peerId")
        if (currentTasksIds.contains(peerId)) return

        currentTasksIds.add(peerId)

        val setter = VKApi.messages().conversationsById.peerIds(peerId).extended(true).fields(VKUser.DEFAULT_FIELDS + "," + VKGroup.DEFAULT_FIELDS)

        addProcedure(setter, VKConversation::class.java, EventInfo<Any>(EventInfo.CONVERSATION_UPDATE, peerId), object : OnResponseListener<VKConversation> {
            override fun onSuccess(models: ArrayList<VKConversation>) {
                currentTasksIds.remove(peerId)

                if (CacheStorage.getConversation(models[0].id) == null) {
                    CacheStorage.insertConversations(models)
                } else {
                    CacheStorage.updateConversations(models)
                }

                listener?.onSuccess(models)
            }

            override fun onError(e: Exception) {
                Log.w(TAG, "Conversation not loaded. Stack: " + Log.getStackTraceString(e))
                listener?.onError(e)
            }
        })
    }

    interface OnEventListener {
        fun onNewEvent(event: EventInfo<*>)
    }
}