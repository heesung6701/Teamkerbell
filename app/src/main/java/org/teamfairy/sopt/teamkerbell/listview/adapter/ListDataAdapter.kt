package org.teamfairy.sopt.teamkerbell.listview.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.LinearLayout
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import org.teamfairy.sopt.teamkerbell.listview.viewholder.ListContentHolder
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.model.data.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Signal


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


        if (dataList[position] is Signal) {

            val signal = dataList[position] as Signal
            holder.ivSign.visibility = View.VISIBLE

            when (signal.color) {
                "r" ->
                    holder.ivSign.setColorFilter(ContextCompat.getColor(mContext, R.color.red))
                "g" ->
                    holder.ivSign.setColorFilter(ContextCompat.getColor(mContext, R.color.green))
                "y" ->
                    holder.ivSign.setColorFilter(ContextCompat.getColor(mContext, R.color.yellow))
                else ->
                    holder.ivSign.visibility = View.GONE
            }

        }
        else {
            holder.ivSign.visibility = View.GONE
        }


        val url = dataList[position].photo
        if (NetworkUtils.getBitmapList(url, holder.ivProfile, mContext,"user"+dataList.get(position).u_idx))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default_png)

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