package org.teamfairy.sopt.teamkerbell.activities.chat.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.ChatUtils
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.items.notice.NoticeActivity
import org.teamfairy.sopt.teamkerbell.activities.items.role.RoleActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.VoteActivity
import org.teamfairy.sopt.teamkerbell.model.data.Role
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessage
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_NOTICE_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROLE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_SIGNAL_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_VOTE_IDX
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.viewholder.chat.*

/**
 * Created by lumiere on 2017-12-30.
 */
class ChatViewAdapter(var dataList: ArrayList<ChatMessage>, var mContext: Context, var group: Team,var room : Room) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val LOG_TAG = this::class.java.simpleName

    private var onLongClickHandler: Handler? = null

    fun setOnLongClickHandler(I: Handler) {
        onLongClickHandler = I
    }

    var isFixedScroll = false
    var pick_idx = -1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {

        val position = holder.adapterPosition
        val data = dataList[position]
        when (data.type) {
            ChatUtils.TYPE_MESSAGE -> {
                if (data.isSender(mContext)) {
                    val requestHolder: RequestMessageHolder = holder as RequestMessageHolder
                    requestHolder.content.text = data.content
                    requestHolder.count.text = if (data.count == 0) "" else data.count.toString()
                    requestHolder.time.text = Utils.getNowToTime(data.date!!)

                    requestHolder.content.setOnLongClickListener(object : OnLongClickListenerByPosition(position) {
                        override fun onLongClick(p0: View?): Boolean {
                            sendMessage(position)
                            return true
                        }
                    })
                    if(data.chat_idx==pick_idx)
                        requestHolder.content.background = ContextCompat.getDrawable(mContext,R.drawable.shape_round_btn_chat_pick)
                    else
                        requestHolder.content.background = ContextCompat.getDrawable(mContext,R.drawable.img_chat_balloon)

                } else {
                    val receiveHolder: ReceiveMessageHolder = holder as ReceiveMessageHolder
                    receiveHolder.name.text = data.name
                    receiveHolder.content.text = data.content
                    receiveHolder.count.text = if (data.count == 0) "" else data.count.toString()
                    receiveHolder.time.text = Utils.getNowToTime(data.date!!)

                    if (isHideProfileImage(position)) {
                        receiveHolder.profile.visibility = View.INVISIBLE
                        receiveHolder.name.visibility = View.GONE

                    } else {
                        receiveHolder.profile.visibility = View.VISIBLE
                        receiveHolder.name.visibility = View.VISIBLE

                        if (NetworkUtils.getBitmapList(data.photo, receiveHolder.profile, mContext, "user${dataList[position].u_idx}"))
                            receiveHolder.profile.setImageResource(R.drawable.icon_profile_default)

                    }
                    receiveHolder.content.setOnLongClickListener(object : OnLongClickListenerByPosition(position) {
                        override fun onLongClick(p0: View?): Boolean {
                            sendMessage(position)
                            return true
                        }
                    })
                    if(data.chat_idx==pick_idx)
                        receiveHolder.content.background = ContextCompat.getDrawable(mContext,R.drawable.shape_round_btn_chat_pick)
                    else
                        receiveHolder.content.background = ContextCompat.getDrawable(mContext, R.drawable.img_chat_balloon_left)
                }

            }
            ChatUtils.TYPE_READLINE -> {
                //NONE
            }
            ChatUtils.TYPE_NOTICE -> {
                val itemHolder = holder as ItemHolder

                itemHolder.time.text = Utils.getNowToTime(data.date!!)
                itemHolder.count.text = if (data.count == 0) "" else data.count.toString()

                itemHolder.icon.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_notice))
                itemHolder.title.text = mContext.getString(R.string.action_notice)
                itemHolder.content.text = data.getItemContent()
                holder.itemView.setOnClickListener {

                    val nIdx : Int =Integer.parseInt(data.content!!.substringBefore("/"))

                    val intent = Intent(mContext.applicationContext, NoticeActivity::class.java)
                    intent.putExtra(INTENT_GROUP, group)
                    intent.putExtra(INTENT_ROOM, room)
                    intent.putExtra(INTENT_NOTICE_IDX, nIdx)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    setFixed(true)


                    mContext.startActivity(intent)
                }

                if (!data.isSender(mContext)) {
                    val receiveHolder = holder as ItemLHolder
                    data.setPhotoInfo(mContext)
                    receiveHolder.name.text = data.name
                    if (isHideProfileImage(position)) {
                        receiveHolder.profile.visibility = View.INVISIBLE
                        receiveHolder.name.visibility = View.GONE
                    } else {
                        receiveHolder.profile.visibility = View.VISIBLE
                        if (NetworkUtils.getBitmapList(data.photo, receiveHolder.profile, mContext, "user${dataList[position].u_idx}"))
                            receiveHolder.profile.setImageResource(R.drawable.icon_profile_default)
                        receiveHolder.name.visibility = View.VISIBLE
                    }
                }
            }
            ChatUtils.TYPE_SIGNAL -> {
                val itemHolder = holder as ItemHolder

                itemHolder.time.text = Utils.getNowToTime(data.date!!)
                itemHolder.count.text = if (data.count == 0) "" else data.count.toString()

                itemHolder.icon.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_signal))
                itemHolder.title.text = mContext.getString(R.string.action_signal)
                itemHolder.content.text = data.getItemContent()



                holder.itemView.setOnClickListener {

                    val sIdx : Int =Integer.parseInt(data.content!!.substringBefore("/"))

                    val intent = Intent(mContext.applicationContext, SignalActivity::class.java)
                    intent.putExtra(INTENT_GROUP, group)
                    intent.putExtra(INTENT_ROOM, room)
                    intent.putExtra(INTENT_SIGNAL_IDX, sIdx)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    setFixed(true)

                    mContext.startActivity(intent)
                }
                if (!data.isSender(mContext)) {
                    val receiveHolder = holder as ItemLHolder
                    data.setPhotoInfo(mContext)
                    receiveHolder.name.text = data.name
                    if (isHideProfileImage(position)) {
                        receiveHolder.profile.visibility = View.INVISIBLE
                        receiveHolder.name.visibility = View.GONE
                    } else {
                        receiveHolder.profile.visibility = View.VISIBLE
                        if (NetworkUtils.getBitmapList(data.photo, receiveHolder.profile, mContext, "user${dataList[position].u_idx}"))
                            receiveHolder.profile.setImageResource(R.drawable.icon_profile_default)
                        receiveHolder.name.visibility = View.VISIBLE
                    }
                }
            }
            ChatUtils.TYPE_VOTE -> {
                val itemHolder = holder as ItemHolder

                itemHolder.time.text = Utils.getNowToTime(data.date!!)
                itemHolder.count.text = if (data.count == 0) "" else data.count.toString()

                itemHolder.icon.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_vote))
                itemHolder.title.text = mContext.getString(R.string.action_vote)
                itemHolder.content.text = data.getItemContent()
                holder.itemView.setOnClickListener {
                    if(data.content.isNullOrBlank()) return@setOnClickListener

                    val vIdx : Int =Integer.parseInt(data.content!!.substringBefore("/"))

                    val intent = Intent(mContext.applicationContext, VoteActivity::class.java)
                    intent.putExtra(INTENT_GROUP, group)
                    intent.putExtra(INTENT_ROOM, room)
                    intent.putExtra(INTENT_VOTE_IDX, vIdx)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    setFixed(true)

                    mContext.startActivity(intent)
                }

                if (!data.isSender(mContext)) {
                    val receiveHolder = holder as ItemLHolder
                    data.setPhotoInfo(mContext)
                    receiveHolder.name.text = data.name
                    if (isHideProfileImage(position)) {
                        receiveHolder.profile.visibility = View.INVISIBLE
                        receiveHolder.name.visibility = View.GONE
                    } else {
                        receiveHolder.profile.visibility = View.VISIBLE
                        receiveHolder.name.visibility = View.VISIBLE

                        if (NetworkUtils.getBitmapList(data.photo, receiveHolder.profile, mContext, "user${dataList[position].u_idx}"))
                            receiveHolder.profile.setImageResource(R.drawable.icon_profile_default)
                    }
                }
            }
            ChatUtils.TYPE_ROLE -> {
                val itemHolder = holder as ItemHolder

                itemHolder.time.text = Utils.getNowToTime(data.date!!)
                itemHolder.count.text = if (data.count == 0) "" else data.count.toString()

                itemHolder.icon.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_role))
                itemHolder.title.text = mContext.getString(R.string.action_role)
                itemHolder.content.text = data.getItemContent()
                holder.itemView.setOnClickListener {
                    if(data.content.isNullOrBlank()) return@setOnClickListener

                    val rIdx : Int =Integer.parseInt(data.content!!.substringBefore("/"))

                    val intent = Intent(mContext.applicationContext, RoleActivity::class.java)
                    intent.putExtra(INTENT_GROUP, group)
                    intent.putExtra(INTENT_ROOM, room)
                    val role = Role(rIdx,room.room_idx,data.getItemContent(),-1,"")
                    intent.putExtra(INTENT_ROLE, role)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    setFixed(true)

                    mContext.startActivity(intent)
                }

                if (!data.isSender(mContext)) {
                    val receiveHolder = holder as ItemLHolder
                    data.setPhotoInfo(mContext)
                    receiveHolder.name.text = data.name
                    if (isHideProfileImage(position)) {
                        receiveHolder.profile.visibility = View.INVISIBLE
                        receiveHolder.name.visibility = View.GONE
                    } else {
                        receiveHolder.profile.visibility = View.VISIBLE
                        receiveHolder.name.visibility = View.VISIBLE

                        if (NetworkUtils.getBitmapList(data.photo, receiveHolder.profile, mContext, "user${dataList[position].u_idx}"))
                            receiveHolder.profile.setImageResource(R.drawable.icon_profile_default)
                    }
                }
            }

            ChatUtils.TYPE_LEAVE, ChatUtils.TYPE_GROUP_LEAVE -> {
                val uId = Integer.parseInt(dataList[position].content)
                val name = DatabaseHelpUtils.getUser(mContext,uId).name

                val leaveHolder: LeaveHolder = holder as LeaveHolder
                leaveHolder.tv.text = (name + "님이 퇴장하셨습니다.")
            }
            ChatUtils.TYPE_ENTER_GROUP, ChatUtils.TYPE_INVITE->{

                if(dataList[position].content.isNullOrEmpty()){

                    val inviteHolder = holder as InviteHolder

                    val name: String = dataList[position].content!!
                    inviteHolder.tv.text = (name + "님이 입장하셨습니다.")
                    return
                }
                val d  = dataList[position].content!!
                val uIds = d.split('/')
                var name: String = ""
                uIds.forEach {
                    val uId = Integer.parseInt(it)
                    if(name.isEmpty()) name = DatabaseHelpUtils.getUser(mContext, uId).name.toString()
                    else name += ",${DatabaseHelpUtils.getUser(mContext, uId).name}"
                }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mainView: View
        var viewHolder: RecyclerView.ViewHolder? = null

        val data = dataList[viewType]
        when (data.type) {
            ChatUtils.TYPE_MESSAGE -> {
                if (data.isSender(mContext)) {
                    mainView = LayoutInflater.from(parent.context).inflate(R.layout.li_chat_request, parent, false)
                    viewHolder = RequestMessageHolder(mainView)
                } else {
                    mainView = LayoutInflater.from(parent.context).inflate(R.layout.li_chat_receive, parent, false)
                    viewHolder = ReceiveMessageHolder(mainView)
                }
            }
            ChatUtils.TYPE_READLINE -> {
                mainView = LayoutInflater.from(parent.context).inflate(R.layout.li_chat_readline, parent, false)
                viewHolder = ReadLineHolder(mainView)
            }

            ChatUtils.TYPE_NOTICE ,ChatUtils.TYPE_SIGNAL,ChatUtils.TYPE_VOTE,ChatUtils.TYPE_ROLE -> {
                if (data.isSender(mContext)) {
                    mainView = LayoutInflater.from(parent.context).inflate(R.layout.li_chat_item_r, parent, false)
                    viewHolder = ItemHolder(mainView)
                } else {
                    mainView = LayoutInflater.from(parent.context).inflate(R.layout.li_chat_item_l, parent, false)
                    viewHolder = ItemLHolder(mainView)
                }
            }
            ChatUtils.TYPE_LEAVE , ChatUtils.TYPE_GROUP_LEAVE-> {
                mainView = LayoutInflater.from(parent.context).inflate(R.layout.li_chat_leave, parent, false)
                viewHolder = LeaveHolder(mainView)
            }
            ChatUtils.TYPE_PHOTO -> {
            }
            ChatUtils.TYPE_VIDEO -> {

            }
            ChatUtils.TYPE_ENTER_GROUP,ChatUtils.TYPE_INVITE ->{
                mainView = LayoutInflater.from(parent.context).inflate(R.layout.li_chat_invite, parent, false)
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

    fun setFixed(b : Boolean){
        isFixedScroll=b
        Log.d("LOG_TAG/isFixedScroll","isFixedScroll be $b")
    }
    fun setPick(i : Int){
        pick_idx = i
        Log.d("LOG_TAG/pick_idx","pick_idx is $i")
    }
}
