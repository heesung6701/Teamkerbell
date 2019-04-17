package org.teamfairy.sopt.teamkerbell.activities.chat.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.chat.viewholder.ItemHolder

open class ItemLHolder(itemView: View) : ItemHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.li_tv_name)
    val profile: ImageView = itemView.findViewById(R.id.li_iv_profile)
}