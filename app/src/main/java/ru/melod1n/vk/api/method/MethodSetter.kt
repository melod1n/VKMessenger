package ru.melod1n.vk.api.method

import android.util.ArrayMap
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.VKApi.OnResponseListener
import ru.melod1n.vk.api.VKApi.execute
import ru.melod1n.vk.api.model.VKModel
import ru.melod1n.vk.util.ArrayUtil
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

open class MethodSetter(private val name: String) {

    private val params: ArrayMap<String, String?> = ArrayMap()

    fun put(key: String, value: Any): MethodSetter {
        params[key] = value.toString()
        return this
    }

    fun put(key: String, value: String): MethodSetter {
        params[key] = value
        return this
    }

    fun put(key: String, value: Int): MethodSetter {
        params[key] = value.toString()
        return this
    }

    fun put(key: String, value: Long): MethodSetter {
        params[key] = value.toString()
        return this
    }

    fun put(key: String, value: Boolean): MethodSetter {
        params[key] = if (value) "1" else "0"
        return this
    }

    private val signedUrl: String
        get() = getSignedUrl(false)

    private fun getSignedUrl(isPost: Boolean): String {
        if (!params.containsKey("access_token")) {
            params["access_token"] = UserConfig.getToken()
        }
        if (!params.containsKey("v")) {
            params["v"] = VKApi.API_VERSION
        }
        if (!params.containsKey("lang")) {
            params["lang"] = VKApi.language
        }
        return VKApi.BASE_URL + name + "?" + if (isPost) "" else getParams()
    }

    private fun getParams(): String {
        val buffer = StringBuilder()
        try {
            for (i in 0 until params.size) {
                val key = params.keyAt(i)
                val value = params.valueAt(i)
                if (buffer.isNotEmpty()) {
                    buffer.append("&")
                }
                buffer.append(key)
                        .append("=")
                        .append(URLEncoder.encode(value, "UTF-8"))
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return buffer.toString()
    }

    fun <E> execute(cls: Class<E>): ArrayList<E>? {
        return execute(signedUrl, cls)
    }

    fun <E> execute(cls: Class<E>, listener: OnResponseListener<E>?) {
        execute(signedUrl, cls, listener)
    }

    fun <E : VKModel> tryExecute(cls: Class<E>): ArrayList<E>? {
        try {
            return execute(cls)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun userId(value: Int): MethodSetter {
        return put("user_id", value)
    }

    fun userIds(vararg ids: Int): MethodSetter {
        return put("user_ids", ArrayUtil.toString(*ids))
    }

    fun userIds(ids: Collection<Int>): MethodSetter {
        return put("user_ids", ArrayUtil.toString<Any>(*ids.toTypedArray()))
    }

    fun ownerId(value: Int): MethodSetter {
        return put("owner_id", value)
    }

    fun groupId(value: Int): MethodSetter {
        return put("group_id", value)
    }

    fun groupIds(vararg ids: Int): MethodSetter {
        return put("group_ids", ArrayUtil.toString(*ids))
    }

    fun fields(values: String): MethodSetter {
        return put("fields", values)
    }

    fun count(value: Int): MethodSetter {
        return put("count", value)
    }

    fun sort(value: Int): MethodSetter {
        put("sort", value)
        return this
    }

    fun order(value: String): MethodSetter {
        put("order", value)
        return this
    }

    fun offset(value: Int): MethodSetter {
        return put("offset", value)
    }

    fun nameCase(value: String): MethodSetter {
        return put("name_case", value)
    }

    fun captchaSid(value: String): MethodSetter {
        return put("captcha_sid", value)
    }

    fun captchaKey(value: String): MethodSetter {
        return put("captcha_key", value)
    }

}