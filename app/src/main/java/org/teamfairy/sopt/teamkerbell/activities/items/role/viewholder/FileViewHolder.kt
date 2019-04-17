package org.teamfairy.sopt.teamkerbell.activities.items.role.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2018-06-03.
 */

open class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvTitle: TextView = itemView.findViewById(R.id.li_tv_title)
    val subTitle: TextView = itemView.findViewById(R.id.li_tv_sub_title)
    val btnMinus: ImageButton = itemView.findViewById(R.id.li_btn_minus)
}