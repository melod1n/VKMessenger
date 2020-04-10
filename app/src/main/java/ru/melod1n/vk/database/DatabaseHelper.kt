package ru.melod1n.vk.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper private constructor(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        dropTables(db)
        onCreate(db)
    }

    private fun createTables(database: SQLiteDatabase) {
        database.execSQL(CREATE_MESSAGES)
        database.execSQL(CREATE_CONVERSATIONS)
        database.execSQL(CREATE_USERS)
        database.execSQL(CREATE_FRIENDS)
        database.execSQL(CREATE_GROUPS)
    }

    private fun dropTables(database: SQLiteDatabase) {
        database.execSQL(DROP_MESSAGES)
        database.execSQL(DROP_CONVERSATIONS)
        database.execSQL(DROP_USERS)
        database.execSQL(DROP_FRIENDS)
        database.execSQL(DROP_GROUPS)
    }

    fun clear(database: SQLiteDatabase) {
        dropTables(database)
        onCreate(database)
    }

    companion object {

        private const val DB_NAME = "cache.db"
        private const val DB_VERSION = 36

        const val TYPE = "type"

        const val PEER_ID = "peer_id"

        const val PHOTO_50 = "photo_50"
        const val PHOTO_100 = "photo_100"
        const val PHOTO_200 = "photo_200"

        const val SCREEN_NAME = "screen_name"
        const val DEACTIVATED = "deactivated"
        const val MESSAGE_ID = "message_id"
        const val DATE = "date"

        const val FROM_ID = "from_id"
        const val EDIT_TIME = "edit_time"
        const val OUT = "out"
        const val READ = "read"
        const val CONVERSATION_MESSAGE_ID = "conversation_message_id"
        const val TEXT = "text"
        const val RANDOM_ID = "random_id"
        const val ATTACHMENTS = "attachments"
        const val IMPORTANT = "important"
        const val FWD_MESSAGES = "fwd_messages"
        const val REPLY_MESSAGE_ID = "reply_message_id"
        const val ACTION = "_action"

        const val IN_READ = "in_read"
        const val OUT_READ = "out_read"
        const val UNREAD_COUNT = "unread_count"
        const val IS_ALLOWED = "is_allowed"
        const val REASON = "reason"
        const val LOCAL_ID = "local_id"
        const val DISABLED_UNTIL = "disabled_until"
        const val IS_DISABLED_FOREVER = "is_disabled_forever"
        const val IS_NO_SOUND = "is_no_sound"
        const val MEMBERS_COUNT = "members_count"
        const val TITLE = "title"
        const val PINNED_MESSAGE_ID = "pinned_message_id"
        const val STATE = "state"
        const val IS_GROUP_CHANNEL = "is_group_channel"
        const val LAST_MESSAGE_ID = "last_message_id"

        const val FRIEND_ID = "friend_id"
        const val USER_ID = "user_id"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val CLOSED = "closed"
        const val CAN_ACCESS_CLOSED = "can_access_closed"
        const val SEX = "sex"
        const val ONLINE = "online"
        const val ONLINE_MOBILE = "online_mobile"
        const val STATUS = "status"
        const val LAST_SEEN = "last_seen"
        const val PLATFORM = "platform"
        const val VERIFIED = "verified"
        const val GROUP_ID = "group_id"
        const val NAME = "name"
        const val IS_CLOSED = "is_closed"

        const val TABLE_CONVERSATIONS = "conversations"
        const val TABLE_MESSAGES = "messages"
        const val TABLE_USERS = "users"
        const val TABLE_FRIENDS = "friends"
        const val TABLE_GROUPS = "groups"

        private const val CREATE_MESSAGES = "create table if not exists " + TABLE_MESSAGES + " (" +
                DATE + " integer primary key on conflict replace, " +
                MESSAGE_ID + " integer, " +
                PEER_ID + " integer, " +
                FROM_ID + " integer, " +
                EDIT_TIME + " integer, " +
                READ + " integer default 0, " +
                OUT + " integer default 0, " +
                CONVERSATION_MESSAGE_ID + " integer, " +
                TEXT + " longtext, " +
                RANDOM_ID + " integer, " +
                ATTACHMENTS + " blob, " +
                IMPORTANT + " integer default 0, " +
                FWD_MESSAGES + " blob, " +
                REPLY_MESSAGE_ID + " integer, " +
                ACTION + " blob" +
                ");"

        private const val CREATE_CONVERSATIONS = "create table if not exists " + TABLE_CONVERSATIONS + " (" +
                PEER_ID + " integer primary key on conflict replace, " +
                IS_ALLOWED + " integer default 1, " +
                LAST_MESSAGE_ID + " integer, " +
                REASON + " integer default -1, " +
                IN_READ + " integer, " +
                OUT_READ + " integer, " +
                UNREAD_COUNT + " integer, " +
                TYPE + " text, " +
                LOCAL_ID + " integer, " +
                DISABLED_UNTIL + " integer, " +
                IS_DISABLED_FOREVER + " integer default 0, " +
                IS_NO_SOUND + " integer default 0, " +
                MEMBERS_COUNT + " integer, " +
                TITLE + " varchar(255), " +
                PINNED_MESSAGE_ID + " blob, " +
                STATE + " text, " +
                IS_GROUP_CHANNEL + " integer default 0, " +
                PHOTO_50 + " varchar(255), " +
                PHOTO_100 + " varchar(255), " +
                PHOTO_200 + " varchar(255)" +
                ");"

        private const val CREATE_USERS = "create table if not exists " + TABLE_USERS + " (" +
                USER_ID + " integer primary key on conflict replace, " +
                FIRST_NAME + " varchar(255), " +
                LAST_NAME + " varchar(255), " +
                DEACTIVATED + " varchar(255), " +
                CLOSED + " integer default 0, " +
                CAN_ACCESS_CLOSED + " integer default 0, " +
                SEX + " integer, " +
                SCREEN_NAME + " varchar(255), " +
                PHOTO_50 + " varchar(255), " +
                PHOTO_100 + " varchar(255), " +
                PHOTO_200 + " varchar(255), " +
                ONLINE + " integer default 0, " +
                ONLINE_MOBILE + " integer default 0, " +
                STATUS + " longtext, " +
                LAST_SEEN + " integer, " +
                PLATFORM + " integer, " +
                VERIFIED + " integer default 0" +
                ");"

        private const val CREATE_FRIENDS = "create table if not exists " + TABLE_FRIENDS + " (" +
                USER_ID + " integer primary key on conflict replace, " +
                FRIEND_ID + " integer" +
                ");"

        private const val CREATE_GROUPS = "create table if not exists " + TABLE_GROUPS + " (" +
                GROUP_ID + " integer primary key on conflict replace, " +
                NAME + " longtext, " +
                SCREEN_NAME + " varchar(255), " +
                IS_CLOSED + " integer, " +
                DEACTIVATED + " varchar(255), " +
                TYPE + " varchar(255), " +
                PHOTO_50 + " varchar(255), " +
                PHOTO_100 + " varchar(255), " +
                PHOTO_200 + " varchar(255)" +
                ");"

        private const val DROP_MESSAGES = "drop table if exists $TABLE_MESSAGES"
        private const val DROP_CONVERSATIONS = "drop table if exists $TABLE_CONVERSATIONS"
        private const val DROP_USERS = "drop table if exists $TABLE_USERS"
        private const val DROP_FRIENDS = "drop table if exists $TABLE_FRIENDS"
        private const val DROP_GROUPS = "drop table if exists $TABLE_GROUPS"

        fun getInstance(context: Context): DatabaseHelper {
            return DatabaseHelper(context)
        }
    }
}