package ru.melod1n.vk.util

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import ru.melod1n.vk.R
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.widget.CircleImageView


object ViewUtils {

    fun prepareNavigationHeader(navigationView: NavigationView, user: VKUser) {
        val headerView = navigationView.getHeaderView(0)

        val profileName = headerView.findViewById<TextView>(R.id.profileName)

        profileName.text = user.toString()

        val profileStatus = headerView.findViewById<TextView>(R.id.profileStatus)

        val statusText = if (TextUtils.isEmpty(user.status)) "@id${user.id}" else user.status

        profileStatus.text = statusText

        val profileAvatar: CircleImageView = headerView.findViewById(R.id.profileAvatar)

        if (!TextUtils.isEmpty(user.photo200) && AndroidUtils.hasConnection()) {
            Picasso.get().load(user.photo200).into(profileAvatar)
        } else {
            profileAvatar.setImageDrawable(ColorDrawable(AppGlobal.colorAccent))
        }
    }

<<<<<<< Updated upstream
    fun setDrawerEdgeSize(drawerLayout: DrawerLayout, edgeSize: Int) {
=======
    fun hideKeyboardFrom(view: View) {
        AppGlobal.inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

//    fun setDrawerEdgeSize(drawerLayout: DrawerLayout, edgeSize: Int) {
>>>>>>> Stashed changes
//        drawerLayout.setOnLongClickListener { false }
//
//        try {
//            val mDragger: Field = drawerLayout.javaClass.getDeclaredField("mLeftDragger")
//            mDragger.isAccessible = true
//            val draggerObj = mDragger.get(drawerLayout) as ViewDragHelper
//
//            val mEdgeSize: Field = draggerObj.javaClass.getDeclaredField("mEdgeSize")
//            mEdgeSize.isAccessible = true
//            mEdgeSize.setInt(draggerObj, edgeSize)
//
//        } catch (e: Exception) {
//            throw RuntimeException("В либе произошли изменения.")
//        }
    }
}