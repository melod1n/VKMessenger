package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable
import java.util.*

class VKAudioMessage(o: JSONObject) : VKModel(), Serializable {
    var duration = 0
    var waveform: ArrayList<Int>? = null
    var linkOgg: String? = null
    var linkMp3: String? = null

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        duration = o.optInt("duration")
        val oWaveform = o.optJSONArray("waveform")
        if (oWaveform != null) {
            var waveform = ArrayList<Int>()
            for (i in 0 until oWaveform.length()) {
                waveform.add(oWaveform.optInt(i))
            }
            waveform = waveform
        }
        linkOgg = o.optString("link_ogg")
        linkMp3 = o.optString("link_mp3")
    }
}