package ru.melod1n.vk.common;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Handler;

import androidx.preference.PreferenceManager;

import java.util.Locale;

import ru.melod1n.vk.api.UserConfig;
import ru.melod1n.vk.database.DatabaseHelper;

public class AppGlobal extends Application {

    public static volatile SharedPreferences preferences;
    public static volatile Locale locale;
    public static volatile Handler handler;
    public static volatile Resources resources;
    public static volatile SQLiteDatabase database;

    public static volatile ConnectivityManager connectivityManager;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        handler = new Handler(getMainLooper());
        locale = Locale.getDefault();
        resources = getResources();
        database = DatabaseHelper.getInstance(this).getWritableDatabase();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        UserConfig.restore();
    }
}