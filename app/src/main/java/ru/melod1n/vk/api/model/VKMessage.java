package ru.melod1n.vk.api.model;

import android.util.ArrayMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import ru.melod1n.vk.api.UserConfig;

public class VKMessage extends VKModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public static int lastHistoryCount;

    public static final int UNREAD = 1;                 // Оно просто есть
    public static final int OUTBOX = 1 << 1;            // Исходящее сообщение
    public static final int REPLIED = 1 << 2;           // На сообщение был создан ответ
    public static final int IMPORTANT = 1 << 3;         // Важное сообщение
    public static final int FRIENDS = 1 << 5;           // Сообщение в чат друга
    public static final int SPAM = 1 << 6;              // Сообщение помечено как спам
    public static final int DELETED = 1 << 7;           // Удаление сообщения
    public static final int AUDIO_LISTENED = 1 << 12;   // ГС прослушано
    public static final int CHAT = 1 << 13;             // Сообщение отправлено в беседу
    public static final int CANCEL_SPAM = 1 << 15;      // Отмена пометки спама
    public static final int HIDDEN = 1 << 16;           // Приветственное сообщение сообщества
    public static final int DELETE_FOR_ALL = 1 << 17;   // Сообщение удалено для всех
    public static final int CHAT_IN = 1 << 19;          // Входяшее сообщение в беседе
    public static final int REPLY_MSG = 1 << 21;        // Ответ на сообщение

    private static final ArrayMap<String, Integer> flags = new ArrayMap<>();

    static {
        flags.put("unread", UNREAD);
        flags.put("outbox", OUTBOX);
        flags.put("replied", REPLIED);
        flags.put("important", IMPORTANT);
        flags.put("friends", FRIENDS);
        flags.put("spam", SPAM);
        flags.put("deleted", DELETED);
        flags.put("audio_listened", AUDIO_LISTENED);
        flags.put("chat", CHAT);
        flags.put("cancel_spam", CANCEL_SPAM);
        flags.put("hidden", HIDDEN);
        flags.put("delete_for_all", DELETE_FOR_ALL);
        flags.put("chat_in", CHAT_IN);
        flags.put("reply_msg", REPLY_MSG);
    }

    public static final String ACTION_CHAT_CREATE = "chat_create";
    public static final String ACTION_CHAT_INVITE_USER = "chat_invite_user";
    public static final String ACTION_CHAT_KICK_USER = "chat_kick_user";

    public static final String ACTION_CHAT_TITLE_UPDATE = "chat_title_update";
    public static final String ACTION_CHAT_PHOTO_UPDATE = "chat_photo_update";
    public static final String ACTION_CHAT_PHOTO_REMOVE = "chat_photo_remove";

    public static final String ACTION_CHAT_PIN_MESSAGE = "chat_pin_message";
    public static final String ACTION_CHAT_UNPIN_MESSAGE = "chat_unpin_message";
    public static final String ACTION_CHAT_INVITE_USER_BY_LINK = "chat_invite_user_by_link";

    private int id;
    private int date;
    private int peerId;
    private int fromId;
    private int editTime;
    private boolean out;
    private String text;
    private int randomId;
    private int conversationMessageId;
    private ArrayList<VKModel> attachments;
    private boolean important;
    private ArrayList<VKMessage> fwdMessages;
    private VKMessage replyMessage;
    private Action action;
    private boolean read;

    public VKMessage() {
    }

    public VKMessage(JSONObject o) {
        setId(o.optInt("id", -1));
        setDate(o.optInt("date"));
        setPeerId(o.optInt("peer_id", -1));
        setFromId(o.optInt("from_id", -1));
        setOut(o.optInt("out") == 1);
        setText(o.optString("text"));
        setRandomId(o.optInt("random_id", -1));
        setConversationMessageId(o.optInt("conversation_message_id", -1));
        setEditTime(o.optInt("edit_time"));

        JSONArray oAttachments = o.optJSONArray("attachments");
        if (oAttachments != null) {
            setAttachments(VKAttachments.parse(oAttachments));
        }

        setImportant(o.optBoolean("important"));

        JSONArray oFwdMessages = o.optJSONArray("fwd_messages");
        if (oFwdMessages != null) {
            ArrayList<VKMessage> fwdMessages = new ArrayList<>(oFwdMessages.length());
            for (int i = 0; i < oFwdMessages.length(); i++) {
                fwdMessages.add(new VKMessage(oFwdMessages.optJSONObject(i)));
            }

            setFwdMessages(fwdMessages);
        }

        JSONObject oReplyMessage = o.optJSONObject("reply_message");
        if (oReplyMessage != null) {
            setReplyMessage(new VKMessage(oReplyMessage));
        }

        JSONObject oAction = o.optJSONObject("action");
        if (oAction != null) {
            setAction(new Action(oAction));
        }

    }

    public static boolean hasFlag(int mask, String flagName) {
        Object object = flags.get(flagName);

        if (object != null) { //has flag
            int flag = (int) object;
            return (flag & mask) > 0;
        } else return false;
    }

    //TODO: нормально парсить сообщение

    // [msg_id, flags, peer_id, timestamp, text, {emoji, from, action, keyboard}, {attachs}, random_id, conv_msg_id, edit_time]
    public static VKMessage parse(JSONArray array) {
        VKMessage message = new VKMessage();

        int id = array.optInt(1);
        message.setId(id);

        int flags = array.optInt(2);

        int peerId = array.optInt(3);
        message.setPeerId(peerId);

        int date = array.optInt(4);
        message.setDate(date);

        String text = array.optString(5);
        message.setText(text);

        JSONObject o = array.optJSONObject(6);

        int fromId = hasFlag(flags, "outbox") ? UserConfig.getUserId() : (o != null ? o.optInt("from") : peerId);
        message.setFromId(fromId);

        boolean out = hasFlag(flags, "outbox") || fromId == UserConfig.getUserId();
        message.setOut(out);

        int editTime = array.optInt(10);
        message.setEditTime(editTime);

        int conversationMessageId = array.optInt(9);
        message.setConversationMessageId(conversationMessageId);

        int randomId = array.optInt(8);
        message.setRandomId(randomId);

        if (o != null) {
            if (o.has("source_act")) {
                Action action = new Action();
                action.setType(o.optString("source_act"));

                if (o.has("source_text"))
                    action.setText(o.optString("source_text"));

                if (o.has("source_mid"))
                    action.setMemberId(o.optInt("source_mid"));

                message.setAction(action);
            }
        }

        return message;
    }

    public static boolean isOut(int flags) {
        return (VKMessage.OUTBOX & flags) > 0;
    }

    public static boolean isDeleted(int flags) {
        return (VKMessage.DELETED & flags) > 0;
    }

    public static boolean isUnread(int flags) {
        return (VKMessage.UNREAD & flags) > 0;
    }

    public static boolean isSpam(int flags) {
        return (VKMessage.SPAM & flags) > 0;
    }

    public static boolean isCanceledSpam(int flags) {
        return (VKMessage.CANCEL_SPAM & flags) > 0;
    }

    public static boolean isImportant(int flags) {
        return (VKMessage.IMPORTANT & flags) > 0;
    }

    public static boolean isDeletedForAll(int flags) {
        return (VKMessage.DELETE_FOR_ALL & flags) > 0;
    }

    public boolean isFromUser() {
        return fromId > 0;
    }

    public boolean isFromGroup() {
        return fromId < 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRandomId() {
        return randomId;
    }

    public void setRandomId(int randomId) {
        this.randomId = randomId;
    }

    public ArrayList<VKModel> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<VKModel> attachments) {
        this.attachments = attachments;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public int getConversationMessageId() {
        return conversationMessageId;
    }

    public void setConversationMessageId(int conversationMessageId) {
        this.conversationMessageId = conversationMessageId;
    }

    public int getEditTime() {
        return editTime;
    }

    public void setEditTime(int editTime) {
        this.editTime = editTime;
    }

    public ArrayList<VKMessage> getFwdMessages() {
        return fwdMessages;
    }

    public void setFwdMessages(ArrayList<VKMessage> fwdMessages) {
        this.fwdMessages = fwdMessages;
    }

    public VKMessage getReplyMessage() {
        return replyMessage;
    }

    public void setReplyMessage(VKMessage replyMessage) {
        this.replyMessage = replyMessage;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public static class Action implements Serializable {

        private static final long serialVersionUID = 1L;

        /*
            chat_photo_update — обновлена фотография беседы;
            chat_photo_remove — удалена фотография беседы;
            chat_create — создана беседа;
            chat_title_update — обновлено название беседы;
            chat_invite_user — приглашен пользователь;
            chat_kick_user — исключен пользователь;
            chat_pin_message — закреплено сообщение;
            chat_unpin_message — откреплено сообщение;
            chat_invite_user_by_link — пользователь присоединился к беседе по ссылке.
        */

        private String type;
        private int memberId; //kick / invite / pin / unpin
        private String text; //for chat_create / title_update
        private Photo photo;

        Action() {
        }

        Action(JSONObject o) {
            setType(o.optString("type"));
            setMemberId(o.optInt("member_id", -1));
            setText(o.optString("text"));
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int memberId) {
            this.memberId = memberId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Photo getPhoto() {
            return photo;
        }

        public void setPhoto(Photo photo) {
            this.photo = photo;
        }

        private class Photo implements Serializable {

            private static final long serialVersionUID = 1L;

            private String photo50;
            private String photo100;
            private String photo200;

            public String getPhoto50() {
                return photo50;
            }

            public void setPhoto50(String photo50) {
                this.photo50 = photo50;
            }

            public String getPhoto100() {
                return photo100;
            }

            public void setPhoto100(String photo100) {
                this.photo100 = photo100;
            }

            public String getPhoto200() {
                return photo200;
            }

            public void setPhoto200(String photo200) {
                this.photo200 = photo200;
            }
        }
    }
}
