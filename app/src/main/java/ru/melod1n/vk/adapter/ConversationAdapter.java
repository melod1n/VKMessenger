package ru.melod1n.vk.adapter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.adapter.model.VKDialog;
import ru.melod1n.vk.api.UserConfig;
import ru.melod1n.vk.api.model.VKAudio;
import ru.melod1n.vk.api.model.VKAudioMessage;
import ru.melod1n.vk.api.model.VKConversation;
import ru.melod1n.vk.api.model.VKDoc;
import ru.melod1n.vk.api.model.VKGift;
import ru.melod1n.vk.api.model.VKGraffiti;
import ru.melod1n.vk.api.model.VKGroup;
import ru.melod1n.vk.api.model.VKLink;
import ru.melod1n.vk.api.model.VKMessage;
import ru.melod1n.vk.api.model.VKModel;
import ru.melod1n.vk.api.model.VKPhoto;
import ru.melod1n.vk.api.model.VKPoll;
import ru.melod1n.vk.api.model.VKSticker;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.api.model.VKVideo;
import ru.melod1n.vk.common.AppGlobal;
import ru.melod1n.vk.common.EventInfo;
import ru.melod1n.vk.current.BaseAdapter;
import ru.melod1n.vk.current.BaseHolder;
import ru.melod1n.vk.database.CacheStorage;
import ru.melod1n.vk.database.DatabaseHelper;
import ru.melod1n.vk.database.MemoryCache;
import ru.melod1n.vk.fragment.FragmentConversations;
import ru.melod1n.vk.util.ArrayUtil;
import ru.melod1n.vk.widget.CircleImageView;

public class ConversationAdapter extends BaseAdapter<VKDialog, ConversationAdapter.ViewHolder> {

    private FragmentConversations conversations;

    public ConversationAdapter(FragmentConversations conversations, ArrayList<VKDialog> values) {
        super(conversations.requireContext(), values);
        this.conversations = conversations;

        EventBus.getDefault().register(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(getInflater().inflate(R.layout.item_dialog, parent, false));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(EventInfo info) {
        switch (info.getKey()) {
            case EventInfo.MESSAGE_NEW:
                addMessage((VKMessage) info.getData());
                break;
            case EventInfo.MESSAGE_EDIT:
                editMessage((VKMessage) info.getData());
                break;
            case EventInfo.MESSAGE_DELETE:
                Object[] data = (Object[]) info.getData();

                deleteMessage((int) data[0], (int) data[1]);
                break;
            case EventInfo.MESSAGE_RESTORE:
                restoreMessage((VKMessage) info.getData());
                break;
            case EventInfo.MESSAGE_READ:
                int[] ints = (int[]) info.getData();
                readMessage(ints[0], ints[1]);
                break;
        }
    }

    private void readMessage(int messageId, int peerId) {
        int index = searchConversationIndex(peerId);
        if (index == -1) return;

        VKDialog dialog = getItem(index);
        if (dialog.getLastMessage().getId() != messageId) return;

        if (dialog.getLastMessage().isOut()) {
            dialog.getConversation().setOutRead(messageId);
        } else {
            dialog.getConversation().setInRead(messageId);
        }

        notifyDataSetChanged();
    }

    private void restoreMessage(VKMessage message) {
        int index = searchConversationIndex(message.getPeerId());
        if (index == -1) return;

        VKDialog dialog = getItem(index);
        if (dialog.getLastMessage().getDate() > message.getDate()) return;

        dialog.setLastMessage(message);
        notifyDataSetChanged();
    }

    private void deleteMessage(int messageId, int peerId) {
        int index = searchConversationIndex(peerId);
        if (index == -1) return;

        VKDialog dialog = getItem(index);

        CacheStorage.delete(DatabaseHelper.TABLE_MESSAGES, DatabaseHelper.MESSAGE_ID, messageId);

        VKMessage preLast = CacheStorage.getMessages(peerId).get(0);

        if (preLast == null) {
            CacheStorage.delete(DatabaseHelper.TABLE_CONVERSATIONS, DatabaseHelper.PEER_ID, peerId);
            remove(index);
            notifyDataSetChanged();
            return;
        }

        if (dialog.getLastMessage().getId() != messageId) return;

        dialog.setLastMessage(preLast);
        notifyDataSetChanged();
    }

    private void editMessage(VKMessage message) {
        int index = searchConversationIndex(message.getPeerId());
        if (index == -1) return;

        VKDialog dialog = getItem(index);

        dialog.setLastMessage(message);

        notifyDataSetChanged();
    }

    private void addMessage(VKMessage message) {
        int index = searchConversationIndex(message.getPeerId());

        LinearLayoutManager manager = (LinearLayoutManager) conversations.getRecyclerView().getLayoutManager();

        if (manager == null) return;

        int firstVisiblePosition = manager.findFirstCompletelyVisibleItemPosition();
        int lastVisiblePosition = manager.findLastCompletelyVisibleItemPosition();

        int maxDialogsCount = lastVisiblePosition - firstVisiblePosition + 1;

        if (index >= 0) {
            if (index == 0) {
                VKDialog dialog = getItem(0);

                remove(0);
                add(0, new VKDialog(prepareConversation(dialog.getConversation(), message), message));
            } else {
                VKConversation conversation = getItem(index).getConversation();

                remove(index);
                add(0, new VKDialog(prepareConversation(conversation, message), message));

//                if (index > maxDialogsCount) {
//                    notifyItemRemoved(index);
//                    notifyItemInserted(0);
//                    notifyItemRangeChanged(0, getItemCount(), null);
//                } else {
//                    notifyItemMoved(index, 0);
//                    notifyItemRangeChanged(0, getItemCount(), null);
//                }
            }
        } else {
            VKConversation conversation = CacheStorage.getConversation(message.getPeerId());
            if (conversation != null) {
                add(0, new VKDialog(prepareConversation(conversation, message), message));
//                notifyItemInserted(0);
//                notifyItemRangeChanged(0, getItemCount(), null);
            }
        }

        if (manager.findFirstVisibleItemPosition() <= 1)
            manager.scrollToPosition(0);

        //notifyItemRangeChanged(0, getItemCount(), -1);

        notifyDataSetChanged();
    }

    private VKConversation prepareConversation(VKConversation conversation, VKMessage newMessage) {
        conversation.setLastMessageId(newMessage.getId());

        if (!newMessage.isOut()) conversation.setUnreadCount(conversation.getUnreadCount() + 1);
        else conversation.setUnreadCount(0);

        if (newMessage.getPeerId() == newMessage.getFromId() && newMessage.getFromId() == UserConfig.getUserId()) {
            conversation.setOutRead(newMessage.getId());
        }

        return conversation;
    }

    private int searchConversationIndex(int peerId) {
        for (int i = 0; i < getItemCount(); i++) {
            VKDialog dialog = getItem(i);
            if (dialog.getConversation().getPeer().getId() == peerId) return i;
        }

        return -1;
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

        @BindView(R.id.dialogUserOnline)
        CircleImageView userOnline;

        @BindView(R.id.dialogType)
        ImageView dialogType;

        @BindView(R.id.dialogCounter)
        TextView dialogCounter;

        @BindView(R.id.dialogOut)
        CircleImageView dialogOut;

        @BindView(R.id.dialogDate)
        TextView dialogDate;

        @BindView(R.id.dialogCounterContainer)
        RelativeLayout dialogCounterContainer;

        private final Drawable placeholderNormal = new ColorDrawable(AppGlobal.colorAccent);

        private int colorHighlight = AppGlobal.colorAccent;

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

            VKGroup peerGroup = searchPeerGroup(lastMessage);
            VKGroup fromGroup = searchFromGroup(lastMessage);

            title.setText(getTitle(conversation, peerUser, peerGroup));

            Drawable onlineIcon = getOnlineIcon(conversation, peerUser);
            userOnline.setImageDrawable(onlineIcon);
            userOnline.setVisibility(onlineIcon == null ? View.GONE : View.VISIBLE);

            if ((conversation.isChat() || lastMessage.isOut()) && !conversation.isChannel()) {
                userAvatar.setVisibility(View.VISIBLE);
                loadImage(getUserAvatar(lastMessage, fromUser, fromGroup), userAvatar);
            } else {
                userAvatar.setVisibility(View.GONE);
                userAvatar.setImageDrawable(null);
            }

            loadImage(getAvatar(conversation, peerUser, peerGroup), avatar);

            Drawable dDialogType = getDialogType(conversation);
            if (dDialogType != null) {
                dialogType.setVisibility(View.VISIBLE);
                dialogType.setImageDrawable(dDialogType);
            } else {
                dialogType.setVisibility(View.GONE);
                dialogType.setImageDrawable(null);
            }

            if (lastMessage.getAction() == null) {
                if (!ArrayUtil.isEmpty(lastMessage.getAttachments())) {
                    String attachmentText = getAttachmentText(lastMessage.getAttachments());

                    SpannableString span = new SpannableString(attachmentText);
                    span.setSpan(new ForegroundColorSpan(colorHighlight), 0, attachmentText.length(), 0);

                    text.setText(span);
                } else if (!ArrayUtil.isEmpty(lastMessage.getFwdMessages())) {
                    String fwdText = getFwdText(lastMessage.getFwdMessages());

                    SpannableString span = new SpannableString(fwdText);
                    span.setSpan(new ForegroundColorSpan(colorHighlight), 0, fwdText.length(), 0);

                    text.setText(span);
                } else {
                    text.setText(lastMessage.getText());
                }
            } else {
                String actionText = getActionText(lastMessage);
                SpannableString span = new SpannableString(actionText);
                span.setSpan(new ForegroundColorSpan(colorHighlight), 0, actionText.length(), 0);

                text.setText(span);
            }

            if (ArrayUtil.isEmpty(lastMessage.getAttachments()) && ArrayUtil.isEmpty(lastMessage.getFwdMessages()) && lastMessage.getAction() == null && TextUtils.isEmpty(lastMessage.getText())) {
                String unknown = "Unknown";

                SpannableString span = new SpannableString(unknown);
                span.setSpan(new ForegroundColorSpan(colorHighlight), 0, unknown.length(), 0);
                text.setText(span);
            }

            boolean read = (lastMessage.isOut() && conversation.getOutRead() == conversation.getLastMessageId()) || (!lastMessage.isOut() && conversation.getInRead() == conversation.getLastMessageId());

            if (read) {
                dialogCounter.setVisibility(View.GONE);
                dialogOut.setVisibility(View.GONE);
            } else {
                if (lastMessage.isOut()) {
                    dialogOut.setVisibility(View.VISIBLE);
                    dialogCounter.setVisibility(View.INVISIBLE);
                    dialogCounter.setText("");
                } else {
                    dialogOut.setVisibility(View.GONE);
                    dialogCounter.setVisibility(View.VISIBLE);
                    dialogCounter.setText(String.valueOf(conversation.getUnreadCount()));
                }
            }

            dialogCounterContainer.setVisibility(dialogOut.getVisibility() == View.VISIBLE || dialogCounter.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);

            dialogDate.setText(getTime(lastMessage));

            dialogCounter.getBackground().setTint(conversation.getPushSettings() != null && conversation.getPushSettings().isNotificationsDisabled() ? Color.GRAY : colorHighlight);
        }

        private String getTime(VKMessage lastMessage) {
            long time = lastMessage.getDate() * 1000L;

            Calendar thenCal = new GregorianCalendar();
            thenCal.setTimeInMillis(time);

            Calendar nowCal = new GregorianCalendar();
            nowCal.setTimeInMillis(System.currentTimeMillis());

            DateFormat formatter =
                    (thenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                            && thenCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
                            && thenCal.get(Calendar.DAY_OF_MONTH) == nowCal.get(Calendar.DAY_OF_MONTH))

                            ? DateFormat.getTimeInstance(DateFormat.SHORT) :
                            (thenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                                    && thenCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
                                    && nowCal.get(Calendar.DAY_OF_MONTH) - thenCal.get(Calendar.DAY_OF_MONTH) < 7)

                                    ? new SimpleDateFormat("EEE", Locale.getDefault())
                                    : DateFormat.getDateInstance(DateFormat.SHORT);

            return formatter.format(thenCal.getTime());
//            Calendar nowTime = Calendar.getInstance();
//            nowTime.setTimeInMillis(System.currentTimeMillis());
//
//            Calendar thenTime = (Calendar) nowTime.clone();
//            thenTime.setTimeInMillis(time * 1000L);
//
//            int nowYear = nowTime.get(Calendar.YEAR);
//            int thenYear = thenTime.get(Calendar.YEAR);
//
//            int nowMonth = nowTime.get(Calendar.MONTH);
//            int thenMonth = thenTime.get(Calendar.MONTH);
//
//            int nowDay = nowTime.get(Calendar.DAY_OF_MONTH);
//            int thenDay = thenTime.get(Calendar.DAY_OF_MONTH);
//
//            if (nowYear > thenYear) {
//                return Util.yearFormatter.format(time * 1000L);
//            } else if (nowMonth > thenMonth) {
//                return Util.monthFormatter.format(time * 1000L);
//            } else {
//                if (nowDay - thenDay == 1) {
//                    return getContext().getString(R.string.message_date_yesterday);
//                } else if (nowDay - thenDay > 1) {
//                    return Util.monthFormatter.format(time * 1000L);
//                }
//            }
//
//            return Util.timeFormatter.format(time * 1000L);
        }

        @Nullable
        private Drawable getOnlineIcon(VKConversation conversation, VKUser peerUser) {
            if (conversation.isUser() && peerUser != null) {
                if (!peerUser.isOnline()) {
                    return null;
                } else {
                    return getContext().getDrawable(peerUser.isOnlineMobile() ? R.drawable.ic_online_mobile : R.drawable.ic_online_pc);
                }
            } else return null;
        }

        @NonNull
        private String getActionText(VKMessage lastMessage) {
            switch (lastMessage.getAction().getType()) {
                case VKMessage.ACTION_CHAT_CREATE:
                    return getContext().getString(R.string.message_action_created_chat, "");
                case VKMessage.ACTION_CHAT_INVITE_USER:
                    if (lastMessage.getFromId() == lastMessage.getAction().getMemberId()) {
                        return getContext().getString(R.string.message_action_returned_to_chat, "");
                    } else {
                        VKUser invited = MemoryCache.getUser(lastMessage.getAction().getMemberId());
                        return getContext().getString(R.string.message_action_invited_user, invited);
                    }
                case VKMessage.ACTION_CHAT_INVITE_USER_BY_LINK:
                    return getContext().getString(R.string.message_action_invited_by_link, "");
                case VKMessage.ACTION_CHAT_KICK_USER:
                    if (lastMessage.getFromId() == lastMessage.getAction().getMemberId()) {
                        return getContext().getString(R.string.message_action_left_from_chat, "");
                    } else {
                        VKUser kicked = MemoryCache.getUser(lastMessage.getAction().getMemberId());
                        return getContext().getString(R.string.message_action_kicked_user, kicked);
                    }
                case VKMessage.ACTION_CHAT_PHOTO_REMOVE:
                    return getContext().getString(R.string.message_action_removed_photo, "");
                case VKMessage.ACTION_CHAT_PHOTO_UPDATE:
                    return getContext().getString(R.string.message_action_updated_photo, "");
                case VKMessage.ACTION_CHAT_PIN_MESSAGE:
                    return getContext().getString(R.string.message_action_pinned_message, "");
                case VKMessage.ACTION_CHAT_UNPIN_MESSAGE:
                    return getContext().getString(R.string.message_action_unpinned_message, "");
                case VKMessage.ACTION_CHAT_TITLE_UPDATE:
                    return getContext().getString(R.string.message_action_updated_title, "");
            }


            return lastMessage.getAction().getType();
        }

        private String getAttachmentText(ArrayList<VKModel> attachments) {
            int resId;

            if (!ArrayUtil.isEmpty(attachments)) {
                if (attachments.size() > 1) {
                    return getContext().getString(R.string.message_attachments_many);
                } else {
                    VKModel attachment = attachments.get(0);

                    if (attachment instanceof VKPhoto) {
                        resId = R.string.message_attachment_photo;
                    } else if (attachment instanceof VKAudio) {
                        resId = R.string.message_attachment_audio;
                    } else if (attachment instanceof VKVideo) {
                        resId = R.string.message_attachment_video;
                    } else if (attachment instanceof VKDoc) {
                        resId = R.string.message_attachment_doc;
                    } else if (attachment instanceof VKGraffiti) {
                        resId = R.string.message_attachment_graffiti;
                    } else if (attachment instanceof VKAudioMessage) {
                        resId = R.string.message_attachment_voice;
                    } else if (attachment instanceof VKSticker) {
                        resId = R.string.message_attachment_sticker;
                    } else if (attachment instanceof VKGift) {
                        resId = R.string.message_attachment_gift;
                    } else if (attachment instanceof VKLink) {
                        resId = R.string.message_attachment_link;
                    } else if (attachment instanceof VKPoll) {
                        resId = R.string.message_attachment_poll;
                    } else {
                        String s = attachments.getClass().getSimpleName();
                        return s.substring(1);
                    }

                }
            } else {
                return "";
            }

            return getContext().getString(resId);
        }

        private String getFwdText(ArrayList<VKMessage> forwardedMessages) {
            if (!ArrayUtil.isEmpty(forwardedMessages)) {
                return getContext().getString(forwardedMessages.size() > 1 ? R.string.message_fwd_many : R.string.message_fwd_one);
            }

            return "";
        }

        @Nullable
        private Drawable getDialogType(VKConversation conversation) {
            if (conversation.isChannel()) {
                return ContextCompat.getDrawable(getContext(), R.drawable.ic_newspaper_variant);
            } else if (conversation.isChat()) {
                return ContextCompat.getDrawable(getContext(), R.drawable.ic_people);
            } else return null;
        }

        private void loadImage(String imageUrl, ImageView imageView) {
            if (!TextUtils.isEmpty(imageUrl)) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(placeholderNormal)
                        .into(imageView);
            } else {
                imageView.setImageDrawable(placeholderNormal);
            }
        }

        private String getTitle(VKConversation conversation, VKUser peerUser, VKGroup peerGroup) {
            if (conversation.isUser()) {
                if (peerUser != null) {
                    return peerUser.toString();
                }
            } else if (conversation.isGroup()) {
                if (peerGroup != null) {
                    return peerGroup.getName();
                }
            } else {
                return conversation.getChatSettings().getTitle();
            }

            return "it\'s title";
        }

        private String getAvatar(VKConversation conversation, VKUser peerUser, VKGroup peerGroup) {
            if (conversation.isUser()) {
                if (peerUser != null) {
                    return peerUser.getPhoto200();
                }

                return null;
            } else if (conversation.isGroup()) {
                if (peerGroup != null) {
                    return peerGroup.getPhoto200();
                }

                return null;
            }

            if (conversation.getChatSettings().getPhoto() == null) return null;

            return conversation.getChatSettings().getPhoto().getPhoto200();
        }

        private String getUserAvatar(VKMessage message, VKUser fromUser, VKGroup fromGroup) {
            if (message.isFromUser()) {
                if (fromUser != null) {
                    return fromUser.getPhoto100();
                }
            } else if (message.isFromGroup()) {
                if (fromGroup != null) {
                    return fromGroup.getPhoto100();
                }
            }

            return null;
        }

        private VKUser searchPeerUser(VKMessage message) {
            return CacheStorage.getUser(message.getPeerId());
        }

        private VKUser searchFromUser(VKMessage message) {
            return CacheStorage.getUser(message.getFromId());
        }

        private VKGroup searchPeerGroup(VKMessage message) {
            return CacheStorage.getGroup(Math.abs(message.getPeerId()));
        }

        private VKGroup searchFromGroup(VKMessage message) {
            return CacheStorage.getGroup(message.getFromId());
        }
    }

    @Override
    public void destroy() {
        EventBus.getDefault().unregister(this);
    }
}
