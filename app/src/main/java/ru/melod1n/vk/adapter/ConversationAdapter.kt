package ru.melod1n.vk.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import ru.melod1n.vk.R
import ru.melod1n.vk.adapter.diffutil.ConversationDiffUtilCallback
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.VKLongPollParser
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.api.util.VKUtil
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.fragment.FragmentConversations
import ru.melod1n.vk.fragment.FragmentSettings
import ru.melod1n.vk.util.AndroidUtils
import ru.melod1n.vk.util.ArrayUtil
import ru.melod1n.vk.util.ImageUtil
import ru.melod1n.vk.widget.CircleImageView


class ConversationAdapter(fragmentConversations: FragmentConversations, values: ArrayList<VKConversation>) : BaseAdapter<VKConversation, ConversationAdapter.NormalMessageOut>(fragmentConversations.requireActivity(), values),
        VKLongPollParser.OnMessagesListener,
        VKLongPollParser.OnEventListener {

    var recyclerView = fragmentConversations.getRecyclerView()
    var layoutManager = recyclerView.layoutManager as LinearLayoutManager

    init {
        VKLongPollParser.addOnEventListener(this)
        VKLongPollParser.addOnMessagesListener(this)
    }

    override fun onDestroy() {
        VKLongPollParser.removeOnEventListener(this)
        VKLongPollParser.removeOnMessagesListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NormalMessageOut {
        return NormalMessageOut(view(R.layout.item_conversation, parent))
    }

    inner class NormalMessageOut(v: View) : BaseAdapter.Holder(v) {
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


        private val avatarSize = AndroidUtils.px(if (isExtended) 60F else 50F)
        private val maxLines = if (isExtended) 2 else 1

        override fun bind(position: Int) {
            Log.d("BIND", "position = $position");

            val conversation = getItem(position)

            val lastMessage = conversation.lastMessage

            if (lastMessage == VKMessage()) {
                Log.e("ConversationAdapter", "EMPTY MESSAGE ON POSITION $position")
                return
            }

            val peerUser = VKUtil.searchUser(lastMessage.peerId)
            val peerGroup = VKUtil.searchGroup(lastMessage.peerId)

            val fromUser = VKUtil.searchUser(lastMessage.fromId)
            val fromGroup = VKUtil.searchGroup(lastMessage.fromId)

            avatar.layoutParams.apply {
                if (height != avatarSize) height = avatarSize
                if (width != avatarSize) width = avatarSize
            }

            if (text.maxLines != maxLines) {
                text.maxLines = maxLines
            }

            val dialogTitle = VKUtil.getTitle(conversation, peerUser, peerGroup)
            title.text = dialogTitle

            val onlineIcon = VKUtil.getUserOnlineIcon(context, conversation, peerUser)

            userOnline.apply {
                setImageDrawable(onlineIcon)
                visibility = if (onlineIcon == null) View.GONE else View.VISIBLE
            }

            if ((conversation.isChat() || lastMessage.isOut) && !conversation.isGroupChannel) {
                userAvatar!!.visibility = View.VISIBLE
                ImageUtil.loadImage(VKUtil.getUserAvatar(lastMessage, fromUser, fromGroup), userAvatar, placeholderNormal)
            } else {
                userAvatar!!.visibility = View.GONE
                userAvatar!!.setImageDrawable(null)
            }

            val dialogAvatarPlaceholder = VKUtil.getAvatarPlaceholder(dialogTitle)

            avatar.setImageDrawable(dialogAvatarPlaceholder)

            ImageUtil.loadImage(VKUtil.getAvatar(conversation, peerUser, peerGroup), avatar, dialogAvatarPlaceholder)

            val dDialogType = VKUtil.getDialogType(context, conversation)

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
                    val attachmentString = VKUtil.getAttachmentText(context, lastMessage.attachments!!)

                    val attachmentText = if (lastMessage.text.isNullOrEmpty()) attachmentString else (lastMessage.text
                            ?: "")

                    val startIndex = if (lastMessage.text.isNullOrEmpty()) 0 else lastMessage.text!!.length

                    val span = SpannableString(attachmentText).apply {
                        setSpan(ForegroundColorSpan(colorHighlight), startIndex, attachmentText.length, 0)
                    }

                    val attachmentDrawable = VKUtil.getAttachmentDrawable(context, lastMessage.attachments!!)

                    text.apply {
                        text = span
                        setCompoundDrawablesRelativeWithIntrinsicBounds(attachmentDrawable, null, null, null)
                        compoundDrawablePadding = 8
                    }
                } else if (!ArrayUtil.isEmpty(lastMessage.fwdMessages)) {
                    val fwdText = VKUtil.getFwdText(context, lastMessage.fwdMessages!!)
                    val span = SpannableString(fwdText).apply {
                        setSpan(ForegroundColorSpan(colorHighlight), 0, fwdText.length, 0)
                    }

                    text.text = span
                } else {
                    text.text = if (text.maxLines == 1) lastMessage.text.toString().replace("\n", " ") else lastMessage.text
                }
            } else {
                val actionText = VKUtil.getActionText(context, lastMessage)
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

            val isRead = ((lastMessage.isOut && conversation.outRead == conversation.lastMessageId ||
                    !lastMessage.isOut && conversation.inRead == conversation.lastMessageId) && conversation.lastMessageId == lastMessage.id) && conversation.unreadCount == 0

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

            dialogDate.text = VKUtil.getTime(context, lastMessage)
            dialogCounter.background.setTint(if (conversation.isNotificationsDisabled()) Color.GRAY else colorHighlight)
        }

    }

    private fun addMessage(message: VKMessage) {
        val index = searchConversationIndex(message.peerId)

        CacheStorage.insertMessage(message)

        val list = ArrayList(values)

        if (index >= 0) {
            val conversation = prepareConversation(getItem(index), message)

            if (index == 0) {
                notifyItemChanged(0)
            } else {
                list.removeAt(index)
                list.add(0, conversation)
            }
        } else {
            val conversation = CacheStorage.getConversation(message.peerId)

            if (conversation != null) {
                list.add(0, prepareConversation(conversation, message))
            } else {
                val temp = VKConversation().apply {
                    id = message.peerId
                    localId = if (VKConversation.isChatId(id)) id - 2000000000 else id
                    type = if (id < 0) VKConversation.TYPE_GROUP else if (id > 2000000000) VKConversation.TYPE_CHAT else VKConversation.TYPE_USER
                    lastMessage = message
                    lastMessageId = message.id
                }

                list.add(0, temp)
            }
        }

        updateList(list)

        if (layoutManager.findFirstVisibleItemPosition() < 2) {
            layoutManager.scrollToPosition(0)
        }
    }

    fun updateList(newList: List<VKConversation>) {
        val diffCallBack = ConversationDiffUtilCallback(values, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallBack, false)

        setItems(ArrayList(newList))

        diffResult.dispatchUpdatesTo(this)
    }

    private fun editMessage(message: VKMessage) {
        val index = searchConversationIndex(message.peerId)
        if (index == -1) return

        val dialog = getItem(index)

        dialog.lastMessage = message

        notifyItemChanged(index)
    }

    private fun readMessage(messageId: Int, peerId: Int) {
        val index = searchConversationIndex(peerId)
        if (index == -1) return

        val dialog = getItem(index)

        if (dialog.lastMessageId == messageId) {
            dialog.unreadCount = 0
        } else {
            dialog.unreadCount = dialog.lastMessageId - messageId
        }

        notifyItemChanged(index)
    }

    private fun deleteMessage(peerId: Int) {
        val index = searchConversationIndex(peerId)
        if (index == -1) return

        val dialog = getItem(index)

        val messages = VKUtil.sortMessagesByDate(CacheStorage.getMessages(dialog.id), true)

        if (messages.isEmpty()) {
            CacheStorage.deleteConversation(dialog.id)

            val items = ArrayList(values)
            items.removeAt(index)

            updateList(items)
        } else {
            val lastMessage = messages[0]

            dialog.lastMessageId = lastMessage.id
            dialog.lastMessage = lastMessage

            setItems(VKUtil.sortConversationsByDate(values, true))
            notifyDataSetChanged()
        }
    }

    private fun restoreMessage(message: VKMessage) {
        val index = searchConversationIndex(message.peerId)
        if (index == -1) return

        val dialog = getItem(index)

        //TODO: кривое сообщение

        val messages = CacheStorage.getMessages(dialog.id).apply { add(message) }
        VKUtil.sortMessagesByDate(messages, true)

        val lastMessage = messages[0]

        dialog.lastMessageId = lastMessage.id
        dialog.lastMessage = lastMessage

        setItems(VKUtil.sortConversationsByDate(values, true))
        notifyDataSetChanged()
    }

    private fun prepareConversation(conversation: VKConversation, newMessage: VKMessage): VKConversation {
        conversation.lastMessage = newMessage
        conversation.lastMessageId = newMessage.id

        if (newMessage.isOut) {
            conversation.unreadCount = 0
            newMessage.isRead = false
        } else {
            conversation.unreadCount += 1
        }

        if (newMessage.peerId == newMessage.fromId && newMessage.fromId == UserConfig.userId) { //для лс
            conversation.outRead = newMessage.id
        }

        return conversation
    }

    private fun searchConversationIndex(peerId: Int): Int {
        for (i in values.indices) {
            val dialog = getItem(i)

            if (dialog.id == peerId) return i
        }
        return -1
    }

    private fun searchMessageIndex(messageId: Int): Int {
        for (i in values.indices) {
            val dialog = getItem(i)
            if (dialog.lastMessageId == messageId) return i
        }
        return -1
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

        values[index] = (CacheStorage.getConversation(peerId) ?: VKConversation())

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
            lastMessageId = messageId
            lastMessage = CacheStorage.getMessage(messageId)!!
        }

        notifyItemChanged(index)
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
        deleteMessage(peerId)
    }

    override fun onRestoredMessage(message: VKMessage) {
        restoreMessage(message)
    }


}