package ru.melod1n.vk.common;

public class EventInfo {

    public static final String MESSAGE_NEW = "message_new";
    public static final String MESSAGE_EDIT = "message_edit";
    public static final String MESSAGE_DELETE = "message_delete";
    public static final String MESSAGE_RESTORE = "message_restore";
    public static final String MESSAGE_READ = "message_read";
    public static final String MESSAGE_UPDATE = "message_update";

    public static final String USER_UPDATE = "user_update";

    public static final String GROUP_UPDATE = "group_update";

    public static final String CONVERSATION_UPDATE = "conversation_update";

    private String key;
    private Object data;

    public EventInfo(String key, Object data) {
        this.key = key;
        this.data = data;
    }

    public EventInfo(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }
}
