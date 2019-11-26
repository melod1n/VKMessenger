package ru.melod1n.vk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.AppCompatImageView;

public class CircleImageView extends AppCompatImageView {

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;
    private Path path;
    private RectF rect;

    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (rect.right == 0 || rect.bottom == 0) {
            createRect(getWidth(), getHeight());
        }

        canvas.clipPath(path);
        super.onDraw(canvas);
    }

    private void init() {
        setScaleType(SCALE_TYPE);

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                createRect(getWidth(), getHeight());
                getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }

    private void createRect(int width, int height) {
        rect = new RectF(0, 0, width, height);
        path = new Path();
        path.addRoundRect(rect, width / 2, height / 2, Path.Direction.CW);
    }
}