package org.teamfairy.sopt.teamkerbell.activities.home.room.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.LinearLayout
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import org.teamfairy.sopt.teamkerbell.listview.viewholder.ListContentHolder
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.home.room.viewholder.RoomViewHolder
import org.teamfairy.sopt.teamkerbell.model.data.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_CHATROOM


/**
 * Created by lumiere on 2017-12-30.
 */
class RoomListAdapter(var dataList: ArrayList<Room>,var mContext: Context) : RecyclerView.Adapter<RoomViewHolder>() {


    private var onClickListener: View.OnClickListener? = null

    fun setOnItemClickListener(l: View.OnClickListener) {
        onClickListener = l
    }

    override fun onBindViewHolder(holder: RoomViewHolder?, position: Int) {
        val room = dataList[position]

        holder!!.tvName.text = room.real_name
        holder.tvContent.text = dataList[position].real_name
        holder.tvTime.text = dataList[position].getTime()
        if(dataList[position].newMsgCnt>0){
            holder.tvCount.visibility=View.VISIBLE
            holder.tvCount.text= dataList[position].newMsgCnt.toString()
        }else
            holder.tvCount.visibility=View.INVISIBLE

        if(NetworkUtils.getBitmapList(room.photo, holder.ivProfile,mContext, "$INTENT_CHATROOM/${room.room_idx}"))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default_png)

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RoomViewHolder {
        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_room, parent, false)
        mainView.setOnClickListener(onClickListener)
        return RoomViewHolder(mainView)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int = dataList.size;

}