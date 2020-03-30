package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable

class VKGroup : VKModel, Serializable {

    var id = 0
    var name: String? = null
    var screenName: String? = null
    var isClosed = false
    var deactivated: String? = null
    var type: String? = null
    var photo50: String? = null
    var photo100: String? = null
    var photo200: String? = null

    constructor()
    constructor(o: JSONObject) {
        id = o.optInt("id", -1)
        name = o.optString("name")
        screenName = o.optString("screen_name")
        isClosed = o.optInt("is_closed") == 1
        deactivated = o.optString("deactivated")
        type = o.optString("type")
        photo50 = o.optString("photo_50")
        photo100 = o.optString("photo_100")
        photo200 = o.optString("photo_200")
    }

    companion object {
        const val DEFAULT_FIELDS = "description,members_count,counters,status,verified"
        private const val serialVersionUID = 1L
        fun isGroupId(id: Int): Boolean {
            return id < 0
        }
    }
}