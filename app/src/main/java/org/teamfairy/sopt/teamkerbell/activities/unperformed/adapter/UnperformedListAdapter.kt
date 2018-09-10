package org.teamfairy.sopt.teamkerbell.activities.unperformed.adapter

import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.viewholder.UnperformedListViewHolder


/**
 * Created by lumiere on 2017-12-30.
 */
class UnperformedListAdapter(var dataList: ArrayList<ListDataInterface>) : RecyclerView.Adapter<UnperformedListViewHolder>() {


    private var mOnClick: View.OnClickListener? = null

    fun setOnItemClick(l: View.OnClickListener) {
        mOnClick = l
    }

    override fun onBindViewHolder(holder: UnperformedListViewHolder, position: Int) {

        holder.tvTitle.text = dataList[position].getGroupTitle()
        holder.tvSubTitle.text = dataList[position].getMainTitle()
        holder.tvTime.text = dataList[position].getTime()

    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UnperformedListViewHolder {

        val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_unperformed, parent, false)

        mainView.setOnClickListener(mOnClick)

        return UnperformedListViewHolder(mainView)
    }


    override fun getItemCount(): Int = dataList.size;

}