package org.teamfairy.sopt.teamkerbell.activities.items.role.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import android.os.Handler
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.items.role.viewholder.ResponseViewHolder
import org.teamfairy.sopt.teamkerbell.model.data.TaskResponse
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils


/**
 * Created by lumiere on 2017-12-30.
 */
class ResponseListAdapter(var dataList: ArrayList<TaskResponse>, var onClickHandler: Handler, var mContext: Context) : RecyclerView.Adapter<ResponseViewHolder>() {


    override fun onBindViewHolder(holder: ResponseViewHolder, position: Int) {
        val taskResponse = dataList[position]
        holder.tvName.text=taskResponse.name
        holder.tvDetail.text=taskResponse.write_time

        holder.tvContent.text=taskResponse.content
        holder.tvCommentC.text = if (taskResponse.count != 0) taskResponse.count.toString() else ""

        if (NetworkUtils.getBitmapList(taskResponse.photo, holder.ivProfile, mContext, "user" + taskResponse.u_idx))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default_png)

        if(taskResponse.fileArray.isNotEmpty())
            holder.ivFile.visibility=View.VISIBLE
        else
            holder.ivFile.visibility=View.GONE
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ResponseViewHolder {

        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_task_response, parent, false)
        mainView.setOnClickListener {
            onClickHandler.sendEmptyMessage(viewType)
        }
        return ResponseViewHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size;

}