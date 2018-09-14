package org.teamfairy.sopt.teamkerbell.activities.main.room

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.tab_chat.*
import org.json.JSONArray
import org.json.JSONObject

import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.*
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.activities.chat.ChatActivity
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.ChatApplication
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.Constants
import org.teamfairy.sopt.teamkerbell.activities.main.interfaces.HasGroupFragment
import org.teamfairy.sopt.teamkerbell.activities.main.room.adapter.RoomListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.realm.*
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
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

    var isUpdateJoined: IsUpdateR? = null

    private var mSocket: Socket? = null

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


        return v
    }

    override fun onResume() {
        super.onResume()

        updateRoomList()
        if(mSocket?.connected()==true) {
            attachSocket()
            enterChatListSocket()
        }
        else
            connectSocket()

        addChangeJoinedRoomListener()
    }

    override fun onPause() {
        super.onPause()
        detachSocket()
        isUpdateJoined?.removeAllChangeListeners()
    }


    override fun onDestroy() {
        super.onDestroy()
        disconnectSocket()
    }


    override fun onClick(p0: View?) {

        val pos = recyclerView.getChildAdapterPosition(p0)

        dataList[pos].newMsgCnt=0

        val i = Intent(activity.applicationContext, ChatActivity::class.java)
        i.putExtra(INTENT_GROUP,group)
        i.putExtra(INTENT_ROOM, dataList[pos])
        startActivity(i)
//        detachSocket()
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
                enterChatListSocket()
                realm.beginTransaction()
                t.isUpdate = false
                realm.commitTransaction()
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

        adapter.notifyDataSetChanged()
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
        if (socket == null)
            activity.finish()

        mSocket = socket!!
        mSocket!!.on(Socket.EVENT_CONNECT, onConnect)
        mSocket!!.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket!!.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket!!.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        attachSocket()
        mSocket!!.connect()

    }
    private fun attachSocket(){
        Log.d("$LOG_TAG/Socket", "attach socket listener")
        mSocket?.on(Constants.ENTER_ROOM_LIST_RESULT, onEnterRoomListResult)
        mSocket?.on(Constants.UPDATE_CHAT_LIST, onUpdateChat)
    }
    private fun detachSocket(){
        Log.d("$LOG_TAG/Socket", "detach socket listener")
        mSocket?.off(Constants.ENTER_ROOM_LIST_RESULT, onEnterRoomListResult)
        mSocket?.off(Constants.UPDATE_CHAT_LIST, onUpdateChat)
    }
    private fun disconnectSocket(){
        mSocket?.disconnect()
        mSocket?.off(Socket.EVENT_CONNECT, onConnect)
        mSocket?.off(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket?.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        detachSocket()
        mSocket?.close()
    }


    private fun enterChatListSocket() {
        if(mSocket?.connected() != true) return

        val jsonObj = JSONObject()
        jsonObj.put(Constants.JSON_U_IDX, LoginToken.getUserIdx(activity.applicationContext))
        mSocket?.emit(Constants.ENTER_ROOM_LIST, jsonObj.toString())
        Log.d("$LOG_TAG/Socket", "${Constants.ENTER_ROOM_LIST} with $jsonObj")


    }



    private val onConnect = Emitter.Listener {

        activity?.runOnUiThread(Runnable {
            Log.i("$LOG_TAG/Socket onConnect/", "connected")
            enterChatListSocket()
        })
    }

    private val onDisconnect = Emitter.Listener {
        Log.i("$LOG_TAG/Socket onDisconnect", "disconnected")
        activity?.runOnUiThread(Runnable {
//            Log.i("$LOG_TAG/Socket onDisconnect", "disconnected")
            detachSocket()
        })
    }

    private val onConnectError = Emitter.Listener {
        Log.e("$LOG_TAG/Socket ConnectError", "Error connecting")
        activity?.runOnUiThread(Runnable {
//            Toast.makeText(activity.applicationContext,
//                    R.string.error_connect, Toast.LENGTH_LONG).show()
        })
    }

    private val onEnterRoomListResult = Emitter.Listener { args ->


        if(args[0]==null  || args[0]==0){
            Log.d("$LOG_TAG/Socket ${Constants.ENTER_ROOM_LIST_RESULT}", "nothing")
            return@Listener
        }

        Log.d("$LOG_TAG/Socket ${Constants.ENTER_ROOM_LIST_RESULT}", args[0].toString())


        activity?.runOnUiThread(Runnable {


            val dataArray: JSONArray = JSONArray(args[0].toString())


            var totalCnt : Int =0
            for (i in 0 until dataArray.length()) {
                val data = dataArray.getJSONObject(i)

                val rIdx = data.get(Constants.JSON_ROOM_IDX)
                val r : Room =(dataList.firstOrNull { it.room_idx ==  rIdx}) ?: Room()

                val type  = data.getInt(Constants.JSON_TYPE)
                when(type){
                    ChatUtils.TYPE_INVITE, ChatUtils.TYPE_ENTER_GROUP->{
                        val d  = data.getString(Constants.JSON_CONTENT)
                        val uIds = d.split('/')
                        var name: String = ""
                        uIds.forEach {
                            val uId = Integer.parseInt(it)
                            if(name.isEmpty()) name = DatabaseHelpUtils.getUser(activity.applicationContext, uId).name.toString()
                            else name += ",${DatabaseHelpUtils.getUser(activity.applicationContext, uId).name}"
                        }

                        r.lastMsgStr=(name + "님이 입장하셨습니다.")
                    }
                    ChatUtils.TYPE_LEAVE, ChatUtils.TYPE_GROUP_LEAVE->{
                        val d  = data.getString(Constants.JSON_CONTENT)
                        val uIds = d.split('/')
                        var name: String = ""
                        uIds.forEach {
                            val uId = Integer.parseInt(it)
                            if(name.isEmpty()) name = DatabaseHelpUtils.getUser(activity.applicationContext, uId).name.toString()
                            else name += ",${DatabaseHelpUtils.getUser(activity.applicationContext, uId).name}"
                        }

                        r.lastMsgStr=(name + "님이 퇴장하셨습니다.")
                    }
                    ChatUtils.TYPE_NOTICE->{
                        r.lastMsgStr  = "공지가 등록되었습니다."
                    }

                    ChatUtils.TYPE_VOTE->{
                        r.lastMsgStr  = "투표가 등록되었습니다."
                    }
                    ChatUtils.TYPE_SIGNAL->{
                        r.lastMsgStr  = "신호등이 등록되었습니다."
                    }
                    ChatUtils.TYPE_ROLE->{
                        r.lastMsgStr  = "역할분담이 등록되었습니다."
                    }
                    ChatUtils.TYPE_MESSAGE->{
                        r.lastMsgStr  = data.getString(Constants.JSON_CONTENT)
                    }
                }
                r.lastMsgTime= data.getString(Constants.JSON_WRITE_TIME)
                r.newMsgCnt = data.getInt(Constants.JSON_UN_READ_COUNT)
                totalCnt+=r.newMsgCnt

            }
            adapter.notifyDataSetChanged()

            if(totalCnt==0) activity.tab_badge.text=""
            activity.tab_badge.text=when(totalCnt){
                in Int.MIN_VALUE..0 -> ""
                in 999 downTo 0 -> totalCnt.toString()
                else-> "999+"
            }
            activity.tab_badge.visibility = if(activity.tab_badge.text.isNullOrBlank()) View.INVISIBLE else View.VISIBLE

            isConnectedRoomList = true

        })
    }


    private val onUpdateChat = Emitter.Listener { args ->
        if(args[0]==null){
            Log.d("$LOG_TAG/Socket ${Constants.UPDATE_CHAT_LIST}", "nothing")
            return@Listener
        }

        Log.d("$LOG_TAG/Socket ${Constants.UPDATE_CHAT_LIST}", args[0].toString())

        activity?.runOnUiThread(Runnable {
            updateListFromJSON(JSONObject(args[0].toString()))
            adapter.notifyDataSetChanged()
        })
    }

    private fun updateListFromJSON(data : JSONObject): Room{

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
        return r
    }

}
