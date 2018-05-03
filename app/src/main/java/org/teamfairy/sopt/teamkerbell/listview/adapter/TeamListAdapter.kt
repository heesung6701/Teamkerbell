package org.teamfairy.sopt.teamkerbell.listview.adapter

import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.viewholder.EmptyViewHolder
import org.teamfairy.sopt.teamkerbell.viewholder.TeamViewHolder

/**
 * Created by lumiere on 2017-12-30.
 */
class TeamListAdapter(var dataList: ArrayList<Team>,var mOnClickListener: View.OnClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
         val TYPE_ITEM = 1
         val TYPE_FOOT = 2
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, pos: Int) {
        val position = holder!!.adapterPosition
        when(getItemViewType(position)){
            TYPE_FOOT->{

            }
            else->{ // TYPE_ITEM
                val h = holder as TeamViewHolder
                h.tvName.text= dataList[position].real_name
                h.tvCount.text= dataList[position].g_idx.toString()
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == dataList.lastIndex) TYPE_FOOT else TYPE_ITEM
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            TYPE_FOOT->{
                val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_team_add, parent, false)
                mainView.setOnClickListener(mOnClickListener)
                EmptyViewHolder(mainView)
            }
            else->{ //TYPE_ITEM
                val mainView: View = LayoutInflater.from(parent!!.context).inflate(R.layout.li_team, parent, false)
                mainView.setOnClickListener(mOnClickListener)
                TeamViewHolder(mainView)
            }
        }
    }


    override fun getItemCount(): Int = dataList.size


}