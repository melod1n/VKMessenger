package ru.melod1n.vk.api;

import androidx.annotation.Nullable;

import java.io.IOException;

public class VKException extends IOException {
    private String url;
    private String message;
    private int code;
    private String captchaSid;
    private String captchaImg;
    private String redirectUri;

    public VKException(String url, String message, int code) {
        super(message);
        this.url = url;
        this.message = message;
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public String getCaptchaSid() {
        return captchaSid;
    }

    public String getCaptchaImg() {
        return captchaImg;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setCaptchaSid(String captchaSid) {
        this.captchaSid = captchaSid;
    }

    public void setCaptchaImg(String captchaImg) {
        this.captchaImg = captchaImg;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public String toString() {
        return "code: " + code + ", message: " + message;
    }
}