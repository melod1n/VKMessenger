package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKPhoto extends VKModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int albumId;
    private int ownerId;
    private String text;
    private int date;
    private ArrayList<VKPhotoSize> sizes;
    private int width;
    private int height;

    public VKPhoto(JSONObject o) {
        setId(o.optInt("id", -1));
        setAlbumId(o.optInt("album_id", -1));
        setOwnerId(o.optInt("owner_id", -1));
        setText(o.optString("text"));
        setDate(o.optInt("date"));

        JSONArray oSizes = o.optJSONArray("sizes");
        if (oSizes != null) {
            ArrayList<VKPhotoSize> sizes = new ArrayList<>();
            for (int i = 0; i < oSizes.length(); i++) {
                sizes.add(new VKPhotoSize(oSizes.optJSONObject(i)));
            }

            setSizes(sizes);
        }

        setWidth(o.optInt("width"));
        setHeight(o.optInt("height"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public ArrayList<VKPhotoSize> getSizes() {
        return sizes;
    }

    public void setSizes(ArrayList<VKPhotoSize> sizes) {
        this.sizes = sizes;
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
}