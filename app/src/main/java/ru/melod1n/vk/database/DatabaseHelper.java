package ru.melod1n.vk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cache.db";
    private static final int DB_VERSION = 5;

    private static final String _ID = "_id";

    static final String MESSAGE_ID = "message_id";
    static final String DATE = "date";
    static final String PEER_ID = "peer_id";
    static final String FROM_ID = "from_id";
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

    static final String TABLE_CONVERSATIONS = "conversations";
    static final String TABLE_MESSAGES = "messages";

    private static final String CREATE_MESSAGES = "create table if not exists " + TABLE_MESSAGES + " (" +
            _ID + " integer, " +
            MESSAGE_ID + " integer primary key on conflict replace, " +
            DATE + " integer, " +
            PEER_ID + " integer, " +
            FROM_ID + " integer, " +
            TEXT + " longtext, " +
            RANDOM_ID + " integer, " +
            ATTACHMENTS + " blob, " +
            IMPORTANT + " integer default 0, " +
            FWD_MESSAGES + " blob, " +
            REPLY_MESSAGE + " blob, " +
            ACTION + " blob" +
            ");";

    private static final String CREATE_CONVERSATIONS = "create table if not exists " + TABLE_CONVERSATIONS + " (" +
            _ID + " integer primary key autoincrement, " +
            CONVERSATION_ID + " integer, " +
            PEER + " blob, " +
            IN_READ + " integer, " +
            OUT_READ + " integer, " +
            UNREAD_COUNT + " integer, " +
            PUSH_SETTINGS + " blob, " +
            CAN_WRITE + " blob, " +
            CHAT_SETTINGS + " blob, " +
            "unique(" + CONVERSATION_ID + ") on conflict replace" +
            ");";

    private static final String DROP_MESSAGES = "drop table if exists " + TABLE_MESSAGES;
    private static final String DROP_CONVERSATIONS = "drop table if exists " + TABLE_CONVERSATIONS;

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
    }

    private void dropTables(SQLiteDatabase database) {
        database.execSQL(DROP_MESSAGES);
        database.execSQL(DROP_CONVERSATIONS);
    }
}
