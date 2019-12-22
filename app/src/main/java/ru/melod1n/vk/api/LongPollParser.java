package ru.melod1n.vk.api;

import android.util.Log;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.common.EventInfo;
import ru.melod1n.vk.common.TaskManager;

public class LongPollParser {

    private static final String TAG = "LongPollParser";

    public static LongPollParser getInstance() {
        return new LongPollParser();
    }

    public void parse(@NonNull JSONArray updates) {
        if (updates.length() == 0) {
            return;
        }

        for (int i = 0; i < updates.length(); i++) {
            JSONArray item = updates.optJSONArray(i);
            int type = item.optInt(0);

            switch (type) {
                case 2: //установка флагов
                    messageSetFlags(item);
                    break;
                case 3: //сброс флагов
                    messageClearFlags(item);
                    break;
                case 4: //новое сообщение
                    messageEvent(item);
                    break;
                case 5: //редактирование сообщения
                    messageEdit(item);
                    break;
            }
        }
    }

    private void messageEvent(JSONArray item) {
        VKMessage message = VKMessage.parse(item);

        TaskManager.loadMessage(message.getId());

        if (VKConversation.isChatId(message.getPeerId())) {
            TaskManager.loadConversation(message.getPeerId());
        }

        sendEvent(new EventInfo(EventInfo.MESSAGE_NEW, message), true);
    }

    private void messageEdit(JSONArray item) {
        VKMessage message = VKMessage.parse(item);
        sendEvent(new EventInfo(EventInfo.MESSAGE_EDIT, message), true);
    }

    private void messageDelete(JSONArray item) {
        int messageId = item.optInt(1);
        int peerId = item.optInt(3);

        sendEvent(new EventInfo(EventInfo.MESSAGE_DELETE, new Object[]{messageId, peerId}), true);
    }

    private void messageRestored(JSONArray item) {
        VKMessage message = VKMessage.parse(item);

        sendEvent(new EventInfo(EventInfo.MESSAGE_RESTORE, message), true);
    }

    private void messageRead(JSONArray item) {
        int messageId = item.optInt(1);
        int peerId = item.optInt(3);

        sendEvent(new EventInfo(EventInfo.MESSAGE_READ, new int[]{messageId, peerId}), true);
    }

    private void messageClearFlags(JSONArray item) {
        int id = item.optInt(1);
        int flags = item.optInt(2);

        if (VKMessage.hasFlag(flags, "cancel_spam")) {
            Log.i(TAG, "Message with id " + id + ": Not spam");
        }

        if (VKMessage.hasFlag(flags, "deleted")) {
            messageRestored(item);
        }

        if (VKMessage.hasFlag(flags, "important")) {
            Log.i(TAG, "Message with id " + id + ": Not Important");
        }

        if (VKMessage.hasFlag(flags, "unread")) {
            messageRead(item);
        }
    }

    private void messageSetFlags(JSONArray item) {
        int id = item.optInt(1);
        int flags = item.optInt(2);

        if (VKMessage.hasFlag(flags, "delete_for_all")) {
            messageDelete(item);
        }

        if (VKMessage.hasFlag(flags, "deleted")) {
            messageDelete(item);
        }

        if (VKMessage.hasFlag(flags, "spam")) {
            Log.i(TAG, "Message with id " + id + ": Spam");
        }

        if (VKMessage.hasFlag(flags, "important")) {
            Log.i(TAG, "Message with id " + id + ": Important");
        }
    }

    private void sendEvent(EventInfo info, boolean sticky) {
        if (sticky) {
            EventBus.getDefault().postSticky(info);
        } else {
            EventBus.getDefault().post(info);
        }
    }

    private void sendEvent(EventInfo info) {
        sendEvent(info, false);
    }
}
