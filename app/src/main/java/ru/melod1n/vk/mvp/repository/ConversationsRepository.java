package ru.melod1n.vk.mvp.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import ru.melod1n.library.mvp.base.MvpConstants;
import ru.melod1n.library.mvp.base.MvpFields;
import ru.melod1n.library.mvp.base.MvpOnLoadListener;
import ru.melod1n.library.mvp.base.MvpRepository;
import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKGroup;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.common.TaskManager;
import ru.melod1n.vk.database.CacheStorage;
import ru.melod1n.vk.util.ArrayUtil;

public class ConversationsRepository extends MvpRepository<VKConversation> {

    @Override
    public void loadValues(@NonNull MvpFields fields, @Nullable MvpOnLoadListener<VKConversation> listener) {
        int count = fields.getInt(MvpConstants.COUNT);
        int offset = fields.getInt(MvpConstants.OFFSET);

        TaskManager.execute(() -> {
            try {
                ArrayList<VKConversation> values = VKApi.messages()
                        .getConversations()
                        .filter("all")
                        .extended(true)
                        .fields(VKUser.DEFAULT_FIELDS)
                        .offset(offset).count(count)
                        .execute(VKConversation.class);

                if (values == null) values = new ArrayList<>();

                cacheLoadedValues(values);

                sendValuesToPresenter(fields, values, listener);

            } catch (Exception e) {
                e.printStackTrace();
                sendError(listener, e);
            }
        });
    }

    @Override
    public void loadCachedValues(@NonNull MvpFields fields, @Nullable MvpOnLoadListener<VKConversation> listener) {
        int offset = fields.getInt(MvpConstants.OFFSET);
        int count = fields.getInt(MvpConstants.COUNT);

        ArrayList<VKConversation> conversations = CacheStorage.getConversations();
        ArrayUtil.prepareList(conversations, offset, count);

        sendValuesToPresenter(fields, conversations, listener);
    }

    @Override
    protected void cacheLoadedValues(@NonNull ArrayList<VKConversation> values) {
        ArrayList<VKUser> profiles = VKConversation.Companion.getProfiles();
        ArrayList<VKGroup> groups = VKConversation.Companion.getGroups();

        ArrayList<VKMessage> messages = new ArrayList<>();

        for (VKConversation conversation : values) {
            messages.add(conversation.getLastMessage());
        }

        CacheStorage.insertMessages(messages);
        CacheStorage.insertConversations(values);
        CacheStorage.insertUsers(profiles);
        CacheStorage.insertGroups(groups);
    }
}
