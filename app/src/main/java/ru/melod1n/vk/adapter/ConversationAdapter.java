package ru.melod1n.vk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.adapter.model.VKDialog;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.concurrent.TaskManager;
import ru.melod1n.vk.concurrent.TryCallback;
import ru.melod1n.vk.current.BaseAdapter;
import ru.melod1n.vk.current.BaseHolder;

public class ConversationAdapter extends BaseAdapter<VKDialog, ConversationAdapter.ViewHolder> {

    public ConversationAdapter(Context context, ArrayList<VKDialog> values) {
        super(context, values);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(getInflater().inflate(R.layout.item_dialog, parent, false));
    }

    class ViewHolder extends BaseHolder {

        @BindView(R.id.dialogText)
        TextView text;

        @BindView(R.id.dialogTitle)
        TextView title;

        @BindView(R.id.dialogAvatar)
        ImageView avatar;

        private final Drawable placeholderNormal = new ColorDrawable(Color.DKGRAY);
        private final Drawable placeholderError = new ColorDrawable(Color.RED);

        ViewHolder(@NonNull View v) {
            super(v);

            ButterKnife.bind(this, v);

        }

        @Override
        protected void bind(int position) {
            VKDialog dialog = getItem(position);

            VKConversation conversation = dialog.getConversation();

            title.setText(getTitle(conversation));

            String sAvatar = getAvatar(conversation);
            if (sAvatar == null) {
                avatar.setImageDrawable(placeholderNormal);
            } else {
                avatar.setImageDrawable(placeholderNormal);
                TaskManager.execute(new TryCallback() {

                    Bitmap bitmap;

                    @Override
                    public void ready() throws Exception {
                        bitmap = Picasso.get().load(sAvatar).get();
                    }

                    @Override
                    public void done() {
                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getContext().getResources(), bitmap);
                        drawable.setCornerRadius(50);
                        drawable.setAntiAlias(true);

                        avatar.setImageDrawable(drawable);
                    }

                    @Override
                    public void error(Exception e) {
                        avatar.setImageDrawable(placeholderError);
                    }
                });
            }

            VKMessage lastMessage = dialog.getLastMessage();

            text.setText(lastMessage.getText());
        }

        private String getTitle(VKConversation conversation) {
            if (conversation.getChatSettings() == null) { //non chat
                return "It\'s title";
            } else {
                return conversation.getChatSettings().getTitle();
            }
        }

        private String getAvatar(VKConversation conversation) {
            if (conversation.getChatSettings() == null)
                return null;

            if (conversation.getChatSettings().getPhoto() == null) return null;

            return conversation.getChatSettings().getPhoto().getPhoto100();
        }

        private String getUserAvatar(VKConversation conversation) {
            return null;
        }

    }
}
