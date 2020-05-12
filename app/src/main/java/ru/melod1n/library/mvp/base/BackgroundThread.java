package ru.melod1n.library.mvp.base;

import android.os.Process;

import androidx.annotation.Nullable;

public class BackgroundThread extends Thread {

    public BackgroundThread() {
    }

    public BackgroundThread(@Nullable Runnable target) {
        super(target);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        super.run();
    }
}
