package ru.melod1n.vk.api.model.attachment;

import org.json.JSONObject;

import java.io.Serializable;

import ru.melod1n.vk.api.VKApi;

public class VKLink implements Serializable {
    private static final long serialVersionUID = 1L;
    public String url;
    public String title;
    public String description;
    public String image_src;

    public static VKLink parse(JSONObject o) {
        VKLink link = new VKLink();
        link.url = o.optString("url");
        link.title = VKApi.unescape(o.optString("title"));
        link.description = VKApi.unescape(o.optString("description"));
        link.image_src = o.optString("image_src");
        return link;
    }

    public static VKLink parseFromGroup(JSONObject o) {
        VKLink link = new VKLink();
        link.url = o.optString("url");
        link.title = VKApi.unescape(o.optString("name"));
        link.description = VKApi.unescape(o.optString("desc"));
        link.image_src = o.optString("photo_100");
        return link;
    }
}
