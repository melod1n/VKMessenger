package ru.melod1n.vk.util

import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
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


    fun hideKeyboardFrom(view: View) {
        AppGlobal.inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}