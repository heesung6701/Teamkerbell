package org.teamfairy.sopt.teamkerbell.activities.items.role.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class RoleListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tvTitle = itemView.findViewById<TextView>(R.id.li_tv_title)
    var tvTime = itemView.findViewById<TextView>(R.id.li_tv_time)
    var tvRoom = itemView.findViewById<TextView>(R.id.li_tv_chat_name)
    var tvName = itemView.findViewById<TextView>(R.id.li_tv_user_name)
}