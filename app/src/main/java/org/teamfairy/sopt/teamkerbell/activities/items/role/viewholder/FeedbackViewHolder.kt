package org.teamfairy.sopt.teamkerbell.activities.items.role.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.teamfairy.sopt.teamkerbell.R


/**
 * Created by lumiere on 2018-03-12.
 */
open class FeedbackViewHolder(itemView : View)  : RecyclerView.ViewHolder(itemView){
    val tvName : TextView = itemView.findViewById(R.id.li_tv_name)
    val tvContent : TextView = itemView.findViewById(R.id.li_tv_content)
    val ivProfile : ImageView = itemView.findViewById(R.id.li_iv_profile)

}