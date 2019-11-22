package ru.melod1n.vk.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.melod1n.vk.BuildConfig;
import ru.melod1n.vk.adapter.model.VKDialog;
import ru.melod1n.vk.api.method.MessageMethodSetter;
import ru.melod1n.vk.api.method.MethodSetter;
import ru.melod1n.vk.api.method.UserMethodSetter;
import ru.melod1n.vk.api.model.VKAttachments;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKGroup;
import ru.melod1n.vk.api.model.VKLongPollServer;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.VKModel;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.common.AppGlobal;
import ru.melod1n.vk.concurrent.TaskManager;
import ru.melod1n.vk.net.HttpRequest;
import ru.melod1n.vk.util.ArrayUtil;

public class VKApi {
    private static final String TAG = "Messenger.VKApi";

    public static final String BASE_URL = "https://api.vk.com/method/";
    public static final String API_VERSION = "5.103";
    public static final String LANGUAGE = AppGlobal.locale.getLanguage();

    public static <T> ArrayList<T> execute(String url, Class<T> cls) throws Exception {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "url: " + url);
        }

        String buffer = HttpRequest.get(url).asString();

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "json: " + buffer);
        }

        JSONObject json = new JSONObject(buffer);

        try {
            checkError(json, url);
        } catch (VKException ex) {
            if (ex.getCode() == ErrorCodes.TOO_MANY_REQUESTS) {
                return execute(url, cls);
            } else throw ex;
        }

        if (cls == null) {
            return null;
        }

        if (cls == VKLongPollServer.class) {
            VKLongPollServer server = new VKLongPollServer(json.optJSONObject("response"));
            return (ArrayList<T>) ArrayUtil.singletonList(server);
        }

        if (cls == Boolean.class) {
            boolean value = json.optInt("response") == 1;
            return (ArrayList<T>) ArrayUtil.singletonList(value);
        }

        if (cls == Long.class) {
            long value = json.optLong("response");
            return (ArrayList<T>) ArrayUtil.singletonList(value);
        }

        if (cls == Integer.class) {
            int value = json.optInt("response");
            return (ArrayList<T>) ArrayUtil.singletonList(value);
        }

        JSONArray array = optItems(json);
        ArrayList<T> models = new ArrayList<>(array.length());

        if (cls == VKUser.class) {
            for (int i = 0; i < array.length(); i++) {
                models.add((T) new VKUser(array.optJSONObject(i)));
            }
        } else if (cls == VKMessage.class) {
            if (url.contains("messages.getHistory")) {
                VKMessage.lastHistoryCount = json.optJSONObject("response").optInt("count");
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject source = array.optJSONObject(i);
                int unread = source.optInt("unread");
                if (source.has("message")) {
                    source = source.optJSONObject("message");
                }
                VKMessage message = new VKMessage(source);
                models.add((T) message);
            }
        } else if (cls == VKGroup.class) {
            for (int i = 0; i < array.length(); i++) {
                models.add((T) new VKGroup(array.optJSONObject(i)));
            }
        } else if (cls == VKModel.class && url.contains("messages.getHistoryAttachments")) {
            return (ArrayList<T>) VKAttachments.parse(array);
        } else if (cls == VKDialog.class) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject source = array.optJSONObject(i);

                JSONObject oConversation = source.optJSONObject("conversation");
                JSONObject oLastMessage = source.optJSONObject("last_message");

                VKConversation conversation = new VKConversation(oConversation);
                VKMessage lastMessage = new VKMessage(oLastMessage);

                VKDialog dialog = new VKDialog();
                dialog.setConversation(conversation);
                dialog.setLastMessage(lastMessage);

                models.add((T) dialog);
            }
        }
        return models;
    }

    public static <E> void execute(final String url, final Class<E> cls, final OnResponseListener<E> listener) {
        TaskManager.execute(() -> {
            try {
                ArrayList<E> models = execute(url, cls);
                if (listener != null) {
                    AppGlobal.handler.post(new SuccessCallback<>(listener, models));
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (listener != null) {
                    AppGlobal.handler.post(new ErrorCallback(listener, e));
                }
            }
        });
    }

    private static JSONArray optItems(JSONObject source) {
        Object response = source.opt("response");
        if (response instanceof JSONArray) {
            return (JSONArray) response;
        }

        if (response instanceof JSONObject) {
            JSONObject json = (JSONObject) response;
            return json.optJSONArray("items");
        }

        return null;
    }

    private static void checkError(JSONObject json, String url) throws VKException {
        if (json.has("error")) {
            JSONObject error = json.optJSONObject("error");

            int code = error.optInt("error_code");
            String message = error.optString("error_msg");

            VKException e = new VKException(url, message, code);
            if (code == ErrorCodes.CAPTCHA_NEEDED) {
                e.setCaptchaImg(error.optString("captcha_img"));
                e.setCaptchaSid(error.optString("captcha_sid"));
            }
            if (code == ErrorCodes.VALIDATION_REQUIRED) {
                e.setRedirectUri(error.optString("redirect_uri"));
            }
            throw e;
        }
    }

    public static VKUsers users() {
        return new VKUsers();
    }

    public static VKFriends friends() {
        return new VKFriends();
    }

    public static VKMessages messages() {
        return new VKMessages();
    }

    public static VKGroups groups() {
        return new VKGroups();
    }

    public static VKAccounts account() {
        return new VKAccounts();
    }

    public static class VKFriends {
        private VKFriends() {

        }

        public MethodSetter get() {
            return new MethodSetter("friends.get");
        }
    }

    public static class VKUsers {
        private VKUsers() {

        }

        public UserMethodSetter get() {
            return new UserMethodSetter("users.get");
        }
    }

    public static class VKMessages {
        private VKMessages() {

        }

        public MessageMethodSetter get() {
            return new MessageMethodSetter("messages.get");
        }

        public MessageMethodSetter getConversations() {
            return new MessageMethodSetter("messages.getConversations");
        }

        public MessageMethodSetter getById() {
            return new MessageMethodSetter("messages.getById");
        }

        public MessageMethodSetter search() {
            return new MessageMethodSetter("messages.search");
        }

        public MessageMethodSetter getHistory() {
            return new MessageMethodSetter("messages.getHistory");
        }

        public MessageMethodSetter getHistoryAttachments() {
            return new MessageMethodSetter("messages.getHistoryAttachments");
        }

        public MessageMethodSetter send() {
            return new MessageMethodSetter("messages.send");
        }

        public MessageMethodSetter sendSticker() {
            return new MessageMethodSetter("messages.sendSticker");
        }

        public MessageMethodSetter delete() {
            return new MessageMethodSetter("messages.delete");
        }

        public MessageMethodSetter deleteDialog() {
            return new MessageMethodSetter("messages.deleteDialog");
        }

        public MessageMethodSetter restore() {
            return new MessageMethodSetter("messages.restore");
        }

        public MessageMethodSetter markAsRead() {
            return new MessageMethodSetter("messages.markAsRead");
        }

        public MessageMethodSetter markAsImportant() {
            return new MessageMethodSetter("messages.markAsImportant");
        }

        public MessageMethodSetter getLongPollServer() {
            return new MessageMethodSetter("messages.getLongPollServer");
        }

        /**
         * Returns updates in user's private messages.
         * To speed up handling of private messages,
         * it can be useful to cache previously loaded messages on
         * a user's mobile device/desktop, to prevent re-receipt at each call.
         * With this method, you can synchronize a local copy of
         * the message list with the actual version.
         * <p/>
         * Result:
         * Returns an object that contains the following fields:
         * 1 — history:     An array similar to updates field returned
         * from the Long Poll server,
         * with these exceptions:
         * - For events with code 4 (addition of a new message),
         * there are no fields except the first three.
         * - There are no events with codes 8, 9 (friend goes online/offline)
         * or with codes 61, 62 (typing during conversation/chat).
         * <p/>
         * 2 — messages:    An array of private message objects that were found
         * among events with code 4 (addition of a new message)
         * from the history field.
         * Each object of message contains a set of fields described here.
         * The first array element is the total number of messages
         */
        public MessageMethodSetter getLongPollHistory() {
            return new MessageMethodSetter(("messages.getLongPollHistory"));
        }

        public MessageMethodSetter getChat() {
            return new MessageMethodSetter("messages.getChat");
        }

        public MessageMethodSetter createChat() {
            return new MessageMethodSetter("messages.createChat");
        }

        public MessageMethodSetter editChat() {
            return new MessageMethodSetter("messages.editChat");
        }

        public MessageMethodSetter getChatUsers() {
            return new MessageMethodSetter("messages.getChatUsers");
        }

        public MessageMethodSetter setActivity() {
            return new MessageMethodSetter("messages.setActivity").type(true);
        }

        public MessageMethodSetter addChatUser() {
            return new MessageMethodSetter("messages.addChatUser");
        }

        public MessageMethodSetter removeChatUser() {
            return new MessageMethodSetter("messages.removeChatUser");
        }
    }

    public static class VKGroups {
        public MethodSetter getById() {
            return new MethodSetter("groups.getById");
        }

        public MethodSetter join() {
            return new MethodSetter("groups.join");
        }
    }

    public static class VKAccounts {

        public MethodSetter setOffline() {
            return new MethodSetter("account.setOffline");
        }


        public MethodSetter setOnline() {
            return new MethodSetter("account.setOnline");
        }
    }

    public interface OnResponseListener<E> {
        void onSuccess(ArrayList<E> models);

        void onError(Exception ex);
    }

    private static class SuccessCallback<E> implements Runnable {
        private ArrayList<E> models;
        private OnResponseListener<E> listener;

        public SuccessCallback(OnResponseListener<E> listener, ArrayList<E> models) {
            this.models = models;
            this.listener = listener;
        }

        @Override
        public void run() {
            if (listener == null) {
                return;
            }

            listener.onSuccess(models);
        }
    }

    private static class ErrorCallback implements Runnable {
        private OnResponseListener listener;
        private Exception ex;

        public ErrorCallback(OnResponseListener listener, Exception ex) {
            this.listener = listener;
            this.ex = ex;
        }

        @Override
        public void run() {
            if (listener == null) {
                return;
            }

            listener.onError(ex);
        }
    }
}
