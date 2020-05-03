package ru.melod1n.vk.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.melod1n.vk.R
import ru.melod1n.vk.current.BaseAdapter
import ru.melod1n.vk.current.BaseHolder
import ru.melod1n.vk.item.ProfileMenuItem
import java.util.ArrayList

class ProfileItemAdapter(context: Context, values: ArrayList<ProfileMenuItem>) : BaseAdapter<ProfileMenuItem, ProfileItemAdapter.ViewHolder>(context, values) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(view(R.layout.item_profile_menu, parent))
    }

    inner class ViewHolder(v: View) : BaseHolder(v) {

        private val title: TextView = v.findViewById(R.id.profileItemTitle)
        private val icon: ImageView = v.findViewById(R.id.profileItemIcon)

        override fun bind(position: Int) {
            val item = getItem(position)

            title.text = item.title

            icon.setImageDrawable(item.icon)
        }

    }
}