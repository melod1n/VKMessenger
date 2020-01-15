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
import ru.melod1n.vk.database.CacheStorage.insertConversation
import ru.melod1n.vk.database.CacheStorage.insertGroup
import ru.melod1n.vk.database.CacheStorage.insertMessage
import ru.melod1n.vk.database.CacheStorage.insertUser
import java.util.*
import kotlin.math.abs

object TaskManager {
    private const val TAG = "TaskManager"
    private val currentTasksIds = ArrayList<Int>()

    fun execute(runnable: () -> Unit) {
        LowThread(runnable).start()
    }

    fun execute(runnable: Runnable) {
        LowThread(runnable).start()
    }

    private fun <T> addProcedure(methodSetter: MethodSetter, className: Class<T>, pushInfo: EventInfo<*>?, onResponseListener: OnResponseListener<T>?) {
        execute {
            methodSetter.execute(className, object : OnResponseListener<T> {
                override fun onSuccess(models: ArrayList<T>) {
                    onResponseListener?.onSuccess(models)
                    if (pushInfo != null) { //                    EventBus.getDefault().postSticky(pushInfo);
                    }
                }

                override fun onError(e: Exception) {
                    onResponseListener?.onError(e)
                }
            })
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
                insertUser(models[0])
            }

            override fun onError(e: Exception) {
                Log.w(TAG, "User not loaded. Stack: " + Log.getStackTraceString(e))
            }
        })
    }

    fun loadGroup(gid: Int) {
        val groupId = abs(gid)

        Log.i(TAG, "loadGroup: $groupId")

        if (currentTasksIds.contains(groupId)) return
        currentTasksIds.add(groupId)
        val setter = VKApi.groups().byId.groupId(groupId).fields(VKGroup.DEFAULT_FIELDS)

        addProcedure(setter, VKGroup::class.java, EventInfo<Any>(EventInfo.GROUP_UPDATE, groupId), object : OnResponseListener<VKGroup> {
            override fun onSuccess(models: ArrayList<VKGroup>) {
                currentTasksIds.remove(groupId)
                insertGroup(models[0])
            }

            override fun onError(e: Exception) {
                Log.w(TAG, "Group not loaded. Stack: " + Log.getStackTraceString(e))
            }
        })
    }

    @JvmOverloads
    fun loadMessage(messageId: Int, listener: OnResponseListener<VKMessage>? = null) {
        Log.i(TAG, "loadMessage: $messageId")

        if (currentTasksIds.contains(messageId)) return

        currentTasksIds.add(messageId)

        val setter = VKApi.messages().byId.messageIds(messageId).extended(true).fields(VKUser.DEFAULT_FIELDS + "," + VKGroup.DEFAULT_FIELDS)

        addProcedure(setter, VKMessage::class.java, EventInfo<Any>(EventInfo.MESSAGE_UPDATE, messageId), object : OnResponseListener<VKMessage> {
            override fun onSuccess(models: ArrayList<VKMessage>) {
                currentTasksIds.remove(messageId)
                insertMessage(models[0])
                listener?.onSuccess(models)
            }

            override fun onError(e: Exception) {
                Log.w(TAG, "Message not loaded. Stack: " + Log.getStackTraceString(e))
                listener?.onError(e)
            }
        })
    }

    @JvmOverloads
    fun loadConversation(peerId: Int, listener: OnResponseListener<VKConversation>? = null) {
        Log.i(TAG, "loadConversation: $peerId")
        if (currentTasksIds.contains(peerId)) return

        currentTasksIds.add(peerId)

        val setter = VKApi.messages().conversationsById.peerIds(peerId).extended(true).fields(VKUser.DEFAULT_FIELDS + "," + VKGroup.DEFAULT_FIELDS)

        addProcedure(setter, VKConversation::class.java, EventInfo<Any>(EventInfo.CONVERSATION_UPDATE, peerId), object : OnResponseListener<VKConversation> {
            override fun onSuccess(models: ArrayList<VKConversation>) {
                currentTasksIds.remove(peerId)
                insertConversation(models[0]) //TODO: тут краш
                listener?.onSuccess(models)
            }

            override fun onError(e: Exception) {
                Log.w(TAG, "Conversation not loaded. Stack: " + Log.getStackTraceString(e))
                listener?.onError(e)
            }
        })
    }
}