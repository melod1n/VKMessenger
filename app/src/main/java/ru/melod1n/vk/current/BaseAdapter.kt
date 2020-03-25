package ru.melod1n.vk.current

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.io.Serializable
import java.util.*
import kotlin.math.abs

@Suppress("UNCHECKED_CAST")
abstract class BaseAdapter<T, VH : BaseAdapter.Holder>(var context: Context, var values: ArrayList<T>) : RecyclerView.Adapter<VH>() {

    protected var inflater: LayoutInflater = LayoutInflater.from(context)

    private val headers = ArrayList<View>()
    private val footers = ArrayList<View>()
    private var manager: RecyclerView.LayoutManager? = null

    var onItemClickListener: OnItemClickListener? = null
    var onItemLongClickListener: OnItemLongClickListener? = null

    open fun onDestroy() {}

    val realItemCount: Int
        get() = values.size

    open fun getItem(position: Int): T {
        return values[position]
    }

    fun add(position: Int, item: T) {
        values.add(position, item)
        notifyItemInserted(position)
        val positionStart = position
        val itemCount = values.size - position
        notifyItemRangeChanged(positionStart, itemCount)
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
        notifyItemChanged(position)
    }

    fun indexOf(`object`: T): Int {
        return values.indexOf(`object`)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): VH {
        return if (type != TYPE_HEADER && type != TYPE_FOOTER) {
            onCreateItemViewHolder(viewGroup, type)
        } else {
            val frameLayout = FrameLayout(viewGroup.context)
            setHeaderFooterLayoutParams(frameLayout)
            HeaderFooterViewHolder(frameLayout) as VH
        }
    }

    protected fun setHeaderFooterLayoutParams(viewGroup: ViewGroup) {
        val layoutParams: ViewGroup.LayoutParams
        layoutParams = if (manager is LinearLayoutManager) {
            val orientation = (manager as LinearLayoutManager).orientation
            if (orientation == LinearLayoutManager.VERTICAL) {
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
            } else {
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
            }
        } else {
            ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        viewGroup.layoutParams = layoutParams
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindItemViewHolder(holder, position, getItemType(position))
    }

    protected fun initListeners(itemView: View, position: Int) {
        itemView.setOnClickListener { if (onItemClickListener != null) onItemClickListener!!.onItemClick(position) }
        itemView.setOnLongClickListener {
            if (onItemLongClickListener != null) onItemLongClickListener!!.onItemLongClick(position)
            onItemClickListener == null
        }
    }

    protected fun onCreateItemViewHolder(parent: ViewGroup?, type: Int): VH {
        return viewHolder(inflater.inflate(layoutId(type), parent, false), type)
    }

    override fun getItemCount(): Int {
        return realItemCount
    }

    override fun getItemViewType(position: Int): Int {
        return getItemType(position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        if (manager == null) {
            setManager(recyclerView.layoutManager)
        }
    }

    private fun setManager(manager: RecyclerView.LayoutManager?) {
        this.manager = manager
        if (this.manager is StaggeredGridLayoutManager) {
            (this.manager as StaggeredGridLayoutManager?)!!.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        }
    }

    protected fun getItemType(position: Int): Int {
        return 0
    }

    protected fun onBindItemViewHolder(holder: VH, position: Int, type: Int) {
        initListeners(holder.itemView, position)
        holder.bind(position)
    }

    protected abstract fun viewHolder(view: View, type: Int): VH
    @LayoutRes
    protected abstract fun layoutId(type: Int): Int

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

    interface SpanItemInterface {
        val gridSpan: Int
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    //our header/footer RecyclerView.ViewHolder is just a FrameLayout
    class HeaderFooterViewHolder(itemView: View) : Holder(itemView) {
        override fun bind(position: Int) {}
    }

    abstract class Holder(v: View) : RecyclerView.ViewHolder(v) {
        abstract fun bind(position: Int)
    }

    companion object {
        const val TYPE_HEADER = 7898
        const val TYPE_FOOTER = 7899

        private const val P_ITEMS = "BaseAdapter.values"
        private const val P_HEADERS = "BaseAdapter.headers"
        private const val P_FOOTERS = "BaseAdapter.footers"
    }

}