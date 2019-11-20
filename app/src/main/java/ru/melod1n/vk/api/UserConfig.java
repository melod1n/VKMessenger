package ru.melod1n.vk.api;

import android.text.TextUtils;

import ru.melod1n.vk.common.AppGlobal;

public class UserConfig {

    private static final String TOKEN = "token";
    private static final String USER_ID = "user_id";

    public static final String API_ID = "6964679";

    private static String token;
    private static int userId;

    public UserConfig() {
    }

    public UserConfig(String token, int userId) {
        UserConfig.token = token;
        UserConfig.userId = userId;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        UserConfig.token = token;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setUserId(int userId) {
        UserConfig.userId = userId;
    }

    public static void save() {
        AppGlobal.preferences.edit()
                .putString(TOKEN, token)
                .putInt(USER_ID, userId)
                .apply();
    }

    public static void restore() {
        token = AppGlobal.preferences.getString(TOKEN, "");
        userId = AppGlobal.preferences.getInt(USER_ID, -1);
    }

    public static boolean isLoggedIn() {
        return userId > 0 && !TextUtils.isEmpty(token);
    }
}
