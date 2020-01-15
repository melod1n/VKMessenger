package ru.melod1n.vk.database

import android.content.ContentValues
import android.database.Cursor
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.model.*
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.database.DatabaseHelper.Companion.ACTION
import ru.melod1n.vk.database.DatabaseHelper.Companion.ACTIVE_IDS
import ru.melod1n.vk.database.DatabaseHelper.Companion.ATTACHMENTS
import ru.melod1n.vk.database.DatabaseHelper.Companion.CAN_ACCESS_CLOSED
import ru.melod1n.vk.database.DatabaseHelper.Companion.CLOSED
import ru.melod1n.vk.database.DatabaseHelper.Companion.CONVERSATION_MESSAGE_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.DATE
import ru.melod1n.vk.database.DatabaseHelper.Companion.DEACTIVATED
import ru.melod1n.vk.database.DatabaseHelper.Companion.DISABLED_UNTIL
import ru.melod1n.vk.database.DatabaseHelper.Companion.EDIT_TIME
import ru.melod1n.vk.database.DatabaseHelper.Companion.FIRST_NAME
import ru.melod1n.vk.database.DatabaseHelper.Companion.FRIEND_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.FROM_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.FWD_MESSAGES
import ru.melod1n.vk.database.DatabaseHelper.Companion.GROUP_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.IMPORTANT
import ru.melod1n.vk.database.DatabaseHelper.Companion.IN_READ
import ru.melod1n.vk.database.DatabaseHelper.Companion.IS_ALLOWED
import ru.melod1n.vk.database.DatabaseHelper.Companion.IS_CLOSED
import ru.melod1n.vk.database.DatabaseHelper.Companion.IS_DISABLED_FOREVER
import ru.melod1n.vk.database.DatabaseHelper.Companion.IS_GROUP_CHANNEL
import ru.melod1n.vk.database.DatabaseHelper.Companion.IS_NO_SOUND
import ru.melod1n.vk.database.DatabaseHelper.Companion.LAST_MESSAGE_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.LAST_NAME
import ru.melod1n.vk.database.DatabaseHelper.Companion.LAST_SEEN
import ru.melod1n.vk.database.DatabaseHelper.Companion.LOCAL_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.MEMBERS_COUNT
import ru.melod1n.vk.database.DatabaseHelper.Companion.MESSAGE_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.NAME
import ru.melod1n.vk.database.DatabaseHelper.Companion.ONLINE
import ru.melod1n.vk.database.DatabaseHelper.Companion.ONLINE_MOBILE
import ru.melod1n.vk.database.DatabaseHelper.Companion.OUT
import ru.melod1n.vk.database.DatabaseHelper.Companion.OUT_READ
import ru.melod1n.vk.database.DatabaseHelper.Companion.PEER_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.PHOTO_100
import ru.melod1n.vk.database.DatabaseHelper.Companion.PHOTO_200
import ru.melod1n.vk.database.DatabaseHelper.Companion.PHOTO_50
import ru.melod1n.vk.database.DatabaseHelper.Companion.PINNED_MESSAGE
import ru.melod1n.vk.database.DatabaseHelper.Companion.RANDOM_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.READ
import ru.melod1n.vk.database.DatabaseHelper.Companion.REASON
import ru.melod1n.vk.database.DatabaseHelper.Companion.REPLY_MESSAGE
import ru.melod1n.vk.database.DatabaseHelper.Companion.SCREEN_NAME
import ru.melod1n.vk.database.DatabaseHelper.Companion.SEX
import ru.melod1n.vk.database.DatabaseHelper.Companion.STATE
import ru.melod1n.vk.database.DatabaseHelper.Companion.STATUS
import ru.melod1n.vk.database.DatabaseHelper.Companion.TABLE_CONVERSATIONS
import ru.melod1n.vk.database.DatabaseHelper.Companion.TABLE_FRIENDS
import ru.melod1n.vk.database.DatabaseHelper.Companion.TABLE_GROUPS
import ru.melod1n.vk.database.DatabaseHelper.Companion.TABLE_MESSAGES
import ru.melod1n.vk.database.DatabaseHelper.Companion.TABLE_USERS
import ru.melod1n.vk.database.DatabaseHelper.Companion.TEXT
import ru.melod1n.vk.database.DatabaseHelper.Companion.TITLE
import ru.melod1n.vk.database.DatabaseHelper.Companion.TYPE
import ru.melod1n.vk.database.DatabaseHelper.Companion.UNREAD_COUNT
import ru.melod1n.vk.database.DatabaseHelper.Companion.USER_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.VERIFIED
import ru.melod1n.vk.util.Util
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

@Suppress("UNCHECKED_CAST")
object CacheStorage {
    private fun selectCursor(table: String, column: String, value: Any): Cursor {
        return QueryBuilder.query()
                .select("*").from(table)
                .where("$column = $value")
                .asCursor(AppGlobal.database)
    }

    private fun selectCursor(table: String, where: String): Cursor {
        return QueryBuilder.query()
                .select("*").from(table).where(where)
                .asCursor(AppGlobal.database)
    }

    private fun selectCursor(table: String): Cursor {
        return QueryBuilder.query()
                .select("*").from(table)
                .asCursor(AppGlobal.database)
    }

    private fun getInt(cursor: Cursor, columnName: String): Int {
        return cursor.getInt(cursor.getColumnIndex(columnName))
    }

    private fun getString(cursor: Cursor, columnName: String): String {
        return cursor.getString(cursor.getColumnIndex(columnName))
    }

    private fun getBlob(cursor: Cursor, columnName: String): ByteArray? {
        return cursor.getBlob(cursor.getColumnIndex(columnName))
    }

    private fun insert(table: String, values: ArrayList<*>) {
        AppGlobal.database.beginTransaction()

        val cv = ContentValues()

        for (i in values.indices) {

            val item = values[i] as VKModel
            when (table) {
                TABLE_MESSAGES -> putValues(item as VKMessage, cv)
                TABLE_CONVERSATIONS -> putValues(item as VKConversation, cv)
                TABLE_USERS -> putValues(item as VKUser, cv, false)
                TABLE_FRIENDS -> putValues(item as VKUser, cv, true)
                TABLE_GROUPS -> putValues(item as VKGroup, cv)
            }

            AppGlobal.database.insert(table, null, cv)
            cv.clear()
        }

        AppGlobal.database.apply {
            setTransactionSuccessful()
            endTransaction()
        }
    }

    fun delete(table: String?, where: Any, args: Any) {
        AppGlobal.database.delete(table, "$where = ?", arrayOf(args.toString()))
    }

    fun delete(table: String?) {
        AppGlobal.database.delete(table, null, null)
    }

    fun getMessage(id: Int): VKMessage? {
        val cursor = selectCursor(TABLE_MESSAGES, MESSAGE_ID, id)
        if (cursor.count == 0) return null
        cursor.moveToFirst()
        val message = parseMessage(cursor)
        cursor.close()
        return message
    }

    val messages: ArrayList<VKMessage>
        get() {
            val cursor = selectCursor(TABLE_MESSAGES)
            val messages = ArrayList<VKMessage>(cursor.count)
            while (cursor.moveToNext()) {
                messages.add(parseMessage(cursor))
            }
            cursor.close()
            return messages
        }

    fun getMessageByPeerId(id: Int): VKMessage? {
        val cursor = selectCursor(TABLE_MESSAGES, PEER_ID, id)
        if (cursor.count == 0) return null

        cursor.moveToFirst()

        val message = parseMessage(cursor)

        cursor.close()
        return message
    }

    fun getMessages(peerId: Int): ArrayList<VKMessage> {
        val cursor = selectCursor(TABLE_MESSAGES, PEER_ID, peerId)
        val messages = ArrayList<VKMessage>(cursor.count)
        while (cursor.moveToNext()) {
            messages.add(parseMessage(cursor))
        }
        cursor.close()
        messages.sortWith(Comparator { o1: VKMessage, o2: VKMessage ->
            val x = o1.date
            val y = o2.date
            y - x
        })
        return messages
    }

    fun getConversation(peerId: Int): VKConversation? {
        val cursor = selectCursor(TABLE_CONVERSATIONS, DatabaseHelper.CONVERSATION_ID, peerId)
        if (cursor.count == 0) return null
        cursor.moveToFirst()
        val conversation = parseConversation(cursor)
        cursor.close()
        return conversation
    }

    val conversations: ArrayList<VKConversation>
        get() = getConversations(0)

    fun getConversations(size: Int): ArrayList<VKConversation> {
        val cursor = selectCursor(TABLE_CONVERSATIONS)
        val conversations = ArrayList<VKConversation>()

        while (cursor.moveToNext()) {
            conversations.add(parseConversation(cursor))
            if (conversations.size == size && size > 0) break
        }

        cursor.close()
        return conversations
    }

    fun getUser(id: Int): VKUser? {
        val cursor = selectCursor(TABLE_USERS, USER_ID, id)
        if (cursor.count == 0) return null

        cursor.moveToFirst()

        val user = parseUser(cursor)

        cursor.close()
        return user
    }

    fun getGroup(gid: Int): VKGroup? {
        val id = abs(gid)
        val cursor = selectCursor(TABLE_GROUPS, GROUP_ID, id)
        if (cursor.count == 0) return null

        cursor.moveToFirst()

        val group = parseGroup(cursor)

        cursor.close()
        return group
    }

    private fun parseMessage(cursor: Cursor): VKMessage {
        return VKMessage().apply {
            id = getInt(cursor, MESSAGE_ID)
            date = getInt(cursor, DATE)
            peerId = getInt(cursor, PEER_ID)
            fromId = getInt(cursor, FROM_ID)
            editTime = getInt(cursor, EDIT_TIME)
            isOut = getInt(cursor, OUT) == 1
            conversationMessageId = getInt(cursor, CONVERSATION_MESSAGE_ID)
            text = getString(cursor, TEXT)
            randomId = getInt(cursor, RANDOM_ID)
            isRead = getInt(cursor, READ) == 1
            isImportant = getInt(cursor, IMPORTANT) == 1

            val attachments = Util.deserialize(getBlob(cursor, ATTACHMENTS))
            val fwdMessages = Util.deserialize(getBlob(cursor, FWD_MESSAGES))
            val replyMessage = Util.deserialize(getBlob(cursor, REPLY_MESSAGE))
            val action = Util.deserialize(getBlob(cursor, ACTION))

            this.fwdMessages = fwdMessages as ArrayList<VKMessage>?
            this.replyMessage = replyMessage as VKMessage?
            this.action = action as VKMessage.Action?
            this.attachments = attachments as ArrayList<VKModel>?
        }
    }

    private fun parseConversation(cursor: Cursor): VKConversation {
        return VKConversation().apply {
            inRead = getInt(cursor, IN_READ)
            outRead = getInt(cursor, OUT_READ)
            unreadCount = getInt(cursor, UNREAD_COUNT)
            id = getInt(cursor, PEER_ID)
            isAllowed = getInt(cursor, IS_ALLOWED) == 1
            reason = getInt(cursor, REASON)
            lastMessageId = getInt(cursor, LAST_MESSAGE_ID)
            type = getString(cursor, TYPE)
            localId = getInt(cursor, LOCAL_ID)
            disabledUntil = getInt(cursor, DISABLED_UNTIL)
            isDisabledForever = getInt(cursor, IS_DISABLED_FOREVER) == 1
            isNoSound = getInt(cursor, IS_NO_SOUND) == 1
            membersCount = getInt(cursor, MEMBERS_COUNT)
            title = getString(cursor, TITLE)
            state = getString(cursor, STATE)
            isGroupChannel = getInt(cursor, IS_GROUP_CHANNEL) == 1
            photo50 = getString(cursor, PHOTO_50)
            photo100 = getString(cursor, PHOTO_100)
            photo200 = getString(cursor, PHOTO_200)

            val activeIds = Util.deserialize(getBlob(cursor, ACTIVE_IDS))
            val pinnedMessage = Util.deserialize(getBlob(cursor, PINNED_MESSAGE))

            this.pinnedMessage = pinnedMessage as VKPinnedMessage?
            this.activeIds = activeIds as Array<Int>?
        }
    }

    private fun parseUser(cursor: Cursor): VKUser {
        return VKUser().apply {
            id = getInt(cursor, USER_ID)
            firstName = getString(cursor, FIRST_NAME)
            lastName = getString(cursor, LAST_NAME)
            deactivated = getString(cursor, DEACTIVATED)
            isClosed = getInt(cursor, CLOSED) == 1
            isCanAccessClosed = getInt(cursor, CAN_ACCESS_CLOSED) == 1
            sex = getInt(cursor, SEX)
            screenName = getString(cursor, SCREEN_NAME)
            photo50 = getString(cursor, PHOTO_50)
            photo100 = getString(cursor, PHOTO_100)
            photo200 = getString(cursor, PHOTO_200)
            isOnline = getInt(cursor, ONLINE) == 1
            isOnlineMobile = getInt(cursor, ONLINE_MOBILE) == 1
            status = getString(cursor, STATUS)
            isVerified = getInt(cursor, VERIFIED) == 1

            val lastSeen = Util.deserialize(getBlob(cursor, LAST_SEEN))
            this.lastSeen = lastSeen as VKUser.LastSeen?
        }
    }

    private fun parseGroup(cursor: Cursor): VKGroup {
        return VKGroup().apply {
            id = abs(getInt(cursor, GROUP_ID))
            name = getString(cursor, NAME)
            screenName = getString(cursor, SCREEN_NAME)
            isClosed = getInt(cursor, IS_CLOSED)
            deactivated = getString(cursor, DEACTIVATED)
            photo50 = getString(cursor, PHOTO_50)
            photo100 = getString(cursor, PHOTO_100)
            photo200 = getString(cursor, PHOTO_200)
        }
    }

    private fun putValues(message: VKMessage, values: ContentValues) {
        values.apply {
            put(MESSAGE_ID, message.id)
            put(DATE, message.date)
            put(PEER_ID, message.peerId)
            put(FROM_ID, message.fromId)
            put(EDIT_TIME, message.editTime)
            put(OUT, if (message.isOut) 1 else 0)
            put(CONVERSATION_MESSAGE_ID, message.conversationMessageId)
            put(TEXT, message.text)
            put(RANDOM_ID, message.randomId)
            put(READ, message.isRead)
            put(IMPORTANT, message.isImportant)
        }

        if (message.attachments != null) {
            values.put(ATTACHMENTS, Util.serialize(message.attachments))
        }

        if (message.fwdMessages != null) {
            values.put(FWD_MESSAGES, Util.serialize(message.fwdMessages))
        }

        if (message.replyMessage != null) {
            values.put(REPLY_MESSAGE, Util.serialize(message.replyMessage))
        }

        if (message.action != null) {
            values.put(ACTION, Util.serialize(message.action))
        }
    }

    private fun putValues(conversation: VKConversation, values: ContentValues) {
        values.apply {
            put(PEER_ID, conversation.id)
            put(IN_READ, conversation.inRead)
            put(OUT_READ, conversation.outRead)
            put(UNREAD_COUNT, conversation.unreadCount)
            put(TYPE, conversation.type)
            put(LOCAL_ID, conversation.localId)
            put(DISABLED_UNTIL, conversation.disabledUntil)
            put(IS_DISABLED_FOREVER, conversation.isDisabledForever)
            put(IS_NO_SOUND, conversation.isNoSound)
            put(MEMBERS_COUNT, conversation.membersCount)
            put(TITLE, conversation.title)
            put(PINNED_MESSAGE, Util.serialize(conversation.pinnedMessage ?: VKPinnedMessage()))
            put(STATE, conversation.state)
            put(ACTIVE_IDS, Util.serialize(conversation.activeIds ?: arrayOf<Int>()))
            put(IS_GROUP_CHANNEL, conversation.isGroupChannel)
            put(PHOTO_50, conversation.photo50)
            put(PHOTO_100, conversation.photo100)
            put(PHOTO_200, conversation.photo200)
        }
    }

    private fun putValues(user: VKUser, values: ContentValues, isFriend: Boolean) {
        if (isFriend) {
            values.put(USER_ID, UserConfig.getUserId())
            values.put(FRIEND_ID, user.id)
            return
        }
        values.apply {
            put(USER_ID, user.id)
            put(FIRST_NAME, user.firstName)
            put(LAST_NAME, user.lastName)
            put(DEACTIVATED, user.deactivated)
            put(CLOSED, user.isClosed)
            put(CAN_ACCESS_CLOSED, user.isCanAccessClosed)
            put(SEX, user.sex)
            put(SCREEN_NAME, user.screenName)
            put(PHOTO_50, user.photo50)
            put(PHOTO_100, user.photo100)
            put(PHOTO_200, user.photo200)
            put(ONLINE, user.isOnline)
            put(ONLINE_MOBILE, user.isOnlineMobile)
            put(STATUS, user.status)
        }

        if (user.lastSeen != null) {
            values.put(LAST_SEEN, Util.serialize(user.lastSeen ?: VKUser.LastSeen()))
        }
        values.put(VERIFIED, user.isVerified)
    }

    private fun putValues(group: VKGroup, values: ContentValues) {
        values.apply {
            put(GROUP_ID, abs(group.id))
            put(NAME, group.name)
            put(SCREEN_NAME, group.screenName)
            put(IS_CLOSED, group.isClosed)
            put(DEACTIVATED, group.deactivated)
            put(TYPE, group.type)
            put(PHOTO_50, group.photo50)
            put(PHOTO_100, group.photo100)
            put(PHOTO_200, group.photo200)
        }
    }

    fun insertMessages(messages: ArrayList<VKMessage>) {
        insert(TABLE_MESSAGES, messages)
    }

    fun insertMessage(message: VKMessage?) {
        insert(TABLE_MESSAGES, ArrayList<VKMessage>(listOf(message)))
    }

    fun insertConversation(conversation: VKConversation) {
        insert(TABLE_CONVERSATIONS, ArrayList<VKConversation>(listOf(conversation)))
    }

    fun insertConversations(conversations: ArrayList<VKConversation>) {
        insert(TABLE_CONVERSATIONS, conversations)
    }

    fun insertUser(user: VKUser?) {
        insert(TABLE_USERS, ArrayList<VKUser>(listOf(user)))
    }

    fun insertUsers(users: ArrayList<VKUser>) {
        insert(TABLE_USERS, users)
    }

    fun insertGroup(group: VKGroup?) {
        insert(TABLE_GROUPS, ArrayList<VKGroup>(listOf(group)))
    }

    fun insertGroups(groups: ArrayList<VKGroup>) {
        insert(TABLE_GROUPS, groups)
    }
}