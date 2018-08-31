package org.teamfairy.sopt.teamkerbell.viewholder.chat

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class InviteHolder(itemView :  View)  : RecyclerView.ViewHolder(itemView){
    val tv : TextView = itemView.findViewById(R.id.li_tv_invite)
}