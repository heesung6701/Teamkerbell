package org.teamfairy.sopt.teamkerbell.activities.main.room


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import io.realm.*
import org.json.JSONObject

import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.*
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.ARG_GROUPS
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.ARG_LAST_MESSAGE
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.dataBaseReference
import org.teamfairy.sopt.teamkerbell.activities.chat.ChatActivity
import org.teamfairy.sopt.teamkerbell.activities.main.interfaces.HasGroupFragment
import org.teamfairy.sopt.teamkerbell.activities.main.room.adapter.RoomListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessageF.Companion.ARG_CHAT_IDX
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessageF.Companion.ARG_CONTENT
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessageF.Companion.ARG_DATE
import org.teamfairy.sopt.teamkerbell.model.realm.*
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LEAVE_ROOM_PARAM_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.io.File
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass.
 * Use the [RoomListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoomListFragment : Fragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, HasGroupFragment {

    private val TAG = this::class.java.simpleName

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()

    override fun onRefresh() {
        NetworkUtils.connectRoomList(activity.applicationContext, null, true)
        NetworkUtils.connectJoinedRoomList(activity.applicationContext, null, true)
        mSwipeRefreshLayout.isRefreshing = false
        if ( fab.visibility == View.GONE)
            fab.show()
    }


    override var group: Team by Delegates.notNull()

    var adapter: RoomListAdapter by Delegates.notNull()
    var dataList: ArrayList<Room> = arrayListOf<Room>()
    var recyclerView: RecyclerView by Delegates.notNull()

    var file: File? = null

    var isUpdateJoined: IsUpdateR? = null

    var lastMsgListeners: HashMap<Room, ValueEventListener> = HashMap()


    private var lastMsgs : RealmResults<LastMsgR>?=null
    var fab : FloatingActionButton by Delegates.notNull()


    var qqq=0
    var ttt=0
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_room_list, container, false)

        recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        dataList = arrayListOf<Room>()
        adapter = RoomListAdapter(dataList, activity.applicationContext)
        adapter.setOnItemClickListener(this)
        recyclerView.adapter = adapter

        fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {

            val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
            realm.beginTransaction()

            val l = realm.where(LastMsgR::class.java).equalTo(Room.ARG_ROOM_IDX,dataList[qqq].room_idx ).findFirst() ?: realm.createObject(LastMsgR::class.java, dataList[qqq].room_idx)
            l.content="더미데이터${ttt++}"
            l.u_idx=LoginToken.getUserIdx(activity.applicationContext)
            l.type=0
            l.date="2018-08-28"

            l.g_idx=group.g_idx
            l.cnt=l.cnt + 1
            realm.commitTransaction()
            qqq++
            if(qqq>dataList.lastIndex) qqq=0

//            val i = Intent(activity.applicationContext, MakeRoomActivity::class.java)
//            i.putExtra(IntentTag.INTENT_GROUP, group)
//            startActivity(i)
//            activity.overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility == View.GONE) {
                    fab.show()
                }
            }
        })


        mSwipeRefreshLayout = v.findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        updateRoomList()
        return v
    }

    override fun onResume() {
        super.onResume()

        NetworkUtils.connectRoomList(activity.applicationContext, HandlerGet(this))
        NetworkUtils.connectJoinedRoomList(activity.applicationContext, HandlerGet(this))

        updateRoomList()
        addChangeJoinedRoomListener()
    }

    override fun onStop() {
        super.onStop()
        isUpdateJoined?.removeAllChangeListeners()
        removeLastMsgListenr()
    }


    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        val i = Intent(activity.applicationContext, ChatActivity::class.java)
        i.putExtra(INTENT_GROUP,group)
        i.putExtra(INTENT_ROOM, dataList[pos])
        startActivity(i)
    }

    private fun addChangeJoinedRoomListener() {

        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
        isUpdateJoined = realm.where(IsUpdateR::class.java).equalTo("what", StatusCode.joinedRoomChange).findFirst()
        if (isUpdateJoined == null) {
            realm.beginTransaction()
            isUpdateJoined = realm.createObject(IsUpdateR::class.java, StatusCode.joinedRoomChange)
            isUpdateJoined!!.isUpdate = false
            realm.commitTransaction()
        } else {
            if (isUpdateJoined?.isUpdate == true) {
                updateRoomList()
                realm.beginTransaction()
                isUpdateJoined!!.isUpdate = false
                realm.commitTransaction()
            }
        }
        isUpdateJoined!!.addChangeListener<IsUpdateR> { t: IsUpdateR, _ ->
            if (t.isUpdate) {
                Log.d("$TAG /isUpdateJoinedRoom", "is ${t.isUpdate}")

                updateRoomList()
                realm.executeTransaction {
                    t.isUpdate = false
                }
            }
        }

    }


    private fun updateRoomList() {

        val realm = getRealmDefault(activity.applicationContext)

        dataList.clear()
        adapter.notifyDataSetChanged()
        var i = 0
        val groupR = realm.where(JoinedRoomR::class.java).equalTo(Team.ARG_G_IDX, group.g_idx).equalTo(User.ARG_U_IDX,LoginToken.getUserIdx(activity.applicationContext)).findAll()
        groupR.forEach {
            val roomR = realm.where(RoomR::class.java).equalTo(Room.ARG_ROOM_IDX, it.room_idx).findFirst()
                    ?: RoomR()

            dataList.add(roomR.toChatRoom())
//            updateRecentMessage(roomR.toChatRoom(), i)
            i++
        }
        removeLastMsgListenr()
        addLastMsgListener()
        adapter.notifyDataSetChanged()
    }


    private fun addLastMsgListener() {


        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
        lastMsgs   = realm.where(LastMsgR::class.java).equalTo(Team.ARG_G_IDX,group.g_idx).findAll()

        dataList.forEach {
            val rIdx = it.room_idx
            val lastMsgR : LastMsgR = lastMsgs!!.firstOrNull(){ it.room_idx== rIdx} ?: return@forEach
            it.setLastMsg(lastMsgR.content, lastMsgR.date,lastMsgR.cnt)
        }
        if(lastMsgs!!.size==0){
            lastMsgs=null
            return
        }
        lastMsgs!!.addChangeListener {t, changeSet ->
            t.forEach {
                val rId : Int = it.room_idx?:-1
                val data : Room? = dataList.first { it.room_idx  == rId }
                data?.setLastMsg(it.content,it.date,it.cnt)
            }
            adapter.notifyDataSetChanged()
        }

    }
    private fun removeLastMsgListenr(){
        lastMsgs?.removeAllChangeListeners()
    }

    private fun updateRecentMessageFire(room: Room, position: Int) {
        Log.d("Firebase_grouplist", "updateRecent")

        val dataBaseGroup = dataBaseReference.child(ARG_GROUPS).child(group.ctrl_name).child(room.ctrl_name)

        val dataBaseLastMessage = dataBaseGroup.child(ARG_LAST_MESSAGE)
        val lastMsgListener = object : ValueEventListenerByPosition(position,room.room_idx) {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapShot: DataSnapshot) {

                val message = dataSnapShot.child(ARG_CONTENT).value.toString()
                val date = dataSnapShot.child(ARG_DATE).value.toString()

                if (message != "null" && date != "null") {
                    val chatIdx = dataSnapShot.child(ARG_CHAT_IDX).value.toString().toInt()

                    dataList[position].lastMsgStr = message
                    dataList[position].lastMsgTime = Utils.getNowToDateTime(date)
                    val newMessage: Int = chatIdx
                    if (activity != null) {
                        val recentChatIdx = DatabaseHelpUtils.getRecentChatIdx(activity.applicationContext, room_idx)
                        if (newMessage > recentChatIdx)
                            dataList[position].newMsgCnt = newMessage - recentChatIdx

                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
        dataBaseLastMessage.addValueEventListener(lastMsgListener)
        lastMsgListeners[room] = lastMsgListener
    }


    private fun removelastMsgListenersFire() {
        lastMsgListeners.forEach {
            val dataBaseGroup = dataBaseReference.child(ARG_GROUPS).child(it.key.ctrl_name)
            val dataBaseLastMessage = dataBaseGroup.child(ARG_LAST_MESSAGE)
            dataBaseLastMessage.removeEventListener(it.value)
        }
    }



    private fun connectRoomList(b: Boolean) {
        NetworkUtils.connectRoomList(activity.applicationContext, HandlerGet(this), b)
    }


    private class HandlerGet(fragment: RoomListFragment) : Handler() {
        private val mFragment: WeakReference<RoomListFragment> = WeakReference<RoomListFragment>(fragment)

        override fun handleMessage(msg: Message) {
            mFragment.get()?.updateRoomList()
        }
    }


    override fun changeGroup(g: Team) {
        group = g
        updateRoomList()

    }

    abstract class ValueEventListenerByPosition(var position: Int,var room_idx:Int) : ValueEventListener


}
