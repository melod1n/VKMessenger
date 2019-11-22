package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKPinnedMessage extends VKModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int date;
    private int fromId;
    private String text;
    private ArrayList<VKModel> attachments;
    private ArrayList<VKMessage> fwdMessages;

    public VKPinnedMessage(JSONObject o) {
        setId(o.optInt("id", -1));
        setDate(o.optInt("date"));
        setFromId(o.optInt("from_id", -1));
        setText(o.optString("text"));
        setAttachments(VKAttachments.parse(o.optJSONArray("attachments")));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<VKModel> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<VKModel> attachments) {
        this.attachments = attachments;
    }

    public ArrayList<VKMessage> getFwdMessages() {
        return fwdMessages;
    }

    public void setFwdMessages(ArrayList<VKMessage> fwdMessages) {
        this.fwdMessages = fwdMessages;
    }
}
