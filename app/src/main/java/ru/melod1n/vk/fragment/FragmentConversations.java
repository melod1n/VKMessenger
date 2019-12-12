package ru.melod1n.vk.fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.adapter.ConversationAdapter;
import ru.melod1n.vk.adapter.model.VKDialog;
import ru.melod1n.vk.common.AppGlobal;
import ru.melod1n.vk.current.BaseAdapter;
import ru.melod1n.vk.current.BaseFragment;
import ru.melod1n.vk.mvp.contract.BaseContract;
import ru.melod1n.vk.mvp.presenter.ConversationsPresenter;
import ru.melod1n.vk.util.Util;

public class FragmentConversations extends BaseFragment implements BaseContract.View<VKDialog>, SwipeRefreshLayout.OnRefreshListener, BaseAdapter.OnItemClickListener {

    private static final String TAG = "FragmentConversations";

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private ConversationAdapter adapter;

    private BaseContract.Presenter<VKDialog> presenter;

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.navigation_conversations);
    }

    @Override
    public void onRefresh() {
        presenter.onValuesLoading();
        presenter.onRequestClearList();
        presenter.onRequestLoadValues(0, 30);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        presenter = new ConversationsPresenter(this);

        prepareRefreshLayout();
        prepareRecyclerView();

        if (Util.hasConnection()) {
            onRefresh();
        } else {
            presenter.onRequestLoadCachedValues(0, 30);
        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    private void prepareRefreshLayout() {
        refreshLayout.setColorSchemeColors(AppGlobal.colorAccent);
        refreshLayout.setOnRefreshListener(this);
    }

    private void prepareRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);

        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        decoration.setDrawable(new ColorDrawable(requireContext().getColor(R.color.divider)));

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(manager);
    }

    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: " + position);
    }

    @Override
    public void showNoItemsView(boolean visible) {
        Log.d(TAG, "showNoItemsView: " + visible);
    }

    @Override
    public void showNoInternetView(boolean visible) {
        Log.d(TAG, "showNoInternetView: " + visible);
    }

    @Override
    public void showErrorView(String errorTitle, String errorDescription) {
        Log.d(TAG, "showErrorView: " + errorTitle + ": " + errorDescription);

        if (!Util.hasConnection()) {
            presenter.onRequestLoadCachedValues(0, 30);
        }
    }

    @Override
    public void hideErrorView() {
        Log.d(TAG, "hideErrorView");
    }

    @Override
    public void showRefreshLayout(boolean visible) {
        Log.d(TAG, "showRefreshLayout: " + visible);
        refreshLayout.setRefreshing(visible);
    }

    @Override
    public void showProgressBar(boolean visible) {
        Log.d(TAG, "showProgressBar: " + visible);
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void loadValuesIntoList(int offset, ArrayList<VKDialog> values) {
        Log.d(TAG, "loadValuesIntoList: " + offset + ", " + values.size());
        if (values.isEmpty()) return;

        if (offset != 0) {
            adapter.addAll(values);
            adapter.notifyDataSetChanged();
            return;
        }

        if (adapter != null) {
            adapter.changeItems(values);
            adapter.notifyDataSetChanged();
            return;
        }

        adapter = new ConversationAdapter(this, values);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void clearList() {
        Log.d(TAG, "clearList");

        if (adapter == null) return;

        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        if (adapter != null) adapter.destroy();
        super.onDetach();
    }
}