package org.teamfairy.sopt.teamkerbell.activities.items.role.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.items.role.viewholder.TaskListViewHolder
import org.teamfairy.sopt.teamkerbell.model.data.RoleTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER


/**
 * Created by lumiere on 2017-12-30.
 */
class TaskListAdapter(var dataList: ArrayList<RoleTask>,var mContext : Context) : RecyclerView.Adapter<TaskListViewHolder>() {


    companion object {
        private  const  val MAX_PROFILE = 5
    }
    private var mOnClick: View.OnClickListener? = null

    fun setOnItemClick(l: View.OnClickListener) {
        mOnClick = l
    }


    override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {

        holder.tvContent.text = dataList[position].content


            var length = dataList.get(position).userIdArray.size

            if (length > MAX_PROFILE) {
                holder.profileC.visibility = View.VISIBLE
                holder.tvProfileC.text = ("""+${(length - MAX_PROFILE).toString()}""")
                length = MAX_PROFILE
            } else {
                holder.profileC.visibility = View.GONE
                for (i in length until MAX_PROFILE)
                    holder.ivProfiles[i].visibility = View.GONE
            }
            for (i in 0 until length) {
                val uIdx: Int = dataList[position].userIdArray[i]
                val url = DatabaseHelpUtils.getUser(mContext, uIdx).photo

                holder.ivProfiles[i].visibility = View.VISIBLE
                if (NetworkUtils.getBitmapList(url, holder.ivProfiles[i], mContext, "user$uIdx")) {
                    holder.ivProfiles[i].setImageResource(R.drawable.icon_profile_default)
                }
            }
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TaskListViewHolder {

        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_task, parent, false)
        mainView.setOnClickListener(mOnClick)

        return TaskListViewHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size;

}