package ru.melod1n.vk.current

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.melod1n.vk.util.AndroidUtils
import kotlin.math.roundToInt

abstract class BaseActivity : AppCompatActivity() {


//    fun Activity.getRootView(): View {
//        return findViewById(android.R.id.content)
//    }

    var Activity.rootView :View
        get() {
            return findViewById(android.R.id.content)
        }
        set(value) {return}


    fun Activity.isKeyboardOpen(): Boolean {
        val visibleBounds = Rect()
        rootView.getWindowVisibleDisplayFrame(visibleBounds)
        val heightDiff = rootView.height - visibleBounds.height()
        val marginOfError = AndroidUtils.px(50F).toDouble().roundToInt()
        return heightDiff > marginOfError
    }

    fun Activity.isKeyboardClosed(): Boolean {
        return !isKeyboardOpen()
    }

}