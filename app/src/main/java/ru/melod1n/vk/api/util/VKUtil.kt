package ru.melod1n.vk.api.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import ru.melod1n.vk.R
import ru.melod1n.vk.adapter.MessageAdapter
import ru.melod1n.vk.api.model.*
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.database.MemoryCache
import ru.melod1n.vk.util.ArrayUtil
import ru.melod1n.vk.util.Util
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

object VKUtil {
    fun extractPattern(string: String, pattern: String): String? {
        val p = Pattern.compile(pattern)
        val m = p.matcher(string)
        return if (!m.find()) null else m.toMatchResult().group(1)
    }

    @Throws(IOException::class)
    fun convertStreamToString(`is`: InputStream): String {
        val r = InputStreamReader(`is`)
        val sw = StringWriter()
        val buffer = CharArray(1024)
        try {
            var n: Int
            while (r.read(buffer).also { n = it } != -1) {
                sw.write(buffer, 0, n)
            }
        } finally {
            try {
                `is`.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
        return sw.toString()
    }

    fun closeStream(oin: Any?) {
        if (oin != null) try {
            if (oin is InputStream) oin.close()
            if (oin is OutputStream) oin.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private const val pattern_string_profile_id = "^(id)?(\\d{1,10})$"

    private val pattern_profile_id = Pattern.compile(pattern_string_profile_id)

    fun parseProfileId(text: String): String? {
        val m = pattern_profile_id.matcher(text)
        return if (!m.find()) null else m.group(2)
    }

    fun sortMessagesByDate(values: ArrayList<VKMessage>, firstOnTop: Boolean): ArrayList<VKMessage> {
        values.sortWith(Comparator { m1, m2 ->
            val d1 = m1.date
            val d2 = m2.date

            if (firstOnTop) {
                d2 - d1
            } else {
                d1 - d2
            }
        })

        return values
    }

    fun sortConversationsByDate(values: ArrayList<VKConversation>, firstOnTop: Boolean): ArrayList<VKConversation> {
        values.sortWith(Comparator { c1, c2 ->
            val d1 = c1.lastMessage.date
            val d2 = c2.lastMessage.date

            if (firstOnTop) {
                d2 - d1
            } else {
                d1 - d2
            }
        })

        return values
    }

    fun prepareList(messages: ArrayList<VKMessage>) {
        for (i in messages.size - 1 downTo 1) {
            val m1 = messages[i]
            val m2 = messages[i - 1]

            val d1 = Util.removeTime(Date(m1.date * 1000L))
            val d2 = Util.removeTime(Date(m2.date * 1000L))

            if (d1 > d2) {
                messages.add(i, MessageAdapter.TimeStamp(d1))
            }
        }
    }

    fun getUserOnline(user: VKUser): String {
        val r = AppGlobal.resources
        return if (user.isOnline) {
            if (user.isOnlineMobile) {
                r.getString(R.string.user_online_mobile)
            } else {
                r.getString(R.string.user_online)
            }
        } else {
            r.getString(R.string.user_last_seen_at, getLastSeenTime(user.lastSeen * 1000L))
        }
    }

    fun getUserOnlineIcon(context: Context, conversation: VKConversation?, peerUser: VKUser?): Drawable? {
        return if (conversation != null) {
            if (conversation.isUser() && peerUser != null) {
                if (!peerUser.isOnline) {
                    null
                } else {
                    ContextCompat.getDrawable(context, if (peerUser.isOnlineMobile) R.drawable.ic_online_mobile else R.drawable.ic_online_pc)
                }
            } else null
        } else {
            if (peerUser!!.isOnline) {
                ContextCompat.getDrawable(context, if (peerUser.isOnlineMobile) R.drawable.ic_online_mobile else R.drawable.ic_online_pc)
            } else {
                null
            }
        }
    }

    fun getUserOnlineIcon(context: Context, user: VKUser): Drawable? {
        return getUserOnlineIcon(context, null, user)
    }

    //TODO: нормальное время
    fun getLastSeenTime(date: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }

    fun getAvatarPlaceholder(dialogTitle: String?): TextDrawable {
        return TextDrawable.builder().buildRound(if (dialogTitle.isNullOrEmpty()) "" else dialogTitle.substring(0, 1), AppGlobal.colorAccent)
    }

    fun searchUser(id: Int): VKUser? {
        return if (VKGroup.isGroupId(id)) {
            null
        } else {
            val user = MemoryCache.getUser(id)

            if (user == null) {
                TaskManager.loadUser(id)
                return VKUser.EMPTY
            }

            user
        }
    }

    fun searchGroup(id: Int): VKGroup? {
        return if (VKGroup.isGroupId(id)) {
            val group = MemoryCache.getGroup(abs(id))

            if (group == null) {
                TaskManager.loadGroup(abs(id))
                return VKGroup.EMPTY
            }

            group
        } else null
    }

    fun getTitle(conversation: VKConversation, peerUser: VKUser?, peerGroup: VKGroup?): String {
        if (conversation.isUser()) {
            if (peerUser != null) {
                return peerUser.toString()
            }
        } else if (conversation.isGroup()) {
            if (peerGroup != null) {
                return peerGroup.name ?: ""
            }
        } else {
            return conversation.title ?: ""
        }

        return ""
    }

    fun getAvatar(conversation: VKConversation, peerUser: VKUser?, peerGroup: VKGroup?): String? {
        if (conversation.isUser()) {
            return peerUser?.photo200
        } else if (conversation.isGroup()) {
            return peerGroup?.photo200
        }
        return conversation.photo200
    }

    fun getUserAvatar(message: VKMessage, fromUser: VKUser?, fromGroup: VKGroup?): String? {
        if (message.isFromUser) {
            return fromUser?.photo100
        } else if (message.isFromGroup) {
            return fromGroup?.photo100
        }
        return null
    }

    fun getDialogType(context: Context, conversation: VKConversation): Drawable? {
        return when {
            conversation.isGroupChannel -> {
                ContextCompat.getDrawable(context, R.drawable.ic_newspaper_variant)
            }
            conversation.isChat() -> {
                ContextCompat.getDrawable(context, R.drawable.ic_people)
            }
            else -> null
        }
    }

    fun getAttachmentText(context: Context, attachments: ArrayList<VKModel>): String {
        val resId: Int

        if (!ArrayUtil.isEmpty(attachments)) {
            if (attachments.size > 1) {
                var oneType = true
                val className = attachments[0].javaClass.simpleName

                for (model in attachments) {
                    if (model.javaClass.simpleName != className) {
                        oneType = false
                        break
                    }
                }

                return if (oneType) {
                    val objectClass: Class<VKModel> = attachments[0].javaClass
                    resId = when (objectClass) {
                        VKPhoto::class.java -> {
                            R.string.message_attachment_photos
                        }
                        VKVideo::class.java -> {
                            R.string.message_attachment_videos
                        }
                        VKAudio::class.java -> {
                            R.string.message_attachment_audios
                        }
                        VKDoc::class.java -> {
                            R.string.message_attachment_docs
                        }
                        else -> {
                            -1
                        }
                    }
                    if (resId == -1) "Unknown attachments" else context.getString(resId, attachments.size).toLowerCase(Locale.getDefault())
                } else {
                    context.getString(R.string.message_attachments_many)
                }
            } else {
                val objectClass: Class<VKModel> = attachments[0].javaClass

                resId = when (objectClass) {
                    VKPhoto::class.java -> {
                        R.string.message_attachment_photo
                    }
                    VKAudio::class.java -> {
                        R.string.message_attachment_audio
                    }
                    VKVideo::class.java -> {
                        R.string.message_attachment_video
                    }
                    VKDoc::class.java -> {
                        R.string.message_attachment_doc
                    }
                    VKGraffiti::class.java -> {
                        R.string.message_attachment_graffiti
                    }
                    VKAudioMessage::class.java -> {
                        R.string.message_attachment_voice
                    }
                    VKSticker::class.java -> {
                        R.string.message_attachment_sticker
                    }
                    VKGift::class.java -> {
                        R.string.message_attachment_gift
                    }
                    VKLink::class.java -> {
                        R.string.message_attachment_link
                    }
                    VKPoll::class.java -> {
                        R.string.message_attachment_poll
                    }
                    VKCall::class.java -> {
                        R.string.message_attachment_call
                    }
                    else -> {
                        return "Unknown"
                    }
                }
            }
        } else {
            return ""
        }
        return context.getString(resId)
    }

    fun getAttachmentDrawable(context: Context, attachments: ArrayList<VKModel>): Drawable? {
        if (ArrayUtil.isEmpty(attachments) || attachments.size > 1) return null

        var resId = -1

        when (attachments[0].javaClass) {
            VKPhoto::class.java -> {
                resId = R.drawable.ic_message_attachment_camera
            }
            VKAudio::class.java -> {
                resId = R.drawable.ic_message_attachment_audio
            }
            VKVideo::class.java -> {
                resId = R.drawable.ic_message_attachment_video
            }
            VKDoc::class.java -> {
                resId = R.drawable.ic_message_attachment_doc
            }
            VKGraffiti::class.java -> {
                resId = R.drawable.ic_message_attachment_graffiti
            }
            VKAudioMessage::class.java -> {
                resId = R.drawable.ic_message_attachment_audio_message
            }
            VKSticker::class.java -> {
                resId = R.drawable.ic_message_attachment_sticker
            }
            VKGift::class.java -> {
                resId = R.drawable.ic_message_attachment_gift
            }
            VKLink::class.java -> {
                resId = R.drawable.ic_message_attachment_link
            }
            VKPoll::class.java -> {
                resId = R.drawable.ic_message_attachment_poll
            }
            VKCall::class.java -> {
                resId = R.drawable.ic_message_attachment_call
            }
        }

        if (resId != -1) {
            val drawable = context.getDrawable(resId)

            drawable?.setTint(AppGlobal.colorAccent)
            return drawable
        }
        return null
    }

    fun getFwdText(context: Context, forwardedMessages: ArrayList<VKMessage>): String {
        return if (!ArrayUtil.isEmpty(forwardedMessages)) {
            if (forwardedMessages.size > 1) {
                context.getString(R.string.message_fwd_many, forwardedMessages.size).toLowerCase(Locale.getDefault())
            } else {
                context.getString(R.string.message_fwd_one)
            }
        } else ""
    }

    fun getActionText(context: Context, lastMessage: VKMessage): String {
        when (lastMessage.action!!.type) {
            VKMessage.ACTION_CHAT_CREATE -> return context.getString(R.string.message_action_created_chat, "")
            VKMessage.ACTION_CHAT_INVITE_USER -> return if (lastMessage.fromId == lastMessage.action!!.memberId) {
                context.getString(R.string.message_action_returned_to_chat, "")
            } else {
                val invited = MemoryCache.getUser(lastMessage.action!!.memberId)
                context.getString(R.string.message_action_invited_user, invited)
            }
            VKMessage.ACTION_CHAT_INVITE_USER_BY_LINK -> return context.getString(R.string.message_action_invited_by_link, "")
            VKMessage.ACTION_CHAT_KICK_USER -> return if (lastMessage.fromId == lastMessage.action!!.memberId) {
                context.getString(R.string.message_action_left_from_chat, "")
            } else {
                val kicked = MemoryCache.getUser(lastMessage.action!!.memberId)
                context.getString(R.string.message_action_kicked_user, kicked)
            }
            VKMessage.ACTION_CHAT_PHOTO_REMOVE -> return context.getString(R.string.message_action_removed_photo, "")
            VKMessage.ACTION_CHAT_PHOTO_UPDATE -> return context.getString(R.string.message_action_updated_photo, "")
            VKMessage.ACTION_CHAT_PIN_MESSAGE -> return context.getString(R.string.message_action_pinned_message, "")
            VKMessage.ACTION_CHAT_UNPIN_MESSAGE -> return context.getString(R.string.message_action_unpinned_message, "")
            VKMessage.ACTION_CHAT_TITLE_UPDATE -> return context.getString(R.string.message_action_updated_title, "")
        }
        return lastMessage.action!!.type ?: ""
    }

    fun getTime(context: Context, lastMessage: VKMessage): String {
        val then = lastMessage.date * 1000L
        val now = System.currentTimeMillis()

        val change = now - then

        val seconds = change / 1000

        if (seconds == 0L) {
            return context.getString(R.string.time_format_now)
        }

        val minutes = seconds / 60

        if (minutes == 0L) {
            return context.getString(R.string.time_format_second, seconds)
        }

        val hours = minutes / 60

        if (hours == 0L) {
            return context.getString(R.string.time_format_minute, minutes)
        }

        val days = hours / 24

        if (days == 0L) {
            return context.getString(R.string.time_format_hour, hours)
        }

        val months = days / 30

        if (months == 0L) {
            return context.getString(R.string.time_format_day, days)
        }

        val years = months / 12

        if (years == 0L) {
            return context.getString(R.string.time_format_month, months)
        } else if (years > 0L) {
            return context.getString(R.string.time_format_year, years)
        }

        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(then)
    }
}