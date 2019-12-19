package ru.melod1n.vk.api.method;

import java.util.Collection;

import ru.melod1n.vk.util.ArrayUtil;

public class MessageMethodSetter extends MethodSetter {

    public MessageMethodSetter(String name) {
        super(name);
    }

    public MessageMethodSetter out(boolean value) {
        put("out", value);
        return this;
    }

    public MessageMethodSetter timeOffset(int value) {
        put("time_offset", value);
        return this;
    }

    public MessageMethodSetter filters(int value) {
        put("filters", value);
        return this;
    }

    public MessageMethodSetter previewLength(int value) {
        put("preview_length", value);
        return this;
    }

    public MessageMethodSetter lastMessageId(int value) {
        put("last_message_id", value);
        return this;
    }

    public MessageMethodSetter unread(boolean value) {
        put("unread", value);
        return this;
    }

    public MessageMethodSetter messageIds(int... ids) {
        put("message_ids", ArrayUtil.toString(ids));
        return this;
    }

    public MessageMethodSetter q(String query) {
        put("q", query);
        return this;
    }

    public MessageMethodSetter startMessageId(int id) {
        put("start_message_id", id);
        return this;
    }

    public MessageMethodSetter peerId(long value) {
        put("peer_id", value);
        return this;
    }

    public MessageMethodSetter peerIds(int... values) {
        put("peer_ids", ArrayUtil.toString(values));
        return this;
    }

    public MessageMethodSetter rev(boolean value) {
        put("rev", value);
        return this;
    }

    public MessageMethodSetter domain(String value) {
        put("domain", value);
        return this;
    }

    public MessageMethodSetter chatId(int value) {
        put("chat_id", value);
        return this;
    }

    public MessageMethodSetter message(String message) {
        put("message", message);
        return this;
    }

    public MessageMethodSetter randomId(int value) {
        put("random_id", value);
        return this;
    }

    public MessageMethodSetter lat(double lat) {
        put("lat", lat);
        return this;
    }

    public MessageMethodSetter longitude(long value) {
        put("LONG", value);
        return this;
    }

    public final MessageMethodSetter attachment(Collection<String> attachments) {
        put("attachment", ArrayUtil.toString(attachments));
        return this;
    }

    public final MessageMethodSetter attachment(String... attachments) {
        put("attachment", ArrayUtil.toString(attachments));
        return this;
    }

    public final MessageMethodSetter forwardMessages(Collection<String> ids) {
        put("forward_messages", ArrayUtil.toString(ids));
        return this;
    }

    public final MessageMethodSetter forwardMessages(int... ids) {
        put("forward_messages", ArrayUtil.toString(ids));
        return this;
    }

    public final MessageMethodSetter stickerId(int value) {
        put("sticker_id", value);
        return this;
    }

    public final MessageMethodSetter messageId(int value) {
        put("message_id", value);
        return this;
    }

    public final MessageMethodSetter important(boolean value) {
        put("important", value);
        return this;
    }

    public final MessageMethodSetter ts(long value) {
        put("ts", value);
        return this;
    }

    public final MessageMethodSetter pts(int value) {
        put("pts", value);
        return this;
    }

    public final MessageMethodSetter msgsLimit(int limit) {
        put("msgs_limit", limit);
        return this;
    }

    public final MessageMethodSetter onlines(boolean onlines) {
        put("onlines", onlines);
        return this;
    }

    public final MessageMethodSetter maxMsgId(int id) {
        put("max_msg_id", id);
        return this;
    }

    public final MessageMethodSetter chatIds(int... ids) {
        put("max_msg_id", ArrayUtil.toString(ids));
        return this;
    }

    public final MessageMethodSetter chatIds(Collection<Integer> ids) {
        put("max_msg_id", ArrayUtil.toString(ids));
        return this;
    }

    public final MessageMethodSetter title(String title) {
        put("title", title);
        return this;
    }

    public final MessageMethodSetter type(boolean typing) {
        if (typing) {
            put("type", "typing");
        }
        return this;
    }

    public final MessageMethodSetter mediaType(String type) {
        put("media_type", type);
        return this;
    }

    public final MessageMethodSetter photoSizes(boolean value) {
        return (MessageMethodSetter) put("photo_sizes", value);
    }

    public final MessageMethodSetter filter(String value) {
        return (MessageMethodSetter) put("filter", value);
    }

    public final MessageMethodSetter extended(boolean value) {
        return (MessageMethodSetter) put("extended", value);
    }

}