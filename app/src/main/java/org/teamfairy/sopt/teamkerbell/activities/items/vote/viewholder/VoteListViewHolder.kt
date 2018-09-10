package org.teamfairy.sopt.teamkerbell.activities.items.vote.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class VoteListViewHolder(itemView :  View)  : RecyclerView.ViewHolder(itemView){
    val tvTitle : TextView = itemView.findViewById(R.id.li_tv_title)
    val tvName : TextView = itemView.findViewById(R.id.li_tv_name)
    val tvGroupName : TextView = itemView.findViewById(R.id.li_tv_group_name)
    val tvRoomName : TextView = itemView.findViewById(R.id.li_tv_room_name)
    val tvTime : TextView = itemView.findViewById(R.id.li_tv_time)

}