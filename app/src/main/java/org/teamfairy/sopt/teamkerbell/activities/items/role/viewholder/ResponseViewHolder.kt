package org.teamfairy.sopt.teamkerbell.activities.items.role.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.teamfairy.sopt.teamkerbell.R


/**
 * Created by lumiere on 2018-03-12.
 */
open class ResponseViewHolder(itemView : View)  : RecyclerView.ViewHolder(itemView){
    val ivProfile : ImageView = itemView.findViewById(R.id.li_iv_profile)
    val tvName : TextView = itemView.findViewById(R.id.li_tv_name)
    val tvDetail : TextView = itemView.findViewById(R.id.li_tv_detail)
    val ivFile : ImageView = itemView.findViewById(R.id.li_iv_file)
    val tvContent : TextView = itemView.findViewById(R.id.li_tv_content)
    val tvCommentC : TextView = itemView.findViewById(R.id.li_tv_comment_cnt)

}