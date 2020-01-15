package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable
import java.util.*

class VKPhoto(o: JSONObject) : VKModel(), Serializable {
    var id = 0
    var albumId = 0
    var ownerId = 0
    var text: String? = null
    var date = 0
    var sizes: ArrayList<VKPhotoSize>? = null
    var width = 0
    var height = 0

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        id = o.optInt("id", -1)
        albumId = o.optInt("album_id", -1)
        ownerId = o.optInt("owner_id", -1)
        text = o.optString("text")
        date = o.optInt("date")
        val oSizes = o.optJSONArray("sizes")
        if (oSizes != null) {
            var sizes = ArrayList<VKPhotoSize>()
            for (i in 0 until oSizes.length()) {
                sizes.add(VKPhotoSize(oSizes.optJSONObject(i)))
            }
            sizes = sizes
        }
        width = o.optInt("width")
        height = o.optInt("height")
    }
}