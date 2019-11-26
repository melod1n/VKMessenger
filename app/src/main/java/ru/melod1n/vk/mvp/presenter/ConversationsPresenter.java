package ru.melod1n.vk.mvp.presenter;

import android.util.Log;

import java.util.ArrayList;

import ru.melod1n.vk.adapter.model.VKDialog;
import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.VKException;
import ru.melod1n.vk.mvp.contract.BaseContract;
import ru.melod1n.vk.mvp.model.ConversationsRepository;
import ru.melod1n.vk.util.ArrayUtil;

public class ConversationsPresenter implements BaseContract.Presenter<VKDialog> {
    private static final String TAG = "ConversationsPresenter";

    private BaseContract.View<VKDialog> view;
    private BaseContract.Repository<VKDialog> repository;

    private ArrayList<VKDialog> loadedValues;
    private ArrayList<VKDialog> cachedValues;

    public ConversationsPresenter(BaseContract.View<VKDialog> view) {
        this.view = view;
        this.repository = new ConversationsRepository();

        Log.d(TAG, "Constructor");
    }

    @Override
    public void readyForLoading() {
        view.showNoInternetView(false);
        view.showNoItemsView(false);
        view.showRefreshLayout(false);
        view.hideErrorView();
    }

    @Override
    public void onRequestLoadCachedValues(int offset, int count) {
        readyForLoading();

        cachedValues = repository.loadCachedValues(offset, count);

        onValuesLoaded(offset, cachedValues);
    }

    @Override
    public void onRequestLoadValues(int offset, int count) {
        readyForLoading();

        repository.loadValues(offset, count, new VKApi.OnResponseListener<VKDialog>() {
            @Override
            public void onSuccess(ArrayList<VKDialog> models) {
                loadedValues = models;

                onValuesLoaded(offset, loadedValues);
            }

            @Override
            public void onError(Exception e) {
                onValuesErrorLoading(e);
            }
        });
    }

    @Override
    public void onValuesLoading() {
        view.showProgressBar(true);
    }

    @Override
    public void onValuesErrorLoading(Exception e) {
        view.clearList();
        view.showProgressBar(false);
        view.showNoItemsView(false);
        view.showRefreshLayout(false);

        if (e instanceof VKException) {
            view.showErrorView(e.toString(), e.getMessage());
        } else {
            view.showErrorView(e.toString(), Log.getStackTraceString(e));
        }

        Log.d(TAG, "onValuesErrorLoading: " + e.toString() + ": " + Log.getStackTraceString(e));
    }

    @Override
    public void onValuesLoaded(int offset, ArrayList<VKDialog> values) {
        view.hideErrorView();
        view.showNoItemsView(false);
        view.showRefreshLayout(false);
        view.showProgressBar(false);
        view.showNoItemsView(ArrayUtil.isEmpty(values));

        view.loadValuesIntoList(offset, values);
    }

    @Override
    public void onRequestClearList() {
        view.clearList();
    }
}
