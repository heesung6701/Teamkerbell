package org.teamfairy.sopt.teamkerbell.activities.items.notice.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.viewholder.CardViewHolder

/**
 * Created by lumiere on 2017-12-30.
 */
class CardListAdapter(var dataList: ArrayList<Notice>,var mContext : Context, var mOnClickListener: View.OnClickListener) : RecyclerView.Adapter<CardViewHolder>() {
    override fun onBindViewHolder(holder: CardViewHolder?, position: Int) {
        val pos = holder!!.adapterPosition

        val notice = dataList[pos]
        holder.tvTitle.text=notice.getMainTitle()
        holder.tvTime.text=notice.getSubTitle()
        holder.tvContent.text=notice.content
        if (NetworkUtils.getBitmapList(notice.photo, holder.ivProfile, mContext,"user"+notice.u_idx))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default_png)

        holder.tvName.text=notice.name
    }

    companion object {
        val TYPE_ITEM = 1
        val TYPE_FOOT = 2
    }



    override fun getItemViewType(position: Int): Int =position

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardViewHolder {
        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_card, parent, false)
        mainView.setOnClickListener(mOnClickListener)
        return CardViewHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size


}