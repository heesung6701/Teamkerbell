package org.teamfairy.sopt.teamkerbell.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
    var tvTime = itemView.findViewById<TextView>(R.id.tv_time)
    var tvContent = itemView.findViewById<TextView>(R.id.tv_content)
    var ivProfile = itemView.findViewById<ImageView>(R.id.li_user_profile)
    var tvName = itemView.findViewById<TextView>(R.id.li_user_name)
}