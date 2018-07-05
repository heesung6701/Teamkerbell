package org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Vote
import org.teamfairy.sopt.teamkerbell.viewholder.VoteListViewHolder


/**
 * Created by lumiere on 2017-12-30.
 */
class VoteListAdapter(var dataList: ArrayList<ListDataInterface>) : RecyclerView.Adapter<VoteListViewHolder>() {


    private var mOnClick: View.OnClickListener? = null

    fun setOnItemClick(l: View.OnClickListener) {
        mOnClick = l
    }

    override fun onBindViewHolder(holder: VoteListViewHolder, position: Int) {
        val vote = dataList[position] as Vote
        if(vote.isFinished())
            holder.tvTitle.setTextColor(Color.LTGRAY)
        else
            holder.tvTitle.setTextColor(Color.DKGRAY)
        holder.tvTitle.text = vote.title
        holder.tvCount.text = ("${vote.vote_idx}명")
        holder.tvSubTitle.text = vote.content

    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VoteListViewHolder {

        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_vote, parent, false)

        mainView.setOnClickListener(mOnClick)

        return VoteListViewHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size;

}