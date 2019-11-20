package ru.melod1n.vk.common;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import ru.melod1n.vk.api.UserConfig;

public class AppGlobal extends Application {

    public static volatile SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        UserConfig.restore();
    }

}
