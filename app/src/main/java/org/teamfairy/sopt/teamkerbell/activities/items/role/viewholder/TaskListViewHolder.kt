package org.teamfairy.sopt.teamkerbell.activities.items.role.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2017-12-30.
 */
open class TaskListViewHolder(itemView :  View)  : RecyclerView.ViewHolder(itemView){
    val tvContent : TextView = itemView.findViewById<TextView>(R.id.li_tv_content)
    val profileC : FrameLayout = itemView.findViewById(R.id.li_layout_profileC)
    val tvProfileC : TextView = itemView.findViewById(R.id.li_tv_profileC)
    val ivProfiles  : Array<ImageView> = arrayOf(
            itemView.findViewById(R.id.li_iv_profile1),
            itemView.findViewById(R.id.li_iv_profile2),
            itemView.findViewById(R.id.li_iv_profile3),
            itemView.findViewById(R.id.li_iv_profile4),
            itemView.findViewById(R.id.li_iv_profile5))

}