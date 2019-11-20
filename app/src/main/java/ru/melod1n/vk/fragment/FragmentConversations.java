package ru.melod1n.vk.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.model.VKConversation;

public class FragmentConversations extends Fragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        toolbar.setTitle("Conversations");

        getConversations();
    }

    private void getConversations() {
        new Thread(() -> {
            try {
                ArrayList<VKConversation> conversations = VKApi.getConversations("all", true, 0, 200, null);

                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), conversations.size() + "", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
