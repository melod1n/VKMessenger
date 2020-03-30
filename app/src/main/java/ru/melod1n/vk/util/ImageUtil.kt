package ru.melod1n.vk.util

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.squareup.picasso.Picasso

object ImageUtil {

    fun loadImage(image: String?, imageView: ImageView, placeholder: Drawable?) {
        if (image == null || image.isEmpty()) return

        val picasso = Picasso.get()
                .load(image)
                .priority(Picasso.Priority.LOW)

        if (placeholder != null) picasso.placeholder(placeholder)

        picasso.into(imageView)
    }

}