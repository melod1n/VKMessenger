package ru.melod1n.vk.mvp.model;

import java.util.ArrayList;
import java.util.Collections;

import ru.melod1n.vk.adapter.model.VKDialog;
import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.common.AppGlobal;
import ru.melod1n.vk.common.TaskManager;
import ru.melod1n.vk.database.CacheStorage;
import ru.melod1n.vk.mvp.contract.BaseContract;

public class ConversationsRepository extends BaseContract.Repository<VKDialog> {

    @Override
    public ArrayList<VKDialog> loadCachedValues(int offset, int count) {
        ArrayList<VKConversation> conversations = CacheStorage.getConversations(count);
        ArrayList<VKDialog> dialogs = new ArrayList<>(conversations.size());

        Collections.sort(conversations, (o1, o2) -> {
            VKMessage m1 = CacheStorage.getMessageByPeerId(o1.getPeer().getId());
            VKMessage m2 = CacheStorage.getMessageByPeerId(o2.getPeer().getId());

            if (m1 == null || m2 == null) return 0;

            long x = m1.getDate();
            long y = m2.getDate();


            return (x > y) ? -1 : ((x == y) ? 1 : 0);
        });

        for (VKConversation conversation : conversations) {
            VKDialog dialog = new VKDialog();

            dialog.setConversation(conversation);

            VKMessage lastMessage = CacheStorage.getMessageByPeerId(conversation.getPeer().getId());
            dialog.setLastMessage(lastMessage);

            dialogs.add(dialog);
        }

        return dialogs;
    }

    @Override
    public void loadValues(int offset, int count, VKApi.OnResponseListener<VKDialog> listener) {
        TaskManager.execute(() -> {
            try {
                ArrayList<VKDialog> models = VKApi.messages()
                        .getConversations()
                        .filter("all")
                        .extended(true)
                        .fields(VKUser.DEFAULT_FIELDS)
                        .offset(offset).count(count)
                        .execute(VKDialog.class);

                insertDataInDatabase(models);

                AppGlobal.handler.post(new VKApi.SuccessCallback<>(listener, models));
            } catch (Exception e) {
                e.printStackTrace();

                AppGlobal.handler.post(new VKApi.ErrorCallback(listener, e));
            }
        });
    }

    private void insertDataInDatabase(ArrayList<VKDialog> dialogs) {
        ArrayList<VKConversation> conversations = new ArrayList<>(dialogs.size());
        ArrayList<VKMessage> messages = new ArrayList<>(dialogs.size());

        for (VKDialog dialog : dialogs) {
            conversations.add(dialog.getConversation());
            messages.add(dialog.getLastMessage());
        }

        CacheStorage.insertMessages(messages);
        CacheStorage.insertConversations(conversations);
        CacheStorage.insertUsers(conversations.get(0).getProfiles());
        CacheStorage.insertGroups(conversations.get(0).getGroups());
    }
}
