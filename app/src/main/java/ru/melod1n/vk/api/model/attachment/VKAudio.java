package ru.melod1n.vk.api.model.attachment;

import org.json.JSONObject;

import java.io.Serializable;

import ru.melod1n.vk.api.VKApi;

public class VKAudio implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public int owner_id;
    public String artist;
    public String title;
    public long duration;
    public String url;

    public static VKAudio parse(JSONObject o) {
        VKAudio audio = new VKAudio();
        audio.id = o.optInt("id", -1);
        audio.owner_id = o.optInt("owner_id", -1);
        audio.artist = VKApi.unescape(o.optString("artist"));
        audio.title = VKApi.unescape(o.optString("title"));
        audio.duration = o.optLong("duration");
        audio.url = o.optString("url");

        return audio;
    }
}