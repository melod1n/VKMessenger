package ru.melod1n.vk.api.model.attachment;

import org.json.JSONObject;

import java.io.Serializable;

public class VKGift implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public String thumb_256;
    //public String thumb_96;
    //public String thumb_48;

    public static VKGift parse(JSONObject o) {
        VKGift gift = new VKGift();
        gift.id = o.optInt("id");
        gift.thumb_256 = o.optString("thumb_256");
        //audio.thumb_96 = o.getString("thumb_96");
        //audio.thumb_48 = o.getString("thumb_48");
        return gift;
    }
}