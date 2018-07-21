package org.teamfairy.sopt.teamkerbell.activities.chat.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.RecyclerView
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.ChatUtils
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.items.notice.NoticeActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.VoteActivity
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessage
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.viewholder.chat.*

/**
 * Created by lumiere on 2017-12-30.
 */
class ChatViewAdapter(var dataList: ArrayList<ChatMessage>, var mContext: Context, var group: Team,var room : Room) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var onLongClickHandler: Handler? = null

    fun setOnLongClickHandler(I: Handler) {
        onLongClickHandler = I
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, pos: Int) {

        val position = holder!!.adapterPosition
        val data = dataList.get(position)
        when (data.type) {
            ChatUtils.TYPE_MESSAGE -> {
                if (data.isSender(mContext)) {
                    val requestHolder: RequestMessageHolder = holder as RequestMessageHolder
                    requestHolder.tvContent.text = data.content
                    requestHolder.tvCount.text = if (data.count == 0) "" else data.count.toString()
                    requestHolder.tvTime.text = Utils.getNowToTime(data.date!!)

                    requestHolder.tvContent.setOnLongClickListener(object : OnLongClickListenerByPosition(position) {
                        override fun onLongClick(p0: View?): Boolean {
                            sendMessage(position)
                            return true
                        }
                    })
                } else {
                    val receiveHolder: ReceiveMessageHolder = holder as ReceiveMessageHolder
                    receiveHolder.tvName.text = data.name
                    receiveHolder.tvContent.text = data.content
                    receiveHolder.tvCount.text = if (data.count == 0) "" else data.count.toString()
                    receiveHolder.tvTime.text = Utils.getNowToTime(data.date!!)

                    if (isHideProfileImage(position)) {
                        receiveHolder.ivProfile.visibility = View.INVISIBLE
                        receiveHolder.tvName.visibility = View.GONE

                    } else {
                        receiveHolder.ivProfile.visibility = View.VISIBLE
                        receiveHolder.tvName.visibility = View.VISIBLE

                        if (NetworkUtils.getBitmapList(data.photo, receiveHolder.ivProfile, mContext, "$INTENT_USER/${dataList.get(position).u_idx}"))
                            receiveHolder.ivProfile.setImageResource(R.drawable.icon_profile_default_png)

                    }
                    receiveHolder.tvContent.setOnLongClickListener(object : OnLongClickListenerByPosition(position) {
                        override fun onLongClick(p0: View?): Boolean {
                            sendMessage(position)
                            return true
                        }
                    })
                }

            }
            ChatUtils.TYPE_READLINE -> {
                //NONE
            }
            ChatUtils.TYPE_NOTICE -> {
                val itemHolder = holder as ItemHolder

                itemHolder.tvTime.text = Utils.getNowToTime(data.date!!)
                itemHolder.tvCount.text = if (data.count == 0) "" else data.count.toString()

                itemHolder.title.text = "공지가 등록되었습니다."
                itemHolder.content.text = data.getItemContent()
                holder.content.setOnClickListener {
                    val intent = Intent(mContext.applicationContext, NoticeActivity::class.java)
                    intent.putExtra(INTENT_GROUP, group)
                    intent.putExtra(INTENT_ROOM, room)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    mContext.startActivity(intent)
                }

                if (!data.isSender(mContext)) {
                    val receiveHolder = holder as ItemLHolder
                    data.setPhotoInfo(mContext)
                    receiveHolder.tvName.text = data.name
                    if (isHideProfileImage(position)) {
                        receiveHolder.ivProfile.visibility = View.INVISIBLE
                        receiveHolder.tvName.visibility = View.GONE
                    } else {
                        receiveHolder.ivProfile.visibility = View.VISIBLE
                        if (NetworkUtils.getBitmapList(data.photo, receiveHolder.ivProfile, mContext, "$INTENT_USER/${dataList[position].u_idx}"))
                            receiveHolder.ivProfile.setImageResource(R.drawable.icon_profile_default_png)
                        receiveHolder.tvName.visibility = View.VISIBLE
                    }
                }
            }
            ChatUtils.TYPE_LIGHT -> {
                val itemHolder = holder as ItemHolder

                itemHolder.tvTime.text = Utils.getNowToTime(data.date!!)
                itemHolder.tvCount.text = if (data.count == 0) "" else data.count.toString()

                itemHolder.title.text = "신호등이 등록되었습니다."
                itemHolder.content.text = data.getItemContent()
                holder.content.setOnClickListener {
                    val intent = Intent(mContext.applicationContext, SignalActivity::class.java)
                    intent.putExtra(INTENT_GROUP, group)
                    intent.putExtra(INTENT_ROOM, room)
                    if (data.isSender(mContext))
                        intent.putExtra("mode", Utils.SIGNAL_SENDER)
                    else
                        intent.putExtra("mode", Utils.SIGNAL_RECEIVER)

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    mContext.startActivity(intent)
                }
                if (!data.isSender(mContext)) {
                    val receiveHolder = holder as ItemLHolder
                    data.setPhotoInfo(mContext)
                    receiveHolder.tvName.text = data.name
                    if (isHideProfileImage(position)) {
                        receiveHolder.ivProfile.visibility = View.INVISIBLE
                        receiveHolder.tvName.visibility = View.GONE
                    } else {
                        receiveHolder.ivProfile.visibility = View.VISIBLE
                        if (NetworkUtils.getBitmapList(data.photo, receiveHolder.ivProfile, mContext, "$INTENT_USER/${dataList[position].u_idx}"))
                            receiveHolder.ivProfile.setImageResource(R.drawable.icon_profile_default_png)
                        receiveHolder.tvName.visibility = View.VISIBLE
                    }
                }
            }
            ChatUtils.TYPE_VOTE -> {
                val itemHolder = holder as ItemHolder

                itemHolder.tvTime.text = Utils.getNowToTime(data.date!!)
                itemHolder.tvCount.text = if (data.count == 0) "" else data.count.toString()

                itemHolder.title.text = "투표가 등록되었습니다."
                itemHolder.content.text = data.getItemContent()
                holder.content.setOnClickListener {
                    val intent = Intent(mContext.applicationContext, VoteActivity::class.java)
                    intent.putExtra(INTENT_GROUP, group)
                    intent.putExtra(INTENT_ROOM, room)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    mContext.startActivity(intent)
                }

                if (!data.isSender(mContext)) {
                    val receiveHolder = holder as ItemLHolder
                    data.setPhotoInfo(mContext)
                    receiveHolder.tvName.text = data.name
                    if (isHideProfileImage(position)) {
                        receiveHolder.ivProfile.visibility = View.INVISIBLE
                        receiveHolder.tvName.visibility = View.GONE
                    } else {
                        receiveHolder.ivProfile.visibility = View.VISIBLE
                        receiveHolder.tvName.visibility = View.VISIBLE

                        if (NetworkUtils.getBitmapList(data.photo, receiveHolder.ivProfile, mContext, "$INTENT_USER/${dataList[position].u_idx}"))
                            receiveHolder.ivProfile.setImageResource(R.drawable.icon_profile_default_png)
                    }
                }
            }
            ChatUtils.TYPE_ROLE -> {
            }

            ChatUtils.TYPE_LEAVE -> {
                val name = dataList[position].content!!
                val leaveHolder: LeaveHolder = holder as LeaveHolder
                leaveHolder.tv.text = (name + "님이 퇴장하셨습니다.")
            }
            ChatUtils.TYPE_INVITE -> {
                val name = dataList[position].content!!
                val inviteHolder = holder as InviteHolder
                inviteHolder.tv.text = (name + "님이 입장하셨습니다.")
            }
            ChatUtils.TYPE_PHOTO -> {
                //링크연결 및 사진 표시
            }
            ChatUtils.TYPE_VIDEO -> {
                //링크연결
            }
            ChatUtils.TYPE_FILE -> {
                //링크연결
            }
            else -> {


            }
        }
    }

    fun sendMessage(position: Int) {
        val msg: Message = Message()

        msg.what = position
        onLongClickHandler!!.sendMessage(msg)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val mainView: View
        var viewHolder: RecyclerView.ViewHolder? = null

        val data = dataList[viewType]
        when (data.type) {
            ChatUtils.TYPE_MESSAGE -> {
                if (data.isSender(mContext)) {
                    mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_request, parent, false)
                    viewHolder = RequestMessageHolder(mainView)
                } else {
                    mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_receive, parent, false)
                    viewHolder = ReceiveMessageHolder(mainView)
                }
            }
            ChatUtils.TYPE_READLINE -> {
                mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_readline, parent, false)
                viewHolder = ReadLineHolder(mainView)
            }
            ChatUtils.TYPE_NOTICE ->
                if (data.isSender(mContext)) {
                    mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_item_r, parent, false)
                    viewHolder = ItemHolder(mainView)
                } else {
                    mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_item_l, parent, false)
                    viewHolder = ItemLHolder(mainView)
                }
            ChatUtils.TYPE_LIGHT ->
                if (data.isSender(mContext)) {
                    mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_item_r, parent, false)
                    viewHolder = ItemHolder(mainView)
                } else {
                    mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_item_l, parent, false)
                    viewHolder = ItemLHolder(mainView)
                }
            ChatUtils.TYPE_VOTE ->
                if (data.isSender(mContext)) {
                    mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_item_r, parent, false)
                    viewHolder = ItemHolder(mainView)
                } else {
                    mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_item_l, parent, false)
                    viewHolder = ItemLHolder(mainView)
                }
            ChatUtils.TYPE_ROLE -> {
                if (data.isSender(mContext)) {
                    mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_item_r, parent, false)
                    viewHolder = ItemHolder(mainView)
                } else {
                    mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_item_l, parent, false)
                    viewHolder = ItemLHolder(mainView)
                }
            }
            ChatUtils.TYPE_LEAVE -> {
                mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_leave, parent, false)
                viewHolder = LeaveHolder(mainView)
            }
            ChatUtils.TYPE_PHOTO -> {
            }
            ChatUtils.TYPE_VIDEO -> {

            }
            ChatUtils.TYPE_INVITE -> {
                mainView = LayoutInflater.from(parent!!.context).inflate(R.layout.li_chat_invite, parent, false)
                viewHolder = InviteHolder(mainView)

            }
            else -> {  // ChatUtils.TYPE_FILE -> {

            }
        }
        return viewHolder!!


    }

    private fun isHideProfileImage(position: Int): Boolean = position > 0 && dataList[position].u_idx == dataList[position - 1].u_idx && dataList[position - 1].type == ChatUtils.TYPE_MESSAGE


    override fun getItemCount(): Int = dataList.size


    abstract class OnLongClickListenerByPosition(var position: Int) : View.OnLongClickListener
}
