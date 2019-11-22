package ru.melod1n.vk.common;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;

import androidx.preference.PreferenceManager;

import java.util.Locale;

import ru.melod1n.vk.api.UserConfig;

public class AppGlobal extends Application {

    public static volatile SharedPreferences preferences;
    public static volatile Locale locale;
    public static volatile Handler handler;
    public static volatile Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        handler = new Handler(getMainLooper());
        locale = Locale.getDefault();
        resources = getResources();

        UserConfig.restore();
    }
}