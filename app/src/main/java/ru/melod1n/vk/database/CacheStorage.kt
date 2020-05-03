package ru.melod1n.vk.database

import android.content.ContentValues
import android.database.Cursor
import android.text.TextUtils
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.model.*
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.database.DatabaseHelper.Companion.ACTION
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
import ru.melod1n.vk.database.DatabaseHelper.Companion.PINNED_MESSAGE_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.PLATFORM
import ru.melod1n.vk.database.DatabaseHelper.Companion.RANDOM_ID
import ru.melod1n.vk.database.DatabaseHelper.Companion.READ
import ru.melod1n.vk.database.DatabaseHelper.Companion.REASON
import ru.melod1n.vk.database.DatabaseHelper.Companion.REPLY_MESSAGE_ID
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

    private fun getString(cursor: Cursor, columnName: String): String? {
        return cursor.getString(cursor.getColumnIndex(columnName))
    }

    private fun getBlob(cursor: Cursor, columnName: String): ByteArray? {
        return cursor.getBlob(cursor.getColumnIndex(columnName))
    }

    private fun insert(table: String, values: ArrayList<*>) {
        AppGlobal.database.beginTransaction()

        for (i in values.indices) {
            val cv = ContentValues()

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

    private fun update(table: String, values: ArrayList<*>, whereClause: String, whereArgs: Array<String>) {
        AppGlobal.database.beginTransaction()

        for (i in values.indices) {
            val cv = ContentValues()

            val item = values[i] as VKModel

            when (table) {
                TABLE_MESSAGES -> putValues(item as VKMessage, cv)
                TABLE_CONVERSATIONS -> putValues(item as VKConversation, cv)
                TABLE_USERS -> putValues(item as VKUser, cv, false)
                TABLE_FRIENDS -> putValues(item as VKUser, cv, true)
                TABLE_GROUPS -> putValues(item as VKGroup, cv)
            }

            AppGlobal.database.update(table, cv, "$whereClause = ?", whereArgs)
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

    fun getMessage(messageId: Int): VKMessage? {
        val cursor = selectCursor(TABLE_MESSAGES, MESSAGE_ID, messageId)

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
        return messages
    }

    fun getConversation(peerId: Int): VKConversation? {
        val cursor = selectCursor(TABLE_CONVERSATIONS, PEER_ID, peerId)

        if (cursor.count == 0) return null

        cursor.moveToFirst()

        val conversation = parseConversation(cursor)

        cursor.close()

        return conversation
    }

    fun getConversations(): ArrayList<VKConversation> {
        return getConversations(0)
    }

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

    fun getFriends(userId: Int, onlyOnline: Boolean): ArrayList<VKUser> {
        val cursor = QueryBuilder.query()
                .select("*")
                .from(TABLE_FRIENDS)
                .leftJoin(TABLE_USERS)
                .on("friends.friend_id = users.user_id")
                .where("friends.user_id = $userId")
                .asCursor(AppGlobal.database)

        val users: ArrayList<VKUser> = ArrayList(cursor.count)

        while (cursor.moveToNext()) {
            val userOnline = getInt(cursor, ONLINE) == 1

            if (onlyOnline && !userOnline) {
                continue
            }

            val user = parseUser(cursor)
            users.add(user)
        }

        cursor.close()
        return users
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
            replyMessageId = getInt(cursor, REPLY_MESSAGE_ID)

            val attachments = Util.deserialize(getBlob(cursor, ATTACHMENTS))
            val fwdMessages = Util.deserialize(getBlob(cursor, FWD_MESSAGES))
            val action = Util.deserialize(getBlob(cursor, ACTION))

            this.fwdMessages = fwdMessages as ArrayList<VKMessage>?
            this.action = action as VKMessage.Action?
            this.attachments = attachments as ArrayList<VKModel>
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

            pinnedMessageId = getInt(cursor, PINNED_MESSAGE_ID)
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
            lastSeen = getInt(cursor, LAST_SEEN)
            lastSeenPlatform = getInt(cursor, PLATFORM)
        }
    }

    private fun parseGroup(cursor: Cursor): VKGroup {
        return VKGroup().apply {
            id = abs(getInt(cursor, GROUP_ID))
            name = getString(cursor, NAME)
            screenName = getString(cursor, SCREEN_NAME)
            isClosed = getInt(cursor, IS_CLOSED) == 1
            deactivated = getString(cursor, DEACTIVATED)
            photo50 = getString(cursor, PHOTO_50)
            photo100 = getString(cursor, PHOTO_100)
            photo200 = getString(cursor, PHOTO_200)
        }
    }

    private fun putValues(message: VKMessage, values: ContentValues) {
        values.put(MESSAGE_ID, message.id)
        values.put(DATE, message.date)
        values.put(PEER_ID, message.peerId)
        values.put(FROM_ID, message.fromId)
        values.put(EDIT_TIME, message.editTime)
        values.put(OUT, if (message.isOut) 1 else 0)
        values.put(CONVERSATION_MESSAGE_ID, message.conversationMessageId)
        values.put(TEXT, message.text)
        values.put(RANDOM_ID, message.randomId)
        values.put(READ, message.isRead)
        values.put(IMPORTANT, message.isImportant)
        values.put(ATTACHMENTS, Util.serialize(message.attachments))
        values.put(FWD_MESSAGES, Util.serialize(message.fwdMessages))
        values.put(REPLY_MESSAGE_ID, message.replyMessageId)
        values.put(ACTION, Util.serialize(message.action))
    }

    private fun putValues(conversation: VKConversation, values: ContentValues) {
        values.put(PEER_ID, conversation.id)
        values.put(IN_READ, conversation.inRead)
        values.put(OUT_READ, conversation.outRead)
        values.put(UNREAD_COUNT, conversation.unreadCount)
        values.put(TYPE, conversation.type)
        values.put(IS_ALLOWED, conversation.isAllowed)
        values.put(LOCAL_ID, conversation.localId)
        values.put(DISABLED_UNTIL, conversation.disabledUntil)
        values.put(IS_DISABLED_FOREVER, conversation.isDisabledForever)
        values.put(IS_NO_SOUND, conversation.isNoSound)
        values.put(MEMBERS_COUNT, conversation.membersCount)
        values.put(TITLE, conversation.title ?: "")
        values.put(PINNED_MESSAGE_ID, conversation.pinnedMessageId)
        values.put(STATE, conversation.state)
        values.put(IS_GROUP_CHANNEL, conversation.isGroupChannel)
        values.put(PHOTO_50, conversation.photo50)
        values.put(PHOTO_100, conversation.photo100)
        values.put(PHOTO_200, conversation.photo200)
        values.put(LAST_MESSAGE_ID, conversation.lastMessageId)
    }

    private fun putValues(user: VKUser, values: ContentValues, isFriend: Boolean) {
        if (isFriend) {
            values.put(USER_ID, UserConfig.userId)
            values.put(FRIEND_ID, user.id)
            return
        }
        values.put(USER_ID, user.id)
        values.put(FIRST_NAME, user.firstName)
        values.put(LAST_NAME, user.lastName)
        values.put(DEACTIVATED, user.deactivated)
        values.put(CLOSED, user.isClosed)
        values.put(CAN_ACCESS_CLOSED, user.isCanAccessClosed)
        values.put(SEX, user.sex)
        values.put(SCREEN_NAME, user.screenName)
        values.put(PHOTO_50, user.photo50)
        values.put(PHOTO_100, user.photo100)
        values.put(PHOTO_200, user.photo200)
        values.put(ONLINE, user.isOnline)
        values.put(ONLINE_MOBILE, user.isOnlineMobile)
        values.put(STATUS, user.status)
        values.put(LAST_SEEN, user.lastSeen)
        values.put(PLATFORM, user.lastSeenPlatform)
        values.put(VERIFIED, user.isVerified)
    }

    private fun putValues(group: VKGroup, values: ContentValues) {
        values.put(GROUP_ID, abs(group.id))
        values.put(NAME, group.name)
        values.put(SCREEN_NAME, group.screenName)
        values.put(IS_CLOSED, group.isClosed)
        values.put(DEACTIVATED, group.deactivated)
        values.put(TYPE, group.type)
        values.put(PHOTO_50, group.photo50)
        values.put(PHOTO_100, group.photo100)
        values.put(PHOTO_200, group.photo200)
    }

    fun insertConversation(conversation: VKConversation) {
        insertConversations(ArrayList(listOf(conversation)))
    }

    fun insertConversations(conversations: ArrayList<VKConversation>) {
        insert(TABLE_CONVERSATIONS, conversations)
    }

    fun updateConversations(conversations: ArrayList<VKConversation>) {
        val ids = Array(conversations.size) {it.toString()}

        for (i in conversations.indices) {
            ids[i] = conversations[i].id.toString()
        }

        update(TABLE_CONVERSATIONS, conversations, PEER_ID, ids)
    }

    fun deleteConversations(conversations: ArrayList<VKConversation>) {
        val ids = IntArray(conversations.size)

        for (i in conversations.indices) {
            ids[i] = conversations[i].id
        }

        deleteConversations(ids)
    }

    fun deleteConversations(conversationsIds: IntArray) {
        val args = TextUtils.join(", ", conversationsIds.toMutableList())

        delete(TABLE_CONVERSATIONS, PEER_ID, args)
    }

    fun deleteConversation(conversation: VKConversation) {
        deleteConversations(ArrayList(listOf(conversation)))
    }

    fun deleteConversation(conversationId: Int) {
        deleteConversations(intArrayOf(conversationId))
    }

    fun insertUser(user: VKUser) {
        insertUsers(ArrayList(listOf(user)))
    }

    fun insertUsers(users: ArrayList<VKUser>) {
        insert(TABLE_USERS, users)
    }

    fun insertFriends(users: ArrayList<VKUser>) {
        insertUsers(users)
        insert(TABLE_FRIENDS, users)
    }

    fun insertGroup(group: VKGroup) {
        insertGroups(ArrayList(listOf(group)))
    }

    fun insertGroups(groups: ArrayList<VKGroup>) {
        insert(TABLE_GROUPS, groups)
    }

    fun updateMessages(messages: ArrayList<VKMessage>) {
        val ids = Array(messages.size) {it.toString()}

        for (i in messages.indices) {
            ids[i] = messages[i].id.toString()
        }

        update(TABLE_MESSAGES, messages, MESSAGE_ID, ids)
    }

    fun updateMessage(message: VKMessage) {
        updateMessages(ArrayList(listOf(message)))
    }

    fun insertMessages(messages: ArrayList<VKMessage>) {
        insert(TABLE_MESSAGES, messages)
    }

    fun insertMessage(message: VKMessage) {
        insertMessages(ArrayList(listOf(message)))
    }

    fun deleteMessages(messages: ArrayList<VKMessage>) {
        val ids = IntArray(messages.size)

        for (i in messages.indices) {
            ids[i] = messages[i].id
        }

        deleteMessages(ids)
    }

    fun deleteMessages(messagesIds: IntArray) {
        val args = TextUtils.join(", ", messagesIds.toMutableList())

        delete(TABLE_MESSAGES, MESSAGE_ID, args)
    }

    fun deleteMessage(message: VKMessage) {
        deleteMessages(ArrayList(listOf(message)))
    }

    fun deleteMessage(messageId: Int) {
        deleteMessages(intArrayOf(messageId))
    }
}