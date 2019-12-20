package ru.melod1n.vk.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.adapter.MessageAdapter;
import ru.melod1n.vk.adapter.model.VKDialog;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.current.BaseFragment;

public class FragmentMessages extends BaseFragment {

    static final String TAG_EXTRA_DIALOG = "dialog";
    static final String TAG_EXTRA_TITLE = "title";
    static final String TAG_EXTRA_AVATAR = "avatar";

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private VKDialog dialog;
    private String title;
    private String avatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        if (savedInstanceState == null)
            init(getArguments());
    }

    private void init(Bundle bundle) {
        initExtraData(bundle);
        setTitle(title);
        prepareRecyclerView();
        prepareAdapter();
    }

    private void prepareRecyclerView() {
        refreshLayout.setEnabled(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    }

    private void prepareAdapter() {
        ArrayList<VKMessage> messages = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            VKMessage message = new VKMessage();
            message.setText(title + " " + i);
            messages.add(message);
        }

        recyclerView.setAdapter(new MessageAdapter(requireContext(), messages));
    }


    @Override
    public void onReopen(@Nullable Bundle bundle) {
        init(bundle);
    }

    private void initExtraData(@Nullable Bundle bundle) {
        Bundle arguments = bundle != null ? bundle : getArguments();

        if (arguments == null) throw new NullPointerException("arguments must not be null.");

        dialog = (VKDialog) arguments.getSerializable(TAG_EXTRA_DIALOG);
        title = arguments.getString(TAG_EXTRA_TITLE);
        avatar = arguments.getString(TAG_EXTRA_AVATAR);

        if (dialog == null) throw new NullPointerException("dialog must not be null.");
    }
}
