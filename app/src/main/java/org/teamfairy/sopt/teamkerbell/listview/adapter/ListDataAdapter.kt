package org.teamfairy.sopt.teamkerbell.listview.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import android.support.v4.content.ContextCompat
import org.teamfairy.sopt.teamkerbell.listview.viewholder.ListContentHolder
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.model.data.SignalResponse
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER


/**
 * Created by lumiere on 2017-12-30.
 */
class ListDataAdapter(var dataList: ArrayList<ListDataInterface>, var mContext: Context) : RecyclerView.Adapter<ListContentHolder>() {


    private var mOnClick: View.OnClickListener? = null

    fun setOnItemClick(l: View.OnClickListener) {
        mOnClick = l
    }

    override fun onBindViewHolder(holder: ListContentHolder, position: Int) {
        holder.tvTitle.text = dataList[position].name
        holder.tvContent.text = dataList[position].getMainTitle()
        holder.tvTime.text = dataList[position].getSubTitle()
        holder.tvRoomName.text = dataList[position].getRoomTitle()

        if (dataList[position] is Signal) {

            val signal = dataList[position] as Signal
            holder.ivSign.visibility = View.VISIBLE

            when (signal.responseColor) {
                "r" ->
                    holder.ivSign.setColorFilter(ContextCompat.getColor(mContext, R.color.red))
                "g" ->
                    holder.ivSign.setColorFilter(ContextCompat.getColor(mContext, R.color.green))
                "y" ->
                    holder.ivSign.setColorFilter(ContextCompat.getColor(mContext, R.color.yellow))
                else ->
                    holder.ivSign.visibility = View.GONE
            }

        } else {
            holder.ivSign.visibility = View.GONE
        }


        if (dataList[position] is SignalResponse){
            val sig = dataList[position] as SignalResponse
            holder.itemView.isClickable =  (Signal.colorStrToByte(sig.color) != Signal.RED) || sig.content?.isNotBlank() ?: false
        }else
            holder.itemView.isClickable=true

        val url = dataList[position].photo
        if (NetworkUtils.getBitmapList(url, holder.ivProfile, mContext, "user${dataList.get(position).u_idx}"))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default)

    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ListContentHolder {

        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.list_item, parent, false)



        mainView.setOnClickListener(mOnClick)


        return ListContentHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size;

}