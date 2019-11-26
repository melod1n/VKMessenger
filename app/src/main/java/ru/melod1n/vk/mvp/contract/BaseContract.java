package ru.melod1n.vk.mvp.contract;

import java.util.ArrayList;

import ru.melod1n.vk.api.VKApi;

public class BaseContract {

    public interface View<T> {
        void showNoItemsView(boolean visible);

        void showNoInternetView(boolean visible);

        void showErrorView(String errorTitle, String errorDescription);

        void hideErrorView();

        void showRefreshLayout(boolean visible);

        void showProgressBar(boolean visible);

        void loadValuesIntoList(int offset, ArrayList<T> values);

        void clearList();
    }

    public interface Presenter<T> {
        void readyForLoading();

        void onRequestLoadCachedValues(int offset, int count);

        void onRequestLoadValues(int offset, int count);

        void onValuesLoading();

        void onValuesErrorLoading(Exception e);

        void onValuesLoaded(int offset, ArrayList<T> values);

        void onRequestClearList();
    }

    public static abstract class Repository<T> {
        public abstract ArrayList<T> loadCachedValues(int offset, int count);

        public abstract void loadValues(int offset, int count, VKApi.OnResponseListener<T> listener);
    }

}
