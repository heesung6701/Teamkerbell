package org.teamfairy.sopt.teamkerbell.activities.items.role.adapter

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.items.role.viewholder.FeedbackViewHolder
import org.teamfairy.sopt.teamkerbell.activities.main.room.adapter.RoomListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.RoleFeedback
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils


/**
 * Created by lumiere on 2017-12-30.
 */
class FeedbackListAdapter(var mContext : Context, var dataList: ArrayList<RoleFeedback>) : RecyclerView.Adapter<FeedbackViewHolder>() {



    private var onLongClickHandler: Handler? = null

    fun setOnLongClickHandler(I: Handler) {
        onLongClickHandler = I
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val fdb = dataList[position]
        holder.tvName.text=fdb.name
        holder.tvContent.text=fdb.content

        if (NetworkUtils.getBitmapList(fdb.photo, holder.ivProfile, mContext, "user${fdb.u_idx}")) {
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default)
        }
        if(fdb.u_idx==LoginToken.getUserIdx(mContext)) {
            holder.itemView.setOnLongClickListener(object : RoomListAdapter.OnLongClickListenerByPosition(position) {
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

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FeedbackViewHolder {
        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_feedback, parent, false)
        return FeedbackViewHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size;


    abstract class OnLongClickListenerByPosition(var position: Int) : View.OnLongClickListener
}