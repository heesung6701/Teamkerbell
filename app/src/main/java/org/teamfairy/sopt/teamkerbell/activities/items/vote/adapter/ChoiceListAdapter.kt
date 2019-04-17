package org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.viewholder.ChoiceListViewHolder

/**
 * Created by lumiere on 2017-12-30.
 */
class ChoiceListAdapter(var dataList: ArrayList<HashMap<String, String>>, var mContext: Context) : RecyclerView.Adapter<ChoiceListViewHolder>() {

    private var mOnClick: View.OnClickListener? = null

    var selectedId = -1

    fun setOnItemClick(l: View.OnClickListener) {
        mOnClick = l
    }

    override fun onBindViewHolder(holder: ChoiceListViewHolder, position: Int) {
        holder.tvContent.text = dataList[position]["content"]
        holder.tvCount.text = dataList[position]["count"]
        if (position == selectedId) {
            holder.itemView.background = ContextCompat.getDrawable(mContext, R.drawable.shape_round_btn)
            holder.tvContent.setTextColor(ContextCompat.getColor(mContext, R.color.white))
            holder.tvCount.setTextColor(ContextCompat.getColor(mContext, R.color.white))
        } else {
            holder.itemView.background = ContextCompat.getDrawable(mContext, R.drawable.shape_round_btn_gray_light)
            holder.tvContent.setTextColor(ContextCompat.getColor(mContext, R.color.black))
            holder.tvCount.setTextColor(ContextCompat.getColor(mContext, R.color.black))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceListViewHolder {

        val mainView: View = LayoutInflater.from(parent.context).inflate(R.layout.li_vote_choice, parent, false)
        mainView.setOnClickListener(mOnClick)
        return ChoiceListViewHolder(mainView)
    }

    override fun getItemCount(): Int = dataList.size
}