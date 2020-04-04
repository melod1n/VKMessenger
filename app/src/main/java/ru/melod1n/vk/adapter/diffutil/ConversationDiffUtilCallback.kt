package ru.melod1n.vk.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import ru.melod1n.vk.api.model.VKConversation

class ConversationDiffUtilCallback(private val oldList: List<VKConversation>, private val newList: List<VKConversation>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return old.title == new.title &&
                old.lastMessageId == new.lastMessageId &&
                old.photo50 == new.photo50 &&
                old.unreadCount == new.unreadCount &&
                old.isNoSound == new.isNoSound &&
                old.isDisabledForever == new.isDisabledForever &&
                old.disabledUntil == new.disabledUntil &&
                old.inRead == new.inRead &&
                old.outRead == new.outRead
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition].lastMessage
        val new = newList[newItemPosition].lastMessage
        return old.isOut == new.isOut &&
                old.fromId == new.fromId &&
                old.date == new.date &&
                old.action == new.action &&
                old.text == new.text &&
                old.attachments == new.attachments &&
                old.fwdMessages == new.fwdMessages &&
                old.id == new.id
    }

}