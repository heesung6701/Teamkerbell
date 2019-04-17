package org.teamfairy.sopt.teamkerbell.activities.chat.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

open class InviteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tv: TextView = itemView.findViewById(R.id.li_tv_invite)
}