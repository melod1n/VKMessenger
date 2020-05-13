package ru.melod1n.library.mvp.base;

import android.app.Application;
import android.os.Handler;

public class MvpBase {

    public static Handler handler;

    public static void init(Application application) {
        handler = new Handler(application.getMainLooper());
    }

    public static void init(Handler appHandler) {
        handler = appHandler;
    }

}
