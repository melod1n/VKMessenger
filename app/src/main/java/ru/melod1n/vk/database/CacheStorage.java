package ru.melod1n.vk.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.VKModel;
import ru.melod1n.vk.util.Util;

import static ru.melod1n.vk.common.AppGlobal.database;
import static ru.melod1n.vk.database.DatabaseHelper.ACTION;
import static ru.melod1n.vk.database.DatabaseHelper.ATTACHMENTS;
import static ru.melod1n.vk.database.DatabaseHelper.CAN_WRITE;
import static ru.melod1n.vk.database.DatabaseHelper.CHAT_SETTINGS;
import static ru.melod1n.vk.database.DatabaseHelper.DATE;
import static ru.melod1n.vk.database.DatabaseHelper.FROM_ID;
import static ru.melod1n.vk.database.DatabaseHelper.FWD_MESSAGES;
import static ru.melod1n.vk.database.DatabaseHelper.IMPORTANT;
import static ru.melod1n.vk.database.DatabaseHelper.IN_READ;
import static ru.melod1n.vk.database.DatabaseHelper.MESSAGE_ID;
import static ru.melod1n.vk.database.DatabaseHelper.OUT_READ;
import static ru.melod1n.vk.database.DatabaseHelper.PEER;
import static ru.melod1n.vk.database.DatabaseHelper.PEER_ID;
import static ru.melod1n.vk.database.DatabaseHelper.PUSH_SETTINGS;
import static ru.melod1n.vk.database.DatabaseHelper.RANDOM_ID;
import static ru.melod1n.vk.database.DatabaseHelper.REPLY_MESSAGE;
import static ru.melod1n.vk.database.DatabaseHelper.TABLE_CONVERSATIONS;
import static ru.melod1n.vk.database.DatabaseHelper.TABLE_MESSAGES;
import static ru.melod1n.vk.database.DatabaseHelper.TEXT;
import static ru.melod1n.vk.database.DatabaseHelper.UNREAD_COUNT;

public class CacheStorage {

    private static Cursor selectCursor(String table, String column, Object value) {
        return QueryBuilder.query()
                .select("*").from(table)
                .where(column.concat(" = ").concat(String.valueOf(value)))
                .asCursor(database);
    }

    private static Cursor selectCursor(String table, String where) {
        return QueryBuilder.query()
                .select("*").from(table).where(where)
                .asCursor(database);
    }

    private static Cursor selectCursor(String table) {
        return QueryBuilder.query()
                .select("*").from(table)
                .asCursor(database);
    }

    private static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    private static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private static byte[] getBlob(Cursor cursor, String columnName) {
        return cursor.getBlob(cursor.getColumnIndex(columnName));
    }

    private static void insert(String table, ArrayList<? extends VKModel> values) {
        database.beginTransaction();

        ContentValues cv = new ContentValues();
        for (int i = 0; i < values.size(); i++) {
            VKModel item = values.get(i);

            switch (table) {
                case TABLE_MESSAGES:
                    putValues((VKMessage) item, cv);
                    break;
                case TABLE_CONVERSATIONS:
                    putValues((VKConversation) item, cv);
                    break;
            }

            database.insert(table, null, cv);
            cv.clear();
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public static void delete(String table, Object where, Object args) {
        database.delete(table, where + " = ?", new String[]{String.valueOf(args)});
    }

    public static void delete(String table) {
        database.delete(table, null, null);
    }

    public static ArrayList<VKMessage> getMessages() {
        Cursor cursor = selectCursor(TABLE_MESSAGES);

        ArrayList<VKMessage> messages = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            messages.add(parseMessage(cursor));
        }

        cursor.close();

        return messages;
    }

    public static VKMessage getMessageByPeerId(int id) {
        Cursor cursor = selectCursor(TABLE_MESSAGES, PEER_ID, id);

        if (cursor.getCount() == 0) return null;

        cursor.moveToFirst();

        VKMessage message = parseMessage(cursor);

        cursor.close();

        return message;
    }

    public static ArrayList<VKConversation> getConversations() {
        return getConversations(0);
    }

    public static ArrayList<VKConversation> getConversations(int size) {
        Cursor cursor = selectCursor(TABLE_CONVERSATIONS);

        ArrayList<VKConversation> conversations = new ArrayList<>();

        while (cursor.moveToNext()) {
            conversations.add(parseConversation(cursor));
            if (conversations.size() == size && size > 0) break;
        }

        cursor.close();

        return conversations;
    }

    private static VKMessage parseMessage(Cursor cursor) {
        VKMessage message = new VKMessage();

        message.setId(getInt(cursor, MESSAGE_ID));
        message.setDate(getInt(cursor, DATE));
        message.setPeerId(getInt(cursor, PEER_ID));
        message.setFromId(getInt(cursor, FROM_ID));
        message.setText(getString(cursor, TEXT));
        message.setRandomId(getInt(cursor, RANDOM_ID));

        Object attachments = Util.deserialize(getBlob(cursor, ATTACHMENTS));
        message.setAttachments((ArrayList<VKModel>) attachments);

        message.setImportant(getInt(cursor, IMPORTANT) == 1);

        Object fwdMessages = Util.deserialize(getBlob(cursor, FWD_MESSAGES));
        message.setFwdMessages((ArrayList<VKMessage>) fwdMessages);

        Object replyMessage = Util.deserialize(getBlob(cursor, REPLY_MESSAGE));
        message.setReplyMessage((VKMessage) replyMessage);

        Object action = Util.deserialize(getBlob(cursor, ACTION));
        message.setAction((VKMessage.Action) action);

        return message;
    }

    private static VKConversation parseConversation(Cursor cursor) {
        VKConversation conversation = new VKConversation();

        Object peer = Util.deserialize(getBlob(cursor, PEER));
        conversation.setPeer((VKConversation.Peer) peer);

        conversation.setInRead(getInt(cursor, IN_READ));
        conversation.setOutRead(getInt(cursor, OUT_READ));
        conversation.setUnreadCount(getInt(cursor, UNREAD_COUNT));

        Object pushSettings = Util.deserialize(getBlob(cursor, PUSH_SETTINGS));
        conversation.setPushSettings((VKConversation.PushSettings) pushSettings);

        Object canWrite = Util.deserialize(getBlob(cursor, CAN_WRITE));
        conversation.setCanWrite((VKConversation.CanWrite) canWrite);

        Object chatSettings = Util.deserialize(getBlob(cursor, CHAT_SETTINGS));
        conversation.setChatSettings((VKConversation.ChatSettings) chatSettings);

        return conversation;
    }

    private static void putValues(VKMessage message, ContentValues values) {
        values.put(MESSAGE_ID, message.getId());
        values.put(DATE, message.getDate());
        values.put(PEER_ID, message.getPeerId());
        values.put(FROM_ID, message.getFromId());
        values.put(TEXT, message.getText());
        values.put(RANDOM_ID, message.getRandomId());

        if (message.getAttachments() != null) {
            values.put(ATTACHMENTS, Util.serialize(message.getAttachments()));
        }

        values.put(IMPORTANT, message.isImportant());

        if (message.getFwdMessages() != null) {
            values.put(FWD_MESSAGES, Util.serialize(message.getFwdMessages()));
        }

        if (message.getReplyMessage() != null) {
            values.put(REPLY_MESSAGE, Util.serialize(message.getReplyMessage()));
        }

        if (message.getAction() != null) {
            values.put(ACTION, Util.serialize(message.getAction()));
        }
    }

    private static void putValues(VKConversation conversation, ContentValues values) {
        if (conversation.getPeer() != null) {
            values.put(PEER, Util.serialize(conversation.getPeer()));
        }

        values.put(IN_READ, conversation.getInRead());
        values.put(OUT_READ, conversation.getOutRead());
        values.put(UNREAD_COUNT, conversation.getUnreadCount());

        if (conversation.getPushSettings() != null) {
            values.put(PUSH_SETTINGS, Util.serialize(conversation.getPushSettings()));
        }

        if (conversation.getCanWrite() != null) {
            values.put(CAN_WRITE, Util.serialize(conversation.getCanWrite()));
        }

        if (conversation.getChatSettings() != null) {
            values.put(CHAT_SETTINGS, Util.serialize(conversation.getChatSettings()));
        }
    }

    public static void insertMessages(ArrayList<VKMessage> messages) {
        insert(TABLE_MESSAGES, messages);
    }

    public static void insertConversations(ArrayList<VKConversation> conversations) {
        insert(TABLE_CONVERSATIONS, conversations);
    }
}
