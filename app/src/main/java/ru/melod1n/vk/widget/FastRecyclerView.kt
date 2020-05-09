package ru.melod1n.vk.widget

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FastRecyclerView : RecyclerView {

    private var mScrollPosition = -1

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val layoutManager = layoutManager
        if (layoutManager is LinearLayoutManager) {
            mScrollPosition = layoutManager.findFirstVisibleItemPosition()
        }
        val newState = SavedState(superState!!)
        newState.mScrollPosition = mScrollPosition
        return newState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            mScrollPosition = state.mScrollPosition
            val layoutManager = layoutManager
            if (layoutManager != null) {
                val count = layoutManager.itemCount
                if (mScrollPosition != NO_POSITION && mScrollPosition < count) {
                    layoutManager.scrollToPosition(mScrollPosition)
                }
            }
        }
    }

    internal class SavedState : BaseSavedState {
        var mScrollPosition: Int = 0

        constructor(`in`: Parcel) : super(`in`) {
            mScrollPosition = `in`.readInt()
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(mScrollPosition)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}