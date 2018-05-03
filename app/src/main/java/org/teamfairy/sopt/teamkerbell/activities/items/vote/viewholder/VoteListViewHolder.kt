package org.teamfairy.sopt.teamkerbell.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class VoteListViewHolder(itemView :  View)  : RecyclerView.ViewHolder(itemView){
    val tvTitle : TextView = itemView.findViewById(R.id.li_tv_title)
    val tvSubTitle : TextView = itemView.findViewById(R.id.li_tv_sub_title)
    val tvCount : TextView = itemView.findViewById(R.id.li_tv_count)

}