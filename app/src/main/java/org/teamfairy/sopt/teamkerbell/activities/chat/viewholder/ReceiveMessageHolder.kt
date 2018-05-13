package org.teamfairy.sopt.teamkerbell.viewholder.chat

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
class ReceiveMessageHolder(itemView :  View)  : RecyclerView.ViewHolder(itemView){
    val tvName : TextView = itemView.findViewById(R.id.list_item_tv_name)
    val tvContent : TextView = itemView.findViewById(R.id.list_item_tv_content)
    val ivProfile : ImageView = itemView.findViewById(R.id.list_item_iv_profile)
    val tvCount : TextView = itemView.findViewById(R.id.list_item_tv_count)
    val tvTime : TextView = itemView.findViewById(R.id.list_item_tv_time)
}