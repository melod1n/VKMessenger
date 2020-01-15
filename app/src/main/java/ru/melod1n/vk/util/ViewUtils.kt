package ru.melod1n.vk.util

import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import ru.melod1n.vk.R
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.database.MemoryCache
import ru.melod1n.vk.widget.CircleImageView

object ViewUtils {

    fun prepareNavigationHeader(navigationView: NavigationView, user: VKUser?) {
        val headerView = navigationView.getHeaderView(0)

        val profile = user ?: MemoryCache.getUser(UserConfig.getUserId()) ?: return

        val profileName = headerView.findViewById<TextView>(R.id.profileName)

        profileName.text = profile.toString()

        val profileStatus = headerView.findViewById<TextView>(R.id.profileStatus)

        val statusText = if (TextUtils.isEmpty(profile.status)) "@id" + profile.id else profile.status

        profileStatus.text = statusText

        val profileAvatar: CircleImageView = headerView.findViewById(R.id.profileAvatar)

        if (!TextUtils.isEmpty(profile.photo200)) {
            Picasso.get().load(profile.photo200).into(profileAvatar)
        } else {
            profileAvatar.setImageDrawable(ColorDrawable(AppGlobal.colorAccent))
        }
    }
}