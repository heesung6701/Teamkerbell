package org.teamfairy.sopt.teamkerbell.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class UserViewHolder(itemView :  View)  : RecyclerView.ViewHolder(itemView){
    val tvName : TextView = itemView.findViewById(R.id.li_user_name)
    val ivProfile : ImageView = itemView.findViewById(R.id.li_user_profile)
    val chk : CheckBox = itemView.findViewById<CheckBox>(R.id.li_user_chk)
}