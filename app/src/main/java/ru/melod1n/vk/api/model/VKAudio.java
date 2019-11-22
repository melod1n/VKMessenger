package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

public class VKAudio extends VKModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int ownerId;
    private String artist;
    private String title;
    private int duration;
    private String url;
    private int date;

    public VKAudio(JSONObject o) {
        setId(o.optInt("id", -1));
        setOwnerId(o.optInt("owner_id", -1));
        setArtist(o.optString("artist"));
        setTitle(o.optString("title"));
        setDuration(o.optInt("duration"));
        setUrl(o.optString("url"));
        setDate(o.optInt("date"));
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

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }
}
