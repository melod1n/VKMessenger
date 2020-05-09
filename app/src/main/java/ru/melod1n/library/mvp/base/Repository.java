package ru.melod1n.library.mvp.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.common.AppGlobal;

public abstract class Repository<T> {

    public abstract void loadValues(@NonNull MvpFields fields, @Nullable OnLoadListener<T> listener);

    public abstract void loadCachedValues(@NonNull MvpFields fields, @Nullable OnLoadListener<T> listener);

    protected void sendError(@Nullable OnLoadListener<T> listener, @NonNull String errorId) {
        if (listener != null) {
            listener.onErrorLoad(new MvpException(errorId));
        }
    }

    protected void sendError(@Nullable OnLoadListener<T> listener, @Nullable Exception e) {
        if (listener != null && e != null) {
            AppGlobal.handler.post(() -> listener.onErrorLoad(e));
        }
    }

    protected void sendValuesToPresenter(@NonNull MvpFields fields, @NonNull ArrayList<T> values, @Nullable OnLoadListener<T> listener) {
        if (listener != null) {
            AppGlobal.handler.post(() -> listener.onSuccessLoad(values));
        }
    }

    protected abstract void cacheLoadedValues(@NonNull ArrayList<T> values);

}