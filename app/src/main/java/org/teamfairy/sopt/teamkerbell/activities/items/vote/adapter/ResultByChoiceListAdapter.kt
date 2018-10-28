package org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_PHOTO
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER
import org.teamfairy.sopt.teamkerbell.viewholder.ChoiceListViewHolder
import org.teamfairy.sopt.teamkerbell.viewholder.UserViewHolder


/**
 * Created by lumiere on 2017-12-30.
 */
class ResultByChoiceListAdapter(var dataList: ArrayList<HashMap<String, String>>, var mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (dataList[position]["type"]) {
            "header" -> {
                val h = holder as ChoiceListViewHolder
                h.tvCount.text=dataList[position]["count"]
                h.tvContent.text=dataList[position]["content"]
            }
            else -> {
                val h = holder as UserViewHolder
                h.tvName.text=dataList[position]["name"]
                if (NetworkUtils.getBitmapList(dataList[position][JSON_PHOTO], holder.ivProfile, mContext,"user${dataList[position][JSON_U_IDX]}"))
                    holder.ivProfile.setImageResource(R.drawable.icon_profile_default)

            }
        }

    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (dataList[viewType]["type"]) {
            "header" -> {
                val mainView: View = LayoutInflater.from(parent.context).inflate(R.layout.li_vote_choice, parent, false)
                ChoiceListViewHolder(mainView)
            }
            else -> {
                val mainView: View = LayoutInflater.from(parent.context).inflate(R.layout.li_user, parent, false)
                UserViewHolder(mainView)
            }
        }

    }


    override fun getItemCount(): Int = dataList.size;

}