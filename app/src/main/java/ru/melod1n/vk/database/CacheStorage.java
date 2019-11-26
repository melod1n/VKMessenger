package ru.melod1n.vk.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

import ru.melod1n.vk.api.UserConfig;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKGroup;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.VKModel;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.util.Util;

import static ru.melod1n.vk.common.AppGlobal.database;
import static ru.melod1n.vk.database.DatabaseHelper.ACTION;
import static ru.melod1n.vk.database.DatabaseHelper.ATTACHMENTS;
import static ru.melod1n.vk.database.DatabaseHelper.CAN_ACCESS_CLOSED;
import static ru.melod1n.vk.database.DatabaseHelper.CAN_WRITE;
import static ru.melod1n.vk.database.DatabaseHelper.CHAT_SETTINGS;
import static ru.melod1n.vk.database.DatabaseHelper.CLOSED;
import static ru.melod1n.vk.database.DatabaseHelper.CONVERSATION_ID;
import static ru.melod1n.vk.database.DatabaseHelper.DATE;
import static ru.melod1n.vk.database.DatabaseHelper.DEACTIVATED;
import static ru.melod1n.vk.database.DatabaseHelper.FIRST_NAME;
import static ru.melod1n.vk.database.DatabaseHelper.FRIEND_ID;
import static ru.melod1n.vk.database.DatabaseHelper.FROM_ID;
import static ru.melod1n.vk.database.DatabaseHelper.FWD_MESSAGES;
import static ru.melod1n.vk.database.DatabaseHelper.GROUP_ID;
import static ru.melod1n.vk.database.DatabaseHelper.IMPORTANT;
import static ru.melod1n.vk.database.DatabaseHelper.IN_READ;
import static ru.melod1n.vk.database.DatabaseHelper.IS_CLOSED;
import static ru.melod1n.vk.database.DatabaseHelper.LAST_NAME;
import static ru.melod1n.vk.database.DatabaseHelper.LAST_SEEN;
import static ru.melod1n.vk.database.DatabaseHelper.MESSAGE_ID;
import static ru.melod1n.vk.database.DatabaseHelper.NAME;
import static ru.melod1n.vk.database.DatabaseHelper.ONLINE;
import static ru.melod1n.vk.database.DatabaseHelper.ONLINE_MOBILE;
import static ru.melod1n.vk.database.DatabaseHelper.OUT_READ;
import static ru.melod1n.vk.database.DatabaseHelper.PEER;
import static ru.melod1n.vk.database.DatabaseHelper.PEER_ID;
import static ru.melod1n.vk.database.DatabaseHelper.PHOTO_100;
import static ru.melod1n.vk.database.DatabaseHelper.PHOTO_200;
import static ru.melod1n.vk.database.DatabaseHelper.PHOTO_50;
import static ru.melod1n.vk.database.DatabaseHelper.PUSH_SETTINGS;
import static ru.melod1n.vk.database.DatabaseHelper.RANDOM_ID;
import static ru.melod1n.vk.database.DatabaseHelper.REPLY_MESSAGE;
import static ru.melod1n.vk.database.DatabaseHelper.SCREEN_NAME;
import static ru.melod1n.vk.database.DatabaseHelper.SEX;
import static ru.melod1n.vk.database.DatabaseHelper.STATUS;
import static ru.melod1n.vk.database.DatabaseHelper.TABLE_CONVERSATIONS;
import static ru.melod1n.vk.database.DatabaseHelper.TABLE_FRIENDS;
import static ru.melod1n.vk.database.DatabaseHelper.TABLE_GROUPS;
import static ru.melod1n.vk.database.DatabaseHelper.TABLE_MESSAGES;
import static ru.melod1n.vk.database.DatabaseHelper.TABLE_USERS;
import static ru.melod1n.vk.database.DatabaseHelper.TEXT;
import static ru.melod1n.vk.database.DatabaseHelper.TYPE;
import static ru.melod1n.vk.database.DatabaseHelper.UNREAD_COUNT;
import static ru.melod1n.vk.database.DatabaseHelper.USER_ID;
import static ru.melod1n.vk.database.DatabaseHelper.VERIFIED;

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

    private static void insert(String table, ArrayList values) {
        database.beginTransaction();

        ContentValues cv = new ContentValues();
        for (int i = 0; i < values.size(); i++) {
            VKModel item = (VKModel) values.get(i);

            switch (table) {
                case TABLE_MESSAGES:
                    putValues((VKMessage) item, cv);
                    break;
                case TABLE_CONVERSATIONS:
                    putValues((VKConversation) item, cv);
                    break;
                case TABLE_USERS:
                    putValues((VKUser) item, cv, false);
                    break;
                case TABLE_FRIENDS:
                    putValues((VKUser) item, cv, true);
                    break;
                case TABLE_GROUPS:
                    putValues((VKGroup) item, cv);
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

    public static VKUser getUser(int id) {
        Cursor cursor = selectCursor(TABLE_USERS, USER_ID, id);

        if (cursor.getCount() == 0) return null;

        cursor.moveToFirst();

        VKUser user = parseUser(cursor);

        cursor.close();

        return user;
    }

    public static VKGroup getGroup(int id) {
        Cursor cursor = selectCursor(TABLE_GROUPS, GROUP_ID, id);

        if (cursor.getCount() == 0) return null;

        cursor.moveToFirst();

        VKGroup group = parseGroup(cursor);

        cursor.close();

        return group;
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

    private static VKUser parseUser(Cursor cursor) {
        VKUser user = new VKUser();

        user.setId(getInt(cursor, USER_ID));
        user.setFirstName(getString(cursor, FIRST_NAME));
        user.setLastName(getString(cursor, LAST_NAME));
        user.setDeactivated(getString(cursor, DEACTIVATED));
        user.setClosed(getInt(cursor, CLOSED) == 1);
        user.setCanAccessClosed(getInt(cursor, CAN_ACCESS_CLOSED) == 1);
        user.setSex(getInt(cursor, SEX));
        user.setScreenName(getString(cursor, SCREEN_NAME));
        user.setPhoto50(getString(cursor, PHOTO_50));
        user.setPhoto100(getString(cursor, PHOTO_100));
        user.setPhoto200(getString(cursor, PHOTO_200));
        user.setOnline(getInt(cursor, ONLINE) == 1);
        user.setOnlineMobile(getInt(cursor, ONLINE_MOBILE) == 1);
        user.setStatus(getString(cursor, STATUS));

        Object lastSeen = Util.deserialize(getBlob(cursor, LAST_SEEN));
        user.setLastSeen((VKUser.LastSeen) lastSeen);

        user.setVerified(getInt(cursor, VERIFIED) == 1);

        return user;
    }

    private static VKGroup parseGroup(Cursor cursor) {
        VKGroup group = new VKGroup();

        group.setId(getInt(cursor, GROUP_ID));
        group.setName(getString(cursor, NAME));
        group.setScreenName(getString(cursor, SCREEN_NAME));
        group.setIsClosed(getInt(cursor, IS_CLOSED));
        group.setDeactivated(getString(cursor, DEACTIVATED));
        group.setPhoto50(getString(cursor, PHOTO_50));
        group.setPhoto100(getString(cursor, PHOTO_100));
        group.setPhoto200(getString(cursor, PHOTO_200));

        return group;
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
            values.put(CONVERSATION_ID, conversation.getPeer().getId());
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

    private static void putValues(VKUser user, ContentValues values, boolean isFriend) {
        if (isFriend) {
            values.put(USER_ID, UserConfig.getUserId());
            values.put(FRIEND_ID, user.getId());
            return;
        }

        values.put(USER_ID, user.getId());
        values.put(FIRST_NAME, user.getFirstName());
        values.put(LAST_NAME, user.getLastName());
        values.put(DEACTIVATED, user.getDeactivated());
        values.put(CLOSED, user.isClosed());
        values.put(CAN_ACCESS_CLOSED, user.isCanAccessClosed());
        values.put(SEX, user.getSex());
        values.put(SCREEN_NAME, user.getScreenName());
        values.put(PHOTO_50, user.getPhoto50());
        values.put(PHOTO_100, user.getPhoto100());
        values.put(PHOTO_200, user.getPhoto200());
        values.put(ONLINE, user.isOnline());
        values.put(ONLINE_MOBILE, user.isOnlineMobile());
        values.put(STATUS, user.getStatus());

        if (user.getLastSeen() != null) {
            values.put(LAST_SEEN, Util.serialize(user.getLastSeen()));
        }

        values.put(VERIFIED, user.isVerified());
    }

    private static void putValues(VKGroup group, ContentValues values) {
        values.put(GROUP_ID, Math.abs(group.getId()));
        values.put(NAME, group.getName());
        values.put(SCREEN_NAME, group.getScreenName());
        values.put(IS_CLOSED, group.getIsClosed());
        values.put(DEACTIVATED, group.getDeactivated());
        values.put(TYPE, group.getType());
        values.put(PHOTO_50, group.getPhoto50());
        values.put(PHOTO_100, group.getPhoto100());
        values.put(PHOTO_200, group.getPhoto200());
    }

    public static void insertMessages(ArrayList<VKMessage> messages) {
        insert(TABLE_MESSAGES, messages);
    }

    public static void insertConversations(ArrayList<VKConversation> conversations) {
        insert(TABLE_CONVERSATIONS, conversations);
    }

    public static void insertUsers(ArrayList<VKUser> users) {
        insert(TABLE_USERS, users);
    }

    public static void insertGroups(ArrayList<VKGroup> groups) {
        insert(TABLE_GROUPS, groups);
    }
}
