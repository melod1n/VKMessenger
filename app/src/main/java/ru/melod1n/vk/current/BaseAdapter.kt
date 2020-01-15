package ru.melod1n.vk.current

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.melod1n.vk.adapter.RecyclerHolder
import java.util.*
import kotlin.collections.ArrayList

abstract class BaseAdapter<T>(val context: Context, var values: ArrayList<T>) : RecyclerView.Adapter<RecyclerHolder>() {
    protected val inflater = LayoutInflater.from(context)!!

    private var cleanValues: ArrayList<T> = ArrayList()

    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        return RecyclerHolder(View(context))
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        holder.bind(position)
        updateListeners(holder.itemView, position)
    }

    private fun updateListeners(v: View, position: Int) {
        if (onItemClickListener != null) {
            v.setOnClickListener { onItemClickListener!!.onItemClick(position) }
        }
        if (onItemLongClickListener != null) {
            v.setOnLongClickListener {
                onItemLongClickListener!!.onItemLongClick(position)
                onItemClickListener != null
            }
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }

    open fun getItem(position: Int): T {
        return values[position]
    }

    fun add(item: T) {
        values.add(item)
    }

    fun add(position: Int, item: T) {
        values.add(position, item)
    }

    fun addAll(values: ArrayList<T>) {
        this.values.addAll(values)
    }

    fun addAll(position: Int, values: ArrayList<T>) {
        this.values.addAll(position, values)
    }

    fun remove(position: Int) {
        values.removeAt(position)
    }

    fun remove(item: T) {
        values.remove(item)
    }

    fun removeAll(values: ArrayList<T>) {
        this.values.removeAll(values)
    }

    open fun changeItems(items: ArrayList<T>) {
        this.values = items
    }

    fun clear() {
        values.clear()
    }

    fun filter(query: String) {
        val lowerQuery = query.toLowerCase(Locale.getDefault())

        cleanValues = ArrayList(values)

        values.clear()
        if (query.isEmpty()) {
            values.addAll(cleanValues)
        } else {
            for (value in cleanValues) {
                if (onQueryItem(value, lowerQuery)) {
                    values.add(value)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun onQueryItem(item: T, lowerQuery: String?): Boolean {
        return false
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener?) {
        this.onItemLongClickListener = onItemLongClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    abstract fun destroy()

}