package ru.melod1n.vk.api

import android.text.TextUtils
import ru.melod1n.vk.common.AppGlobal

object UserConfig {
    private const val TOKEN = "token"
    private const val USER_ID = "user_id"
    const val API_ID = "6964679"
    private var token: String? = null
    private var userId = 0
    fun getToken(): String? {
        return token
    }

    fun setToken(token: String?) {
        UserConfig.token = token
    }

    fun getUserId(): Int {
        return userId
    }

    fun setUserId(userId: Int) {
        UserConfig.userId = userId
    }

    fun save() {
        AppGlobal.preferences.edit().putString(TOKEN, token).putInt(USER_ID, userId).apply()
    }

    fun restore() {
        token = AppGlobal.preferences.getString(TOKEN, "")
        userId = AppGlobal.preferences.getInt(USER_ID, -1)
    }

    fun clear() {
        token = ""
        userId = -1
        AppGlobal.preferences.edit().remove(TOKEN).remove(USER_ID).apply()
    }

    val isLoggedIn: Boolean
        get() = userId > 0 && !TextUtils.isEmpty(token)
}