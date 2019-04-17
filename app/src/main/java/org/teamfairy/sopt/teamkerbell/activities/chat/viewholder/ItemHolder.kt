package org.teamfairy.sopt.teamkerbell.activities.chat.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

open class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: ImageView = itemView.findViewById(R.id.li_iv_icon)
    val title: TextView = itemView.findViewById(R.id.li_tv_title)
    val content: TextView = itemView.findViewById(R.id.li_tv_content)
    val time: TextView = itemView.findViewById(R.id.li_tv_time)
    val count: TextView = itemView.findViewById(R.id.li_tv_count)
}