package org.teamfairy.sopt.teamkerbell.activities.items.pick.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.LinearLayout
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.items.pick.viewholder.PickViewHolder
import org.teamfairy.sopt.teamkerbell.model.data.Pick
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER


/**
 * Created by lumiere on 2017-12-30.
 */
class PickListAdapter(var dataList: ArrayList<Pick>, var mContext: Context) : RecyclerView.Adapter<PickViewHolder>() {


    private var mOnClick: View.OnClickListener? = null

    fun setOnItemClick(l: View.OnClickListener) {
        mOnClick = l
    }

    override fun onBindViewHolder(holder: PickViewHolder, position: Int) {

        holder.tvTitle.text = dataList[position].name
        holder.tvContent.text = dataList[position].getMainTitle()
        holder.tvTime.text = dataList[position].getTime()
        holder.tvRoomName.text=dataList[position].roomName

        val url = dataList[position].photo
        if (NetworkUtils.getBitmapList(url, holder.ivProfile, mContext,"user${dataList.get(position).u_idx}"))
            holder.ivProfile.setImageResource(R.drawable.icon_profile_default)

        val vto : ViewTreeObserver = holder.tvContent.getViewTreeObserver()
        vto.addOnGlobalLayoutListener {
            val l = holder.tvContent.layout
            if(l!=null) {
                val lines = l.lineCount
                if (lines > 0 && l.getEllipsisCount(lines - 1) > 0) holder.btnExpand.visibility = View.VISIBLE
            }
        }

        holder.btnExpand.setOnClickListener {
            if (it.rotation == 90.0f) {
                it.rotation = (-90).toFloat()
                holder.tvContent.maxLines = Integer.MAX_VALUE
                holder.tvContent.ellipsize = null

                val layoutParam = holder.tvContent.layoutParams
                layoutParam.height = LinearLayout.LayoutParams.WRAP_CONTENT
                holder.tvContent.layoutParams = layoutParam

            } else {
                it.rotation = 90.toFloat()
                holder.tvContent.maxLines = 2
                holder.tvContent.ellipsize = TextUtils.TruncateAt.END
            }
        }

    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PickViewHolder {

        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_pick, parent, false)
        mainView.setOnClickListener(mOnClick)
        return PickViewHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size;

}