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
    val name : TextView = itemView.findViewById(R.id.li_tv_name)
    val content : TextView = itemView.findViewById(R.id.li_tv_content)
    val profile : ImageView = itemView.findViewById(R.id.li_iv_profile)
    val count : TextView = itemView.findViewById(R.id.li_tv_count)
    val time : TextView = itemView.findViewById(R.id.li_tv_time)
}