package ru.melod1n.vk.api;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ru.melod1n.vk.api.util.VKUtil;

public class VKAuth {

    private static final String TAG = "Kate.VKAuth";
    public static String redirect_url = "https://oauth.vk.com/blank.html";

    public static String getUrl(String api_id, String settings) {
        try {
            return "https://oauth.vk.com/authorize?client_id=" + api_id + "&display=mobile&scope=" + settings + "&redirect_uri=" + URLEncoder.encode(redirect_url, "utf-8") + "&response_type=token"
                    + "&v=" + URLEncoder.encode(VKApi.API_VERSION, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSettings() {
        return "notify,friends,photos,audio,video,docs,status,notes,pages,wall,groups,messages,offline,notifications";
    }

    public static String[] parseRedirectUrl(String url) throws Exception {
        String access_token = VKUtil.extractPattern(url, "access_token=(.*?)&");
        Log.i(TAG, "access_token=" + access_token);
        String user_id = VKUtil.extractPattern(url, "user_id=(\\d*)");
        Log.i(TAG, "user_id=" + user_id);
        if (user_id == null || user_id.length() == 0 || access_token == null || access_token.length() == 0)
            throw new Exception("Failed to parse redirect url " + url);
        return new String[]{access_token, user_id};
    }
}
