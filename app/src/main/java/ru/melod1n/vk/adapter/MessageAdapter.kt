package ru.melod1n.vk.adapter

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import ru.melod1n.vk.R
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.widget.BoundedLinearLayout
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(context: Context, values: ArrayList<VKMessage>) : BaseAdapter<VKMessage, MessageAdapter.ViewHolder>(context, values) {

    inner class ViewHolder(v: View) : BaseAdapter.Holder(v) {
        private val text: TextView = v.findViewById(R.id.messageText)
        private val date: TextView = v.findViewById(R.id.messageDate)
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
                gravity = if (message.isOut) Gravity.END else Gravity.START
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