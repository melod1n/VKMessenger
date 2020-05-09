package ru.melod1n.vk.api

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import ru.melod1n.vk.BuildConfig
import ru.melod1n.vk.api.method.MessageMethodSetter
import ru.melod1n.vk.api.method.MethodSetter
import ru.melod1n.vk.api.method.UserMethodSetter
import ru.melod1n.vk.api.model.*
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.net.HttpRequest
import ru.melod1n.vk.util.ArrayUtil

object VKApi {
    private const val TAG = "Messenger.VKApi"

    const val BASE_URL = "https://api.vk.com/method/"
    const val API_VERSION = "5.113"

    val language: String = AppGlobal.locale.language

    @Suppress("UNCHECKED_CAST")
    fun <T> execute(url: String, cls: Class<T>?): ArrayList<T>? {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "url: $url")
        }

        val buffer = HttpRequest[url].asString()
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "json: $buffer")
        }

        val json = JSONObject(buffer)

        try {
            checkError(json, url)
        } catch (ex: VKException) {
            return if (ex.code == ErrorCodes.TOO_MANY_REQUESTS) {
                execute(url, cls)
            } else throw ex
        }


        when (cls) {
            null -> return null

            VKLongPollServer::class.java -> {
                val server = VKLongPollServer(json.optJSONObject("response")!!)
                return ArrayUtil.singletonList(server) as ArrayList<T>
            }

            Boolean::class.java -> {
                val value = json.optInt("response") == 1
                return ArrayUtil.singletonList(value) as ArrayList<T>
            }

            Long::class.java -> {
                val value = json.optLong("response")
                return ArrayUtil.singletonList(value) as ArrayList<T>
            }

            Int::class.java -> {
                val value = json.optInt("response")
                return ArrayUtil.singletonList(value) as ArrayList<T>
            }
        }

        val response = json.opt("response")

        val array = optItems(json)
        val models = ArrayList<T>(array!!.length())

        when (cls) {
            VKUser::class.java -> {
                for (i in 0 until array.length()) {
                    models.add(VKUser(array.optJSONObject(i)) as T)
                }
            }

            VKMessage::class.java -> {
                if (url.contains("messages.getHistory")) {
                    VKMessage.lastHistoryCount = (response as JSONObject).optInt("count")
                }

                for (i in 0 until array.length()) {
                    var source = array.optJSONObject(i)
                    if (source.has("message")) {
                        source = source.optJSONObject("message")
                    }

                    val message = VKMessage(source)
                    models.add(message as T)
                    //TODO: сохранять группы и юзеров
                }
            }

            VKGroup::class.java -> {
                for (i in 0 until array.length()) {
                    models.add(VKGroup(array.optJSONObject(i)) as T)
                }
            }

            VKModel::class.java -> {
                if (url.contains("messages.getHistoryAttachments")) {
                    return VKAttachments.parse(array) as ArrayList<T>
                }
            }

            VKConversation::class.java -> {
                if (url.contains("getConversationsById")) {

                    for (i in 0 until array.length()) {
                        val source = array.optJSONObject(i)
                        models.add(VKConversation(source) as T)
                    }

                    return models
                }

                for (i in 0 until array.length()) {
                    val source = array.optJSONObject(i)
                    val oConversation = source.optJSONObject("conversation") ?: return null
                    val oLastMessage = source.optJSONObject("last_message") ?: return null

                    val conversation = VKConversation(oConversation).also { it.lastMessage = VKMessage(oLastMessage) }

                    val oProfiles = (response as JSONObject).optJSONArray("profiles")
                    if (oProfiles != null) {
                        val profiles = ArrayList<VKUser>()

                        for (j in 0 until oProfiles.length()) {
                            profiles.add(VKUser(oProfiles.optJSONObject(j)))
                        }

                        VKConversation.profiles = profiles
                    }

                    val oGroups = response.optJSONArray("groups")
                    if (oGroups != null) {
                        val groups = ArrayList<VKGroup>()

                        for (j in 0 until oGroups.length()) {
                            groups.add(VKGroup(oGroups.optJSONObject(j)))
                        }

                        VKConversation.groups = groups
                    }

                    models.add(conversation as T)
                }
            }
        }

        return models
    }

    fun <E> execute(url: String, cls: Class<E>, listener: OnResponseListener<E>?) {
        TaskManager.execute(Runnable {
            try {
                val models = execute(url, cls)

                if (listener != null) {
                    AppGlobal.handler.post(SuccessCallback(listener, models ?: ArrayList()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (listener != null) {
                    AppGlobal.handler.post(ErrorCallback(listener, e))
                }
            }
        })
    }

    private fun optItems(source: JSONObject): JSONArray? {
        val response = source.opt("response")
        if (response is JSONArray) {
            return response
        }
        if (response is JSONObject) {
            return response.optJSONArray("items")
        }
        return null
    }

    private fun checkError(json: JSONObject, url: String) {
        if (json.has("error")) {
            val error = json.optJSONObject("error") ?: return

            val code = error.optInt("error_code")
            val message = error.optString("error_msg")
            val e = VKException(url, message, code)

            if (code == ErrorCodes.CAPTCHA_NEEDED) {
                e.captchaImg = error.optString("captcha_img")
                e.captchaSid = error.optString("captcha_sid")
            }

            if (code == ErrorCodes.VALIDATION_REQUIRED) {
                e.redirectUri = error.optString("redirect_uri")
            }

            throw e
        }
    }

    @JvmStatic
    fun users(): VKUsers {
        return VKUsers()
    }

    @JvmStatic
    fun friends(): VKFriends {
        return VKFriends()
    }

    @JvmStatic
    fun messages(): VKMessages {
        return VKMessages()
    }

    @JvmStatic
    fun groups(): VKGroups {
        return VKGroups()
    }

    @JvmStatic
    fun account(): VKAccounts {
        return VKAccounts()
    }

    class VKFriends {
        fun get(): MethodSetter {
            return MethodSetter("friends.get")
        }
    }

    class VKUsers {
        fun get(): UserMethodSetter {
            return UserMethodSetter("users.get")
        }
    }

    class VKMessages {
        fun get(): MessageMethodSetter {
            return MessageMethodSetter("messages.get")
        }

        val conversations: MessageMethodSetter
            get() = MessageMethodSetter("messages.getConversations")

        val conversationsById: MessageMethodSetter
            get() = MessageMethodSetter("messages.getConversationsById")

        val byId: MessageMethodSetter
            get() = MessageMethodSetter("messages.getById")

        fun search(): MessageMethodSetter {
            return MessageMethodSetter("messages.search")
        }

        val history: MessageMethodSetter
            get() = MessageMethodSetter("messages.getHistory")

        val historyAttachments: MessageMethodSetter
            get() = MessageMethodSetter("messages.getHistoryAttachments")

        fun send(): MessageMethodSetter {
            return MessageMethodSetter("messages.send")
        }

        fun sendSticker(): MessageMethodSetter {
            return MessageMethodSetter("messages.sendSticker")
        }

        fun delete(): MessageMethodSetter {
            return MessageMethodSetter("messages.delete")
        }

        fun deleteDialog(): MessageMethodSetter {
            return MessageMethodSetter("messages.deleteDialog")
        }

        fun restore(): MessageMethodSetter {
            return MessageMethodSetter("messages.restore")
        }

        fun markAsRead(): MessageMethodSetter {
            return MessageMethodSetter("messages.markAsRead")
        }

        fun markAsImportant(): MessageMethodSetter {
            return MessageMethodSetter("messages.markAsImportant")
        }

        val longPollServer: MessageMethodSetter
            get() = MessageMethodSetter("messages.getLongPollServer")

        /**
         * Returns updates in user's private messages.
         * To speed up handling of private messages,
         * it can be useful to cache previously loaded messages on
         * a user's mobile device/desktop, to prevent re-receipt at each call.
         * With this method, you can synchronize a local copy of
         * the message list with the actual version.
         *
         *
         * Result:
         * Returns an object that contains the following fields:
         * 1 — history:     An array similar to updates field returned
         * from the Long Poll server,
         * with these exceptions:
         * - For events with code 4 (addition of a new message),
         * there are no fields except the first three.
         * - There are no events with codes 8, 9 (friend goes online/offline)
         * or with codes 61, 62 (typing during conversation/chat).
         *
         *
         * 2 — messages:    An array of private message objects that were found
         * among events with code 4 (addition of a new message)
         * from the history field.
         * Each object of message contains a set of fields described here.
         * The first array element is the total number of messages
         */
        val longPollHistory: MessageMethodSetter
            get() = MessageMethodSetter("messages.getLongPollHistory")

        val chat: MessageMethodSetter
            get() = MessageMethodSetter("messages.getChat")

        fun createChat(): MessageMethodSetter {
            return MessageMethodSetter("messages.createChat")
        }

        fun editChat(): MessageMethodSetter {
            return MessageMethodSetter("messages.editChat")
        }

        val chatUsers: MessageMethodSetter
            get() = MessageMethodSetter("messages.getChatUsers")

        fun setActivity(): MessageMethodSetter {
            return MessageMethodSetter("messages.setActivity").type(true)
        }

        fun addChatUser(): MessageMethodSetter {
            return MessageMethodSetter("messages.addChatUser")
        }

        fun removeChatUser(): MessageMethodSetter {
            return MessageMethodSetter("messages.removeChatUser")
        }
    }

    class VKGroups {
        val byId: MethodSetter
            get() = MethodSetter("groups.getById")

        fun join(): MethodSetter {
            return MethodSetter("groups.join")
        }
    }

    class VKAccounts {
        fun setOffline(): MethodSetter {
            return MethodSetter("account.setOffline")
        }

        fun setOnline(): MethodSetter {
            return MethodSetter("account.setOnline")
        }
    }

    interface OnResponseListener<E> {
        fun onSuccess(models: ArrayList<E>)
        fun onError(e: Exception)
    }

    class SuccessCallback<E>(private val listener: OnResponseListener<E>?, private val models: ArrayList<E>) : Runnable {
        override fun run() {

            if (listener == null) {
                return
            }

            listener.onSuccess(models)
        }

    }

    class ErrorCallback<E>(private val listener: OnResponseListener<E>?, private val ex: Exception) : Runnable {
        override fun run() {
            if (listener == null) {
                return
            }

            listener.onError(ex)
        }

    }
}