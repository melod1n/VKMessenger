package ru.melod1n.vk.api.model;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKConversation extends VKModel implements Serializable {

    public static int count;

    private static final long serialVersionUID = 1L;

    private Peer peer;

    private int inRead;
    private int outRead;
    private int lastMessageId;
    private int unreadCount;

    private PushSettings pushSettings;
    private CanWrite canWrite;
    private ChatSettings chatSettings;

    private ArrayList<VKUser> profiles = new ArrayList<>();
    private ArrayList<VKGroup> groups = new ArrayList<>();

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
        setLastMessageId(o.optInt("last_message_id", -1));
        setUnreadCount(o.optInt("unread_count", 0));

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

            JSONObject oPinnedMessage = oChatSettings.optJSONObject("pinned_message");
            if (oPinnedMessage != null) {
                settings.setPinnedMessage(new VKPinnedMessage(oPinnedMessage));
            }

            settings.setState(oChatSettings.optString("state"));

            JSONObject oPhoto = oChatSettings.optJSONObject("photo");
            if (oPhoto != null) {
                settings.setPhoto(new ChatSettings.Photo(oPhoto));
            }

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

    public boolean isChat() {
        return getPeer().getType().equals(Peer.TYPE_CHAT);
    }

    public boolean isUser() {
        return getPeer().getType().equals(Peer.TYPE_USER);
    }

    public boolean isGroup() {
        return getPeer().getType().equals(Peer.TYPE_GROUP);
    }

    public boolean isChannel() {
        return getChatSettings() != null && getChatSettings().isGroupChannel();
    }

    public static ArrayList<VKConversation> parse(JSONArray array) {
        ArrayList<VKConversation> conversations = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            conversations.add(new VKConversation(array.optJSONObject(i)));
        }

        return conversations;
    }

    public class Peer implements Serializable {

        private static final long serialVersionUID = 1L;

        static final String TYPE_USER = "user";
        static final String TYPE_CHAT = "chat";
        static final String TYPE_GROUP = "group";

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

    public class PushSettings implements Serializable {

        private static final long serialVersionUID = 1L;

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

        public boolean isNotificationsDisabled() {
            return isDisabledForever() || getDisabledUntil() > 0 || isNoSound();
        }
    }

    public class CanWrite implements Serializable {

        private static final long serialVersionUID = 1L;

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

    public static class ChatSettings implements Serializable {

        private static final long serialVersionUID = 1L;

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

        public static class Photo implements Serializable {

            private static final long serialVersionUID = 1L;

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

    public int getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
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

    public ArrayList<VKUser> getProfiles() {
        return profiles;
    }

    public void setProfiles(ArrayList<VKUser> profiles) {
        this.profiles = profiles;
    }

    public ArrayList<VKGroup> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<VKGroup> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public String toString() {
        return getChatSettings() == null ? "" : getChatSettings().getTitle();
    }
}
