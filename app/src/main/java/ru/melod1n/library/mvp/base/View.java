package ru.melod1n.library.mvp.base;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public interface View<T> {

    void prepareNoInternetView();

    void prepareNoItemsView();

    void prepareErrorView();

    void showNoInternetView();

    void hideNoInternetView();

    void showNoItemsView();

    void hideNoItemsView();

    void showErrorView(Exception e);

    void hideErrorView();

    void startRefreshing();

    void stopRefreshing();

    void showProgressBar();

    void hideProgressBar();

    void insertValues(@NonNull MvpFields fields, @NonNull ArrayList<T> values);

    void clearList();

}
