package ru.melod1n.vk.api.model;

import org.json.JSONObject;

public class VKPhotoSize {

    private String type;
    private String url;
    private int height;
    private int width;

    public VKPhotoSize(JSONObject o) {
        setType(o.optString("type"));
        setUrl(o.optString("url"));
        setHeight(o.optInt("height"));
        setWidth(o.optInt("width"));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
