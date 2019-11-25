package ru.melod1n.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

public class VKLink extends VKModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String url;
    private String title;
    private String caption;
    private String description;
    private VKPhoto photo;
    private Button button;
    private String previewPage;
    private String previewUrl;

    public VKLink(JSONObject o) {
        setUrl(o.optString("url"));
        setTitle(o.optString("title"));
        setCaption(o.optString("caption"));
        setDescription(o.optString("description"));

        JSONObject oPhoto = o.optJSONObject("photo");
        if (oPhoto != null) {
            setPhoto(new VKPhoto(oPhoto));
        }

        JSONObject oButton = o.optJSONObject("button");
        if (oButton != null) {
            setButton(new Button(oButton));
        }

        setPreviewPage(o.optString("preview_page"));
        setPreviewUrl(o.optString("preview_url"));
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VKPhoto getPhoto() {
        return photo;
    }

    public void setPhoto(VKPhoto photo) {
        this.photo = photo;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public String getPreviewPage() {
        return previewPage;
    }

    public void setPreviewPage(String previewPage) {
        this.previewPage = previewPage;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    private class Button implements Serializable {

        private static final long serialVersionUID = 1L;

        private String title;
        private Action action;

        public Button(JSONObject o) {
            setTitle(o.optString("title"));

            JSONObject oAction = o.optJSONObject("action");
            if (oAction != null) {
                setAction(new Action(oAction));
            }
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Action getAction() {
            return action;
        }

        public void setAction(Action action) {
            this.action = action;
        }

        private class Action implements Serializable {

            private static final long serialVersionUID = 1L;

            private String type;
            private String url;

            public Action(JSONObject o) {
                setType(o.optString("type"));
                setUrl(o.optString("url"));
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}