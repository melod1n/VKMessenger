package ru.melod1n.vk.api.model;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class VKUser extends VKModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_FIELDS = "photo_50,photo_100,photo_200,status,screen_name,online,online_mobile,last_seen,verified,sex";

    public static final VKUser EMPTY = new VKUser() {
        @NonNull
        @Override
        public String toString() {
            return "Unknown Unknown";
        }
    };

    private int id;
    private String firstName;
    private String lastName;
    private String deactivated;
    private boolean closed;
    private boolean canAccessClosed;
    private int sex;
    private String screenName;
    private String photo50;
    private String photo100;
    private String photo200;
    private boolean online;
    private boolean onlineMobile;
    private String status;
    private LastSeen lastSeen;
    private boolean verified;

    public VKUser() {
    }

    public VKUser(JSONObject o) {
        setId(o.optInt("id", -1));
        setFirstName(o.optString("first_name"));
        setLastName(o.optString("last_name"));
        setDeactivated(o.optString("deactivated"));
        setClosed(o.optBoolean("is_closed"));
        setCanAccessClosed(o.optBoolean("can_access_closed"));
        setSex(o.optInt("sex"));
        setScreenName(o.optString("screen_name"));
        setPhoto50(o.optString("photo_50"));
        setPhoto100(o.optString("photo_100"));
        setPhoto200(o.optString("photo_200"));
        setOnline(o.optInt("online") == 1);
        if (isOnline()) setOnlineMobile(o.optInt("online_mobile") == 1);
        setStatus(o.optString("status"));

        JSONObject oLastSeen = o.optJSONObject("last_seen");
        if (oLastSeen != null) {
            setLastSeen(new LastSeen(oLastSeen));
        }

        setVerified(o.optInt("verified") == 1);
    }

    public class LastSeen implements Serializable {

        private static final long serialVersionUID = 1L;

        private int time;
        private int platform;

        LastSeen(JSONObject o) {
            setTime(o.optInt("time"));
            setPlatform(o.optInt("platform"));
        }

        public int getTime() {
            return time;
        }

        void setTime(int time) {
            this.time = time;
        }

        public int getPlatform() {
            return platform;
        }

        void setPlatform(int platform) {
            this.platform = platform;
        }
    }

    public static boolean isUserId(int id) {
        return id > 0 && id < 2_000_000_000;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDeactivated() {
        return deactivated;
    }

    public void setDeactivated(String deactivated) {
        this.deactivated = deactivated;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isCanAccessClosed() {
        return canAccessClosed;
    }

    public void setCanAccessClosed(boolean canAccessClosed) {
        this.canAccessClosed = canAccessClosed;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
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

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isOnlineMobile() {
        return onlineMobile;
    }

    public void setOnlineMobile(boolean onlineMobile) {
        this.onlineMobile = onlineMobile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LastSeen getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LastSeen lastSeen) {
        this.lastSeen = lastSeen;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @NonNull
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    @Override
    public ArrayList<VKUser> asList() {
        return new ArrayList<>(Collections.singletonList(this));
    }
}