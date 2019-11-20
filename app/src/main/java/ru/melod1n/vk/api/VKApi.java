package ru.melod1n.vk.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLException;

import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.api.model.attachment.VKAudio;
import ru.melod1n.vk.api.model.attachment.VKPhoto;
import ru.melod1n.vk.api.util.VKUtil;
import ru.melod1n.vk.api.util.WrongResponseCodeException;

public class VKApi {
    private static final String TAG = "Kate.VKApi";

    private static final String BASE_URL = "https://api.vk.com/method/";
    static final String API_VERSION = "5.103";


    /*** utils methods***/
    private static void checkError(JSONObject root, String url) throws JSONException, VKException {
        if (!root.isNull("error")) {
            JSONObject error = root.getJSONObject("error");
            int code = error.getInt("error_code");
            String message = error.getString("error_msg");
            VKException e = new VKException(code, message, url);
            if (code == 14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            if (code == 17)
                e.redirect_uri = error.optString("redirect_uri");
            throw e;
        }
        if (!root.isNull("execute_errors")) {
            JSONArray errors = root.getJSONArray("execute_errors");
            if (errors.length() == 0)
                return;
            //only first error is processed if there are multiple
            JSONObject error = errors.getJSONObject(0);
            int code = error.getInt("error_code");
            String message = error.getString("error_msg");
            VKException e = new VKException(code, message, url);
            if (code == 14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            if (code == 17)
                e.redirect_uri = error.optString("redirect_uri");
            throw e;
        }
    }

    private static JSONObject sendRequest(VKParams params) throws IOException, JSONException, VKException {
        return sendRequest(params, false);
    }

    private final static int MAX_TRIES = 3;

    private static JSONObject sendRequest(VKParams params, boolean is_post) throws IOException, JSONException, VKException {
        String url = getSignedUrl(params, is_post);
        String body = "";
        if (is_post)
            body = params.getParamsString();
        Log.i(TAG, "url=" + url);
        if (body.length() != 0)
            Log.i(TAG, "body=" + body);
        String response = "";
        for (int i = 1; i <= MAX_TRIES; ++i) {
            try {
                if (i != 1)
                    Log.i(TAG, "try " + i);
                response = sendRequestInternal(url, body, is_post);
                break;
            } catch (SSLException | SocketException ex) {
                processNetworkException(i, ex);
            }
        }
        Log.i(TAG, "response=" + response);
        JSONObject root = new JSONObject(response);
        checkError(root, url);
        return root;
    }

    private static void processNetworkException(int i, IOException ex) throws IOException {
        ex.printStackTrace();
        if (i == MAX_TRIES)
            throw ex;
    }

    private static String sendRequestInternal(String url, String body, boolean is_post) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoOutput(is_post);
            connection.setDoInput(true);
            connection.setRequestMethod(is_post ? "POST" : "GET");
            //TODO: it's not faster, even slower on slow devices. Maybe we should add an option to disable it. It's only good for paid internet connection.
            boolean enable_compression = true;
            if (enable_compression)
                connection.setRequestProperty("Accept-Encoding", "gzip");
            if (is_post)
                connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            int code = connection.getResponseCode();
            Log.i(TAG, "code=" + code);
            //It may happen due to keep-alive problem http://stackoverflow.com/questions/1440957/httpurlconnection-getresponsecode-returns-1-on-second-invocation
            if (code == -1)
                throw new WrongResponseCodeException("Network error");
            //может стоит проверить на код 200
            //on error can also read error stream from connection.
            InputStream is = new BufferedInputStream(connection.getInputStream(), 8192);
            String enc = connection.getHeaderField("Content-Encoding");
            if (enc != null && enc.equalsIgnoreCase("gzip"))
                is = new GZIPInputStream(is);
            return VKUtil.convertStreamToString(is);
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    private static String getSignedUrl(VKParams params, boolean is_post) {
        params.put("access_token", UserConfig.getToken());
        if (!params.contains("v"))
            params.put("v", API_VERSION);

        String args = "";
        if (!is_post)
            args = params.getParamsString();

        return BASE_URL + params.getMethodName() + "?" + args;
    }

    public static String unescape(String text) {
        if (text == null)
            return null;
        return text.replace("&amp;", "&").replace("&quot;", "\"").replace("<br>", "\n").replace("&gt;", ">").replace("&lt;", "<")
                .replace("<br/>", "\n").replace("&ndash;", "-").trim();
        //Баг в API
        //amp встречается в сообщении, br в Ответах тип comment_photo, gt lt на стене - баг API, ndash в статусе когда аудио транслируется
        //quot в тексте сообщения из LongPoll - то есть в уведомлении
    }

    public static String unescapeWithSmiles(String text) {
        return unescape(text)
                //May be useful to someone
                //.replace("\uD83D\uDE0A", ":-)")
                //.replace("\uD83D\uDE03", ":D")
                //.replace("\uD83D\uDE09", ";-)")
                //.replace("\uD83D\uDE06", "xD")
                //.replace("\uD83D\uDE1C", ";P")
                //.replace("\uD83D\uDE0B", ":p")
                //.replace("\uD83D\uDE0D", "8)")
                //.replace("\uD83D\uDE0E", "B)")
                //
                //.replace("\ud83d\ude12", ":(")  //F0 9F 98 92
                //.replace("\ud83d\ude0f", ":]")  //F0 9F 98 8F
                //.replace("\ud83d\ude14", "3(")  //F0 9F 98 94
                //.replace("\ud83d\ude22", ":'(")  //F0 9F 98 A2
                //.replace("\ud83d\ude2d", ":_(")  //F0 9F 98 AD
                //.replace("\ud83d\ude29", ":((")  //F0 9F 98 A9
                //.replace("\ud83d\ude28", ":o")  //F0 9F 98 A8
                //.replace("\ud83d\ude10", ":|")  //F0 9F 98 90
                //                           
                //.replace("\ud83d\ude0c", "3)")  //F0 9F 98 8C
                //.replace("\ud83d\ude20", ">(")  //F0 9F 98 A0
                //.replace("\ud83d\ude21", ">((")  //F0 9F 98 A1
                //.replace("\ud83d\ude07", "O:)")  //F0 9F 98 87
                //.replace("\ud83d\ude30", ";o")  //F0 9F 98 B0
                //.replace("\ud83d\ude32", "8o")  //F0 9F 98 B2
                //.replace("\ud83d\ude33", "8|")  //F0 9F 98 B3
                //.replace("\ud83d\ude37", ":X")  //F0 9F 98 B7
                //                           
                //.replace("\ud83d\ude1a", ":*")  //F0 9F 98 9A
                //.replace("\ud83d\ude08", "}:)")  //F0 9F 98 88
                //.replace("\u2764", "<3")  //E2 9D A4   
                //.replace("\ud83d\udc4d", ":like:")  //F0 9F 91 8D
                //.replace("\ud83d\udc4e", ":dislike:")  //F0 9F 91 8E
                //.replace("\u261d", ":up:")  //E2 98 9D   
                //.replace("\u270c", ":v:")  //E2 9C 8C   
                //.replace("\ud83d\udc4c", ":ok:")  //F0 9F 91 8C
                ;
    }

    /*** API methods ***/


    private <T> String arrayToString(Collection<T> items) {
        if (items == null)
            return null;
        StringBuilder str_cids = new StringBuilder();
        for (Object item : items) {
            if (str_cids.length() != 0)
                str_cids.append(',');
            str_cids.append(item);
        }
        return str_cids.toString();
    }

    private static void addCaptchaParams(String captcha_key, String captcha_sid, VKParams params) {
        params.put("captcha_sid", captcha_sid);
        params.put("captcha_key", captcha_key);
    }

    public static ArrayList<VKConversation> getConversations(String filter, Boolean extended, Integer offset, Integer count, Integer startMessageId) throws JSONException, VKException, IOException {
        VKParams params = VKParams.request("messages.getConversations");
        if (filter != null)
            params.put("filter", filter);
        if (extended != null)
            params.put("extended", extended);
        if (offset != null)
            params.put("offset", offset);
        if (count != null)
            params.put("count", count);
        if (startMessageId != null)
            params.put("start_message_id", startMessageId);

        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray items = response.optJSONArray("items");

        return VKConversation.parse(items);
    }

    //http://vk.com/dev/users.get
    public ArrayList<VKUser> getProfiles(Collection<Long> uids, Collection<String> domains, String fields, String name_case, String captcha_key, String captcha_sid) throws IOException, JSONException, VKException {
        if (uids == null && domains == null)
            return null;
        if ((uids != null && uids.size() == 0) || (domains != null && domains.size() == 0))
            return null;
        VKParams params = new VKParams("users.get");
        if (uids != null && uids.size() > 0)
            params.put("user_ids", arrayToString(uids));
        if (domains != null && domains.size() > 0)
            params.put("user_ids", arrayToString(domains));
        params.put("fields", fields);
        params.put("name_case", name_case);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return VKUser.parseUsers(array);
    }

    /*** methods for friends ***/
    //http://vk.com/dev/friends.get
    public ArrayList<VKUser> getFriends(Long user_id, String fields, Integer lid, String captcha_key, String captcha_sid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("friends.get");
        params.put("fields", fields);
        params.put("user_id", user_id);
        params.put("list_id", lid);

        //сортировка по популярности не даёт запросить друзей из списка

        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        ArrayList<VKUser> users = new ArrayList<VKUser>();
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        //if there are no friends "response" will not be array
        if (array == null)
            return users;
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKUser u = VKUser.parse(o);
            users.add(u);
        }
        return users;
    }

    //http://vk.com/dev/friends.getOnline
    public ArrayList<Long> getOnlineFriends(Long uid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("friends.getOnline");
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Long> users = new ArrayList<Long>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                long id = array.optLong(i, -1);
                if (id != -1)
                    users.add(id);
            }
        }
        return users;
    }

    //http://vk.com/dev/likes.getList
    public ArrayList<Long> getLikeUsers(String item_type, long item_id, long owner_id, String filter) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("likes.getList");
        params.put("type", item_type);
        params.put("owner_id", owner_id);
        params.put("item_id", item_id);
        params.put("filter", filter); //likes - default, copies 
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<Long> users = new ArrayList<Long>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                long id = array.optLong(i, -1);
                if (id != -1)
                    users.add(id);
            }
        }
        return users;
    }

    //http://vk.com/dev/friends.getMutual
    public ArrayList<Long> getMutual(Long target_uid, Long source_uid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("friends.getMutual");
        params.put("target_uid", target_uid);
        params.put("source_uid", source_uid);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Long> users = new ArrayList<Long>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                long id = array.optLong(i, -1);
                if (id != -1)
                    users.add(id);
            }
        }
        return users;
    }

    /*** methods for messages 
     * @throws VKException ***/
    //http://vk.com/dev/messages.get
    public ArrayList<VKMessage> getMessages(long time_offset, boolean is_out, int count) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.get");
        if (is_out)
            params.put("out", "1");
        if (time_offset != 0)
            params.put("time_offset", time_offset);
        if (count != 0)
            params.put("count", count);
        params.put("preview_length", "0");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<VKMessage> messages = parseMessages(array, false, 0, false, 0);
        return messages;
    }

    //http://vk.com/dev/messages.getHistory
    public ArrayList<VKMessage> getMessagesHistory(long uid, long chat_id, long me, Long offset, int count) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.getHistory");
        if (chat_id <= 0)
            params.put("user_id", uid);
        else
            params.put("chat_id", chat_id);
        params.put("offset", offset);
        if (count != 0)
            params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<VKMessage> messages = parseMessages(array, chat_id <= 0, uid, chat_id > 0, me);
        return messages;
    }

    //http://vk.com/dev/messages.getDialogs
    public ArrayList<VKMessage> getMessagesDialogs(long offset, int count, String captcha_key, String captcha_sid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.getDialogs");
        if (offset != 0)
            params.put("offset", offset);
        if (count != 0)
            params.put("count", count);
        params.put("preview_length", "0");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<VKMessage> messages = parseMessages(array, false, 0, false, 0);
        return messages;
    }

    private ArrayList<VKMessage> parseMessages(JSONArray array, boolean from_history, long history_uid, boolean from_chat, long me) throws JSONException {
        ArrayList<VKMessage> messages = new ArrayList<VKMessage>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                JSONObject o = array.getJSONObject(i);
                VKMessage m = VKMessage.parse(o, from_history, history_uid, from_chat, me);
                messages.add(m);
            }
        }
        return messages;
    }

    //http://vk.com/dev/messages.send
    public String sendMessage(Long uid, long chat_id, String message, String title, String type, Collection<String> attachments, ArrayList<Long> forward_messages, String lat, String lon, String captcha_key, String captcha_sid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.send");
        if (chat_id <= 0)
            params.put("user_id", uid);
        else
            params.put("chat_id", chat_id);
        params.put("message", message);
        params.put("title", title);
        params.put("type", type);
        params.put("attachment", arrayToString(attachments));
        params.put("forward_messages", arrayToString(forward_messages));
        params.put("lat", lat);
        params.put("long", lon);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        Object message_id = root.opt("response");
        if (message_id != null)
            return String.valueOf(message_id);
        return null;
    }

    //http://vk.com/dev/messages.delete
    public String deleteMessage(Collection<Long> message_ids) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.delete");
        params.put("message_ids", arrayToString(message_ids));
        sendRequest(params);
        //не парсим ответ - там приходят отдельные флаги для каждого удалённого сообщения
        return null;
    }

    //http://vk.com/dev/status.set
    public String setStatus(String status_text) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("status.set");
        params.put("text", status_text);
        JSONObject root = sendRequest(params);
        Object response_id = root.opt("response");
        if (response_id != null)
            return String.valueOf(response_id);
        return null;
    }


    private ArrayList<VKPhoto> parsePhotos(JSONArray array) throws JSONException {
        ArrayList<VKPhoto> photos = new ArrayList<VKPhoto>();
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKPhoto p = VKPhoto.parse(o);
            photos.add(p);
        }
        return photos;
    }

    private ArrayList<VKAudio> parseAudioList(JSONArray array)
            throws JSONException {
        ArrayList<VKAudio> audios = new ArrayList<VKAudio>();
        if (array != null) {
            for (int i = 0; i < array.length(); ++i) { //get(0) is integer, it is audio count
                JSONObject o = (JSONObject) array.get(i);
                audios.add(VKAudio.parse(o));
            }
        }
        return audios;
    }

    //http://vk.com/dev/messages.getLongPollServer
    public Object[] getLongPollServer(String captcha_key, String captcha_sid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.getLongPollServer");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        String key = response.getString("key");
        String server = response.getString("server");
        Long ts = response.getLong("ts");
        return new Object[]{key, server, ts};
    }

    //http://vk.com/dev/account.setOnline
    public void setOnline(String captcha_key, String captcha_sid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("account.setOnline");
        addCaptchaParams(captcha_key, captcha_sid, params);
        sendRequest(params);
    }

    //http://vk.com/dev/friends.add
    public long addFriend(Long uid, String text, String captcha_key, String captcha_sid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("friends.add");
        params.put("user_id", uid);
        params.put("text", text);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }

    //http://vk.com/dev/friends.delete
    public long deleteFriend(Long uid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("friends.delete");
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }

    //http://vk.com/dev/friends.getRequests
    public ArrayList<Object[]> getRequestsFriends(Integer out) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("friends.getRequests");
        params.put("need_messages", "1");
        params.put("out", out);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<Object[]> users = new ArrayList<Object[]>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                JSONObject item = array.optJSONObject(i);
                if (item != null) {
                    Long id = item.optLong("user_id", -1);
                    if (id != -1) {
                        Object[] u = new Object[2];
                        u[0] = id;
                        u[1] = item.optString("message");
                        users.add(u);
                    }
                }
            }
        }
        return users;
    }

    //http://vk.com/dev/users.getSubscriptions
    public ArrayList<Long> getSubscriptions(Long uid, int offset, int count, Integer extended) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("users.getSubscriptions");
        params.put("user_id", uid);
        //params.put("extended", extended); //TODO
        if (offset > 0)
            params.put("offset", offset);
        if (count > 0)
            params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        JSONObject jusers = response.optJSONObject("users");
        JSONArray array = jusers.optJSONArray("items");
        ArrayList<Long> users = new ArrayList<Long>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                Long id = array.optLong(i, -1);
                if (id != -1)
                    users.add(id);
            }
        }
        return users;
    }

    //http://vk.com/dev/users.getFollowers
    public ArrayList<VKUser> getFollowers(Long uid, int offset, int count, String fields, String name_case) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("users.getFollowers");
        params.put("user_id", uid);
        if (offset > 0)
            params.put("offset", offset);
        if (count > 0)
            params.put("count", count);
        //if this method is called without fields it will return just ids in wrong format
        if (fields == null)
            fields = "first_name,last_name,photo_100,online";
        params.put("fields", fields);
        params.put("name_case", name_case);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return VKUser.parseUsers(array);
    }

    //http://vk.com/dev/messages.deleteDialog
    public int deleteMessageThread(Long uid, Long chatId) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.deleteDialog");
        params.put("user_id", uid);
        params.put("chat_id", chatId);
        JSONObject root = sendRequest(params);
        return root.getInt("response");
    }

    //http://vk.com/dev/execute
    public void execute(String code) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("execute");
        params.put("code", code);
        sendRequest(params);
    }

    //http://vk.com/dev/messages.getById
    public ArrayList<VKMessage> getMessagesById(ArrayList<Long> message_ids) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.getById");
        params.put("message_ids", arrayToString(message_ids));
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = null;

        if (response != null) {
            array = response.optJSONArray("items");
        }
        return parseMessages(array, false, 0, false, 0);
    }

    /*** chat methods ***/
    //http://vk.com/dev/messages.createChat
    public Long chatCreate(ArrayList<Long> uids, String title) throws IOException, JSONException, VKException {
        if (uids == null || uids.size() == 0)
            return null;
        VKParams params = new VKParams("messages.createChat");
        StringBuilder str_uids = new StringBuilder(String.valueOf(uids.get(0)));
        for (int i = 1; i < uids.size(); i++)
            str_uids.append(",").append(uids.get(i));
        params.put("user_ids", str_uids.toString());
        params.put("title", title);
        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }

    //http://vk.com/dev/messages.editChat
    public Integer chatEdit(long chat_id, String title) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.editChat");
        params.put("chat_id", chat_id);
        params.put("title", title);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/dev/messages.getChatUsers
    public ArrayList<VKUser> getChatUsers(long chat_id, String fields) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.getChatUsers");
        params.put("chat_id", chat_id);
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return VKUser.parseUsers(array);
    }

    //http://vk.com/dev/messages.addChatUser
    public Integer addUserToChat(long chat_id, long uid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.addChatUser");
        params.put("chat_id", chat_id);
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/dev/messages.removeChatUser
    public Integer removeUserFromChat(long chat_id, long uid) throws IOException, JSONException, VKException {
        VKParams params = new VKParams("messages.removeChatUser");
        params.put("chat_id", chat_id);
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    /*** end chat methods ***/

    //http://vk.com/dev/utils.getServerTime
    public long getServerTime() throws IOException, JSONException, VKException {
        VKParams params = new VKParams("utils.getServerTime");
        JSONObject root = sendRequest(params);
        return root.getLong("response");
    }

    //http://vk.com/dev/account.setOffline
    public Long setOffline() throws IOException, JSONException, VKException {
        VKParams params = new VKParams("account.setOffline");
        JSONObject root = sendRequest(params);
        Long response = root.optLong("response");
        return response;
    }
}
