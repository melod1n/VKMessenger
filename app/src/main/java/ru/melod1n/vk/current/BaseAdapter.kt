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

    private val spanSizeLookup: SpanSizeLookup = object : SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return getGridSpan(position)
        }
    }

    open fun onDestroy() {}

    val realItemCount: Int
        get() = values.size

    open fun getItem(position: Int): T {
        return values[position]
    }

    fun add(position: Int, item: T) {
        values.add(position, item)
        notifyItemInserted(position)
        val positionStart = position + headersCount
        val itemCount = values.size - position
        notifyItemRangeChanged(positionStart, itemCount)
    }

    fun add(item: T) {
        values.add(item)
        notifyItemInserted(values.size - 1 + headersCount)
    }

    fun addAll(items: List<T>) {
        val size = values.size
        values.addAll(items)
        notifyItemRangeInserted(size + headersCount, items.size)
    }

    fun addAll(position: Int, items: List<T>) {
        values.addAll(position, items)
        notifyItemRangeInserted(position + headersCount, items.size)
    }

    operator fun set(position: Int, item: T) {
        values[position] = item
        notifyItemChanged(position + headersCount)
    }

    fun removeChild(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position + headersCount)
        val positionStart = position + headersCount
        val itemCount = values.size - position
        notifyItemRangeChanged(positionStart, itemCount)
    }

    fun clear() {
        val size = values.size
        values.clear()
        notifyItemRangeRemoved(headersCount, size)
    }

    fun moveChildTo(fromPosition: Int, toPosition: Int) {
        if (toPosition != -1 && toPosition < values.size) {
            val item = values.removeAt(fromPosition)
            values.add(toPosition, item)
            notifyItemMoved(headersCount + fromPosition, headersCount + toPosition)
            val positionStart = if (fromPosition < toPosition) fromPosition else toPosition
            val itemCount = abs(fromPosition - toPosition) + 1
            notifyItemRangeChanged(positionStart + headersCount, itemCount)
        }
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
        when {
            isHeader(position) -> {
                val v = headers[position]
                //add our view to a header view and display it
                prepareHeaderFooter(holder as HeaderFooterViewHolder, v)
            }
            isFooter(position) -> {
                val v = footers[position - realItemCount - headersCount]
                //add our view to a footer view and display it
                prepareHeaderFooter(holder as HeaderFooterViewHolder, v)
            }
            else -> { //it's one of our values, display as required
                onBindItemViewHolder(holder, position - headers.size, getItemType(position))
            }
        }
    }


    protected fun initListeners(itemView: View, position: Int) {
        itemView.setOnClickListener { if (onItemClickListener != null) onItemClickListener!!.onItemClick(position) }
        itemView.setOnLongClickListener {
            if (onItemLongClickListener != null) onItemLongClickListener!!.onItemLongClick(position)
            onItemClickListener == null
        }
    }

    private fun prepareHeaderFooter(vh: HeaderFooterViewHolder, view: View) { //if it's a staggered grid, span the whole layout
        if (manager is StaggeredGridLayoutManager) {
            val layoutParams = StaggeredGridLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutParams.isFullSpan = true
            vh.itemView.layoutParams = layoutParams
        }
        //if the view already belongs to another layout, remove it
        if (view.parent != null) {
            (view.parent as ViewGroup).removeView(view)
        }
        //empty out our FrameLayout and replace with our header/footer
        (vh.itemView as ViewGroup).removeAllViews()
        vh.itemView.addView(view)
    }

    private fun isHeader(position: Int): Boolean {
        return position < headers.size
    }

    private fun isFooter(position: Int): Boolean {
        return footers.size > 0 && position >= headersCount + realItemCount
    }

    protected fun onCreateItemViewHolder(parent: ViewGroup?, type: Int): VH {
        return viewHolder(inflater.inflate(layoutId(type), parent, false), type)
    }

    override fun getItemCount(): Int {
        return headers.size + realItemCount + footers.size
    }

    override fun getItemViewType(position: Int): Int { //check what type our position is, based on the assumption that the order is headers > values > footers
        if (isHeader(position)) {
            return TYPE_HEADER
        } else if (isFooter(position)) {
            return TYPE_FOOTER
        }
        val type = getItemType(position - headersCount)
        require(!(type == TYPE_HEADER || type == TYPE_FOOTER)) { "Item type cannot equal $TYPE_HEADER or $TYPE_FOOTER" }
        return type
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        if (manager == null) {
            setManager(recyclerView.layoutManager)
        }
    }

    private fun setManager(manager: RecyclerView.LayoutManager?) {
        this.manager = manager
        if (this.manager is GridLayoutManager) {
            (this.manager as GridLayoutManager?)!!.spanSizeLookup = spanSizeLookup
        } else if (this.manager is StaggeredGridLayoutManager) {
            (this.manager as StaggeredGridLayoutManager?)!!.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        }
    }

    protected fun getGridSpan(p: Int): Int {
        var position = p

        if (isHeader(position) || isFooter(position)) {
            return maxGridSpan
        }

        position -= headers.size
        return if (getItem(position) is SpanItemInterface) {
            (getItem(position) as SpanItemInterface).gridSpan
        } else 1
    }

    protected val maxGridSpan: Int
        get() {
            if (manager is GridLayoutManager) {
                return (manager as GridLayoutManager).spanCount
            } else if (manager is StaggeredGridLayoutManager) {
                return (manager as StaggeredGridLayoutManager).spanCount
            }
            return 1
        }

    //add a header to the adapter
    open fun addHeader(header: View) {
        if (!headers.contains(header)) {
            headers.add(header)
            //animate

            notifyDataSetChanged()
//            notifyItemInserted(headers.size - 1)
        }
    }

    //remove header from adapter
    fun removeHeader(header: View?) {
        if (headers.contains(header)) { //animate
            notifyItemRemoved(headers.indexOf(header))
            headers.remove(header)
        }
    }

    //add a footer to the adapter
    open fun addFooter(footer: View) {
        if (!footers.contains(footer)) {
            footers.add(footer)
            //animate
            notifyDataSetChanged()
//            notifyItemInserted(headers.size + itemCount + footers.size - 1)
        }
    }

    //remove footer from adapter
    fun removeFooter(footer: View?) {
        if (footers.contains(footer)) { //animate
            notifyItemRemoved(headers.size + itemCount + footers.indexOf(footer))
            footers.remove(footer)
        }
    }

    val headersCount: Int
        get() = headers.size

    fun getHeader(location: Int): View {
        return headers[location]
    }

    val footersCount: Int
        get() = footers.size

    fun getFooter(location: Int): View {
        return footers[location]
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