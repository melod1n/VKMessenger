package ru.melod1n.vk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.model.VKLongPollServer;
import ru.melod1n.vk.api.LongPollParser;
import ru.melod1n.vk.concurrent.LowThread;
import ru.melod1n.vk.net.HttpRequest;
import ru.melod1n.vk.util.Util;

public class LongPollService extends Service {

    private static final String TAG = "LongPollService";

    private Thread thread;
    private boolean running;

    @Override
    public void onCreate() {
        super.onCreate();

        running = false;
        thread = new LowThread(new Updater());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((flags & START_FLAG_RETRY) == 0) {
            Log.w(TAG, "Retry launch!");
        } else {
            Log.d(TAG, "Simple launch");
        }

        if (running) return START_STICKY;

        running = true;

        try {
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        running = false;
        thread.interrupt();
    }

    private class Updater implements Runnable {
        @Override
        public void run() {
            VKLongPollServer server = null;
            while (running) {
                if (!Util.hasConnection()) {

                    try {
                        Thread.sleep(5_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                try {
                    if (server == null) {
                        server = VKApi.messages().getLongPollServer().execute(VKLongPollServer.class).get(0);
                    }

                    JSONObject response = getResponse(server);
                    if (response == null || response.has("failed")) {
                        Log.w(TAG, "Failed get response from");
                        Thread.sleep(1_000);
                        server = null;
                        continue;
                    }

                    long tsResponse = response.optLong("ts");
                    JSONArray updates = response.getJSONArray("updates");

                    Log.i(TAG, "updates: " + updates);

                    server.ts = tsResponse;
                    if (updates.length() != 0) {
                        process(updates);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(5_000);
                        server = null;
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

            }
        }

        private JSONObject getResponse(@NonNull VKLongPollServer server) throws Exception {
            ArrayMap<String, String> params = new ArrayMap<>();
            params.put("act", "a_check");
            params.put("key", server.key);
            params.put("ts", String.valueOf(server.ts));
            params.put("wait", "10");
            params.put("mode", "490");
            params.put("version", "9");

            String buffer = HttpRequest.get("https://" + server.server, params).asString();
            return buffer == null ? null : new JSONObject(buffer);
        }


        private void process(JSONArray updates) {
            LongPollParser.getInstance().parse(updates);
        }

    }
}
