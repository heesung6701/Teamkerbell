package org.teamfairy.sopt.teamkerbell.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/*
 * Created by lumiere on 2017-12-30.
 */
open class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvName: TextView = itemView.findViewById(R.id.li_tv_name)
    val tvCount: TextView = itemView.findViewById(R.id.li_tv_count)
}