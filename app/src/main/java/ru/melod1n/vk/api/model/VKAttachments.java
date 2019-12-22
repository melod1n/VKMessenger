package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class VKAttachments {

    private static final String TYPE_PHOTO = "photo";
    private static final String TYPE_VIDEO = "video";
    private static final String TYPE_AUDIO = "audio";
    private static final String TYPE_DOC = "doc";
    private static final String TYPE_LINK = "link";
    private static final String TYPE_STICKER = "sticker";
    private static final String TYPE_GIFT = "gift";
    private static final String TYPE_AUDIO_MESSAGE = "audio_message";
    private static final String TYPE_GRAFFITI = "graffiti";
    private static final String TYPE_POLL = "poll";
    private static final String TYPE_GEO = "geo";
    private static final String TYPE_WALL = "wall";
    private static final String TYPE_CALL = "call";
    private static final String TYPE_STORY = "story";
    private static final String TYPE_POINT = "point";
    private static final String TYPE_MARKET = "market";
    private static final String TYPE_ARTICLE = "article";
    private static final String TYPE_PODCAST = "podcast";
    private static final String TYPE_WALL_REPLY = "wall_reply";
    private static final String TYPE_MONEY_REQUEST = "money_request";
    private static final String TYPE_AUDIO_PLAYLIST = "audio_playlist";

    public static ArrayList<VKModel> parse(JSONArray array) {
        ArrayList<VKModel> attachments = new ArrayList<>(array.length());

        for (int i = 0; i < array.length(); i++) {
            JSONObject attachment = array.optJSONObject(i);
            if (attachment.has("attachment")) {
                attachment = attachment.optJSONObject("attachment");
            }

            if (attachment == null) continue;

            String type = attachment.optString("type");
            JSONObject object = attachment.optJSONObject(type);

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
                case TYPE_AUDIO_MESSAGE:
                    attachments.add(new VKAudioMessage(object));
                    break;
                case TYPE_GRAFFITI:
                    attachments.add(new VKGraffiti(object));
                    break;
                case TYPE_POLL:
                    attachments.add(new VKPoll(object));
                    break;
                case TYPE_CALL:
                    attachments.add(new VKCall(object));
                    break;
            }
        }

        return attachments;
    }
}