package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKMessage extends VKModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public static int count;
    public static int lastHistoryCount;

    public static final int UNREAD = 1;       // message unread
    public static final int OUTBOX = 2;       // исходящее сообщение
    public static final int REPLIED = 4;      // на сообщение был создан ответ
    public static final int IMPORTANT = 8;    // помеченное сообщение
    public static final int CHAT = 16;        // сообщение отправлено через диалог
    public static final int FRIENDS = 32;     // сообщение отправлено другом
    public static final int SPAM = 64;        // сообщение помечено как "Спам"
    public static final int DELETED = 128;    // сообщение удалено (в корзине)
    public static final int FIXED = 256;      // сообщение проверено пользователем на спам
    public static final int MEDIA = 512;      // сообщение содержит медиаконтент
    public static final int BESEDA = 8192;    // беседа

    public static final String ACTION_CHAT_CREATE = "chat_create";
    public static final String ACTION_CHAT_INVITE_USER = "chat_invite_user";
    public static final String ACTION_CHAT_KICK_USER = "chat_kick_user";

    public static final String ACTION_CHAT_TITLE_UPDATE = "chat_title_update";
    public static final String ACTION_CHAT_PHOTO_UPDATE = "chat_photo_update";
    public static final String ACTION_CHAT_PHOTO_REMOVE = "chat_photo_remove";

    private int id;
    private int date;
    private int peerId;
    private int fromId;
    private String text;
    private int randomId;
    private ArrayList<VKModel> attachments;
    private boolean important;
    private ArrayList<VKMessage> fwdMessages;
    private VKMessage replyMessage;
    private Action action;

    public VKMessage(JSONObject o) {
        setId(o.optInt("id", -1));
        setDate(o.optInt("date"));
        setPeerId(o.optInt("peer_id", -1));
        setFromId(o.optInt("from_id", -1));
        setText(o.optString("text"));
        setRandomId(o.optInt("random_id", -1));

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

    private class Action extends VKModel implements Serializable {
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

        public Action(JSONObject o) {
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

        private class Photo {
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
