package ru.melod1n.vk.api.model

import android.util.ArrayMap
import org.json.JSONArray
import org.json.JSONObject
import ru.melod1n.vk.api.UserConfig
import java.io.Serializable
import java.util.*

open class VKMessage : VKModel, Serializable {
    companion object {
        private const val serialVersionUID = 1L

        var lastHistoryCount = 0

        const val UNREAD = 1 // Оно просто есть
        const val OUTBOX = 1 shl 1 // Исходящее сообщение
        const val REPLIED = 1 shl 2 // На сообщение был создан ответ
        const val IMPORTANT = 1 shl 3 // Важное сообщение
        const val FRIENDS = 1 shl 5 // Сообщение в чат друга
        const val SPAM = 1 shl 6 // Сообщение помечено как спам
        const val DELETED = 1 shl 7 // Удаление сообщения
        const val AUDIO_LISTENED = 1 shl 12 // ГС прослушано
        const val CHAT = 1 shl 13 // Сообщение отправлено в беседу
        const val CANCEL_SPAM = 1 shl 15 // Отмена пометки спама
        const val HIDDEN = 1 shl 16 // Приветственное сообщение сообщества
        const val DELETE_FOR_ALL = 1 shl 17 // Сообщение удалено для всех
        const val CHAT_IN = 1 shl 19 // Входящее сообщение в беседе
        const val REPLY_MSG = 1 shl 21 // Ответ на сообщение

        private val flags = ArrayMap<String, Int>()

        const val ACTION_CHAT_CREATE = "chat_create"
        const val ACTION_CHAT_INVITE_USER = "chat_invite_user"
        const val ACTION_CHAT_KICK_USER = "chat_kick_user"
        const val ACTION_CHAT_TITLE_UPDATE = "chat_title_update"
        const val ACTION_CHAT_PHOTO_UPDATE = "chat_photo_update"
        const val ACTION_CHAT_PHOTO_REMOVE = "chat_photo_remove"
        const val ACTION_CHAT_PIN_MESSAGE = "chat_pin_message"
        const val ACTION_CHAT_UNPIN_MESSAGE = "chat_unpin_message"
        const val ACTION_CHAT_INVITE_USER_BY_LINK = "chat_invite_user_by_link"

        fun hasFlag(mask: Int, flagName: String?): Boolean {
            val o: Any? = flags[flagName]
            return if (o != null) { //has flag
                val flag = o as Int
                flag and mask > 0
            } else false
        }

        //TODO: нормально парсить сообщение
        // [msg_id, flags, peer_id, timestamp, text, {emoji, from, action, keyboard}, {attachs}, random_id, conv_msg_id, edit_time]
        fun parse(array: JSONArray): VKMessage {
            val message = VKMessage()

            val id = array.optInt(1)
            message.id = id

            val mask = array.optInt(2)

            val peerId = array.optInt(3)
            message.peerId = peerId

            val date = array.optInt(4)
            message.date = date

            val text = array.optString(5)
            message.text = text

            val o = array.optJSONObject(6)
            val fromId = if (hasFlag(mask, "outbox")) UserConfig.userId else {
                if (hasFlag(mask, "chat_in")) o?.optInt("from")
                else peerId
            } ?: peerId

            message.fromId = fromId

            val out = hasFlag(mask, "outbox") || fromId == UserConfig.userId
            message.isOut = out

            val editTime = array.optInt(10)
            message.editTime = editTime

            val conversationMessageId = array.optInt(9)
            message.conversationMessageId = conversationMessageId

            val randomId = array.optInt(8)
            message.randomId = randomId

            if (o != null) {
                if (o.has("source_act")) {
                    val action = Action()
                    action.type = o.optString("source_act")
                    if (o.has("source_text")) action.text = o.optString("source_text")
                    if (o.has("source_mid")) action.memberId = o.optInt("source_mid")
                    message.action = action
                }
            }
            return message
        }

        fun isOut(flags: Int): Boolean {
            return OUTBOX and flags > 0
        }

        fun isDeleted(flags: Int): Boolean {
            return DELETED and flags > 0
        }

        fun isUnread(flags: Int): Boolean {
            return UNREAD and flags > 0
        }

        fun isSpam(flags: Int): Boolean {
            return SPAM and flags > 0
        }

        fun isCanceledSpam(flags: Int): Boolean {
            return CANCEL_SPAM and flags > 0
        }

        fun isImportant(flags: Int): Boolean {
            return IMPORTANT and flags > 0
        }

        fun isDeletedForAll(flags: Int): Boolean {
            return DELETE_FOR_ALL and flags > 0
        }

        init {
            flags["unread"] = UNREAD
            flags["outbox"] = OUTBOX
            flags["replied"] = REPLIED
            flags["important"] = IMPORTANT
            flags["friends"] = FRIENDS
            flags["spam"] = SPAM
            flags["deleted"] = DELETED
            flags["audio_listened"] = AUDIO_LISTENED
            flags["chat"] = CHAT
            flags["cancel_spam"] = CANCEL_SPAM
            flags["hidden"] = HIDDEN
            flags["delete_for_all"] = DELETE_FOR_ALL
            flags["chat_in"] = CHAT_IN
            flags["reply_msg"] = REPLY_MSG
        }
    }

    var id = 0
    var date = 0
    var peerId = 0
    var fromId = 0
    var editTime = 0
    var isOut = false
    var text: String? = null
    var randomId = 0
    var conversationMessageId = 0

    var attachments: ArrayList<VKModel>? = null

    var isImportant = false

    var fwdMessages: ArrayList<VKMessage>? = null

    var replyMessageId = 0

    var action: Action? = null

    var isRead = false

    constructor()
    constructor(o: JSONObject) {
        id = o.optInt("id", -1)
        date = o.optInt("date")
        peerId = o.optInt("peer_id", -1)
        fromId = o.optInt("from_id", -1)
        isOut = o.optInt("out") == 1

        text = o.optString("text")
        randomId = o.optInt("random_id", -1)
        conversationMessageId = o.optInt("conversation_message_id", -1)
        editTime = o.optInt("edit_time")

        val oAttachments = o.optJSONArray("attachments")
        if (oAttachments != null) {
            attachments = VKAttachments.parse(oAttachments)
        }

        isImportant = o.optBoolean("important")

        val oFwdMessages = o.optJSONArray("fwd_messages")
        if (oFwdMessages != null) {
            val fwdMessages = ArrayList<VKMessage>(oFwdMessages.length())
            for (i in 0 until oFwdMessages.length()) {
                fwdMessages.add(VKMessage(oFwdMessages.optJSONObject(i)))
            }
            this.fwdMessages = fwdMessages
        }

        val oReplyMessage = o.optJSONObject("reply_message")
        if (oReplyMessage != null) {
            replyMessageId = VKMessage(oReplyMessage).id
        }

        val oAction = o.optJSONObject("action")
        if (oAction != null) {
            action = Action(oAction)
        }
    }

    val isFromUser: Boolean
        get() = fromId > 0

    val isFromGroup: Boolean
        get() = fromId < 0

    class Action : Serializable {
        /*
            chat_photo_update — обновлена фотография беседы;
            chat_photo_remove — удалена фотография беседы;
            chat_create — создана беседа;
            chat_title_update — обновлено название беседы;
            chat_invite_user — приглашен пользователь;
            chat_kick_user — исключен пользователь;
            chat_pin_message — закреплено сообщение;
            chat_unpin_message — откреплено сообщение;
            chat_invite_user_by_link — пользователь присоединился к беседе по ссылке.
        */
        var type: String? = null
        var memberId = 0 //kick / invite / pin / unpin = 0
        var text: String? = null //for chat_create / title_update
        var photo: Photo? = null

        internal constructor()
        internal constructor(o: JSONObject) {
            type = o.optString("type")
            memberId = o.optInt("member_id", -1)
            text = o.optString("text")
        }

        inner class Photo : Serializable {
            var photo50: String? = null
            var photo100: String? = null
            var photo200: String? = null
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }
}