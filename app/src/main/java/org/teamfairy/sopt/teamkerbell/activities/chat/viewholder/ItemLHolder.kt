package org.teamfairy.sopt.teamkerbell.viewholder.chat

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class ItemLHolder(itemView :  View)  : ItemHolder(itemView){
    val tvName : TextView = itemView.findViewById(R.id.list_item_tv_name)
    val ivProfile : ImageView = itemView.findViewById(R.id.list_item_iv_profile)
}