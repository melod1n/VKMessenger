package ru.melod1n.vk.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import ru.melod1n.vk.api.model.VKConversation

class ConversationCallback(private val oldList: List<VKConversation>, private val newList: List<VKConversation>) : DiffUtil.Callback() {

    companion object {
        const val DATE = "date"
        const val ONLINE = "online"
        const val AVATAR = "avatar"
        const val USER_AVATAR = "user_avatar"
        const val ATTACHMENTS = "attachments"
        const val READ = "read"
        const val NOTIFICATIONS = "notifications"
        const val EDIT_MESSAGE = "edit_message"
        const val MESSAGE = "message"
        const val USER = "user"
        const val GROUP = "group"
        const val CONVERSATION = "conversation"
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return old.id == new.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        val oldMessage = old.lastMessage
        val newMessage = new.lastMessage

        return old.title == new.title &&
                old.lastMessageId == new.lastMessageId &&
                old.photo50 == new.photo50 &&
                old.unreadCount == new.unreadCount &&
                old.isNoSound == new.isNoSound &&
                old.isDisabledForever == new.isDisabledForever &&
                old.disabledUntil == new.disabledUntil &&
                old.inRead == new.inRead &&
                old.outRead == new.outRead &&

                oldMessage.isOut == newMessage.isOut &&
                oldMessage.fromId == newMessage.fromId &&
                oldMessage.date == newMessage.date &&
                oldMessage.action == newMessage.action &&
                oldMessage.text == newMessage.text &&
                oldMessage.attachments == newMessage.attachments &&
                oldMessage.fwdMessages == newMessage.fwdMessages &&
                oldMessage.id == newMessage.id
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val d1 = oldList[oldItemPosition].lastMessage.date
        val d2 = newList[newItemPosition].lastMessage.date

        if (d1 != d2) return DATE

        return super.getChangePayload(oldItemPosition, newItemPosition)
    }

}