package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

public class VKLongPollServer extends VKModel implements Serializable {
    private static final long serialVersionUID = 1L;

    public String key;
    public String server;
    public long ts;

    public VKLongPollServer(JSONObject source) {
        this.key = source.optString("key");
        this.server = source.optString("server").replace("\\", "");
        this.ts = source.optLong("ts");
    }
}