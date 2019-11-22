package ru.melod1n.vk.concurrent;

import ru.melod1n.vk.common.AppGlobal;

public abstract class TryCallback implements Runnable {

    public abstract void ready() throws Exception;

    public abstract void done();

    public abstract void error(Exception e);

    @Override
    public void run() {
        try {
            ready();
        } catch (Exception e) {
            e.printStackTrace();

            AppGlobal.handler.post(() -> error(e));
            return;
        }

        AppGlobal.handler.post(this::done);
    }
}
