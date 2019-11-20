package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Peer peer;

    private int inRead;
    private int outRead;
    private int unreadCount;

    private PushSettings pushSettings;
    private CanWrite canWrite;
    private ChatSettings chatSettings;

    public VKConversation() {
    }

    public VKConversation(JSONObject o) {
        JSONObject oPeer = o.optJSONObject("peer");
        if (oPeer != null) {
            Peer peer = new Peer();
            peer.setId(oPeer.optInt("id", -1));
            peer.setType(oPeer.optString("type"));
            peer.setLocalId(oPeer.optInt("local_id"));
            setPeer(peer);
        }

        setInRead(o.optInt("in_read"));
        setOutRead(o.optInt("out_read"));
        setUnreadCount(o.optInt("unread_count"));

        JSONObject oPushSettings = o.optJSONObject("push_settings");
        if (oPushSettings != null) {
            PushSettings settings = new PushSettings();
            settings.setDisabledUntil(oPushSettings.optInt("disabled_until"));
            settings.setDisabledForever(oPushSettings.optBoolean("disabled_forever"));
            settings.setNoSound(oPushSettings.optBoolean("no_sound"));
            setPushSettings(settings);
        }

        JSONObject oCanWrite = o.optJSONObject("can_write");
        if (oCanWrite != null) {
            CanWrite canWrite = new CanWrite();
            canWrite.setAllowed(oCanWrite.optBoolean("allowed"));
            canWrite.setReason(oCanWrite.optInt("reason", -1));
            setCanWrite(canWrite);
        }

        JSONObject oChatSettings = o.optJSONObject("chat_settings");
        if (oChatSettings != null) {
            ChatSettings settings = new ChatSettings();
            settings.setMembersCount(oChatSettings.optInt("members_count"));
            settings.setTitle(oChatSettings.optString("title"));
            settings.setPinnedMessage(new PinnedMessage(oChatSettings.optJSONObject("pinned_message")));
            settings.setState(oChatSettings.optString("state"));
            settings.setPhoto(new ChatSettings.Photo(oChatSettings.optJSONObject("photo")));
            settings.setGroupChannel(oChatSettings.optBoolean("is_group_channel"));

            JSONArray activeIds = oChatSettings.optJSONArray("active_ids");
            if (activeIds != null) {
                Integer[] ids = new Integer[activeIds.length()];
                for (int i = 0; i < activeIds.length(); i++) {
                    ids[i] = activeIds.optInt(i);
                }
                settings.setActiveIds(ids);
            }

            setChatSettings(settings);
        }
    }

    public static ArrayList<VKConversation> parse(JSONArray array) {
        ArrayList<VKConversation> conversations = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            conversations.add(new VKConversation(array.optJSONObject(i)));
        }

        return conversations;
    }

    private class Peer {
        public static final String TYPE_USER = "user";
        public static final String TYPE_CHAT = "chat";
        public static final String TYPE_GROUP = "group";
        public static final String TYPE_EMAIL = "email";

        private int id;
        private String type;
        private int localId;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getLocalId() {
            return localId;
        }

        public void setLocalId(int localId) {
            this.localId = localId;
        }
    }

    private class PushSettings {
        private int disabledUntil;
        private boolean disabledForever;
        private boolean noSound;

        public int getDisabledUntil() {
            return disabledUntil;
        }

        public void setDisabledUntil(int disabledUntil) {
            this.disabledUntil = disabledUntil;
        }

        public boolean isDisabledForever() {
            return disabledForever;
        }

        public void setDisabledForever(boolean disabledForever) {
            this.disabledForever = disabledForever;
        }

        public boolean isNoSound() {
            return noSound;
        }

        public void setNoSound(boolean noSound) {
            this.noSound = noSound;
        }
    }

    private class CanWrite {
        private boolean allowed;
        private int reason = -1;

        /*
             18 — пользователь заблокирован или удален;
             900 — нельзя отправить сообщение пользователю, который в чёрном списке;
             901 — пользователь запретил сообщения от сообщества;
             902 — пользователь запретил присылать ему сообщения с помощью настроек приватности;
             915 — в сообществе отключены сообщения;
             916 — в сообществе заблокированы сообщения;
             917 — нет доступа к чату;
             918 — нет доступа к e-mail;
             203 — нет доступа к сообществу
         */

        public boolean isAllowed() {
            return allowed;
        }

        public void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }

        public int getReason() {
            return reason;
        }

        public void setReason(int reason) {
            this.reason = reason;
        }
    }

    private static class ChatSettings {
        public static final String STATE_IN = "in";
        public static final String STATE_KICKED = "kicked";
        public static final String STATE_LEFT = "left";

        private int membersCount;
        private String title;
        private Object pinnedMessage;
        private String state;
        private Photo photo;
        private Integer[] activeIds;
        private boolean isGroupChannel;

        public int getMembersCount() {
            return membersCount;
        }

        public void setMembersCount(int membersCount) {
            this.membersCount = membersCount;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Object getPinnedMessage() {
            return pinnedMessage;
        }

        public void setPinnedMessage(Object pinnedMessage) {
            this.pinnedMessage = pinnedMessage;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Photo getPhoto() {
            return photo;
        }

        public void setPhoto(Photo photo) {
            this.photo = photo;
        }

        public Integer[] getActiveIds() {
            return activeIds;
        }

        public void setActiveIds(Integer[] activeIds) {
            this.activeIds = activeIds;
        }

        public boolean isGroupChannel() {
            return isGroupChannel;
        }

        public void setGroupChannel(boolean groupChannel) {
            isGroupChannel = groupChannel;
        }

        private static class Photo {
            private String photo50;
            private String photo100;
            private String photo200;

            public Photo() {
            }

            public Photo(JSONObject o) {
                setPhoto50(o.optString("photo_50"));
                setPhoto100(o.optString("photo_100"));
                setPhoto200(o.optString("photo_200"));
            }

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


    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public int getInRead() {
        return inRead;
    }

    public void setInRead(int inRead) {
        this.inRead = inRead;
    }

    public int getOutRead() {
        return outRead;
    }

    public void setOutRead(int outRead) {
        this.outRead = outRead;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public PushSettings getPushSettings() {
        return pushSettings;
    }

    public void setPushSettings(PushSettings pushSettings) {
        this.pushSettings = pushSettings;
    }

    public CanWrite getCanWrite() {
        return canWrite;
    }

    public void setCanWrite(CanWrite canWrite) {
        this.canWrite = canWrite;
    }

    public ChatSettings getChatSettings() {
        return chatSettings;
    }

    public void setChatSettings(ChatSettings chatSettings) {
        this.chatSettings = chatSettings;
    }
}
