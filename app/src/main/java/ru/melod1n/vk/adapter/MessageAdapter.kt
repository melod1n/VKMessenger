package ru.melod1n.vk.adapter

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import ru.melod1n.vk.R
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.util.AndroidUtils
import ru.melod1n.vk.widget.BoundedLinearLayout
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


class MessageAdapter(context: Context, values: ArrayList<VKMessage>) : BaseAdapter<VKMessage, MessageAdapter.BaseHolder>(context, values) {

    companion object {
        private const val TYPE_TIME_STAMP = 7900
    }

    open inner class BaseHolder(v: View) : BaseAdapter.Holder(v) {
        override fun bind(position: Int) {
        }
    }


    override fun getItemCount(): Int {
        return values.size + 1
    }

//    override fun getItem(position: Int): VKMessage {
//        return if (position == 0)  super.getItem(0)
//        else super.getItem(position - 1)
//    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == values.size -> TYPE_FOOTER
            getItem(position) is Timestamp -> TYPE_TIME_STAMP
            else -> 0
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): BaseHolder {
        return when (type) {
            TYPE_TIME_STAMP -> {
                TimeStampHolder(inflater.inflate(R.layout.item_message_timestamp, viewGroup, false))
            }
            TYPE_FOOTER -> {
                HeaderFooterHolder(generateEmptyView())
            }
//            TYPE_HEADER -> {
//                HeaderFooterHolder(generateEmptyView())
//            }
            else -> ViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false))
        }
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        if (holder is HeaderFooterHolder) return

        when (holder) {
            is TimeStampHolder -> holder.bind(position)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    class TimeStamp(var string: String = "") : VKMessage()

    inner class TimeStampHolder(v: View) : BaseHolder(v) {

        private val stamp: TextView = v.findViewById(R.id.messageTimeStamp)


        override fun bind(position: Int) {
            stamp.text = (getItem(position) as TimeStamp).string
        }
    }

    private fun generateEmptyView(): View {
        return View(context).also {
            it.isFocusable = false
            it.isClickable = false
            it.isEnabled = false
            it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtils.px(100F))
        }
    }

    inner class HeaderFooterHolder(v: View) : BaseHolder(v)

    open inner class ViewHolder(v: View) : BaseHolder(v) {
        private val date: TextView = v.findViewById(R.id.messageDate)
        private val text: TextView = v.findViewById(R.id.messageText)
        private val root: LinearLayout = v.findViewById(R.id.messageRoot)
        private val bubble: BoundedLinearLayout = v.findViewById(R.id.messageBubble)

        private val inBackground = ContextCompat.getDrawable(context, R.drawable.ic_message_bubble_in_simple)
        private val outBackground = ContextCompat.getDrawable(context, R.drawable.ic_message_bubble_out_simple)

        private val inTextColor = Color.WHITE
        private val outTextColor = Color.BLACK

        override fun bind(position: Int) {
            val message = getItem(position)

            bubble.maxWidth = AppGlobal.screenWidth - AppGlobal.screenWidth / 4

            text.apply {
                text = message.text
                background = if (message.isOut) outBackground else inBackground
                setTextColor(if (message.isOut) outTextColor else inTextColor)
            }

            root.gravity = if (message.isOut) Gravity.END else Gravity.START

            date.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.date * 1000L)


        }
    }

    override fun viewHolder(view: View, type: Int): ViewHolder {
        return ViewHolder(view)
    }

    override fun layoutId(type: Int): Int {
        return R.layout.item_message
    }
}