package ru.melod1n.vk.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amulyakhare.textdrawable.TextDrawable
import com.squareup.picasso.Picasso
import ru.melod1n.vk.R
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.VKLongPollParser
import ru.melod1n.vk.api.model.*
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.common.TimeManager.OnMinuteChangeListener
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.database.DatabaseHelper
import ru.melod1n.vk.database.MemoryCache
import ru.melod1n.vk.fragment.FragmentConversations
import ru.melod1n.vk.fragment.FragmentSettings
import ru.melod1n.vk.util.AndroidUtils
import ru.melod1n.vk.util.ArrayUtil
import ru.melod1n.vk.widget.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class ConversationAdapter(var fragmentConversations: FragmentConversations, values: ArrayList<VKConversation>) : BaseAdapter<VKConversation, ConversationAdapter.ViewHolder>(fragmentConversations.requireContext(), values),
        OnMinuteChangeListener,
        VKLongPollParser.OnMessagesListener,
        VKLongPollParser.OnEventListener {

    init {
//        VKLongPollParser.addOnMessagesListener(this)
//        VKLongPollParser.addOnEventListener(this)
    }

    override fun onDestroy() {
//        VKLongPollParser.removeOnMessagesListener(this)
//        VKLongPollParser.removeOnEventListener(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_conversation, viewGroup, false))
    }

    override fun onEvent(info: EventInfo<*>) {
        when (info.key) {
            EventInfo.MESSAGE_UPDATE -> updateMessage(info.data as Int)
            EventInfo.USER_UPDATE -> updateUser(info.data as Int)
            EventInfo.GROUP_UPDATE -> updateGroup(info.data as Int)
            EventInfo.CONVERSATION_UPDATE -> updateConversation(info.data as Int)
        }
    }

    private fun updateConversation(peerId: Int) {
        val index = searchConversationIndex(peerId)
        if (index == -1) return

        values[index] = CacheStorage.getConversation(peerId) ?: VKConversation()

        notifyItemChanged(index)
    }

    private fun updateGroup(groupId: Int) {
        val index = searchConversationIndex(groupId)
        if (index == -1) return
        notifyItemChanged(index)
    }

    private fun updateUser(userId: Int) {
        val index = searchConversationIndex(userId)
        if (index == -1) return
        notifyItemChanged(index)
    }

    private fun updateMessage(messageId: Int) {
        val index = searchMessageIndex(messageId)
        if (index == -1) return
        getItem(index).apply {
            lastMessage = CacheStorage.getMessage(messageId)
        }

        notifyItemChanged(index)
    }

    private fun readMessage(messageId: Int, peerId: Int) {
        val index = searchConversationIndex(peerId)
        if (index == -1) return
        val dialog = getItem(index)

        if (dialog.lastMessage!!.id != messageId) return

        if (dialog.lastMessage!!.isOut) {
            dialog.outRead = messageId
        } else {
            dialog.inRead = messageId
        }
        notifyItemChanged(index)
    }

    private fun restoreMessage(message: VKMessage) {
        val index = searchConversationIndex(message.peerId)
        if (index == -1) return
        val dialog = getItem(index)
        if (dialog.lastMessage!!.date > message.date) return
        dialog.lastMessage = message
        notifyItemChanged(index)
    }

    private fun deleteMessage(messageId: Int, peerId: Int) {
        val index = searchConversationIndex(peerId)
        if (index == -1) return

        val dialog = getItem(index)

        CacheStorage.delete(DatabaseHelper.TABLE_MESSAGES, DatabaseHelper.MESSAGE_ID, messageId)

        val messages = CacheStorage.getMessages(peerId)

        val preLast = if (ArrayUtil.isEmpty(messages)) null else messages[0]

        if (preLast == null) {
            TaskManager.loadConversation(peerId, object : OnResponseListener<VKConversation> {
                override fun onSuccess(models: ArrayList<VKConversation>) {
                    val conversation = models[0]
                    if (conversation.lastMessageId == 0) {
                        CacheStorage.delete(DatabaseHelper.TABLE_CONVERSATIONS, DatabaseHelper.PEER_ID, peerId)
                        values.removeAt(index)
                        notifyItemRemoved(index)
                        notifyItemRangeChanged(0, itemCount, 0)
                    } else {
                        TaskManager.loadMessage(conversation.lastMessageId, object : OnResponseListener<VKMessage> {
                            override fun onSuccess(models: ArrayList<VKMessage>) {
                                dialog.lastMessage = models[0]
                                notifyItemChanged(index, 0)
                            }

                            override fun onError(e: Exception) {}
                        })
                    }
                }

                override fun onError(e: Exception) {}
            })
        } else {
            if (dialog.lastMessage!!.id != messageId) return

            dialog.lastMessage = preLast
            notifyItemChanged(index)
        }
    }

    private fun editMessage(message: VKMessage) {
        val index = searchConversationIndex(message.peerId)
        if (index == -1) return

        val dialog = getItem(index)

        dialog.lastMessage = message

        notifyItemChanged(index, 0)
    }

    private fun addMessage(message: VKMessage) {
        val index = searchConversationIndex(message.peerId)

        val layoutManager = fragmentConversations.getRecyclerView().layoutManager as LinearLayoutManager

        if (index >= 0) {
            val conversation = getItem(index)
            val prepared = prepareConversation(conversation, message)

            if (index == 0) {
                notifyItemRangeChanged(0, 1)
            } else {
                val maxPosition = layoutManager.findLastCompletelyVisibleItemPosition() - layoutManager.findFirstCompletelyVisibleItemPosition()

                if (index > maxPosition) {
                    notifyItemRemoved(index)

                    values.removeAt(index)

                    notifyItemInserted(0)

                    values.add(0, prepared)
                } else {
                    notifyItemMoved(index, 0)

                    values.removeAt(index)
                    values.add(0, prepared)
                }
            }
        } else {
            val conversation = CacheStorage.getConversation(message.peerId)
            if (conversation != null) {
                values.add(0, prepareConversation(conversation, message))
            } else {
                val temp = VKConversation().apply {
                    id = message.peerId
                    localId = if (VKConversation.isChatId(id)) id - 2000000000 else id
                    type = if (id < 0) VKConversation.TYPE_GROUP else if (id > 2000000000) VKConversation.TYPE_CHAT else VKConversation.TYPE_USER
                    lastMessage = message
                }

                values.add(0, temp)
            }

            notifyItemInserted(0)
        }

        if (layoutManager.findFirstVisibleItemPosition() < 2) {
            layoutManager.scrollToPosition(0)
        }

        notifyDataSetChanged()

    }

    private fun prepareConversation(conversation: VKConversation, newMessage: VKMessage): VKConversation {
        if (conversation.lastMessage != newMessage)
            conversation.lastMessage = newMessage

        conversation.lastMessageId = newMessage.id

        if (newMessage.isOut) {
            conversation.unreadCount = 0
            newMessage.isRead = false
        } else {
            conversation.unreadCount = conversation.unreadCount + 1
        }
        if (newMessage.peerId == newMessage.fromId && newMessage.fromId == UserConfig.userId) { //для лс
            conversation.outRead = newMessage.id
        }
        return conversation
    }

    private fun searchMessageIndex(messageId: Int): Int {
        for (i in 0 until itemCount) {
            val dialog = getItem(i)
            if (dialog.lastMessage!!.id == messageId) return i
        }
        return -1
    }

    private fun searchConversationIndex(peerId: Int): Int {
        for (i in 0 until itemCount) {
            val dialog = getItem(i)
            if (dialog.id == peerId) return i
        }
        return -1
    }

    override fun onMinuteChange(currentMinute: Int) {
        notifyItemRangeChanged(0, itemCount, 0)
    }

    override fun onNewMessage(message: VKMessage) {
        addMessage(message)
    }

    override fun onEditMessage(message: VKMessage) {
        editMessage(message)
    }

    override fun onReadMessage(messageId: Int, peerId: Int) {
        readMessage(messageId, peerId)
    }

    override fun onDeleteMessage(messageId: Int, peerId: Int) {
        deleteMessage(messageId, peerId)
    }

    override fun onRestoredMessage(message: VKMessage) {
        restoreMessage(message)
    }

    inner class ViewHolder(v: View) : BaseAdapter.Holder(v) {

        private var text = v.findViewById<TextView>(R.id.dialogText)
        private var title = v.findViewById<TextView>(R.id.dialogTitle)
        private var avatar = v.findViewById<CircleImageView>(R.id.dialogAvatar)
        private var userAvatar = v.findViewById<CircleImageView>(R.id.dialogUserAvatar)
        private var userOnline = v.findViewById<ImageView>(R.id.dialogUserOnline)
        private var dialogType = v.findViewById<ImageView>(R.id.dialogType)
        private var dialogCounter = v.findViewById<TextView>(R.id.dialogCounter)
        private var dialogOut = v.findViewById<CircleImageView>(R.id.dialogOut)
        private var dialogDate = v.findViewById<TextView>(R.id.dialogDate)

        private val placeholderNormal: Drawable = ColorDrawable(Color.TRANSPARENT)
        private val colorHighlight = AppGlobal.colorAccent

        private val isExtended = Integer.parseInt(AppGlobal.preferences.getString(FragmentSettings.KEY_EXTENDED_CONVERSATIONS, "1")
                ?: "1") == 2

        override fun bind(position: Int) {
            val conversation = getItem(position)

            val lastMessage = CacheStorage.getMessage(conversation.lastMessageId)
                    ?: conversation.lastMessage!!

            val peerUser = searchPeerUser(lastMessage)
            val fromUser = searchFromUser(lastMessage)
            val peerGroup = searchPeerGroup(lastMessage)
            val fromGroup = searchFromGroup(lastMessage)

            val avatarSize = AndroidUtils.px(if (isExtended) 75F else 68F)

            avatar.layoutParams.apply {
                height = avatarSize
                width = avatarSize
            }

            text.maxLines = if (isExtended) 2 else 1

            val dialogTitle = getTitle(conversation, peerUser, peerGroup)
            title.text = dialogTitle

            val onlineIcon = getOnlineIcon(conversation, peerUser)

            userOnline.apply {
                setImageDrawable(onlineIcon)
                visibility = if (onlineIcon == null) View.GONE else View.VISIBLE
            }

            if ((conversation.isChat || lastMessage.isOut) && !conversation.isGroupChannel) {
                userAvatar!!.visibility = View.VISIBLE
                loadImage(getUserAvatar(lastMessage, fromUser, fromGroup), userAvatar, placeholderNormal)
            } else {
                userAvatar!!.visibility = View.GONE
                userAvatar!!.setImageDrawable(null)
            }

            val dialogAvatarPlaceholder = TextDrawable
                    .builder()
                    .buildRound(if (dialogTitle.isEmpty()) "" else dialogTitle.substring(0, 1), AppGlobal.colorAccent)


            avatar.setImageDrawable(dialogAvatarPlaceholder)

            loadImage(getAvatar(conversation, peerUser, peerGroup), avatar, dialogAvatarPlaceholder)

            val dDialogType = getDialogType(conversation)

            dialogType.apply {
                visibility = if (dDialogType != null) View.VISIBLE else View.GONE
                setImageDrawable(dDialogType)
            }

            text.apply {
                compoundDrawablePadding = 0
                setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            }

            if (lastMessage.action == null) {
                if (!ArrayUtil.isEmpty(lastMessage.attachments)) {
                    val attachmentString = getAttachmentText(lastMessage.attachments!!)

                    val attachmentText = if (lastMessage.text.isNullOrEmpty()) attachmentString else (lastMessage.text ?: "")

                    val startIndex = if (lastMessage.text.isNullOrEmpty()) 0 else lastMessage.text!!.length

                    val span = SpannableString(attachmentText).apply {
                        setSpan(ForegroundColorSpan(colorHighlight), startIndex, attachmentText.length, 0)
                    }

                    val attachmentDrawable = getAttachmentDrawable(lastMessage.attachments!!)

                    text.apply {
                        text = span
                        setCompoundDrawablesRelativeWithIntrinsicBounds(attachmentDrawable, null, null, null)
                        compoundDrawablePadding = 8
                    }
                } else if (!ArrayUtil.isEmpty(lastMessage.fwdMessages)) {
                    val fwdText = getFwdText(lastMessage.fwdMessages!!)
                    val span = SpannableString(fwdText).apply {
                        setSpan(ForegroundColorSpan(colorHighlight), 0, fwdText.length, 0)
                    }

                    text.text = span
                } else {
                    text.text = if (text.maxLines == 1) lastMessage.text.toString().replace("\n", " ") else lastMessage.text
                }
            } else {
                val actionText = getActionText(lastMessage)
                val span = SpannableString(actionText).apply {
                    setSpan(ForegroundColorSpan(colorHighlight), 0, actionText.length, 0)
                }

                text.text = span
            }

            if (ArrayUtil.isEmpty(lastMessage.attachments) && ArrayUtil.isEmpty(lastMessage.fwdMessages) && lastMessage.action == null && TextUtils.isEmpty(lastMessage.text)) {
                val unknown = "Unknown"
                val span = SpannableString(unknown).apply {
                    setSpan(ForegroundColorSpan(colorHighlight), 0, unknown.length, 0)
                }

                text.text = span
            }

            val isRead = (lastMessage.isOut && conversation.outRead == conversation.lastMessageId || !lastMessage.isOut && conversation.inRead == conversation.lastMessageId) && conversation.lastMessageId == lastMessage.id

            if (isRead) {
                dialogCounter.visibility = View.GONE
                dialogOut.visibility = View.GONE
            } else {
                if (lastMessage.isOut) {
                    dialogOut.visibility = View.VISIBLE
                    dialogCounter.visibility = View.GONE
                    dialogCounter.text = ""
                } else {
                    dialogOut.visibility = View.GONE
                    dialogCounter.visibility = View.VISIBLE
                    dialogCounter.text = conversation.unreadCount.toString()
                }
            }

            dialogDate.text = getTime(lastMessage)
            dialogCounter.background.setTint(if (conversation.isNotificationsDisabled) Color.GRAY else colorHighlight)
        }

        private fun replaceText(text: TextView) {
            text.text = if (text.maxLines == 1) text.text.toString().replace("\n", " ") else text.text
        }

        private fun getTime(lastMessage: VKMessage): String {
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

        private fun getOnlineIcon(conversation: VKConversation, peerUser: VKUser?): Drawable? {
            return if (conversation.isUser && peerUser != null) {
                if (!peerUser.isOnline) {
                    null
                } else {
                    context.getDrawable(if (peerUser.isOnlineMobile) R.drawable.ic_online_mobile else R.drawable.ic_online_pc)
                }
            } else null
        }

        private fun getActionText(lastMessage: VKMessage): String {
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

        private fun getAttachmentText(attachments: ArrayList<VKModel>): String {
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

        private fun getAttachmentDrawable(attachments: ArrayList<VKModel>): Drawable? {
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

        private fun getFwdText(forwardedMessages: ArrayList<VKMessage>): String {
            return if (!ArrayUtil.isEmpty(forwardedMessages)) {
                if (forwardedMessages.size > 1) {
                    context.getString(R.string.message_fwd_many, forwardedMessages.size).toLowerCase(Locale.getDefault())
                } else {
                    context.getString(R.string.message_fwd_one)
                }
            } else ""
        }

        private fun getDialogType(conversation: VKConversation?): Drawable? {
            return when {
                conversation!!.isGroupChannel -> {
                    ContextCompat.getDrawable(context, R.drawable.ic_newspaper_variant)
                }
                conversation.isChat -> {
                    ContextCompat.getDrawable(context, R.drawable.ic_people)
                }
                else -> null
            }
        }

        private fun loadImage(image: String?, imageView: ImageView, placeholder: Drawable) {
            if (image == null || image.isEmpty()) return

            Picasso.get().load(image).priority(Picasso.Priority.LOW).placeholder(placeholder).into(imageView)
        }

        private fun getUserAvatar(message: VKMessage, fromUser: VKUser?, fromGroup: VKGroup?): String? {
            if (message.isFromUser) {
                if (fromUser != null) {
                    return fromUser.photo100
                }
            } else if (message.isFromGroup) {
                if (fromGroup != null) {
                    return fromGroup.photo100
                }
            }
            return null
        }

        private fun searchPeerUser(message: VKMessage): VKUser? {
            val user = MemoryCache.getUser(message.peerId)
            if (user == null && VKUser.isUserId(message.peerId)) {
                TaskManager.loadUser(message.peerId)
            }
            return user
        }

        private fun searchFromUser(message: VKMessage): VKUser? {
            val user = MemoryCache.getUser(message.fromId)
            if (user == null && VKUser.isUserId(message.fromId)) {
                TaskManager.loadUser(message.fromId)
            }
            return user
        }

        private fun searchPeerGroup(message: VKMessage): VKGroup? {
            val id = abs(message.peerId)
            val group = MemoryCache.getGroup(id)
            if (group == null && VKGroup.isGroupId(message.peerId)) {
                TaskManager.loadGroup(message.peerId)
            }
            return group
        }

        private fun searchFromGroup(message: VKMessage): VKGroup? {
            val group = MemoryCache.getGroup(message.fromId)
            if (group == null && VKGroup.isGroupId(message.fromId)) {
                TaskManager.loadGroup(message.fromId)
            }
            return group
        }
    }

    fun getId(conversation: VKConversation, peerUser: VKUser?, peerGroup: VKGroup?): Int {
        if (conversation.isUser) {
            if (peerUser != null) {
                return peerUser.id
            }
        } else if (conversation.isGroup) {
            if (peerGroup != null) {
                return peerGroup.id
            }
        } else {
            return conversation.id
        }
        return -1
    }

    fun getTitle(conversation: VKConversation, peerUser: VKUser?, peerGroup: VKGroup?): String {
        if (conversation.isUser) {
            if (peerUser != null) {
                return peerUser.toString()
            }
        } else if (conversation.isGroup) {
            if (peerGroup != null) {
                return peerGroup.name ?: ""
            }
        } else {
            return conversation.title!!
        }
        return ""
    }

    fun getAvatar(conversation: VKConversation, peerUser: VKUser?, peerGroup: VKGroup?): String? {
        if (conversation.isUser) {
            return peerUser?.photo200
        } else if (conversation.isGroup) {
            return peerGroup?.photo200
        }
        return conversation.photo200
    }

    override fun viewHolder(view: View, type: Int): ViewHolder {
        return ViewHolder(view)
    }

    override fun layoutId(type: Int): Int {
        return R.layout.item_conversation
    }
}