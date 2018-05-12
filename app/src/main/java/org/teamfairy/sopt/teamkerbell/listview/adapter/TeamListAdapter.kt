package org.teamfairy.sopt.teamkerbell.listview.adapter

import android.os.Handler
import android.os.Message
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.sendMessage
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.viewholder.EmptyViewHolder
import org.teamfairy.sopt.teamkerbell.viewholder.TeamViewHolder
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2017-12-30.
 */
class TeamListAdapter(var dataList: ArrayList<HashMap<String,String>>,var mOnClickListener: View.OnClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var onLongClickHandler: Handler? = null

    fun setOnLongClickHandler(I: Handler) {
        onLongClickHandler = I
    }





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
                h.tvName.text= dataList[position]["name"]
                h.tvCount.text= dataList[position]["cnt"]

                h.itemView.setOnLongClickListener(object : OnLongClickListenerByPosition(position) {
                    override fun onLongClick(p0: View?): Boolean {
                        sendMessage(position)
                        return true
                    }
                })
            }
        }

    }

    fun sendMessage(position: Int) {
        val msg: Message = Message()

        msg.what = position
        onLongClickHandler?.sendMessage(msg)
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



    abstract class OnLongClickListenerByPosition(var position: Int) : View.OnLongClickListener
}