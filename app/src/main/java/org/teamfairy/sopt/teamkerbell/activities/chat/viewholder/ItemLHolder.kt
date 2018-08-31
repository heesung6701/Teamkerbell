package org.teamfairy.sopt.teamkerbell.viewholder.chat

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class ItemLHolder(itemView :  View)  : ItemHolder(itemView){
    val name : TextView = itemView.findViewById(R.id.li_tv_name)
    val profile : ImageView = itemView.findViewById(R.id.li_iv_profile)
}