package org.teamfairy.sopt.teamkerbell.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvContent = itemView.findViewById<TextView>(R.id.li_tv_content)
}