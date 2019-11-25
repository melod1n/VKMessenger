package ru.melod1n.vk.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.adapter.ConversationAdapter;
import ru.melod1n.vk.adapter.model.VKDialog;
import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.concurrent.TaskManager;
import ru.melod1n.vk.current.BaseAdapter;
import ru.melod1n.vk.database.CacheStorage;

public class FragmentConversations extends Fragment implements SwipeRefreshLayout.OnRefreshListener, BaseAdapter.OnItemClickListener {

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ConversationAdapter adapter;

    @Override
    public void onRefresh() {
        getDialogs(0, 30);
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

        prepareToolbar();
        prepareRefreshLayout();
        prepareRecyclerView();

        getCachedDialogs(0, 30);
        getDialogs(0, 30);
    }

    private void prepareToolbar() {
        toolbar.setTitle("Conversations");
    }

    private void prepareRefreshLayout() {
        refreshLayout.setOnRefreshListener(this);
    }

    private void prepareRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);

        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
    }

    private void getCachedDialogs(int offset, int count) {
        ArrayList<VKConversation> conversations = CacheStorage.getConversations(count);
        ArrayList<VKDialog> dialogs = new ArrayList<>(conversations.size());

        for (VKConversation conversation : conversations) {
            VKDialog dialog = new VKDialog();
            dialog.setConversation(conversation);

            VKMessage lastMessage = CacheStorage.getMessageByPeerId(conversation.getPeer().getId());
            dialog.setLastMessage(lastMessage);

            dialogs.add(dialog);
        }

        createAdapter(offset, dialogs);
    }

    private void getDialogs(int offset, int count) {
        TaskManager.execute(() -> VKApi.messages()
                .getConversations()
                .filter("all")
                .extended(true)
                .fields(VKUser.DEFAULT_FIELDS)
                .offset(offset).count(count)
                .execute(VKDialog.class, new VKApi.OnResponseListener<VKDialog>() {
                    @Override
                    public void onSuccess(ArrayList<VKDialog> models) {
                        insertDataInDatabase(models);
                        Log.d("getDialogs", "Success");

                        refreshLayout.setRefreshing(false);
                        createAdapter(offset, models);
                    }

                    @Override
                    public void onError(Exception ex) {
                        refreshLayout.setRefreshing(false);
                        Log.d("getDialogs", "Error: " + Log.getStackTraceString(ex));
                    }
                }));
    }

    private void insertDataInDatabase(ArrayList<VKDialog> dialogs) {
        ArrayList<VKConversation> conversations = new ArrayList<>(dialogs.size());
        ArrayList<VKMessage> messages = new ArrayList<>(dialogs.size());

        for (VKDialog dialog : dialogs) {
            conversations.add(dialog.getConversation());
            messages.add(dialog.getLastMessage());
        }

        CacheStorage.insertMessages(messages);
        CacheStorage.insertConversations(conversations);
    }

    private void createAdapter(int offset, ArrayList<VKDialog> dialogs) {
        if (dialogs.isEmpty()) return;

        if (offset != 0) {
            adapter.addAll(dialogs);
            adapter.notifyDataSetChanged();
            return;
        }

        if (adapter != null) {
            adapter.changeItems(dialogs);
            adapter.notifyDataSetChanged();
            return;
        }
        adapter = new ConversationAdapter(getActivity(), dialogs);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(requireContext(), "Clicked position: " + position, Toast.LENGTH_SHORT).show();
    }
}
