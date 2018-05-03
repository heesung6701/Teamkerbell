package org.teamfairy.sopt.teamkerbell.listview.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.viewholder.UserListHolder
import kotlinx.android.synthetic.main.li_user.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.list.UserCheckData
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils

/**
 * Created by lumiere on 2017-12-30.
 */
class UserListAdapter(var dataList: ArrayList<UserCheckData>, var mContext: Context) : RecyclerView.Adapter<UserListHolder>() {


    private var isCheckable: Boolean = true
    override fun onBindViewHolder(holder: UserListHolder?, pos: Int) {
        val position = holder!!.adapterPosition
        holder.itemView.visibility = View.VISIBLE
        holder.tvName.text = dataList.get(position).user.name
        if (isCheckable) {
            holder.chk.isChecked = dataList.get(position).isChecked!!
            holder.chk.setOnCheckedChangeListener { p0, p1 -> dataList.get(position).isChecked = p1 }
        }
        val url = dataList.get(position).user.photo
        if (NetworkUtils.getBitmapList(url, holder.ivProfile, mContext, "user" + dataList.get(position).user.u_idx))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default_png)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UserListHolder {
        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_user, parent, false)
        val viewHolder: UserListHolder?

        val position = viewType

        if (dataList.get(position).isChecked == null) {
            isCheckable = false
        } else {
            mainView.setOnClickListener {
                dataList.get(position).isChecked = !dataList.get(position).isChecked!!
                mainView.li_user_chk.isChecked = dataList.get(position).isChecked!!
            }
            mainView.li_user_chk.visibility = View.VISIBLE

        }
        viewHolder = UserListHolder(mainView)

        return viewHolder


    }


    override fun getItemCount(): Int = dataList.size


}