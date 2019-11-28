package ru.melod1n.vk.database;

import android.util.SparseArray;

import java.util.ArrayList;

import ru.melod1n.vk.api.model.VKGroup;
import ru.melod1n.vk.api.model.VKUser;

public class MemoryCache {

    private static SparseArray<VKUser> users = new SparseArray<>(30);
    private static SparseArray<VKGroup> groups = new SparseArray<>(30);

    public static VKUser getUser(int id) {
        VKUser user = users.get(id);
        if (user == null) {
            user = CacheStorage.getUser(id);
            if (user != null) {
                append(user);
            }
        }
        return user;
    }

    public static VKGroup getGroup(int id) {
        VKGroup group = groups.get(id);
        if (group == null) {
            group = CacheStorage.getGroup(id);
            if (group != null) {
                append(group);
            }
        }
        return group;
    }

    public static void update(ArrayList<VKUser> users) {
        for (VKUser user : users) {
            append(user);
        }
    }

    public static void append(VKGroup value) {
        groups.append(value.getId(), value);
    }

    public static void append(VKUser value) {
        users.append(value.getId(), value);
    }

    public static void clear() {
        users.clear();
        groups.clear();
    }

}
