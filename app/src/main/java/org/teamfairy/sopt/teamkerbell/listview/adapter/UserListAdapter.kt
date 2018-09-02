package org.teamfairy.sopt.teamkerbell.listview.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.viewholder.UserViewHolder
import kotlinx.android.synthetic.main.li_user_chk.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.list.UserCheckData
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER

/**
 * Created by lumiere on 2017-12-30.
 */
class UserListAdapter(var dataList: ArrayList<User>, var mContext: Context) : RecyclerView.Adapter<UserViewHolder>() {

    private var mOnClick: View.OnClickListener? = null

    fun setOnItemClick(l: View.OnClickListener) {
        mOnClick = l
    }

    private  var isCheckable: Boolean = true
    override fun onBindViewHolder(holder: UserViewHolder?, pos: Int) {
        val position = holder!!.adapterPosition
        holder.itemView.visibility = View.VISIBLE
        holder.tvName.text = dataList[position].name
        if (isCheckable && dataList[position] is UserCheckData) {
            val userCheckData = dataList[position] as UserCheckData
            holder.chk!!.isChecked = userCheckData.isChecked
            holder.chk.setOnCheckedChangeListener { _, p1 -> userCheckData.isChecked = p1 }
        }
        val url = dataList[position].photo
        if (NetworkUtils.getBitmapList(url, holder.ivProfile, mContext, "$INTENT_USER/${dataList[position].u_idx}"))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UserViewHolder {
        val mainView: View?
        val viewHolder: UserViewHolder?
        if (dataList[viewType] is UserCheckData) {
            mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_user_chk, parent, false)
            isCheckable = true
            val userCheckData = dataList[viewType] as UserCheckData
            mainView.setOnClickListener {
                userCheckData.isChecked = !userCheckData.isChecked
                mainView.li_user_chk.isChecked = userCheckData.isChecked
            }
            mainView.li_user_chk.visibility = View.VISIBLE
        } else {
            mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_user, parent, false)
            isCheckable=false
        }
        if(mOnClick!=null)
            mainView.setOnClickListener(mOnClick)
        viewHolder = UserViewHolder(mainView)

        return viewHolder


    }


    override fun getItemCount(): Int = dataList.size


}