package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable

class VKAudio(o: JSONObject) : VKModel(), Serializable {
    var id = 0
    var ownerId = 0
    var artist: String? = null
    var title: String? = null
    var duration = 0
    var url: String? = null
    var date = 0

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        id = o.optInt("id", -1)
        ownerId = o.optInt("owner_id", -1)
        artist = o.optString("artist")
        title = o.optString("title")
        duration = o.optInt("duration")
        url = o.optString("url")
        date = o.optInt("date")
    }
}