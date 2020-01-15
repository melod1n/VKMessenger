package ru.melod1n.vk.api

import android.util.Log
import ru.melod1n.vk.api.util.VKUtil
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object VKAuth {
    private const val TAG = "Kate.VKAuth"
    var redirect_url = "https://oauth.vk.com/blank.html"
    fun getUrl(api_id: String, settings: String): String? {
        return try {
            ("https://oauth.vk.com/authorize?client_id=" + api_id + "&display=mobile&scope=" + settings + "&redirect_uri=" + URLEncoder.encode(redirect_url, "utf-8") + "&response_type=token"
                    + "&v=" + URLEncoder.encode(VKApi.API_VERSION, "utf-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }
    }

    val settings: String
        get() = "notify,friends,photos,audio,video,docs,status,notes,pages,wall,groups,messages,offline,notifications"

    @Throws(Exception::class)
    fun parseRedirectUrl(url: String): Array<String> {
        val access_token = VKUtil.extractPattern(url, "access_token=(.*?)&")
        Log.i(TAG, "access_token=$access_token")
        val user_id = VKUtil.extractPattern(url, "user_id=(\\d*)")
        Log.i(TAG, "user_id=$user_id")
        if (user_id == null || user_id.length == 0 || access_token == null || access_token.length == 0) throw Exception("Failed to parse redirect url $url")
        return arrayOf(access_token, user_id)
    }
}