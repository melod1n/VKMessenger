package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

public class VKGroup extends VKModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String screenName;
    private int isClosed;
    private String deactivated;
    private String type;
    private String photo50;
    private String photo100;
    private String photo200;

    public VKGroup() {
    }

    public VKGroup(JSONObject o) {
        setId(o.optInt("id", -1));
        setName(o.optString("name"));
        setScreenName(o.optString("screen_name"));
        setIsClosed(o.optInt("is_closed"));
        setDeactivated(o.optString("deactivated"));
        setType(o.optString("type"));
        setPhoto50(o.optString("photo_50"));
        setPhoto100(o.optString("photo_100"));
        setPhoto200(o.optString("photo_200"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public int getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(int isClosed) {
        this.isClosed = isClosed;
    }

    public String getDeactivated() {
        return deactivated;
    }

    public void setDeactivated(String deactivated) {
        this.deactivated = deactivated;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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