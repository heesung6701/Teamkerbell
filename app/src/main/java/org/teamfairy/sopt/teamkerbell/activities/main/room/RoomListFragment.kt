package org.teamfairy.sopt.teamkerbell.activities.main.room

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.realm.*
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject

import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.*
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.activities.chat.ChatActivity
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.ChatApplication
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.Constants
import org.teamfairy.sopt.teamkerbell.activities.main.interfaces.HasGroupFragment
import org.teamfairy.sopt.teamkerbell.activities.main.room.adapter.RoomListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.realm.*
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.viewholder.chat.InviteHolder
import java.io.File
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass.
 * Use the [RoomListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoomListFragment : Fragment(), View.OnClickListener, HasGroupFragment {

    private val LOG_TAG = this::class.java.simpleName

    override var group: Team by Delegates.notNull()

    var adapter: RoomListAdapter by Delegates.notNull()
    var dataList: ArrayList<Room> = arrayListOf<Room>()
    var recyclerView: RecyclerView by Delegates.notNull()

    var file: File? = null

    var isUpdateJoined: IsUpdateR? = null

    private var lastMsgs : RealmResults<LastMsgR>?=null


    private var mSocket: Socket by Delegates.notNull()

    private var isConnectedRoomList = false


    var fab : FloatingActionButton by Delegates.notNull()

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

//            val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
//            realm.beginTransaction()
//
//            val l = realm.where(LastMsgR::class.java).equalTo(Room.ARG_ROOM_IDX,dataList[qqq].room_idx ).findFirst() ?: realm.createObject(LastMsgR::class.java, dataList[qqq].room_idx)
//            l.content="더미데이터${ttt++}"
//            l.u_idx=LoginToken.getUserIdx(activity.applicationContext)
//            l.type=0
//            l.date="2018-08-28"
//
//            l.g_idx=group.g_idx
//            l.cnt=l.cnt + 1
//            realm.commitTransaction()
//            qqq++
//            if(qqq>dataList.lastIndex) qqq=0

            val i = Intent(activity.applicationContext, MakeRoomActivity::class.java)
            i.putExtra(IntentTag.INTENT_GROUP, group)
            startActivity(i)
            activity.overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
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


        updateRoomList()

        connectSocket()

        return v
    }

    override fun onResume() {
        super.onResume()

        attachSocket()
        enterChatListSocket()

        addChangeJoinedRoomListener()
    }

    override fun onStop() {
        super.onStop()
        detachSocket()
        isUpdateJoined?.removeAllChangeListeners()
//        removeLastMsgListenr()

    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectSocket()
    }


    override fun onClick(p0: View?) {

        val pos = recyclerView.getChildAdapterPosition(p0)
        val i = Intent(activity.applicationContext, ChatActivity::class.java)
        i.putExtra(INTENT_GROUP,group)
        i.putExtra(INTENT_ROOM, dataList[pos])
        startActivity(i)
        detachSocket()
        dataList[pos].newMsgCnt=0
    }

    private fun addChangeJoinedRoomListener() {

        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
        isUpdateJoined = realm.where(IsUpdateR::class.java).equalTo(IsUpdateR.ARG_WHAT, StatusCode.joinedRoomChange).findFirst()
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
                Log.d("$LOG_TAG /isUpdateJoinedRoom", "is ${t.isUpdate}")

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
        var i = 0
        val groupR = realm.where(JoinedRoomR::class.java).equalTo(Team.ARG_G_IDX, group.g_idx).equalTo(User.ARG_U_IDX,LoginToken.getUserIdx(activity.applicationContext)).findAll()
        groupR.forEach {
            val roomR = realm.where(RoomR::class.java).equalTo(Room.ARG_ROOM_IDX, it.room_idx).findFirst()
                    ?: RoomR()

            dataList.add(roomR.toChatRoom())
            i++
        }
//        removeLastMsgListenr()
//        addLastMsgListener()
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


    private class HandlerGet(fragment: RoomListFragment) : Handler() {
        private val mFragment: WeakReference<RoomListFragment> = WeakReference<RoomListFragment>(fragment)

        override fun handleMessage(msg: Message) {
            mFragment.get()?.updateRoomList()
        }
    }


    override fun changeGroup(g: Team) {

        Log.i("$LOG_TAG/Change Group", "${group.g_idx}->${g.g_idx}")
        group = g
        updateRoomList()

        connectSocket()

    }

    /* 소켓 관련 함수 */

    private fun connectSocket() {
        val socket = ChatApplication.getSocket(group.g_idx)
        if (socket == null) activity.finish()

        mSocket = socket!!
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        mSocket.connect()



    }
    private fun attachSocket(){
        Log.d(LOG_TAG, "attach socket listener")
        mSocket.on(Constants.UPDATE_CHAT, onUpdateChat)
        mSocket.on(Constants.ENTER_ROOM_LIST_RESULT, onEnterRoomListResult)
    }
    private fun detachSocket(){
        Log.d(LOG_TAG, "detach socket listener")
        mSocket.off(Constants.UPDATE_CHAT, onUpdateChat)
        mSocket.off(Constants.ENTER_ROOM_LIST_RESULT, onEnterRoomListResult)
    }
    private fun disconnectSocket(){
        mSocket.disconnect()
        mSocket.off(Socket.EVENT_CONNECT, onConnect)
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        mSocket.off(Constants.UPDATE_CHAT, onUpdateChat)
        mSocket.off(Constants.ENTER_ROOM_LIST_RESULT, onEnterRoomListResult)
        mSocket.close()
    }


    private fun enterChatListSocket() {
        val jsonObj = JSONObject()
        jsonObj.put(Constants.JSON_U_IDX, LoginToken.getUserIdx(activity.applicationContext))
        mSocket.emit(Constants.ENTER_ROOM_LIST, jsonObj.toString())
        Log.d("$LOG_TAG/Socket", "${Constants.ENTER_ROOM_LIST} with $jsonObj")


    }



    private val onConnect = Emitter.Listener {

        activity.runOnUiThread(Runnable {

                Log.i("$LOG_TAG/Socket onConnect/", "connected")
                enterChatListSocket()

        })
    }

    private val onDisconnect = Emitter.Listener {
        activity.runOnUiThread(Runnable {
            Log.i("$LOG_TAG/Socket onDisconnect", "disconnected")

        })
    }

    private val onConnectError = Emitter.Listener {
        Log.e("$LOG_TAG/Socket ConnectError", "Error connecting")
        activity.runOnUiThread(Runnable {
            Toast.makeText(activity.applicationContext,
                    R.string.error_connect, Toast.LENGTH_LONG).show()
        })
    }

    private val onEnterRoomListResult = Emitter.Listener { args ->

        if(args[0]==null) return@Listener

        Log.d("$LOG_TAG/Socket ${Constants.ENTER_ROOM_LIST_RESULT}", args[0].toString())
        activity.runOnUiThread(Runnable {


            val dataArray: JSONArray = JSONArray(args[0].toString())


            for (i in 0 until dataArray.length()) {
                val data = dataArray.getJSONObject(i)

                val rIdx = data.get(Constants.JSON_ROOM_IDX)
                val r : Room =(dataList.firstOrNull { it.room_idx ==  rIdx}) ?: Room()

                r.lastMsgTime= data.getString(Constants.JSON_WRITE_TIME)
                r.newMsgCnt = data.getInt(Constants.JSON_UN_READ_COUNT)
                r.lastMsgStr  = data.getString(Constants.JSON_CONTENT)

            }
            adapter.notifyDataSetChanged()

            isConnectedRoomList = true

        })
    }


    private val onUpdateChat = Emitter.Listener { args ->
        if(args[0]==null) return@Listener

        Log.d("$LOG_TAG/Socket ${Constants.UPDATE_CHAT}", args[0].toString())
        activity.runOnUiThread(Runnable {
            updateListFromJSON(org.json.JSONObject(args[0].toString()))
        })
    }

    private fun updateListFromJSON(data : JSONObject){

        val rIdx = data.getInt(Constants.JSON_ROOM_IDX)
        val message: String = data.getString(Constants.JSON_CONTENT)
        val uIdx = data.getInt(Constants.JSON_U_IDX)
        val type = data.getInt(Constants.JSON_TYPE)
        val writeTime = data.getString(Constants.JSON_WRITE_TIME)


        val r : Room =(dataList.firstOrNull { it.room_idx ==  rIdx}) ?: Room()


        when(type){

            ChatUtils.TYPE_ENTER_GROUP->{
                val uId = Integer.parseInt(message)
                val name = DatabaseHelpUtils.getUser(activity.applicationContext,uId).name
                r.lastMsgStr = (name + "님이 입장하셨습니다.")
            }
            else->{
                r.lastMsgStr = message
                r.lastMsgTime=writeTime
                r.newMsgCnt+=1
            }
        }


        adapter.notifyDataSetChanged()




    }

}
