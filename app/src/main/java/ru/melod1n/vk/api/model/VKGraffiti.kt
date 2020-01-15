package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable

class VKGraffiti(o: JSONObject) : VKModel(), Serializable {
    var id = 0
    var ownerId = 0
    var url: String? = null
    var width = 0
    var height = 0
    var accessKey: String? = null

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        id = o.optInt("id", -1)
        ownerId = o.optInt("owner_id", -1)
        url = o.optString("url")
        width = o.optInt("width")
        height = o.optInt("height")
        accessKey = o.optString("access_key")
    }
}