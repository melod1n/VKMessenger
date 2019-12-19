package ru.melod1n.vk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cache.db";
    private static final int DB_VERSION = 13;

    static final String SCREEN_NAME = "screen_name";
    static final String DEACTIVATED = "deactivated";
    static final String PHOTO_50 = "photo_50";
    static final String PHOTO_100 = "photo_100";
    static final String PHOTO_200 = "photo_200";

    public static final String MESSAGE_ID = "message_id";
    static final String DATE = "date";
    public static final String PEER_ID = "peer_id";
    static final String FROM_ID = "from_id";
    static final String EDIT_TIME = "edit_time";
    static final String OUT = "out";
    static final String READ = "read";
    static final String CONVERSATION_MESSAGE_ID = "conversation_message_id";
    static final String TEXT = "text";
    static final String RANDOM_ID = "random_id";
    static final String ATTACHMENTS = "attachments";
    static final String IMPORTANT = "important";
    static final String FWD_MESSAGES = "fwd_messages";
    static final String REPLY_MESSAGE = "reply_message";
    static final String ACTION = "_action";

    static final String CONVERSATION_ID = "conversation_id";
    static final String PEER = "peer";
    static final String IN_READ = "in_read";
    static final String OUT_READ = "out_read";
    static final String UNREAD_COUNT = "unread_count";
    static final String PUSH_SETTINGS = "push_settings";
    static final String CAN_WRITE = "can_write";
    static final String CHAT_SETTINGS = "chat_settings";

    static final String FRIEND_ID = "friend_id";
    static final String USER_ID = "user_id";
    static final String FIRST_NAME = "first_name";
    static final String LAST_NAME = "last_name";
    static final String CLOSED = "closed";
    static final String CAN_ACCESS_CLOSED = "can_access_closed";
    static final String SEX = "sex";
    static final String ONLINE = "online";
    static final String ONLINE_MOBILE = "online_mobile";
    static final String STATUS = "status";
    static final String LAST_SEEN = "last_seen";
    static final String VERIFIED = "verified";

    static final String GROUP_ID = "group_id";
    static final String NAME = "name";
    static final String IS_CLOSED = "is_closed";
    static final String TYPE = "type";

    public static final String TABLE_CONVERSATIONS = "conversations";
    public static final String TABLE_MESSAGES = "messages";
    static final String TABLE_USERS = "users";
    static final String TABLE_FRIENDS = "friends";
    static final String TABLE_GROUPS = "groups";

    private static final String CREATE_MESSAGES = "create table if not exists " + TABLE_MESSAGES + " (" +
            MESSAGE_ID + " integer primary key on conflict replace, " +
            DATE + " integer, " +
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
            REPLY_MESSAGE + " blob, " +
            ACTION + " blob" +
            ");";

    private static final String CREATE_CONVERSATIONS = "create table if not exists " + TABLE_CONVERSATIONS + " (" +
            CONVERSATION_ID + " integer primary key on conflict replace, " +
            PEER_ID + " integer, " +
            PEER + " blob, " +
            IN_READ + " integer, " +
            OUT_READ + " integer, " +
            UNREAD_COUNT + " integer, " +
            PUSH_SETTINGS + " blob, " +
            CAN_WRITE + " blob, " +
            CHAT_SETTINGS + " blob" +
            ");";

    private static final String CREATE_USERS = "create table if not exists " + TABLE_USERS + " (" +
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
            LAST_SEEN + " blob, " +
            VERIFIED + " integer default 0" +
            ");";

    private static final String CREATE_FRIENDS = "create table if not exists " + TABLE_FRIENDS + " (" +
            USER_ID + " integer primary key on conflict replace, " +
            FRIEND_ID + " integer" +
            ");";

    private static final String CREATE_GROUPS = "create table if not exists " + TABLE_GROUPS + " (" +
            GROUP_ID + " integer primary key on conflict replace, " +
            NAME + " longtext, " +
            SCREEN_NAME + " varchar(255), " +
            IS_CLOSED + " integer, " +
            DEACTIVATED + " varchar(255), " +
            TYPE + " varchar(255), " +
            PHOTO_50 + " varchar(255), " +
            PHOTO_100 + " varchar(255), " +
            PHOTO_200 + " varchar(255)" +
            ");";

    private static final String DROP_MESSAGES = "drop table if exists " + TABLE_MESSAGES;
    private static final String DROP_CONVERSATIONS = "drop table if exists " + TABLE_CONVERSATIONS;
    private static final String DROP_USERS = "drop table if exists " + TABLE_USERS;
    private static final String DROP_FRIENDS = "drop table if exists " + TABLE_FRIENDS;
    private static final String DROP_GROUPS = "drop table if exists " + TABLE_GROUPS;

    private DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DatabaseHelper getInstance(@Nullable Context context) {
        return new DatabaseHelper(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }

    private void createTables(SQLiteDatabase database) {
        database.execSQL(CREATE_MESSAGES);
        database.execSQL(CREATE_CONVERSATIONS);
        database.execSQL(CREATE_USERS);
        database.execSQL(CREATE_FRIENDS);
        database.execSQL(CREATE_GROUPS);
    }

    private void dropTables(SQLiteDatabase database) {
        database.execSQL(DROP_MESSAGES);
        database.execSQL(DROP_CONVERSATIONS);
        database.execSQL(DROP_USERS);
        database.execSQL(DROP_FRIENDS);
        database.execSQL(DROP_GROUPS);
    }

    public void clear(SQLiteDatabase database) {
        dropTables(database);
        onCreate(database);
    }
}
