package org.teamfairy.sopt.teamkerbell.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class ChoiceListViewHolder(itemView :  View)  : RecyclerView.ViewHolder(itemView){
    val tvContent : TextView = itemView.findViewById(R.id.li_tv_content)
    val tvCount : TextView = itemView.findViewById(R.id.li_tv_count)

}