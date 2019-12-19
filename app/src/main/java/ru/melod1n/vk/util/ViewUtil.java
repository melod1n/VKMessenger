package ru.melod1n.vk.util;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import ru.melod1n.vk.common.AppGlobal;

public class ViewUtil {

    public static Bitmap roundBitmap(Bitmap bitmap, int radius) {
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(AppGlobal.resources, bitmap);
        drawable.setCornerRadius(radius);
        drawable.setAntiAlias(true);

        return drawable.getBitmap();
    }

    public static void changeToolbarTitleFont(@NonNull Toolbar toolbar, @NonNull Typeface font) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View childView = toolbar.getChildAt(i);
            if (childView instanceof TextView && ((TextView) childView).getText().equals(toolbar.getTitle())) {
                ((TextView) childView).setTypeface(font);
                break;
            }
        }
    }

    @Nullable
    public static TextView getToolbarTitleTextView(@NonNull Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View childView = toolbar.getChildAt(i);
            if (childView instanceof TextView && ((TextView) childView).getText().equals(toolbar.getTitle())) {
                return (TextView) childView;
            }
        }

        return null;
    }

}
