package org.teamfairy.sopt.teamkerbell.activities.items.role.adapter

import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.data.Role
import org.teamfairy.sopt.teamkerbell.activities.items.role.viewholder.RoleListViewHolder


/**
 * Created by lumiere on 2017-12-30.
 */
class RoleListAdapter(var dataList: ArrayList<Role>) : RecyclerView.Adapter<RoleListViewHolder>() {

    private var mOnClick: View.OnClickListener? = null

    fun setOnItemClick(l: View.OnClickListener) {
        mOnClick = l
    }

    override fun onBindViewHolder(holder: RoleListViewHolder, position: Int) {
        holder.tvTitle.text = dataList[position].title
        holder.tvTime.text = dataList[position].getTime()
        holder.tvName.text = dataList[position].name
        holder.tvChat.text= "채팅방 이름"
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RoleListViewHolder {

        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_role, parent, false)
        mainView.setOnClickListener(mOnClick)
        return RoleListViewHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size;

}