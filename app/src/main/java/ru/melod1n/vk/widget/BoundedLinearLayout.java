package ru.melod1n.vk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import ru.melod1n.vk.R;

public class BoundedLinearLayout extends LinearLayout {

    private int mBoundedWidth;
    private int mBoundedHeight;

    public BoundedLinearLayout(Context context) {
        super(context);
        mBoundedWidth = 0;
        mBoundedHeight = 0;
    }

    public BoundedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BoundedView);
        mBoundedWidth = a.getDimensionPixelSize(R.styleable.BoundedView_bounded_width, 0);
        mBoundedHeight = a.getDimensionPixelSize(R.styleable.BoundedView_bounded_height, 0);
        a.recycle();
    }

    public void setMaxWidth(int width) {
        if (mBoundedWidth != width) {
            mBoundedWidth = width;
            requestLayout();
        }
    }

    public int getMaxWidth() {
        return mBoundedWidth;
    }

    public void setMaxHeight(int height) {
        if (mBoundedHeight != height) {
            mBoundedHeight = height;
            requestLayout();
            ;
        }
    }

    public int getMaxHeight() {
        return mBoundedHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Adjust width as necessary
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (mBoundedWidth > 0 && mBoundedWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedWidth, measureMode);
        }
        // Adjust height as necessary
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mBoundedHeight > 0 && mBoundedHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedHeight, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}