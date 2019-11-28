package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKAudioMessage extends VKModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int duration;
    private ArrayList<Integer> waveform;
    private String linkOgg;
    private String linkMp3;

    public VKAudioMessage(JSONObject o) {
        setDuration(o.optInt("duration"));

        JSONArray oWaveform = o.optJSONArray("waveform");
        if (oWaveform != null) {
            ArrayList<Integer> waveform = new ArrayList<>();
            for (int i = 0; i < oWaveform.length(); i++) {
                waveform.add(oWaveform.optInt(i));
            }

            setWaveform(waveform);
        }

        setLinkOgg(o.optString("link_ogg"));
        setLinkMp3(o.optString("link_mp3"));
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ArrayList<Integer> getWaveform() {
        return waveform;
    }

    public void setWaveform(ArrayList<Integer> waveform) {
        this.waveform = waveform;
    }

    public String getLinkOgg() {
        return linkOgg;
    }

    public void setLinkOgg(String linkOgg) {
        this.linkOgg = linkOgg;
    }

    public String getLinkMp3() {
        return linkMp3;
    }

    public void setLinkMp3(String linkMp3) {
        this.linkMp3 = linkMp3;
    }
}