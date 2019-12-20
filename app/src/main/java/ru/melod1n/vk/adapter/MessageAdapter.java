package ru.melod1n.vk.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import ru.melod1n.vk.R;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.current.BaseAdapter;
import ru.melod1n.vk.current.BaseHolder;

public class MessageAdapter extends BaseAdapter<VKMessage, MessageAdapter.ViewHolder> {

    private static final String TAG = "MessageAdapter";

    public MessageAdapter(Context context, ArrayList<VKMessage> values) {
        super(context, values);

//        EventBus.getDefault().register(this);
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

        ViewHolder(@NonNull View v) {
            super(v);
        }

        @Override
        protected void bind(int position) {
            Log.d(TAG, "position: " + position);
        }
    }
}
