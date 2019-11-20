package ru.melod1n.vk.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.VKAttachments;

public class VKMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public long date;
    public long uid;
    public long mid;
    public String title;
    public String body;
    public boolean read_state;
    public boolean is_out;
    public ArrayList<VKAttachments> attachments = new ArrayList<VKAttachments>();
    public Long chat_id;
    public ArrayList<Long> chat_members;
    public Long admin_id;

    public static VKMessage parse(JSONObject o, boolean from_history, long history_uid, boolean from_chat, long me) throws NumberFormatException, JSONException {
        VKMessage m = new VKMessage();
        if (from_chat) {
            long from_id = o.getLong("user_id");
            m.uid = from_id;
            m.is_out = (from_id == me);
        } else if (from_history) {
            m.uid = history_uid;
            Long from_id = o.getLong("from_id");
            m.is_out = !(from_id == history_uid);
        } else {
            //тут не очень, потому что при получении списка диалогов если есть моё сообщение, которое я написал в беседу, то в нём uid будет мой. Хотя в других случайх uid всегда собеседника.
            m.uid = o.getLong("user_id");
            m.is_out = o.optInt("out") == 1;
        }
        m.mid = o.optLong("id");
        m.date = o.optLong("date");
        m.title = VKApi.unescape(o.optString("title"));
        m.body = VKApi.unescapeWithSmiles(o.optString("body"));
        m.read_state = (o.optInt("read_state") == 1);
        if (o.has("chat_id"))
            m.chat_id = o.getLong("chat_id");

        //for dialog list
        JSONArray tmp = o.optJSONArray("chat_active");
        if (tmp != null && tmp.length() != 0) {
            m.chat_members = new ArrayList<Long>();
            for (int i = 0; i < tmp.length(); ++i)
                m.chat_members.add(tmp.getLong(i));
        }

        JSONArray attachments = o.optJSONArray("attachments");
        JSONObject geo_json = o.optJSONObject("geo");
        m.attachments = VKAttachments.parseAttachments(attachments);

        //parse fwd_messages and add them to attachments
        JSONArray fwd_messages = o.optJSONArray("fwd_messages");
        if (fwd_messages != null) {
            for (int i = 0; i < fwd_messages.length(); ++i) {
                JSONObject fwd_message_json = fwd_messages.getJSONObject(i);
                VKMessage fwd_message = VKMessage.parse(fwd_message_json, false, 0, false, 0);
                VKAttachments att = new VKAttachments();
                att.type = "message";
                att.message = fwd_message;
                m.attachments.add(att);
            }
        }

        return m;
    }

    public static int UNREAD = 1;        //сообщение не прочитано
    public static int OUTBOX = 2;        //исходящее сообщение
    public static int REPLIED = 4;        //на сообщение был создан ответ
    public static int IMPORTANT = 8;    //помеченное сообщение
    public static int CHAT = 16;        //сообщение отправлено через диалог
    public static int FRIENDS = 32;        //сообщение отправлено другом
    public static int SPAM = 64;        //сообщение помечено как "Спам"
    public static int DELETED = 128;    //сообщение удалено (в корзине)
    public static int FIXED = 256;        //сообщение проверено пользователем на спам
    public static int MEDIA = 512;        //сообщение содержит медиаконтент
    public static int BESEDA = 8192;    //беседа

    public static VKMessage parse(JSONArray a) throws JSONException {
        VKMessage m = new VKMessage();
        m.mid = a.getLong(1);
        m.uid = a.getLong(3);
        m.date = a.getLong(4);
        m.title = VKApi.unescape(a.getString(5));
        m.body = VKApi.unescapeWithSmiles(a.getString(6));
        int flag = a.getInt(2);
        m.read_state = (flag & UNREAD) == 0;
        m.is_out = (flag & OUTBOX) != 0;
        if ((flag & BESEDA) != 0) {
            m.chat_id = a.getLong(3) & 63;//cut 6 last digits
            JSONObject o = a.getJSONObject(7);
            m.uid = o.getLong("from");
        }
        //m.attachment = a.getJSONArray(7); TODO
        return m;
    }
}
