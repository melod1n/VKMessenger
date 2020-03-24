package ru.melod1n.vk.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import ru.melod1n.vk.common.AppGlobal

object AndroidUtils {
    fun px(dp: Float): Int {
        return (dp * (AppGlobal.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun dp(px: Float): Int {
        return (px / (AppGlobal.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun hasConnection(): Boolean {
        val manager = AppGlobal.connectivityManager
        val info = manager.activeNetworkInfo
        return info != null && info.isConnected
    }

    fun getDisplayWidth(): Int {
        val metrics = DisplayMetrics()
        AppGlobal.windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    fun getDisplayHeight(): Int {
        val metrics = DisplayMetrics()
        AppGlobal.windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }
}