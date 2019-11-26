package ru.melod1n.vk.util;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ru.melod1n.vk.common.AppGlobal;
import ru.melod1n.vk.io.BytesOutputStream;

public class Util {

    public static byte[] serialize(Object source) {
        try {
            BytesOutputStream bos = new BytesOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);

            out.writeObject(source);
            out.close();
            return bos.getByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object deserialize(byte[] source) {
        if (ArrayUtil.isEmpty(source)) {
            return null;
        }

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(source);
            ObjectInputStream in = new ObjectInputStream(bis);

            Object o = in.readObject();

            in.close();
            return o;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean hasConnection() {
        ConnectivityManager manager = AppGlobal.connectivityManager;

        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

}
