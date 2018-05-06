package org.teamfairy.sopt.teamkerbell.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class ResultByMemberViewHolder(itemView :  View)  : RecyclerView.ViewHolder(itemView){
    val tvName : TextView = itemView.findViewById(R.id.li_tv_name)
    val ivProfile : ImageView = itemView.findViewById(R.id.li_iv_profile)
    val tvContent : TextView = itemView.findViewById(R.id.li_tv_content)

}