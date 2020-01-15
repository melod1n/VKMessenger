package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable

class VKPhotoSize(o: JSONObject) : Serializable {
    var type: String? = null
    var url: String? = null
    var height = 0
    var width = 0

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        type = o.optString("type")
        url = o.optString("url")
        height = o.optInt("height")
        width = o.optInt("width")
    }
}