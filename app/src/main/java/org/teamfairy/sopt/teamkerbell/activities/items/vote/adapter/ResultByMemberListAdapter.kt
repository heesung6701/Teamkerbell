package org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_PHOTO
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER
import org.teamfairy.sopt.teamkerbell.viewholder.ResultByMemberViewHolder


/**
 * Created by lumiere on 2017-12-30.
 */
class ResultByMemberListAdapter(var dataList: ArrayList<HashMap<String,String>>, var mContext: Context) : RecyclerView.Adapter<ResultByMemberViewHolder>() {


    override fun onBindViewHolder(holder: ResultByMemberViewHolder, position: Int) {
        holder.tvContent.text = dataList[position]["content"]
        holder.tvName.text = dataList[position]["name"]

        if (NetworkUtils.getBitmapList(dataList[position][JSON_PHOTO], holder.ivProfile, mContext, "user${dataList[position][JSON_U_IDX]}"))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default)

    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ResultByMemberViewHolder {

        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_vote_result_by_member, parent, false)
        return ResultByMemberViewHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size

}