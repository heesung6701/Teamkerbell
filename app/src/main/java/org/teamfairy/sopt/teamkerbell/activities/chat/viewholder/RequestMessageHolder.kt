package org.teamfairy.sopt.teamkerbell.activities.chat.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

class RequestMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val content: TextView = itemView.findViewById(R.id.li_tv_content)
    val count: TextView = itemView.findViewById(R.id.li_tv_count)
    val time: TextView = itemView.findViewById(R.id.li_tv_time)
}