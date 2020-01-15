package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable
import java.util.*

open class VKUser : VKModel, Serializable {
    var id = 0
    var firstName: String? = null
    var lastName: String? = null
    var deactivated: String? = null
    var isClosed = false
    var isCanAccessClosed = false
    var sex = 0
    var screenName: String? = null
    var photo50: String? = null
    var photo100: String? = null
    var photo200: String? = null
    var isOnline = false
    var isOnlineMobile = false
    var status: String? = null
    var lastSeen: LastSeen? = null
    var isVerified = false

    constructor()
    constructor(o: JSONObject) {
        id = o.optInt("id", -1)
        firstName = o.optString("first_name")
        lastName = o.optString("last_name")
        deactivated = o.optString("deactivated")
        isClosed = o.optBoolean("is_closed")
        isCanAccessClosed = o.optBoolean("can_access_closed")
        sex = o.optInt("sex")
        screenName = o.optString("screen_name")
        photo50 = o.optString("photo_50")
        photo100 = o.optString("photo_100")
        photo200 = o.optString("photo_200")
        isOnline = o.optInt("online") == 1
        if (isOnline) isOnlineMobile = o.optInt("online_mobile") == 1
        status = o.optString("status")
        val oLastSeen = o.optJSONObject("last_seen")
        if (oLastSeen != null) {
            lastSeen = LastSeen(oLastSeen)
        }
        isVerified = o.optInt("verified") == 1
    }

    class LastSeen : Serializable {
        var time = 0
        var platform = 0

        constructor()
        constructor(o: JSONObject) {
            time = o.optInt("time")
            platform = o.optInt("platform")
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    override fun toString(): String {
        return "$firstName $lastName"
    }

    override fun asList(): ArrayList<VKUser> {
        return ArrayList(listOf(this))
    }

    companion object {
        private const val serialVersionUID = 1L
        const val DEFAULT_FIELDS = "photo_50,photo_100,photo_200,status,screen_name,online,online_mobile,last_seen,verified,sex"
        val EMPTY: VKUser = object : VKUser() {
            override fun toString(): String {
                return "Unknown Unknown"
            }
        }

        fun isUserId(id: Int): Boolean {
            return id > 0 && id < 2000000000
        }
    }
}