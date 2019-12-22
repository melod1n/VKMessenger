package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

public class VKCall extends VKModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int initiatorId;
    private int receiverId;
    private String state;
    private int time;
    private int duration;

    public VKCall(JSONObject o) {
        setInitiatorId(o.optInt("initiator_id", -1));
        setReceiverId(o.optInt("receiver_id", -1));
        setState(o.optString("state")); //reached, canceled_by_initiator, canceled_by_receiver
        setTime(o.optInt("time"));
        setDuration(o.optInt("duration"));
    }

    public int getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(int initiatorId) {
        this.initiatorId = initiatorId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

}
