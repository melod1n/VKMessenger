package ru.melod1n.vk.util;

import android.graphics.Bitmap;

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

}
