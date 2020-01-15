package ru.melod1n.vk.database

import android.util.SparseArray
import ru.melod1n.vk.api.model.VKGroup
import ru.melod1n.vk.api.model.VKUser
import java.util.*

object MemoryCache {
    private val users = SparseArray<VKUser>(30)
    private val groups = SparseArray<VKGroup>(30)

    fun getUser(id: Int): VKUser? {
        var user = users[id]
        if (user == null) {
            user = CacheStorage.getUser(id)

            user?.let { append(it) }
        }
        return user
    }

    @JvmStatic
    fun getGroup(id: Int): VKGroup? {
        var group = groups[id]
        if (group == null) {
            group = CacheStorage.getGroup(id)

            group?.let { append(it) }
        }
        return group
    }

    fun update(users: ArrayList<VKUser>) {
        for (user in users) {
            append(user)
        }
    }

    fun append(value: VKGroup) {
        groups.append(value.id, value)
    }

    fun append(value: VKUser) {
        users.append(value.id, value)
    }

    fun clear() {
        users.clear()
        groups.clear()
    }
}