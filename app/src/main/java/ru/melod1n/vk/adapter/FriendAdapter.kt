package ru.melod1n.vk.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ru.melod1n.vk.R
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.api.util.VKUtil
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.util.ImageUtil
import ru.melod1n.vk.widget.CircleImageView

class FriendAdapter(context: Context, values: ArrayList<VKUser>) : BaseAdapter<VKUser, FriendAdapter.ViewHolder>(context, values) {

    open inner class ViewHolder(v: View) : BaseAdapter.Holder(v) {
        private val avatar: CircleImageView = v.findViewById(R.id.userAvatar)
        private val name: TextView = v.findViewById(R.id.userName)
        private val online: ImageView = v.findViewById(R.id.userOnline)
        private val onlineText: TextView = v.findViewById(R.id.userOnlineText)

        override fun bind(position: Int) {
            val user = getItem(position)

            name.text = user.toString()

            val avatarPlaceholder = VKUtil.getAvatarPlaceholder(user.toString())
            avatar.setImageDrawable(avatarPlaceholder)

            ImageUtil.loadImage(user.photo200, avatar, avatarPlaceholder)

            val onlineIcon = VKUtil.getUserOnlineIcon(context, user)

            online.apply {
                setImageDrawable(onlineIcon)
                visibility = if (onlineIcon == null) View.GONE else View.VISIBLE
            }

            onlineText.text = VKUtil.getUserOnline(user)
        }
    }

    override fun viewHolder(view: View, type: Int): ViewHolder {
        return ViewHolder(view)
    }

    override fun layoutId(type: Int): Int {
        return R.layout.item_user
    }
}