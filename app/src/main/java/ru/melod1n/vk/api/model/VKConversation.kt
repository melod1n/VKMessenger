package ru.melod1n.vk.api.model

import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.util.*

class VKConversation : VKModel, Serializable {

    companion object {
        private const val serialVersionUID = 1L

        const val STATE_IN = "in"
        const val STATE_KICKED = "kicked"
        const val STATE_LEFT = "left"

        const val TYPE_USER = "user"
        const val TYPE_CHAT = "chat"
        const val TYPE_GROUP = "group"

        var count = 0

        fun isChatId(id: Int): Boolean {
            return id > 2_000_000_000
        }

        fun toChatId(id: Int): Int {
            return if (id > 2_000_000_000) id else 2_000_000_000 + id
        }

        fun parse(array: JSONArray): ArrayList<VKConversation> {
            val conversations = ArrayList<VKConversation>()
            for (i in 0 until array.length()) {
                conversations.add(VKConversation(array.optJSONObject(i)))
            }
            return conversations
        }
    }

    /*
           18 — пользователь заблокирован или удален;
           900 — нельзя отправить сообщение пользователю, который в чёрном списке;
           901 — пользователь запретил сообщения от сообщества;
           902 — пользователь запретил присылать ему сообщения с помощью настроек приватности;
           915 — в сообществе отключены сообщения;
           916 — в сообществе заблокированы сообщения;
           917 — нет доступа к чату;
           918 — нет доступа к e-mail;
           203 — нет доступа к сообществу
       */

    var isAllowed = false
    var reason = -1

    var inRead = 0
    var outRead = 0
    var lastMessageId = 0
    var unreadCount = 0

    var id = 0
    var type: String? = null
    var localId = 0

    var disabledUntil = 0
    var isDisabledForever = false
    var isNoSound = false

    var profiles = ArrayList<VKUser>()
    var groups = ArrayList<VKGroup>()

    var membersCount = 0
    var title: String? = null
    var pinnedMessage: VKPinnedMessage? = null
    var state: String? = null
    var activeIds: Array<Int>? = null
    var isGroupChannel = false

    var photo50: String? = null
    var photo100: String? = null
    var photo200: String? = null

    var lastMessage: VKMessage? = null

    val isNotificationsDisabled: Boolean
        get() = isDisabledForever || disabledUntil > 0 || isNoSound

    val isChatId: Boolean
        get() = id > 2_000_000_000

    constructor()
    constructor(o: JSONObject) {
        val oPeer = o.optJSONObject("peer")
        if (oPeer != null) {
            id = oPeer.optInt("id", -1)
            type = oPeer.optString("type")
            localId = oPeer.optInt("local_id")
        }

        inRead = o.optInt("in_read")
        outRead = o.optInt("out_read")
        lastMessageId = o.optInt("last_message_id", -1)
        unreadCount = o.optInt("unread_count", 0)

        val oPushSettings = o.optJSONObject("push_settings")
        if (oPushSettings != null) {
            disabledUntil = oPushSettings.optInt("disabled_until")
            isDisabledForever = oPushSettings.optBoolean("disabled_forever")
            isNoSound = oPushSettings.optBoolean("no_sound")
        }

        val oCanWrite = o.optJSONObject("can_write")
        if (oCanWrite != null) {
            isAllowed = oCanWrite.optBoolean("allowed")
            reason = oCanWrite.optInt("reason", -1)
        }

        val oChatSettings = o.optJSONObject("chat_settings")
        if (oChatSettings != null) {
            membersCount = oChatSettings.optInt("members_count")
            title = oChatSettings.optString("title")

            val oPinnedMessage = oChatSettings.optJSONObject("pinned_message")
            if (oPinnedMessage != null) {
                pinnedMessage = VKPinnedMessage(oPinnedMessage)
            }

            state = oChatSettings.optString("state")

            val oPhoto = oChatSettings.optJSONObject("photo")
            if (oPhoto != null) {
                photo50 = oPhoto.optString("photo_50")
                photo100 = oPhoto.optString("photo_100")
                photo200 = oPhoto.optString("photo_200")
            }

            isGroupChannel = oChatSettings.optBoolean("is_group_channel")
        }
    }

    val isChat: Boolean
        get() = type == TYPE_CHAT

    val isUser: Boolean
        get() = type == TYPE_USER

    val isGroup: Boolean
        get() = type == TYPE_GROUP

    override fun toString(): String {
        return title ?: ""
    }
}