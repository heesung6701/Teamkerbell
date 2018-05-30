package org.teamfairy.sopt.teamkerbell.listview.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2018-01-10.
 */
class ListContentHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val tvTitle : TextView = itemView.findViewById(R.id.li_tv_title)
    val tvContent : TextView = itemView.findViewById(R.id.li_tv_content)
    val tvRoomName : TextView = itemView.findViewById(R.id.li_tv_room_name)
    val tvTime : TextView = itemView.findViewById(R.id.li_tv_time)
    val ivProfile : ImageView = itemView.findViewById(R.id.li_iv_profile)
    val ivSign : ImageView = itemView.findViewById(R.id.li_iv_sign)

}