package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable

class VKGift(o: JSONObject) : VKModel(), Serializable {
    var id = 0
    var thumb256: String? = null
    var thumb96: String? = null
    var thumb48: String? = null

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        id = o.optInt("id", -1)
        thumb256 = o.optString("thumb_256")
        thumb96 = o.optString("thumb_96")
        thumb48 = o.optString("thumb_48")
    }
}