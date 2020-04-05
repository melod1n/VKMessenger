package ru.melod1n.vk.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import ru.melod1n.vk.R
import ru.melod1n.vk.activity.MessagesActivity
import ru.melod1n.vk.api.VKLongPollParser
import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.api.model.VKGroup
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.api.util.VKUtil
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.util.AndroidUtils
import ru.melod1n.vk.util.ArrayUtil
import ru.melod1n.vk.util.ImageUtil
import ru.melod1n.vk.util.Util
import ru.melod1n.vk.widget.BoundedLinearLayout
import ru.melod1n.vk.widget.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class MessageAdapter(context: Context, values: ArrayList<VKMessage>, var conversation: VKConversation) : BaseAdapter<VKMessage, MessageAdapter.BaseHolder>(context, values), VKLongPollParser.OnMessagesListener,
        VKLongPollParser.OnEventListener {

    private var layoutManager = (context as MessagesActivity).getRecyclerView().layoutManager as LinearLayoutManager
    private var recyclerView = (context as MessagesActivity).getRecyclerView()

    companion object {
        private const val TYPE_TIME_STAMP = 7900

        private const val TYPE_NORMAL_IN = 7910
        private const val TYPE_NORMAL_OUT = 7911

        private const val TYPE_ATTACHMENT_IN = 7920
        private const val TYPE_ATTACHMENT_OUT = 7921

        private const val TYPE_ACTION = 7930

        private const val TYPE_NORMAL_CHANNEL = 7940
    }

    init {
        VKLongPollParser.addOnMessagesListener(this)
        VKLongPollParser.addOnEventListener(this)
    }

    override fun onDestroy() {
        VKLongPollParser.removeOnMessagesListener(this)
        VKLongPollParser.removeOnEventListener(this)
    }

    override fun getItemCount(): Int {
        return values.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == values.size) return TYPE_FOOTER

        val message = getItem(position)

        if (message is TimeStamp) return TYPE_TIME_STAMP

        return when {
            message.action != null -> TYPE_ACTION
            conversation.isGroupChannel -> TYPE_NORMAL_CHANNEL
            message.isOut && ArrayUtil.isEmpty(message.attachments) && ArrayUtil.isEmpty(message.fwdMessages) -> TYPE_NORMAL_OUT
            !message.isOut && ArrayUtil.isEmpty(message.attachments) && ArrayUtil.isEmpty(message.fwdMessages) -> TYPE_NORMAL_IN
            message.isOut && (!ArrayUtil.isEmpty(message.attachments) || !ArrayUtil.isEmpty(message.fwdMessages)) -> TYPE_ATTACHMENT_OUT
            !message.isOut && (!ArrayUtil.isEmpty(message.attachments) || !ArrayUtil.isEmpty(message.fwdMessages)) -> TYPE_ATTACHMENT_IN
            else -> 0
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): BaseHolder {
        return when (type) {
            TYPE_FOOTER -> FooterHolder(generateEmptyView())

            TYPE_TIME_STAMP -> TimeStampHolder(view(R.layout.item_message_timestamp, viewGroup))

            TYPE_NORMAL_IN -> ItemNormalIn(view(R.layout.item_message_normal_in, viewGroup))
            TYPE_NORMAL_OUT -> ItemNormalOut(view(R.layout.item_message_normal_out, viewGroup))

            TYPE_ATTACHMENT_IN -> ItemAttachmentIn(view(R.layout.item_message_attachment_in, viewGroup))
            TYPE_ATTACHMENT_OUT -> ItemAttachmentOut(view(R.layout.item_message_attachment_out, viewGroup))

            TYPE_ACTION -> ItemAction(view(R.layout.item_message_action, viewGroup))

            TYPE_NORMAL_CHANNEL -> ItemChannel(view(R.layout.item_message_channel, viewGroup))

            else -> PlaceHolder(view(R.layout.item_message, viewGroup))
        }
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        if (holder is FooterHolder) return

        when (holder) {
            is TimeStampHolder -> holder.bind(position)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    class TimeStamp(var time: Long) : VKMessage()

    private fun generateEmptyView(): View {
        return View(context).also {
            it.isFocusable = false
            it.isClickable = false
            it.isEnabled = false
            it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, if (conversation.isGroupChannel) 0 else AndroidUtils.px(74f))
        }
    }

    inner class TimeStampHolder(v: View) : BaseHolder(v) {

        private val stamp: TextView = v.findViewById(R.id.messageTimeStamp)

        override fun bind(position: Int) {
            val item = getItem(position) as TimeStamp
            val nowTime = Util.removeTime(Date(System.currentTimeMillis()))

            stamp.text = when {
                item.time == nowTime -> context.getString(R.string.today)
                nowTime - item.time == DateUtils.DAY_IN_MILLIS -> context.getString(R.string.yesterday)
                else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(item.time)
            }
        }
    }

    inner class FooterHolder(v: View) : BaseHolder(v) {
        override fun bind(position: Int) {}
    }

    inner class ItemChannel(v: View) : ItemNormalIn(v) {
        private val title: TextView = v.findViewById(R.id.channelTitle)

        override fun bind(position: Int) {
            val message = getItem(position)

            ViewController().prepareDate(message, date)

            val avatarString = conversation.photo100

            val placeHolder = VKUtil.getAvatarPlaceholder(conversation.title)

            avatar.setImageDrawable(placeHolder)
            ImageUtil.loadImage(avatarString, avatar, placeHolder)

            title.text = conversation.title

            text.text = message.text
        }
    }

    inner class ItemAction(v: View) : BaseHolder(v) {
        private val text: TextView = v.findViewById(R.id.messageAction)

        override fun bind(position: Int) {
            val message = getItem(position)

            val user = VKUtil.searchUser(message.fromId)
            val group = VKUtil.searchGroup(message.fromId)

            val name = (if (group == null && !VKGroup.isGroupId(message.fromId)) user?.firstName else group?.name)
                    ?: "null"

            val actionText = "$name${VKUtil.getActionText(context, message)}"

            val spannable = SpannableString(actionText)
            spannable.setSpan(StyleSpan(Typeface.BOLD), 0, name.length, 0)

            text.text = spannable
        }
    }

    open inner class ItemNormalIn(v: View) : NormalViewHolder(v) {

        override fun bind(position: Int) {
            val message = getItem(position)

            val user = VKUtil.searchUser(message.fromId)
            val group = VKUtil.searchGroup(message.fromId)

            ViewController().apply {
                prepareText(message, bubble, text)
                prepareDate(message, date)
                prepareAvatar(message, avatar)
                loadAvatarImage(message, user, group, avatar)
            }

        }
    }

    inner class ItemAttachmentIn(v: View) : ItemNormalIn(v) {
    }


    open inner class ItemNormalOut(v: View) : NormalViewHolder(v) {

        override fun bind(position: Int) {
            val message = getItem(position)

            val user = VKUtil.searchUser(message.fromId)
            val group = VKUtil.searchGroup(message.fromId)

            ViewController().apply {
                prepareText(message, bubble, text)
                prepareDate(message, date)
                prepareAvatar(message, avatar)
                loadAvatarImage(message, user, group, avatar)
            }
        }
    }

    inner class ItemAttachmentOut(v: View) : ItemNormalOut(v) {
    }

    abstract inner class NormalViewHolder(v: View) : BaseHolder(v) {
        protected val date: TextView = v.findViewById(R.id.messageDate)
        protected val text: TextView = v.findViewById(R.id.messageText)
        protected val root: LinearLayout = v.findViewById(R.id.messageRoot)
        protected val bubble: BoundedLinearLayout = v.findViewById(R.id.messageBubble)
        protected val avatar: CircleImageView = v.findViewById(R.id.messageAvatar)
    }

    inner class ViewController {

        fun prepareText(message: VKMessage, bubble: BoundedLinearLayout, text: TextView) {
            bubble.maxWidth = AppGlobal.screenWidth - AppGlobal.screenWidth / 4
            text.text = message.text
        }

        fun prepareDate(message: VKMessage, date: TextView) {
            date.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.date * 1000L)
        }

        fun prepareAvatar(message: VKMessage, avatar: ImageView) {
            avatar.visibility = if (message.isOut) View.GONE else View.VISIBLE
        }

        fun loadAvatarImage(message: VKMessage, user: VKUser?, group: VKGroup?, avatar: ImageView) {
            val dialogTitle = if (group == null && !VKGroup.isGroupId(message.fromId)) user.toString() else group!!.name

            val avatarPlaceholder = VKUtil.getAvatarPlaceholder(dialogTitle)

            avatar.setImageDrawable(avatarPlaceholder)
            val avatarString = if (group == null && !VKGroup.isGroupId(message.fromId)) user?.photo100 else group!!.photo100

            ImageUtil.loadImage(avatarString, avatar, avatarPlaceholder)
        }

    }

    open inner class BaseHolder(v: View) : BaseAdapter.Holder(v) {
        override fun bind(position: Int) {
        }
    }

    inner class PlaceHolder(v: View) : NormalViewHolder(v)

    private fun updateGroup(groupId: Int) {
        var index = -1

        for (i in values.indices) {
            val item = getItem(i)

            if (abs(item.fromId) == groupId) {
                index = i
                break
            }
        }

        if (index == -1) return

        notifyItemChanged(index)
    }

    private fun updateUser(userId: Int) {
        var index = -1

        for (i in values.indices) {
            val item = getItem(i)

            if (item.fromId == userId) {
                index = i
                break
            }
        }

        if (index == -1) return
        notifyItemChanged(index)
    }

    private fun updateMessage(messageId: Int) {
        var index = -1

        for (i in values.indices) {
            val item = getItem(i)

            if (item.id == messageId) {
                index = i
                break
            }
        }

        if (index == -1) return

        values[index] = CacheStorage.getMessage(messageId)!!
        notifyItemChanged(index)
    }

    private fun containsRandomId(randomId: Int): Boolean {
        for (message in values) {
            if (message.randomId == randomId) return true
        }

        return false
    }

    override fun onEvent(info: EventInfo<*>) {
        when (info.key) {
            EventInfo.MESSAGE_UPDATE -> updateMessage(info.data as Int)
            EventInfo.USER_UPDATE -> updateUser(info.data as Int)
            EventInfo.GROUP_UPDATE -> updateGroup(info.data as Int)
        }
    }

    override fun onNewMessage(message: VKMessage) {
        if (containsRandomId(message.randomId)) return

        add(message)
        notifyDataSetChanged()

        val lastPosition = layoutManager.findLastVisibleItemPosition()

        if (lastPosition >= itemCount - 2) {
            recyclerView.smoothScrollToPosition(itemCount + 2)
        }
    }

    override fun onEditMessage(message: VKMessage) {

    }

    override fun onReadMessage(messageId: Int, peerId: Int) {

    }

    override fun onDeleteMessage(messageId: Int, peerId: Int) {

    }

    override fun onRestoredMessage(message: VKMessage) {

    }
}