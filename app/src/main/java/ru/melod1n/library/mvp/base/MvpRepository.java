package ru.melod1n.library.mvp.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public abstract class MvpRepository<T> {

    public abstract void loadValues(@NonNull MvpFields fields, @Nullable MvpOnLoadListener<T> listener);

    public abstract void loadCachedValues(@NonNull MvpFields fields, @Nullable MvpOnLoadListener<T> listener);

    protected void sendError(@Nullable MvpOnLoadListener<T> listener, @NonNull String errorId) {
        if (listener != null) {
            listener.onErrorLoad(new MvpException(errorId));
        }
    }

    protected void sendError(@Nullable MvpOnLoadListener<T> listener, @Nullable Exception e) {
        if (listener != null && e != null) {
            MvpBase.handler.post(() -> listener.onErrorLoad(e));
        }
    }

    protected void sendValuesToPresenter(@NonNull MvpFields fields, @NonNull ArrayList<T> values, @Nullable MvpOnLoadListener<T> listener) {
        if (listener != null) {
            MvpBase.handler.post(() -> listener.onSuccessLoad(values));
        }
    }

    protected abstract void cacheLoadedValues(@NonNull ArrayList<T> values);

    protected void startNewThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    protected void post(Runnable runnable) {
        MvpBase.handler.post(runnable);
    }

}