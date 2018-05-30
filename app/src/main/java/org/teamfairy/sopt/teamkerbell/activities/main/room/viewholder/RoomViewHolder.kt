package org.teamfairy.sopt.teamkerbell.activities.main.room.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2018-01-10.
 */
class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val tvName : TextView = itemView.findViewById(R.id.li_tv_room_name)
    val tvContent : TextView = itemView.findViewById(R.id.li_tv_content)
    val tvTime : TextView= itemView.findViewById(R.id.li_tv_time)
    val ivProfile : ImageView = itemView.findViewById(R.id.li_iv_profile)
    val tvCount : TextView=itemView.findViewById(R.id.li_tv_count)
}