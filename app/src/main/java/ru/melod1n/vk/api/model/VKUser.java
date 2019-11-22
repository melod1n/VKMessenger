package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKUser extends VKModel implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_FIELDS = "photo_50, photo_100, photo_200, status, screen_name, online, online_mobile, last_seen, verified, sex";

    public static final VKUser EMPTY = new VKUser() {
        @Override
        public String toString() {
            return "Unknown Unknown";
        }
    };

    public int id;
    public String first_name;
    public String last_name;
    public String screen_name;
    public boolean online;
    public boolean online_mobile;
    public int online_app;
    public String photo_50;
    public String photo_100;
    public String photo_200;
    public String status;
    public long last_seen;
    public boolean verified;
    public String deactivated;
    public int sex;

    public static ArrayList<VKUser> parse(JSONArray array) {
        ArrayList<VKUser> users = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            Object value = array.opt(i);
            users.add(new VKUser((JSONObject) value));
        }

        return users;
    }

    public VKUser() {
        this.first_name = "Unknown";
        this.last_name = "Unknown";
    }

    public VKUser(JSONObject source) {
        this.id = source.optInt("id");
        this.first_name = source.optString("first_name", "DELETED");
        this.last_name = source.optString("last_name", "DELETED");
        this.photo_50 = source.optString("photo_50");
        this.photo_100 = source.optString("photo_100");
        this.photo_200 = source.optString("photo_200");
        this.screen_name = source.optString("screen_name");
        this.online = source.optInt("online") == 1;
        this.status = source.optString("status");
        this.online_mobile = source.optInt("online_mobile") == 1;
        this.verified = source.optInt("verified") == 1;
        this.deactivated = source.optString("deactivated");
        this.sex = source.optInt("sex");
        if (this.online_mobile) {
            this.online_app = source.optInt("online_app");
        }
        JSONObject lastSeen = source.optJSONObject("last_seen");
        if (lastSeen != null) {
            this.last_seen = lastSeen.optLong("time");
        }
    }

    @Override
    public String toString() {
        return first_name + " " + last_name;
    }

    public static class Sex {
        public static final int NONE = 0;
        public static final int FEMALE = 1;
        public static final int MALE = 2;
    }
}