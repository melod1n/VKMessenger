package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable
import java.util.*

class VKPinnedMessage : VKModel, Serializable {
    var id = 0
    var date = 0
    var fromId = 0
    var text: String? = null
    var attachments: ArrayList<VKModel>? = null
    var fwdMessages: ArrayList<VKMessage>? = null

    constructor()
    constructor(o: JSONObject) {
        id = o.optInt("id", -1)
        date = o.optInt("date")
        fromId = o.optInt("from_id", -1)
        text = o.optString("text")
        val attachments = o.optJSONArray("attachments")
        if (attachments != null) this.attachments = VKAttachments.parse(attachments)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}