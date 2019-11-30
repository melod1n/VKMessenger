package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

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

    public ArrayList<? extends VKModel> asList() {
        return new ArrayList<>(Collections.singletonList(this));
    }
}