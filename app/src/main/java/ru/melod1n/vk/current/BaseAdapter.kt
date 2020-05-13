package ru.melod1n.vk.current

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
abstract class BaseAdapter<T, VH : BaseHolder>(var context: Context, var values: ArrayList<T>) : RecyclerView.Adapter<VH>() {

    protected var inflater: LayoutInflater = LayoutInflater.from(context)

    var onItemClickListener: OnItemClickListener? = null
    var onItemLongClickListener: OnItemLongClickListener? = null

    open fun onDestroy() {}

    open fun getItem(position: Int): T {
        return values[position]
    }

    fun add(position: Int, item: T) {
        values.add(position, item)
        notifyItemInserted(position)
        val itemCount = values.size - position
        notifyItemRangeChanged(position, itemCount)
    }

    fun add(item: T) {
        values.add(item)
        notifyItemInserted(values.size - 1)
    }

    fun addAll(items: List<T>) {
        val size = values.size
        values.addAll(items)
        notifyItemRangeInserted(size, items.size)
    }

    fun addAll(position: Int, items: List<T>) {
        values.addAll(position, items)
        notifyItemRangeInserted(position, items.size)
    }

    operator fun set(position: Int, item: T) {
        values[position] = item
    }

    fun indexOf(item: T): Int {
        return values.indexOf(item)
    }

    fun removeAt(index: Int) {
        values.removeAt(index)
    }

    fun isEmpty(): Boolean {
        return values.isNullOrEmpty()
    }

    fun view(resId: Int, viewGroup: ViewGroup): View {
        return inflater.inflate(resId, viewGroup, false)
    }

    fun setItems(list: ArrayList<T>) {
//        values = list
        values.clear()
        values.addAll(list)
    }


    fun setItems(list: List<T>) {
        values.clear()
        values.addAll(list)
//        values = ArrayList(list)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindItemViewHolder(holder, position)
    }

    fun initListeners(itemView: View, position: Int) {
        itemView.setOnClickListener { if (onItemClickListener != null) onItemClickListener!!.onItemClick(position) }
        itemView.setOnLongClickListener {
            if (onItemLongClickListener != null) onItemLongClickListener!!.onItemLongClick(position)
            onItemClickListener == null
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }

    private fun onBindItemViewHolder(holder: VH, position: Int) {
        initListeners(holder.itemView, position)
        holder.bind(position)
    }

    fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        if (values.size > 0 && (values[0] is Parcelable
                        || values[0] is Serializable)) {
            bundle.putSerializable(P_ITEMS, values)
        }
        return bundle
    }

    fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            if (state.containsKey(P_ITEMS)) {
                values = state.getSerializable(P_ITEMS) as ArrayList<T>
            }
        }
    }

    fun clear() {
        values.clear()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    companion object {
        const val TYPE_HEADER = 7898
        const val TYPE_FOOTER = 7899

        private const val P_ITEMS = "BaseAdapter.values"
    }

}