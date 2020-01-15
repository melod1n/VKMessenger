package ru.melod1n.vk.adapter.conversations

import androidx.recyclerview.widget.DiffUtil
import ru.melod1n.vk.api.model.VKConversation

class ConversationsDiffUtilCallback(private val oldList: List<VKConversation>, private val newList: List<VKConversation>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldConversation = oldList[oldItemPosition]
        val newConversation = newList[newItemPosition]
        return oldConversation.id == newConversation.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldList[oldItemPosition].lastMessage
        val newMessage = newList[newItemPosition].lastMessage
        return oldMessage == newMessage
    }

}