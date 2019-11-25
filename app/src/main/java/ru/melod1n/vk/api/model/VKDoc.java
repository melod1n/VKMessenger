package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKDoc extends VKModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int TYPE_NONE = 0;
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_ARCHIVE = 2;
    public static final int TYPE_GIF = 3;
    public static final int TYPE_IMAGE = 4;
    public static final int TYPE_AUDIO = 5;
    public static final int TYPE_VIDEO = 6;
    public static final int TYPE_BOOK = 7;
    public static final int TYPE_UNKNOWN = 8;

    private int id;
    private int ownerId;
    private String title;
    private int size;
    private String ext;
    private String url;
    private int date;
    private int type;
    private Preview preview;

    public VKDoc(JSONObject o) {
        setId(o.optInt("id", -1));
        setOwnerId(o.optInt("owner_id", -1));
        setTitle(o.optString("title"));
        setSize(o.optInt("size"));
        setExt(o.optString("ext"));
        setUrl(o.optString("url"));
        setDate(o.optInt("date"));
        setType(o.optInt("type"));

        JSONObject oPreview = o.optJSONObject("preview");
        if (oPreview != null) {
            setPreview(new Preview(oPreview));
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Preview getPreview() {
        return preview;
    }

    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    private class Preview implements Serializable {

        private static final long serialVersionUID = 1L;

        private Photo photo;
        private Graffiti graffiti;
        private AudioMessage audioMessage;

        public Preview(JSONObject o) {
            JSONObject oPhoto = o.optJSONObject("photo");
            if (oPhoto != null) {
                setPhoto(new Photo(oPhoto));
            }

            JSONObject oGraffiti = o.optJSONObject("graffiti");
            if (oGraffiti != null) {
                setGraffiti(new Graffiti(oGraffiti));
            }

            JSONObject oAudioMessage = o.optJSONObject("audio_message");
            if (oAudioMessage != null) {
                setAudioMessage(new AudioMessage(oAudioMessage));
            }
        }

        public Photo getPhoto() {
            return photo;
        }

        public void setPhoto(Photo photo) {
            this.photo = photo;
        }

        public Graffiti getGraffiti() {
            return graffiti;
        }

        public void setGraffiti(Graffiti graffiti) {
            this.graffiti = graffiti;
        }

        public AudioMessage getAudioMessage() {
            return audioMessage;
        }

        public void setAudioMessage(AudioMessage audioMessage) {
            this.audioMessage = audioMessage;
        }

        private class Photo implements Serializable {

            private static final long serialVersionUID = 1L;

            private ArrayList<VKPhotoSize> sizes;

            public Photo(JSONObject o) {
                JSONArray oSizes = o.optJSONArray("sizes");
                if (oSizes != null) {
                    ArrayList<VKPhotoSize> sizes = new ArrayList<>();
                    for (int i = 0; i < oSizes.length(); i++) {
                        sizes.add(new VKPhotoSize(oSizes.optJSONObject(i)));
                    }

                    setSizes(sizes);
                }
            }

            public ArrayList<VKPhotoSize> getSizes() {
                return sizes;
            }

            public void setSizes(ArrayList<VKPhotoSize> sizes) {
                this.sizes = sizes;
            }
        }

        private class Graffiti implements Serializable {

            private static final long serialVersionUID = 1L;

            private String src;
            private int width;
            private int height;

            public Graffiti(JSONObject o) {
                setSrc(o.optString("src"));
                setWidth(o.optInt("width"));
                setHeight(o.optInt("height"));
            }

            public String getSrc() {
                return src;
            }

            public void setSrc(String src) {
                this.src = src;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }
        }

        private class AudioMessage implements Serializable {

            private static final long serialVersionUID = 1L;

            private int duration;
            private ArrayList<Integer> waveform;
            private String linkOgg;
            private String linkMp3;

            public AudioMessage(JSONObject o) {
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
    }
}