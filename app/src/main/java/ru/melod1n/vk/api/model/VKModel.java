package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

public abstract class VKModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private Object tag;

    protected VKModel() {
    }

    protected VKModel(JSONObject o) {
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }
}