package ru.melod1n.vk.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.ButterKnife
import ru.melod1n.vk.R
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.util.AndroidUtils
import java.util.*

class MessageAdapter(context: Context, values: ArrayList<VKMessage>) : BaseAdapter<VKMessage>(context, values) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        return if (viewType == TYPE_FOOTER) Footer(generateFooterView()) else ViewHolder(inflater.inflate(R.layout.item_message, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        if (holder is Footer) return
        super.onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int {
        return if (values.size == 0) 1 else super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == values.size) TYPE_FOOTER else super.getItemViewType(position)
    }

    override fun getItem(position: Int): VKMessage {
        return values[if (position == values.size) position - 1 else position]
    }

    override fun destroy() {

    }

    inner class ViewHolder(v: View) : RecyclerHolder(v) {
        var textView: TextView = v.findViewById(R.id.text)

        override fun bind(position: Int) {
            val message = getItem(position)
            textView.text = message.text
        }

        init {
            ButterKnife.bind(this, v)
        }
    }

    private fun generateFooterView(): View {
        val view = View(context)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtils.px(60f))
        view.isClickable = false
        view.isFocusable = false
        view.isEnabled = false
        return view
    }

    internal inner class Footer(v: View) : RecyclerHolder(v) {
        override fun bind(position: Int) {}
    }

    companion object {
        private const val TYPE_FOOTER = 1
    }
}