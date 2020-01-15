package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable

class VKLink(o: JSONObject) : VKModel(), Serializable {
    var url: String? = null
    var title: String? = null
    var caption: String? = null
    var description: String? = null
    var photo: VKPhoto? = null
    var button: Button? = null
    var previewPage: String? = null
    var previewUrl: String? = null

    inner class Button(o: JSONObject) : Serializable {
        var title: String? = null
        var action: Action? = null

        inner class Action(o: JSONObject) : Serializable {
            var type: String? = null
            var url: String? = null

            init {
                type = o.optString("type")
                url = o.optString("url")
            }
        }

        init {
            title = o.optString("title")
            val oAction = o.optJSONObject("action")
            if (oAction != null) {
                action = Action(oAction)
            }
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        url = o.optString("url")
        title = o.optString("title")
        caption = o.optString("caption")
        description = o.optString("description")
        val oPhoto = o.optJSONObject("photo")
        if (oPhoto != null) {
            photo = VKPhoto(oPhoto)
        }
        val oButton = o.optJSONObject("button")
        if (oButton != null) {
            button = Button(oButton)
        }
        previewPage = o.optString("preview_page")
        previewUrl = o.optString("preview_url")
    }
}