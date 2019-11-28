package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

public class VKGraffiti extends VKModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int ownerId;
    private String url;
    private int width;
    private int height;
    private String accessKey;

    public VKGraffiti(JSONObject o) {
        setId(o.optInt("id", -1));
        setOwnerId(o.optInt("owner_id", -1));
        setUrl(o.optString("url"));
        setWidth(o.optInt("width"));
        setHeight(o.optInt("height"));
        setAccessKey(o.optString("access_key"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
}
