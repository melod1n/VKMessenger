package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

public class VKGroup extends VKModel implements Serializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public String name;
    public String screen_name;
    public int is_closed;
    public boolean is_admin;
    public int admin_level;
    public boolean is_member;
    public int type;
    public boolean verified;
    public String photo_50;
    public String photo_100;
    public String photo_200;
    public String description;
    public long members_count;
    public String status;

    public VKGroup() {
    }

    public VKGroup(JSONObject source) {
        this.id = source.optInt("id");
        this.name = source.optString("name");
        this.screen_name = source.optString("screen_name");
        this.is_closed = source.optInt("is_closed");
        this.is_admin = source.optLong("is_admin") == 1;
        this.is_member = source.optLong("is_member") == 1;
        this.verified = source.optInt("verified") == 1;
        this.admin_level = source.optInt("admin_level");

        String type = source.optString("type", "group");
        switch (type) {
            case "group":
                this.type = Type.GROUP;
                break;
            case "page":
                this.type = Type.PAGE;
                break;
            case "event":
                this.type = Type.EVENT;
                break;
        }

        this.photo_50 = source.optString("photo_50");
        this.photo_100 = source.optString("photo_100");
        this.photo_200 = source.optString("photo_200");

        this.description = source.optString("description");
        this.status = source.optString("status");
        this.members_count = source.optLong("members_count");
    }

    @Override
    public String toString() {
        return name;
    }

    public static int toGroupId(int id) {
        return (id < 0) ? Math.abs(id) : (1_000_000_000 - id);
    }

    public static boolean isGroupId(int id) {
        return id < 0;
    }

    public static class AdminLevel {
        public final static int MODERATOR = 1;
        public final static int EDITOR = 2;
        public final static int ADMIN = 3;

        private AdminLevel() {
        }
    }

    public static class Status {
        public final static int OPEN = 0;
        public final static int CLOSED = 1;
        public final static int PRIVATE = 2;

        private Status() {
        }
    }

    public static class Type {
        public final static int GROUP = 0;
        public final static int PAGE = 1;
        public final static int EVENT = 2;

        private Type() {
        }
    }

}