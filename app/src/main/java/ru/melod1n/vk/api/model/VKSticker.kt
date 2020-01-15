package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable
import java.util.*

class VKSticker(o: JSONObject) : VKModel(), Serializable {
    var productId = 0
    var stickerId = 0
    var images: ArrayList<Image>? = null

    inner class Image(o: JSONObject) : Serializable {
        var url: String? = null
        var width = 0
        var height = 0

        init {
            url = o.optString("url")
            width = o.optInt("width")
            height = o.optInt("height")
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        productId = o.optInt("product_id", -1)
        stickerId = o.optInt("sticker_id", -1)
        val oImages = o.optJSONArray("images")
        if (oImages != null) {
            var images = ArrayList<Image>()
            for (i in 0 until oImages.length()) {
                images.add(Image(oImages.optJSONObject(i)))
            }
            images = images
        }
    }
}