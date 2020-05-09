package ru.melod1n.library.mvp.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public abstract class Presenter<T, V extends View> {

    protected String TAG;

    protected Repository<T> repository;

    @Nullable
    protected V view;

    @NonNull
    protected ArrayList<T> loadedValues = new ArrayList<>();

    @NonNull
    protected ArrayList<T> cachedValues = new ArrayList<>();

    @NonNull
    protected ArrayList<T> values = new ArrayList<>();


    public Presenter(@NonNull V view) {
        this.view = view;
    }

    protected void initRepository(Repository<T> repository) {
        this.repository = repository;
    }

    public void prepareForLoading() {
        if (view != null) {
            view.hideNoInternetView();
            view.hideNoItemsView();
            view.hideErrorView();

            if (values.isEmpty()) {
                view.showProgressBar();
            }
        }
    }

    public void showList() {
        if (view != null) {
            view.hideProgressBar();
            view.stopRefreshing();
        }
    }

    public void checkListIsEmpty(boolean fromCache) {
        if (((fromCache && cachedValues.isEmpty()) || (!fromCache && loadedValues.isEmpty())) && view != null) {
            view.showNoItemsView();
        }
    }

    public void checkListIsEmpty(ArrayList<T> values) {
        if (view != null && values.isEmpty()) {
            view.showNoItemsView();
        }
    }

    public void requestLoadValues(@NonNull MvpFields fields) {
        repository.loadValues(fields, new OnLoadListener<T>() {
            @Override
            public void onSuccessLoad(ArrayList<T> values) {
                onValuesLoaded(fields, values);
            }

            @Override
            public void onErrorLoad(Exception e) {
                onValuesError(e);
            }
        });
    }

    public void requestCachedData(@NonNull MvpFields fields) {
        repository.loadCachedValues(fields, new OnLoadListener<T>() {
            @Override
            public void onSuccessLoad(ArrayList<T> values) {
                onValuesLoaded(fields, values);
            }

            @Override
            public void onErrorLoad(Exception e) {
                onValuesError(e);
            }
        });
    }

    protected void onValuesLoaded(@NonNull MvpFields fields, @NonNull ArrayList<T> values) {
        showList();

        boolean fromCache = fields.getBoolean(MvpConstants.FROM_CACHE);

        this.values = values;

        if (fromCache) {
            cachedValues = values;
        } else {
            loadedValues = values;
        }

        insertValues(fields, values);
    }

    protected void onValuesError(Exception e) {
        showList();

        loadedValues.clear();

        if (view != null) view.showErrorView(e);
    }

    protected abstract void insertValues(@NonNull MvpFields fields, @NonNull ArrayList<T> values);

    public void destroy() {
        view = null;
    }
}
