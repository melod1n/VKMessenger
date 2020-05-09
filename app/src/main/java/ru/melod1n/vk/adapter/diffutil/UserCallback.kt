package ru.melod1n.vk.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import ru.melod1n.vk.api.model.VKUser

class UserCallback(private val oldList: List<VKUser>, private val newList: List<VKUser>) : DiffUtil.Callback() {

    companion object {
        const val ONLINE = "online"
        const val ONLINE_MOBILE = "online_mobile"
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        return old.id == new.id
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        return old.firstName == new.firstName &&
                old.lastName == new.lastName &&
                old.isOnline == new.isOnline &&
                old.isOnlineMobile == new.isOnlineMobile &&
                old.lastSeen == new.lastSeen &&
                old.lastSeenPlatform == new.lastSeenPlatform &&
                old.deactivated == new.deactivated
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        if (old.isOnlineMobile != new.isOnlineMobile) {
            if (old.isOnline != new.isOnline) return ONLINE

            return ONLINE_MOBILE
        }

        return super.getChangePayload(oldItemPosition, newItemPosition)
    }

}