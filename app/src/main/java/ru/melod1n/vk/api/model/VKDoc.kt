package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable
import java.util.*

class VKDoc(o: JSONObject) : VKModel(), Serializable {
    var id = 0
    var ownerId = 0
    var title: String? = null
    var size = 0
    var ext: String? = null
    var url: String? = null
    var date = 0
    var type = 0
    var preview: Preview? = null

    inner class Preview(o: JSONObject) : Serializable {
        var photo: Photo? = null
        var graffiti: Graffiti? = null

        inner class Photo(o: JSONObject) : Serializable {
            var sizes: ArrayList<VKPhotoSize>? = null

            init {
                val oSizes = o.optJSONArray("sizes")
                if (oSizes != null) {
                    val sizes = ArrayList<VKPhotoSize>()
                    for (i in 0 until oSizes.length()) {
                        sizes.add(VKPhotoSize(oSizes.optJSONObject(i)))
                    }
                    this.sizes = sizes
                }
            }
        }

        inner class Graffiti(o: JSONObject) : Serializable {
            var src: String? = null
            var width = 0
            var height = 0

            init {
                src = o.optString("src")
                width = o.optInt("width")
                height = o.optInt("height")
            }
        }

        init {
            val oPhoto = o.optJSONObject("photo")
            if (oPhoto != null) {
                photo = Photo(oPhoto)
            }
            val oGraffiti = o.optJSONObject("graffiti")
            if (oGraffiti != null) {
                graffiti = Graffiti(oGraffiti)
            }
        }
    }

    companion object {
        private const val serialVersionUID = 1L
        const val TYPE_NONE = 0
        const val TYPE_TEXT = 1
        const val TYPE_ARCHIVE = 2
        const val TYPE_GIF = 3
        const val TYPE_IMAGE = 4
        const val TYPE_AUDIO = 5
        const val TYPE_VIDEO = 6
        const val TYPE_BOOK = 7
        const val TYPE_UNKNOWN = 8
    }

    init {
        id = o.optInt("id", -1)
        ownerId = o.optInt("owner_id", -1)
        title = o.optString("title")
        size = o.optInt("size")
        ext = o.optString("ext")
        url = o.optString("url")
        date = o.optInt("date")
        type = o.optInt("type")
        val oPreview = o.optJSONObject("preview")
        if (oPreview != null) {
            preview = Preview(oPreview)
        }
    }
}