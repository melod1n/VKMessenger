package ru.melod1n.vk.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import ru.melod1n.vk.R
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.database.DatabaseHelper
import java.util.*

class AppGlobal : Application() {
    override fun onCreate() {
        super.onCreate()
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        handler = Handler(mainLooper)
        locale = Locale.getDefault()

        Companion.resources = resources

        database = DatabaseHelper.getInstance(this).writableDatabase
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        Companion.packageName = packageName

        colorPrimary = ContextCompat.getColor(this, R.color.primary)
        colorAccent = ContextCompat.getColor(this, R.color.accent)

        UserConfig.restore()
        TimeManager.init()
    }

    companion object {
        lateinit var connectivityManager: ConnectivityManager
        lateinit var preferences: SharedPreferences
        lateinit var locale: Locale
        lateinit var handler: Handler
        lateinit var resources: Resources
        lateinit var database: SQLiteDatabase
        lateinit var packageName: String

        var colorPrimary = 0
        var colorAccent = 0
    }
}