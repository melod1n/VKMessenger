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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.adapter.model.VKDialog;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.concurrent.TaskManager;
import ru.melod1n.vk.concurrent.TryCallback;
import ru.melod1n.vk.current.BaseAdapter;
import ru.melod1n.vk.current.BaseHolder;
import ru.melod1n.vk.database.CacheStorage;
import ru.melod1n.vk.widget.CircleImageView;

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
        CircleImageView avatar;

        @BindView(R.id.dialogUserAvatar)
        CircleImageView userAvatar;

        @BindView(R.id.userOnline)
        CircleImageView userOnline;

        private final Drawable placeholderNormal = new ColorDrawable(Color.DKGRAY);
        private final Drawable placeholderError = new ColorDrawable(Color.RED);

        ViewHolder(@NonNull View v) {
            super(v);

            ButterKnife.bind(this, v);
        }

        @Override
        protected void bind(int position) {
            VKDialog dialog = getItem(position);
            VKMessage lastMessage = dialog.getLastMessage();
            VKConversation conversation = dialog.getConversation();

            VKUser peerUser = searchPeerUser(lastMessage);
            VKUser fromUser = searchFromUser(lastMessage);

            title.setText(getTitle(conversation, peerUser));

            loadImage(getAvatar(conversation, peerUser), avatar);
            loadImage(getUserAvatar(conversation, fromUser), userAvatar);

            if (conversation.isUser() && peerUser != null && peerUser.isOnline()) {
                userOnline.setVisibility(View.GONE);
            } else {
                userOnline.setVisibility(View.GONE);
            }

            text.setText(lastMessage.getText());
        }

        private void loadImage(String imageUrl, ImageView imageView) {
            if (imageUrl == null) {
                imageView.setImageDrawable(placeholderNormal);
            } else {
                imageView.setImageDrawable(placeholderNormal);
                TaskManager.execute(new TryCallback() {

                    Bitmap bitmap;

                    @Override
                    public void ready() throws Exception {
                        bitmap = Picasso.get().load(imageUrl).get();
                    }

                    @Override
                    public void done() {
                        imageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void error(Exception e) {
                        imageView.setImageDrawable(placeholderError);
                    }
                });
            }
        }

        private String getTitle(VKConversation conversation, VKUser peerUser) {
            if (conversation.isUser()) {
                if (peerUser != null) {
                    return peerUser.toString();
                }
            } else if (conversation.isGroup()) {
                return "it\'s group";
            } else {
                return conversation.getChatSettings().getTitle();
            }

            return "it\'s title";
        }

        private String getAvatar(VKConversation conversation, VKUser peerUser) {
            if (conversation.isUser()) {
                if (peerUser != null) {
                    return peerUser.getPhoto200();
                }

                return null;
            } else if (conversation.isGroup()) {
                return null;
            }

            if (conversation.getChatSettings().getPhoto() == null) return null;

            return conversation.getChatSettings().getPhoto().getPhoto100();
        }

        private String getUserAvatar(VKConversation conversation, VKUser fromUser) {
            if (conversation.isUser()) {
                if (fromUser != null) {
                    return fromUser.getPhoto100();
                }
                return null;
            } else {
                return null;
            }
        }

        private VKUser searchPeerUser(VKMessage message) {
            return CacheStorage.getUser(message.getPeerId());
        }

        private VKUser searchFromUser(VKMessage message) {
            return CacheStorage.getUser(message.getFromId());
        }
    }
}
