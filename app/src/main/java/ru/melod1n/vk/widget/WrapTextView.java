package ru.melod1n.vk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import ru.melod1n.vk.R;

public class WrapTextView extends AppCompatTextView {

    private boolean mFixWrapText;

    public WrapTextView(@NonNull Context context) {
        this(context, null);
    }

    public WrapTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.WrapTextView, 0, 0);
        try {
            mFixWrapText = a.getBoolean(R.styleable.WrapTextView_fixWrap, false);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mFixWrapText) {
            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
                int width = getMaxWidth(getLayout());
                if (width > 0 && width < getMeasuredWidth()) {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), heightMeasureSpec);
                }
            }
        }
    }

    private int getMaxWidth(Layout layout) {
        int linesCount = layout.getLineCount();
        if (linesCount < 2) {
            return 0;
        }

        float maxWidth = 0;
        for (int i = 0; i < linesCount; i++) {
            maxWidth = Math.max(maxWidth, layout.getLineWidth(i));
        }

        return (int) Math.ceil(maxWidth);
    }
}
