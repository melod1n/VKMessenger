package ru.melod1n.vk.mvp.presenter;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ru.melod1n.library.mvp.base.MvpFields;
import ru.melod1n.library.mvp.base.MvpPresenter;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.mvp.repository.ConversationsRepository;
import ru.melod1n.vk.mvp.view.ConversationsView;

public class ConversationsPresenter extends MvpPresenter<VKConversation, ConversationsView> {

    public ConversationsPresenter(@NonNull ConversationsView view) {
        super(view);

        initRepository(new ConversationsRepository());
    }

    @Override
    protected void insertValues(@NonNull MvpFields fields, @NonNull ArrayList<VKConversation> values) {
        if (view != null) {
            view.insertValues(fields, values);
        }
    }
}
