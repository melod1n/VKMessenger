package ru.melod1n.vk.common

class EventInfo<T> {
    var key: String
    var data: T? = null
        private set

    constructor(key: String, data: T) {
        this.key = key
        this.data = data
    }

    constructor(key: String) {
        this.key = key
    }

    fun setData(data: T) {
        this.data = data
    }

    companion object {
        const val MESSAGE_NEW = "message_new"
        const val MESSAGE_EDIT = "message_edit"
        const val MESSAGE_DELETE = "message_delete"
        const val MESSAGE_RESTORE = "message_restore"
        const val MESSAGE_READ = "message_read"
        const val MESSAGE_UPDATE = "message_update"
        const val USER_UPDATE = "user_update"
        const val GROUP_UPDATE = "group_update"
        const val CONVERSATION_UPDATE = "conversation_update"
        const val CONVERSATIONS_REFRESH = "conversations_refresh"
        const val UPDATE_INSTALL = "update_install"
    }
}