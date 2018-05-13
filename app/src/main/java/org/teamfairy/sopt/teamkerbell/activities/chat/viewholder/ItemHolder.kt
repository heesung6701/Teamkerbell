package org.teamfairy.sopt.teamkerbell.viewholder.chat

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class ItemHolder(itemView :  View)  : RecyclerView.ViewHolder(itemView){
    val title : TextView = itemView.findViewById(R.id.list_item_tv_title)
    val content: TextView = itemView.findViewById(R.id.list_item_tv_content)
    val tvTime: TextView = itemView.findViewById(R.id.list_item_tv_time)
    val tvCount : TextView = itemView.findViewById(R.id.list_item_tv_count)
}