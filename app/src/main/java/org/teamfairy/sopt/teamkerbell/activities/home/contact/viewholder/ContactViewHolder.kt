package org.teamfairy.sopt.teamkerbell.activities.home.contact.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R

/**
 * Created by lumiere on 2018-01-10.
 */
class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val tvName : TextView = itemView.findViewById(R.id.li_tv_name)
    val ivProfile : ImageView = itemView.findViewById(R.id.li_iv_profile)
    val layoutContactInfo : LinearLayout = itemView.findViewById(R.id.li_layout_contact_info)
    val tvEmail : TextView= itemView.findViewById(R.id.li_tv_email)
    val tvPhone : TextView= itemView.findViewById(R.id.li_tv_phone)
}