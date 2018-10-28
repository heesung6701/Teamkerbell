package org.teamfairy.sopt.teamkerbell.listview.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.interfaces.GroupInterface
import org.teamfairy.sopt.teamkerbell.viewholder.TextViewHolder

/**
 * Created by lumiere on 2018-05-05.
 */
class  TextListAdapter(var dataList : ArrayList<GroupInterface>, var mContext: Context) : RecyclerView.Adapter<TextViewHolder>() {

     var currentIdx : Int =-1
    private var onItemClick: View.OnClickListener? = null

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) { // binding

        holder.tvContent.text = dataList[position].real_name
        if(dataList[position].getIdx()==currentIdx){
            holder.tvContent.setTypeface(holder.tvContent.typeface,Typeface.BOLD)
            holder.tvContent.setTextColor(ContextCompat.getColor(mContext,R.color.mainColor))
        }else{
            holder.tvContent.typeface = Typeface.DEFAULT
            holder.tvContent.setTextColor(ContextCompat.getColor(mContext,R.color.grayDark))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder { // use my viewholder
        val mainView: View = LayoutInflater.from(parent.context).inflate(R.layout.li_group, parent, false)
        mainView.setOnClickListener(onItemClick)
        return TextViewHolder(mainView)
    }

    fun setOnItemClickListener(l: View.OnClickListener) {
        onItemClick = l
    }
}
