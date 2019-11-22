package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class VKAttachments {

    public static final String TYPE_PHOTO = "photo";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_DOC = "doc";
    public static final String TYPE_POST = "wall";
    public static final String TYPE_POSTED_PHOTO = "posted_photo";
    public static final String TYPE_LINK = "link";
    public static final String TYPE_NOTE = "note";
    public static final String TYPE_APP = "app";
    public static final String TYPE_POLL = "poll";
    public static final String TYPE_WIKI_PAGE = "page";
    public static final String TYPE_ALBUM = "album";
    public static final String TYPE_STICKER = "sticker";
    public static final String TYPE_GIFT = "gift";

    public static ArrayList<VKModel> parse(JSONArray array) {
        ArrayList<VKModel> attachments = new ArrayList<>(array.length());

        for (int i = 0; i < array.length(); i++) {
            JSONObject attach = array.optJSONObject(i);
            if (attach.has("attachment")) {
                attach = attach.optJSONObject("attachment");
            }

            if (attach == null) continue;

            String type = attach.optString("type");
            JSONObject object = attach.optJSONObject(type);

            if (object == null) continue;

            switch (type) {
                case TYPE_PHOTO:
                    attachments.add(new VKPhoto(object));
                    break;
                case TYPE_AUDIO:
                    attachments.add(new VKAudio(object));
                    break;
                case TYPE_VIDEO:
                    attachments.add(new VKVideo(object));
                    break;
                case TYPE_DOC:
                    attachments.add(new VKDoc(object));
                    break;
                case TYPE_STICKER:
                    attachments.add(new VKSticker(object));
                    break;
                case TYPE_LINK:
                    attachments.add(new VKLink(object));
                    break;
                case TYPE_GIFT:
                    attachments.add(new VKGift(object));
                    break;
            }
        }

        return attachments;
    }
}