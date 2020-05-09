package ru.melod1n.vk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.adapter.diffutil.ConversationCallback;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.util.VKUtil;
import ru.melod1n.vk.current.BaseAdapter;
import ru.melod1n.vk.current.BaseFragment;
import ru.melod1n.vk.current.BaseHolder;
import ru.melod1n.vk.database.CacheStorage;

public class FragmentDialogs extends BaseFragment {

    //    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private TestAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(requireContext());

        return recyclerView;
//        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ButterKnife.bind(this, view);

        recyclerView = (RecyclerView) view;

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));


        ArrayList<VKConversation> conversations = CacheStorage.INSTANCE.getConversations();
        VKUtil.INSTANCE.sortConversationsByDate(conversations, false);

        adapter = new TestAdapter(requireContext(), conversations);
        adapter.setOnItemClickListener(position -> {
            Toast.makeText(requireContext(), "a", Toast.LENGTH_SHORT).show();

            int count = adapter.getItemCount();

            List<VKConversation> list = new ArrayList<>(adapter.getValues());
            Collections.reverse(list);
//            list.remove(list.size() - 1);
//            list.remove(list.size() - 1);
//            list.remove(list.size() - 1);

            updateList(list);

//            adapter.setItems(VKUtil.INSTANCE.sortConversationsByDate(conversations, true));
//            adapter.notifyItemRangeRemoved(count - 3, 3);
        });

        recyclerView.setAdapter(adapter);
    }

    private void updateList(List<VKConversation> newList) {
        ConversationCallback diffCallBack = new ConversationCallback(adapter.getValues(), newList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallBack, false);

        adapter.setItems(newList);

        diffResult.dispatchUpdatesTo(adapter);
    }

    public class TestAdapter extends BaseAdapter<VKConversation, BaseHolder> {


        TestAdapter(@NotNull Context context, @NotNull ArrayList<VKConversation> values) {
            super(context, values);
        }

        @NonNull
        @Override
        public BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(requireContext());

            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);

//            return new ViewHolder(textView);

            return new ViewHolder2(getLayoutInflater().inflate(R.layout.item_conversation_light, parent, false));
        }

        class ViewHolder extends BaseHolder {

            TextView messageText;

            ViewHolder(@NotNull View v) {
                super(v);

                messageText = (TextView) v;

            }

            @Override
            public void bind(int position) {
                messageText.setText(getItem(position).getTitle());
            }
        }

        class ViewHolder2 extends BaseHolder {

            @BindView(R.id.conversationText)
            TextView messageText;

            ViewHolder2(@NotNull View v) {
                super(v);

//                messageText = (TextView) v;

                ButterKnife.bind(this, v);
            }

            @Override
            public void bind(int position) {
                messageText.setText(getItem(position).getTitle());
            }
        }
    }
}
