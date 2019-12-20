package ru.melod1n.vk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.common.EventInfo;
import ru.melod1n.vk.current.BaseAdapter;
import ru.melod1n.vk.current.BaseHolder;

public class MessageAdapter extends BaseAdapter<VKMessage, MessageAdapter.ViewHolder> {

    private static final String TAG = "MessageAdapter";

    public MessageAdapter(Context context, ArrayList<VKMessage> values) {
        super(context, values);

        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(EventInfo info) {

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(getInflater().inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void destroy() {
        EventBus.getDefault().unregister(this);
    }

    class ViewHolder extends BaseHolder {

        @BindView(R.id.text)
        TextView textView;

        ViewHolder(@NonNull View v) {
            super(v);

            ButterKnife.bind(this, v);
        }

        @Override
        protected void bind(int position) {
            VKMessage message = getItem(position);

            textView.setText(message.getText());
        }
    }
}
