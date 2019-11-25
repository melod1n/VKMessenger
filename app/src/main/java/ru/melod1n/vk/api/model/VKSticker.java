package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKSticker extends VKModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int productId;
    private int stickerId;
    private ArrayList<Image> images;

    public VKSticker(JSONObject o) {
        setProductId(o.optInt("product_id", -1));
        setStickerId(o.optInt("sticker_id", -1));

        JSONArray oImages = o.optJSONArray("images");
        if (oImages != null) {
            ArrayList<Image> images = new ArrayList<>();
            for (int i = 0; i < oImages.length(); i++) {
                images.add(new Image(oImages.optJSONObject(i)));
            }

            setImages(images);
        }
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getStickerId() {
        return stickerId;
    }

    public void setStickerId(int stickerId) {
        this.stickerId = stickerId;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public void setImages(ArrayList<Image> images) {
        this.images = images;
    }

    private class Image implements Serializable {

        private static final long serialVersionUID = 1L;

        private String url;
        private int width;
        private int height;

        public Image(JSONObject o) {
            setUrl(o.optString("url"));
            setWidth(o.optInt("width"));
            setHeight(o.optInt("height"));
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
    }
}