package org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Vote
import org.teamfairy.sopt.teamkerbell.activities.items.vote.viewholder.VoteListViewHolder
import org.teamfairy.sopt.teamkerbell.utils.LoginToken


/**
 * Created by lumiere on 2017-12-30.
 */
class VoteListAdapter(var mContext : Context, var dataList: ArrayList<ListDataInterface>) : RecyclerView.Adapter<VoteListViewHolder>() {


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
        holder.tvName.text=LoginToken.getUser(mContext).name
        holder.tvGroupName.text=vote.groupName
        holder.tvRoomName.text=("${vote.roomName}") // (${.vote_idx}명)") 몇명인지 어케 아냐..
        holder.tvTime.text=vote.getTime()

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