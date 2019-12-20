package ru.melod1n.vk.adapter.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKMessage;

public class VKDialog implements Serializable {

    private static final long serialVersionUID = 1L;

    private VKConversation conversation;
    private VKMessage lastMessage;

    public VKDialog() {}

    public VKDialog(VKConversation conversation, VKMessage lastMessage) {
        this.conversation = conversation;
        this.lastMessage = lastMessage;
    }

    public VKConversation getConversation() {
        return conversation;
    }

    public void setConversation(VKConversation conversation) {
        this.conversation = conversation;
    }

    public VKMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(VKMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    @NonNull
    @Override
    public String toString() {
        return getConversation().toString();
    }
}
