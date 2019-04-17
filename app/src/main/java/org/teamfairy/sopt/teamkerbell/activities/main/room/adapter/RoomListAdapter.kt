package org.teamfairy.sopt.teamkerbell.activities.main.room.adapter

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.main.room.viewholder.RoomViewHolder
import org.teamfairy.sopt.teamkerbell.model.data.Room

/**
 * Created by lumiere on 2017-12-30.
 */
class RoomListAdapter(var dataList: ArrayList<Room>, var mContext: Context) : RecyclerView.Adapter<RoomViewHolder>() {

    private var onLongClickHandler: Handler? = null

    fun setOnLongClickHandler(I: Handler) {
        onLongClickHandler = I
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnItemClickListener(l: View.OnClickListener) {
        onClickListener = l
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = dataList[position]

        holder.tvName.text = room.real_name
        holder.tvContent.text = room.lastMsgStr
        holder.tvTime.text = room.lastMsgTime
        if (dataList[position].newMsgCnt> 0) {
            holder.tvCount.visibility = View.VISIBLE
            holder.tvCount.text = when (room.newMsgCnt) {
                in Int.MIN_VALUE..0 -> ""
                in 999 downTo 0 -> room.newMsgCnt.toString()
                else -> "999+"
            }
        } else
            holder.tvCount.visibility = View.INVISIBLE

        if (NetworkUtils.getBitmapList(room.photo, holder.ivProfile, mContext, "room${room.room_idx}"))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default)

        if (onLongClickHandler != null) {
            holder.itemView.setOnLongClickListener(object : OnLongClickListenerByPosition(position) {
                override fun onLongClick(p0: View?): Boolean {
                    sendMessage(position)
                    return true
                }
            })
        }
    }

    fun sendMessage(position: Int) {
        val msg: Message = Message()

        msg.what = position
        onLongClickHandler?.sendMessage(msg)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val mainView: View = LayoutInflater.from(parent.context).inflate(R.layout.li_room, parent, false)
        mainView.setOnClickListener(onClickListener)
        return RoomViewHolder(mainView)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int = dataList.size

    abstract class OnLongClickListenerByPosition(var position: Int) : View.OnLongClickListener
}