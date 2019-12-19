package ru.melod1n.vk.common;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.method.MethodSetter;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKGroup;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.concurrent.LowThread;
import ru.melod1n.vk.database.CacheStorage;

public class TaskManager {

    private static final String TAG = "TaskManager";

    private static ArrayList<Integer> currentTasksIds = new ArrayList<>();

    public static void execute(Runnable runnable) {
        new LowThread(runnable).start();
    }

    private static <T> void addProcedure(MethodSetter methodSetter, Class<T> className, EventInfo pushInfo, VKApi.OnResponseListener<T> onResponseListener) {
        execute(() -> methodSetter.execute(className, new VKApi.OnResponseListener<T>() {
            @Override
            public void onSuccess(ArrayList<T> models) {
                if (onResponseListener != null)
                    onResponseListener.onSuccess(models);

                if (pushInfo != null) {
                    EventBus.getDefault().postSticky(pushInfo);
                }
            }

            @Override
            public void onError(Exception e) {
                if (onResponseListener != null)
                    onResponseListener.onError(e);
            }
        }));
    }

    public static void loadUser(Integer userId) {
        Log.i(TAG, "loadUser: " + userId);

        if (currentTasksIds.contains(userId)) return;
        currentTasksIds.add(userId);

        MethodSetter setter = VKApi.users().get().userId(userId).fields(VKUser.DEFAULT_FIELDS);

        addProcedure(setter, VKUser.class, new EventInfo(EventInfo.USER_UPDATE, userId), new VKApi.OnResponseListener<VKUser>() {
            @Override
            public void onSuccess(ArrayList<VKUser> models) {
                currentTasksIds.remove(userId);

                CacheStorage.insertUser(models.get(0));
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "User not loaded. Stack: " + Log.getStackTraceString(e));
            }
        });
    }

    public static void loadGroup(Integer groupId) {
        groupId = Math.abs(groupId);

        Log.i(TAG, "loadGroup: " + groupId);

        if (currentTasksIds.contains(groupId)) return;
        currentTasksIds.add(groupId);

        MethodSetter setter = VKApi.groups().getById().groupId(groupId).fields(VKGroup.DEFAULT_FIELDS);

        Integer finalGroupId = groupId;
        addProcedure(setter, VKGroup.class, new EventInfo(EventInfo.GROUP_UPDATE, groupId), new VKApi.OnResponseListener<VKGroup>() {
            @Override
            public void onSuccess(ArrayList<VKGroup> models) {
                currentTasksIds.remove(finalGroupId);

                CacheStorage.insertGroup(models.get(0));
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Group not loaded. Stack: " + Log.getStackTraceString(e));
            }
        });
    }

    public static void loadMessage(Integer messageId, VKApi.OnResponseListener<VKMessage> listener) {
        Log.i(TAG, "loadMessage: " + messageId);

        if (currentTasksIds.contains(messageId)) return;
        currentTasksIds.add(messageId);

        MethodSetter setter = VKApi.messages().getById().messageIds(messageId).extended(true).fields(VKUser.DEFAULT_FIELDS + "," + VKGroup.DEFAULT_FIELDS);
        addProcedure(setter, VKMessage.class, new EventInfo(EventInfo.MESSAGE_UPDATE, messageId), new VKApi.OnResponseListener<VKMessage>() {
            @Override
            public void onSuccess(ArrayList<VKMessage> models) {
                currentTasksIds.remove(messageId);

                CacheStorage.insertMessage(models.get(0));

                if (listener != null)
                    listener.onSuccess(models);
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Message not loaded. Stack: " + Log.getStackTraceString(e));

                if (listener != null)
                    listener.onError(e);
            }
        });
    }

    public static void loadMessage(Integer messageId) {
        loadMessage(messageId, null);
    }

    public static void loadConversation(Integer peerId, VKApi.OnResponseListener<VKConversation> listener) {
        Log.i(TAG, "loadConversation: " + peerId);

        if (currentTasksIds.contains(peerId)) return;
        currentTasksIds.add(peerId);

        MethodSetter setter = VKApi.messages().getConversationsById().peerIds(peerId).extended(true).fields(VKUser.DEFAULT_FIELDS + "," + VKGroup.DEFAULT_FIELDS);
        addProcedure(setter, VKConversation.class, new EventInfo(EventInfo.CONVERSATION_UPDATE, peerId), new VKApi.OnResponseListener<VKConversation>() {
            @Override
            public void onSuccess(ArrayList<VKConversation> models) {
                currentTasksIds.remove(peerId);

                CacheStorage.insertConversation(models.get(0));

                if (listener != null)
                    listener.onSuccess(models);
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Conversation not loaded. Stack: " + Log.getStackTraceString(e));

                if (listener != null)
                    listener.onError(e);
            }
        });
    }

    public static void loadConversation(Integer peerId) {
        loadConversation(peerId, null);
    }
}
