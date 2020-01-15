package ru.melod1n.vk.api.model

import org.json.JSONArray
import java.util.*

object VKAttachments {
    private const val TYPE_PHOTO = "photo"
    private const val TYPE_VIDEO = "video"
    private const val TYPE_AUDIO = "audio"
    private const val TYPE_DOC = "doc"
    private const val TYPE_LINK = "link"
    private const val TYPE_STICKER = "sticker"
    private const val TYPE_GIFT = "gift"
    private const val TYPE_AUDIO_MESSAGE = "audio_message"
    private const val TYPE_GRAFFITI = "graffiti"
    private const val TYPE_POLL = "poll"
    private const val TYPE_GEO = "geo"
    private const val TYPE_WALL = "wall"
    private const val TYPE_CALL = "call"
    private const val TYPE_STORY = "story"
    private const val TYPE_POINT = "point"
    private const val TYPE_MARKET = "market"
    private const val TYPE_ARTICLE = "article"
    private const val TYPE_PODCAST = "podcast"
    private const val TYPE_WALL_REPLY = "wall_reply"
    private const val TYPE_MONEY_REQUEST = "money_request"
    private const val TYPE_AUDIO_PLAYLIST = "audio_playlist"
    fun parse(array: JSONArray): ArrayList<VKModel> {
        val attachments = ArrayList<VKModel>(array.length())
        for (i in 0 until array.length()) {
            var attachment = array.optJSONObject(i)
            if (attachment!!.has("attachment")) {
                attachment = attachment.optJSONObject("attachment")
            }
            if (attachment == null) continue
            val type = attachment.optString("type")
            val `object` = attachment.optJSONObject(type) ?: continue
            when (type) {
                TYPE_PHOTO -> attachments.add(VKPhoto(`object`))
                TYPE_AUDIO -> attachments.add(VKAudio(`object`))
                TYPE_VIDEO -> attachments.add(VKVideo(`object`))
                TYPE_DOC -> attachments.add(VKDoc(`object`))
                TYPE_STICKER -> attachments.add(VKSticker(`object`))
                TYPE_LINK -> attachments.add(VKLink(`object`))
                TYPE_GIFT -> attachments.add(VKGift(`object`))
                TYPE_AUDIO_MESSAGE -> attachments.add(VKAudioMessage(`object`))
                TYPE_GRAFFITI -> attachments.add(VKGraffiti(`object`))
                TYPE_POLL -> attachments.add(VKPoll(`object`))
                TYPE_CALL -> attachments.add(VKCall(`object`))
            }
        }
        return attachments
    }
}