package ru.melod1n.vk.mvp.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import ru.melod1n.library.mvp.base.MvpConstants;
import ru.melod1n.library.mvp.base.MvpException;
import ru.melod1n.library.mvp.base.MvpFields;
import ru.melod1n.library.mvp.base.OnLoadListener;
import ru.melod1n.library.mvp.base.Repository;
import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.common.TaskManager;
import ru.melod1n.vk.database.CacheStorage;
import ru.melod1n.vk.mvp.presenter.FriendsPresenter;
import ru.melod1n.vk.util.ArrayUtil;

public class FriendsRepository extends Repository<VKUser> {

    @Override
    public void loadValues(@NonNull MvpFields fields, @Nullable OnLoadListener<VKUser> listener) {
        int count = fields.getInt(MvpConstants.COUNT);
        int offset = fields.getInt(MvpConstants.OFFSET);

        TaskManager.INSTANCE.execute(() -> {
            try {
                ArrayList<VKUser> values = VKApi.friends()
                        .get()
                        .order("hints")
                        .fields(VKUser.DEFAULT_FIELDS)
                        .count(count)
                        .offset(offset)
                        .execute(VKUser.class);

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
    public void loadCachedValues(@NonNull MvpFields fields, @Nullable OnLoadListener<VKUser> listener) {
        int userId = fields.getInt(MvpConstants.ID);
        int offset = fields.getInt(MvpConstants.OFFSET);
        int count = fields.getInt(MvpConstants.COUNT);
        boolean onlyOnline = fields.getBoolean(FriendsPresenter.ONLY_ONLINE);

        ArrayList<VKUser> friends = CacheStorage.getFriends(userId, onlyOnline);
        ArrayUtil.INSTANCE.prepareList(friends, offset, count);

        if (friends.isEmpty()) {
            sendError(listener, MvpException.ERROR_EMPTY);
        } else {
            sendValuesToPresenter(fields, friends, listener);
        }
    }


    @Override
    protected void cacheLoadedValues(@NonNull ArrayList<VKUser> values) {
        CacheStorage.insertFriends(values);
    }
}
