package ru.melod1n.vk.mvp.presenter;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ru.melod1n.library.mvp.base.MvpFields;
import ru.melod1n.library.mvp.base.Presenter;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.mvp.repository.FriendsRepository;
import ru.melod1n.vk.mvp.view.FriendsView;

public class FriendsPresenter extends Presenter<VKUser, FriendsView> {

    public static final String ONLY_ONLINE = "_only_online";

    public FriendsPresenter(@NonNull FriendsView view) {
        super(view);

        initRepository(new FriendsRepository());
    }


    @Override
    protected void insertValues(@NonNull MvpFields fields, @NonNull ArrayList<VKUser> values) {
//        if (view != null) {
            view.insertValues(fields, values);
//        }
    }


}
