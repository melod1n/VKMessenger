package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable

class VKVideo(o: JSONObject) : VKModel(), Serializable {
    var id = 0
    var ownerId = 0
    var title: String? = null
    var description: String? = null
    var duration = 0
    var photo130: String? = null
    var photo320: String? = null
    var photo640: String? = null
    var photo800: String? = null
    var photo1280: String? = null
    var firstFrame130: String? = null
    var firstFrame320: String? = null
    var firstFrame640: String? = null
    var firstFrame800: String? = null
    var firstFrame1280: String? = null
    var date = 0
    var views = 0
    var comments = 0
    var player: String? = null
    var isCanEdit = false
    var isCanAdd = false
    var isPrivate = false
    var accessKey: String? = null
    var isProcessing = false
    var isLive = false
    var isUpcoming = false
    var isFavorite = false

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        id = o.optInt("id", -1)
        ownerId = o.optInt("owner_id", -1)
        title = o.optString("title")
        description = o.optString("description")
        duration = o.optInt("duration", -1)
        photo130 = o.optString("photo_130")
        photo320 = o.optString("photo_320")
        photo640 = o.optString("photo_640")
        photo800 = o.optString("photo_800")
        photo1280 = o.optString("photo_1280")
        firstFrame130 = o.optString("first_frame_130")
        firstFrame320 = o.optString("first_frame_320")
        firstFrame640 = o.optString("first_frame_640")
        firstFrame800 = o.optString("first_frame_800")
        firstFrame1280 = o.optString("first_frame_1280")
        date = o.optInt("date")
        views = o.optInt("views")
        comments = o.optInt("comments")
        player = o.optString("player")
        isCanEdit = o.optInt("can_edit", 0) == 1
        isCanAdd = o.optInt("can_add") == 1
        isPrivate = o.optInt("is_private", 0) == 1
        accessKey = o.optString("access_key")
        isProcessing = o.optInt("processing", 0) == 1
        isLive = o.optInt("live", 0) == 1
        isUpcoming = o.optInt("upcoming", 0) == 1
        isFavorite = o.optBoolean("favorite")
    }
}