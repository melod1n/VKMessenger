package ru.melod1n.vk.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.Handler
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import ru.melod1n.library.mvp.base.MvpBase
import ru.melod1n.vk.R
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.database.DatabaseHelper
import ru.melod1n.vk.util.AndroidUtils
import java.util.*


class AppGlobal : Application() {

    companion object {
        lateinit var windowManager: WindowManager
        lateinit var connectivityManager: ConnectivityManager
        lateinit var inputMethodManager: InputMethodManager

        lateinit var preferences: SharedPreferences
        lateinit var locale: Locale
        lateinit var handler: Handler
        lateinit var resources: Resources
        lateinit var database: SQLiteDatabase
        lateinit var packageName: String

        var instance: AppGlobal? = null

        var packageNameString = ""

        var appVersionName = ""
        var appVersionCode = 0L

        var screenWidth = 0
        var screenHeight = 0

        var colorPrimary = 0
        var colorAccent = 0
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        handler = Handler(mainLooper)
        locale = Locale.getDefault()

        val info = packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        appVersionName = info.versionName
        appVersionCode = PackageInfoCompat.getLongVersionCode(info)

        packageNameString = packageName

        Companion.resources = resources

        database = DatabaseHelper.getInstance(this).writableDatabase

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        Companion.packageName = packageName

        screenWidth = AndroidUtils.getDisplayWidth()
        screenHeight = AndroidUtils.getDisplayHeight()

        colorPrimary = ContextCompat.getColor(this, R.color.primary)
        colorAccent = ContextCompat.getColor(this, R.color.accent)

        UserConfig.restore()
        TimeManager.init(this)

        MvpBase.init(handler)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

}