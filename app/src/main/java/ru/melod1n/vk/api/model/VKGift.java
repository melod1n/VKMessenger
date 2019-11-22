package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

public class VKGift extends VKModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String thumb256;
    private String thumb96;
    private String thumb48;

    public VKGift(JSONObject o) {
        setId(o.optInt("id", -1));
        setThumb256(o.optString("thumb_256"));
        setThumb96(o.optString("thumb_96"));
        setThumb48(o.optString("thumb_48"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getThumb256() {
        return thumb256;
    }

    public void setThumb256(String thumb256) {
        this.thumb256 = thumb256;
    }

    public String getThumb96() {
        return thumb96;
    }

    public void setThumb96(String thumb96) {
        this.thumb96 = thumb96;
    }

    public String getThumb48() {
        return thumb48;
    }

    public void setThumb48(String thumb48) {
        this.thumb48 = thumb48;
    }
}