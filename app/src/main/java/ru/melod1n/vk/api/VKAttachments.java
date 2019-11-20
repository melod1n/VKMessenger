package ru.melod1n.vk.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.attachment.VKApp;
import ru.melod1n.vk.api.model.attachment.VKAudio;
import ru.melod1n.vk.api.model.attachment.VKDoc;
import ru.melod1n.vk.api.model.attachment.VKGift;
import ru.melod1n.vk.api.model.attachment.VKGraffiti;
import ru.melod1n.vk.api.model.attachment.VKLink;
import ru.melod1n.vk.api.model.attachment.VKPhoto;
import ru.melod1n.vk.api.model.attachment.VKVideo;

public class VKAttachments implements Serializable {
    private static final long serialVersionUID = 1L;
    public long id;//used only for wall post attached to message
    public String type; //photo,posted_photo,video,audio,link,note,app,poll,doc,geo,message,page,album
    public VKPhoto photo;
    //public VKPhoto posted_photo;
    public VKVideo video;
    public VKAudio audio;
    public VKLink link;
    public VKGraffiti graffiti;
    public VKApp app;
    public VKDoc document;
    public VKMessage message;
    public VKGift gift;

    public static ArrayList<VKAttachments> parseAttachments(JSONArray array) {
        ArrayList<VKAttachments> attachments = new ArrayList<>();

        if (array != null) {
            int size = array.length();
            for (int j = 0; j < size; ++j) {
                Object att = array.opt(j);
                if (!(att instanceof JSONObject))
                    continue;
                JSONObject json_attachment = (JSONObject) att;
                VKAttachments attachment = new VKAttachments();
                attachment.type = json_attachment.optString("type");

                switch (attachment.type) {
                    case "photo":
                        JSONObject x = json_attachment.optJSONObject("photo");
                        if (x != null)
                            attachment.photo = VKPhoto.parse(x);
                        break;
                    case "graffiti":
                        attachment.graffiti = VKGraffiti.parse(json_attachment.optJSONObject("graffiti"));
                        break;
                    case "link":
                        attachment.link = VKLink.parse(json_attachment.optJSONObject("link"));
                        break;
                    case "audio":
                        attachment.audio = VKAudio.parse(json_attachment.optJSONObject("audio"));
                        break;
                    case "video":
                        attachment.video = VKVideo.parseForAttachments(json_attachment.optJSONObject("video"));
                        break;
                    case "doc":
                        attachment.document = VKDoc.parse(json_attachment.optJSONObject("doc"));
                        break;
                    case "gift":
                        attachment.gift = VKGift.parse(json_attachment.optJSONObject("gift"));
                        break;
                }
                attachments.add(attachment);
            }
        }

        return attachments;
    }
}
