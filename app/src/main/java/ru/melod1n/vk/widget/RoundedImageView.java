package ru.melod1n.vk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import ru.melod1n.vk.R;
import ru.melod1n.vk.util.ViewUtil;

public class RoundedImageView extends AppCompatImageView {

    private int cornerRadius;

    public RoundedImageView(Context context) {
        this(context, null);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundedImageView, 0, 0);
        cornerRadius = a.getInt(R.styleable.RoundedImageView_radius, 0);

        init();
    }

    private void init() {
        if (getDrawable() != null) {
            Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            setImageBitmap(bitmap);
        }
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        if (drawable == null) super.setImageDrawable(null);

        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) drawable;

            int color = colorDrawable.getColor();
            Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            image.eraseColor(color);
        }

        setImageBitmap(bitmap);
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        bitmap = ViewUtil.roundBitmap(bitmap, cornerRadius);
        super.setImageBitmap(bitmap);
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        init();
    }
}
