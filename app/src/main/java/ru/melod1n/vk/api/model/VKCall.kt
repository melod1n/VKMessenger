package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable

class VKCall(o: JSONObject) : VKModel(), Serializable {
    var initiatorId = 0
    var receiverId = 0
    var state: String? = null
    var time = 0
    var duration = 0

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        initiatorId = o.optInt("initiator_id", -1)
        receiverId = o.optInt("receiver_id", -1)
        state = o.optString("state") //reached, canceled_by_initiator, canceled_by_receiver
        time = o.optInt("time")
        duration = o.optInt("duration")
    }
}