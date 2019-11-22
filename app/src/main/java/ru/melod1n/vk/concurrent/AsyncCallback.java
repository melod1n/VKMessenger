package ru.melod1n.vk.concurrent;

import android.app.Activity;

import java.lang.ref.WeakReference;

public abstract class AsyncCallback implements Runnable {

    private WeakReference<Activity> reference;

    public AsyncCallback(Activity activity) {
        reference = new WeakReference<>(activity);
    }

    public abstract void ready() throws Exception;

    public abstract void done();

    public abstract void error(Exception e);

    @Override
    public void run() {
        try {
            ready();
        } catch (Exception e) {
            e.printStackTrace();

            if (reference != null && reference.get() != null) {
                reference.get().runOnUiThread(() -> error(e));
            }

            return;
        }

        if (reference != null && reference.get() != null) {
            reference.get().runOnUiThread(this::done);
        }
    }
}