package ru.melod1n.vk.common

import android.util.ArrayMap
import org.json.JSONObject
import ru.melod1n.vk.net.HttpRequest
import ru.melod1n.vk.util.AndroidUtils

object UpdateManager {

    private val listeners = ArrayList<OnUpdateListener>()

    private const val CHECK_LINK = "https://temply.procsec.top/prop/deploy/api/method/getOTA"

    fun addOnUpdateListener(listener: OnUpdateListener) {
        listeners.add(listener)
    }

    fun removeOnUpdateListener(listener: OnUpdateListener?) {
        listeners.remove(listener)
    }

    fun checkUpdates() {
        if (!AndroidUtils.hasConnection()) {
            sendEvent(null, null)
            return
        }

        TaskManager.execute {
            try {
                val params = ArrayMap<String, String>()
                params["product"] = "vkm"
                params["branch"] = "alpha"
                params["offset"] = "0"
                params["code"] = "${AppGlobal.appVersionCode}"

                val buffer = HttpRequest[CHECK_LINK, params].asString()

                if (buffer == "[]") {
                    sendEvent(null, null)
                    return@execute
                }

                val response = JSONObject(buffer)

                val update = Update(response)

                sendEvent(buffer, update)
            } catch (e: Exception) {
                e.printStackTrace()
                sendEvent(null, null)
            }
        }
    }

    class Update() {
        var id = 0
        var version = ""
        var code = 0
        var time = 0
        var changelog = ""
        var downloadLink = ""

        constructor(o: JSONObject) : this() {
            id = o.optInt("id", -1)
            version = o.optString("version")
            code = o.optInt("code", -1)
            time = o.optInt("time")
            changelog = o.optString("changelog")
            downloadLink = o.optString("download")
        }

    }

    private fun sendEvent(response: String?, update: Update?) {
        for (listener in listeners) {
            listener.onNewUpdate(response, update)
        }
    }

    interface OnUpdateListener {
        fun onNewUpdate(response: String?, update: Update?)
    }

}