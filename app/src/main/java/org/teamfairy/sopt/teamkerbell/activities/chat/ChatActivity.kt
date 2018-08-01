package org.teamfairy.sopt.teamkerbell.activities.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import io.realm.Realm
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.app_bar_chat.*
import kotlinx.android.synthetic.main.content_chat.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONArray
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.*
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.ARG_LAST_MESSAGE
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.dataBaseEndpoints
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.dataBaseFireToken
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.dataBaseGroup
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.dataBaseLastMessage
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.dataBaseMessages
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils.Companion.setDatabaseGroup
import org.teamfairy.sopt.teamkerbell.activities.chat.adapter.ChatViewAdapter
import org.teamfairy.sopt.teamkerbell.activities.chat.dialog.ChooseWorkDialog
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.ChatApplication
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.Constants
import org.teamfairy.sopt.teamkerbell.activities.main.MainActivity
import org.teamfairy.sopt.teamkerbell.activities.items.notice.MakeNoticeActivity
import org.teamfairy.sopt.teamkerbell.activities.items.notice.NoticeCardActivity
import org.teamfairy.sopt.teamkerbell.activities.items.pick.PickListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.role.RoleListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.MakeSignalActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.MakeVoteActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.VoteListActivity
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.Team.Companion.ARG_G_IDX
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.data.User.Companion.ARG_U_IDX
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessage
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessageF
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessageF.Companion.ARG_CHAT_IDX
import org.teamfairy.sopt.teamkerbell.model.realm.*
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR.Companion.ARG_WHAT
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_DELETE
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LEAVE_ROOM_PARAM_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_CHATID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_ENTIRESTATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_OPENSTATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_UID
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_PICK_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.ENTIRE_STATUS_ENTIRE
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.OPEN_STATUS_OPEN
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class ChatActivity : AppCompatActivity() {


    private val LOG_TAG = ChatActivity::class.java.simpleName

    private var isAddedFirebaseListener: Boolean = false

    private var isOpenDrawLayout: Boolean = false
    private var isExpanded: Boolean = false

    var group: Team by Delegates.notNull()
    var room: Room by Delegates.notNull()

    var dataList: ArrayList<ChatMessage> = arrayListOf<ChatMessage>()

    private var adapter_chat: ChatViewAdapter by Delegates.notNull()
    private var adapter_user: UserListAdapter by Delegates.notNull()

    var endPoints: HashMap<Int, Int> = HashMap<Int, Int>()

    private var userList = ArrayList<User>()


    var pick_idx = -1
    var recentChatIdx = -1
    var lastChatIdx = 0
    var isShowReadLine = false

    var positionNow = -1


    private var mSocket: Socket by Delegates.notNull()
    private var isConnected: Boolean = false

    private var isConnectedRoom = false


    private var lastChatIdxListener: ValueEventListener = object : ValueEventListener {

        override fun onDataChange(dataSnapShot: DataSnapshot) {
            if (dataSnapShot.value != null && !dataSnapShot.value!!.equals("null")) {
                lastChatIdx = dataSnapShot.value.toString().toInt()
                Log.d("$LOG_TAG/lastChatIdx", lastChatIdx.toString())

                dataBaseEndpoints.child(LoginToken.getUserIdx(applicationContext).toString()).setValue(lastChatIdx)
                if (pick_idx == -1) {
                    positionNow = lastChatIdx
                    Log.d("$LOG_TAG/positionNow", positionNow.toString())
                }
                if (dataList.size > 0) {
                    if (dataList[dataList.lastIndex].chat_idx >= positionNow) {
                        scrollToPosition(listView_chat, dataList.lastIndex)
                    }
                }

            }

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)

        getUserList()

        group = intent.getParcelableExtra(INTENT_GROUP)
        room = intent.getParcelableExtra(INTENT_ROOM)

        pick_idx = intent.getIntExtra(INTENT_PICK_IDX, -1)

        supportActionBar!!.title = room.real_name
        recentChatIdx = DatabaseHelpUtils.getRecentChatIdx(applicationContext, room.room_idx)
        Log.d("$LOG_TAG/RecentChatIdx", recentChatIdx.toString())

        val layoutManager = LinearLayoutManager(this)
        listView_chat.layoutManager = layoutManager
        adapter_chat = ChatViewAdapter(dataList, applicationContext, group, room)
        adapter_chat.setOnLongClickHandler(HandlerLongClick(this))
        listView_chat.adapter = adapter_chat

        tv_nav_room_name.text = room.real_name


        setDatabaseGroup(group, room)
        FirebaseMessageUtils.getLastChatIdx(group, room, HandlerGetLastMsg(this))



        edt_sendmessage.setOnFocusChangeListener { _, b ->
            if (b) {
                if (isExpanded) {
                    isExpanded = false
                    layout_expanded_menu.visibility = View.GONE
                    btn_expand.background = VectorDrawableCompat.create(resources, R.drawable.icon_chat_expand, null)
                }

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(edt_sendmessage, InputMethodManager.SHOW_IMPLICIT)

                scrollToPosition(listView_chat, dataList.size - 1)
            }
        }
        btn_expand.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                hideKeyboard()
                layout_expanded_menu.visibility = View.VISIBLE
                btn_expand.setImageDrawable(VectorDrawableCompat.create(resources, R.drawable.icon_chat_expandclose, null))
            } else {
                layout_expanded_menu.visibility = View.GONE
                btn_expand.setImageDrawable(VectorDrawableCompat.create(resources, R.drawable.icon_chat_expand, null))
            }
        }

        btn_back.setOnClickListener {
            onBackPressed()
        }
        btn_invite.setOnClickListener {

            val intent = Intent(applicationContext, InviteUserActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            intent.putExtra(INTENT_ROOM, room)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
            drawer_layout.closeDrawer(Gravity.END)
        }
        btn_draw_menu.setOnClickListener {
            isOpenDrawLayout = !isOpenDrawLayout
            if (isOpenDrawLayout) {
                hideKeyboard()
                drawer_layout.openDrawer(Gravity.END)
            } else {
                drawer_layout.closeDrawer(Gravity.END)
            }
        }
        btn_board.setOnClickListener {
            //            val intent = Intent(this, BoardActivity::class.java)
//            intent.putExtra("group", group)
//            startActivity(intent)
            drawer_layout.closeDrawer(Gravity.END)
        }
        btn_leave.setOnClickListener {
            attemptLeave()

        }

        btn_camera.setOnClickListener {
            //카메라 버튼을 눌렀을 때
        }
        btn_gallery.setOnClickListener {
            //갤러리 버튼을 눌렀을 때
        }
        btn_video.setOnClickListener {
            //비디오 버튼을 눌렀을 때
        }
        btn_notice.setOnClickListener {

            val intent = Intent(this, MakeNoticeActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            intent.putExtra(INTENT_ROOM, room)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }
        btn_light.setOnClickListener {
            val intent = Intent(this, MakeSignalActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            intent.putExtra(INTENT_ROOM, room)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }
        btn_vote.setOnClickListener {
            val intent = Intent(this, MakeVoteActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            intent.putExtra(INTENT_ROOM, room)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }


        btn_sendMessage.setOnClickListener {
            if (!isConnectedRoom) {
                Toast.makeText(applicationContext, "현재 채팅방에 연결할수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val edt: EditText = edt_sendmessage
            if (edt.text.isNotEmpty()) {
                val txt = edt.text.toString()
                edt.setText("")
//                sendMessage(txt)
                sendMessageSocket(txt)

            } else {
                edt.requestFocus()
            }
        }


        btn_nav_notice.setOnClickListener {
            val intent = Intent(this, NoticeCardActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            intent.putExtra(INTENT_ROOM, room)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }
        btn_nav_pick.setOnClickListener {
            val intent = Intent(this, PickListActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            intent.putExtra(INTENT_ROOM, room)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)

        }
        btn_nav_role.setOnClickListener {
            val intent = Intent(this, RoleListActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            intent.putExtra(INTENT_ROOM, room)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }
        btn_nav_signal.setOnClickListener {
            val intent = Intent(this, SignalListActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            intent.putExtra(INTENT_ROOM, room)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }
        btn_nav_vote.setOnClickListener {
            val intent = Intent(this, VoteListActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            intent.putExtra(INTENT_ROOM, room)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }

        btn_nav_cloud_drive.setOnClickListener {
            if(tv_nav_cloud_link.visibility == View.VISIBLE){
                tv_nav_cloud_link.visibility=View.GONE
                edt_nav_cloud_link.visibility=View.VISIBLE

                btn_nav_cloud_drive.setColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColor))
            }else{
                if(!edt_nav_cloud_link.text.isNullOrBlank()){
                    if(URLUtil.isValidUrl( edt_nav_cloud_link.text.toString()))
                        tv_nav_cloud_link.text=edt_nav_cloud_link.text.toString()
                    else
                        Toast.makeText(applicationContext,"유효하지 않는 URL 입니다.",Toast.LENGTH_SHORT).show()
                }
                tv_nav_cloud_link.visibility=View.VISIBLE
                edt_nav_cloud_link.visibility=View.GONE

                btn_nav_cloud_drive.setColorFilter(ContextCompat.getColor(applicationContext, R.color.black))
            }
        }



        connectSocket()


    }


    private fun hideKeyboard() {
        edt_sendmessage.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edt_sendmessage.windowToken, 0)

    }

    fun scrollToPosition(recyclerView: RecyclerView, position: Int) {
        recyclerView.layoutManager = LinearLayoutManagerWithSmoothScroller(applicationContext)
        recyclerView.layoutManager.offsetChildrenVertical(0)
        recyclerView.scrollToPosition(position)
    }

    private fun attemptLeave() {
        val task = GetMessageTask(applicationContext, HandlerLeave(this), LoginToken.getToken(applicationContext))

        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_LEAVE_ROOM_PARAM_ROOM_IDX, room.room_idx)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        task.execute(USGS_REQUEST_URL.URL_LEAVE_ROOM, METHOD_DELETE, jsonParam.toString())
    }

    private fun leavedRoom(msg: Message) {
        when (msg.what) {
            MSG_SUCCESS -> {

                val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
                realm.beginTransaction()
                realm.where(JoinedRoomR::class.java).equalTo(ARG_ROOM_IDX, room.room_idx).equalTo(ARG_U_IDX, LoginToken.getUserIdx(applicationContext)).findAll().deleteAllFromRealm()
                realm.where(RoomR::class.java).equalTo(ARG_ROOM_IDX, room.room_idx).findAll().deleteAllFromRealm()

                val isUpdateR
                        : IsUpdateR = realm.where(IsUpdateR::class.java).equalTo(ARG_WHAT, StatusCode.joinedRoomChange).findFirst()
                        ?: makeUpdateR(realm, StatusCode.joinedRoomChange)
                isUpdateR.isUpdate = true

                realm.commitTransaction()

                finish()
            }
            else ->
                Toast.makeText(applicationContext, "잠시 후 시도해주세요", Toast.LENGTH_SHORT).show()
        }

    }

    private class HandlerLeave(activity: ChatActivity) : Handler() {
        private val mActivity: WeakReference<ChatActivity> = WeakReference<ChatActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            activity?.leavedRoom(msg)
        }
    }

    private fun getUserList() {

        NetworkUtils.connectUserList(applicationContext, HandlerGet(this))

    }

    private fun updateUserList() {

        userList.clear()
        DatabaseHelpUtils.getRoomUserListFromRealm(applicationContext, userList, room)

        //drawer layout
        listView_user!!.layoutManager = LinearLayoutManager(applicationContext);
        adapter_user = UserListAdapter(userList, applicationContext)
        listView_user.adapter = adapter_user
        val divider = DividerItemDecoration(
                listView_user.context,
                DividerItemDecoration.VERTICAL
        )
        divider.setDrawable(ContextCompat.getDrawable(baseContext, R.drawable.shape_line_divider))
        listView_user.addItemDecoration(divider)

        tv_numberOfUser.text = if (userList.size > 0) "(" + userList.size.toString() + ")" else ""

    }


    fun sendMessage(txt: String) {

        val chatMessage = ChatMessage(lastChatIdx + 1, ChatUtils.TYPE_MESSAGE, LoginToken.getUserIdx(applicationContext), txt, Utils.getNowForFirebase())

        val chatMessageF = chatMessage.toChatMessageF()
        dataBaseMessages.push().setValue(chatMessageF)
        dataBaseGroup.child(ARG_LAST_MESSAGE).setValue(chatMessageF)
        pick_idx = -1
        sendNotification(txt)
    }

    fun sendLeaveMessage() {
        val chatIdx: Int = lastChatIdx
        val chatMessageF = ChatMessageF(chatIdx + 1, ChatUtils.TYPE_LEAVE, LoginToken.getUserIdx(applicationContext), LoginToken.getUser(applicationContext).name, Utils.getNowForFirebase())
        dataBaseMessages.push().setValue(chatMessageF)
        dataBaseGroup.child(ARG_LAST_MESSAGE).child(ARG_CHAT_IDX).setValue(chatIdx + 1)
    }


    private fun sendNotification(txt: String) {
        userList.iterator().forEach {
            val uIdx = it.u_idx
            if (uIdx != LoginToken.getUserIdx(applicationContext)) {
                dataBaseFireToken.child(uIdx.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(dataSnapShot: DataSnapshot) {

                        val key: String = dataSnapShot.key!!
                        val value: String? = dataSnapShot.value.toString()

                        Log.d("$LOG_TAG/token_key", key)
                        Log.d("$LOG_TAG/token_value", value)

                        if (value != null && value != "null") {
//                            val json = FcmSendMessageTask.makeNotificationMessage(value, group.real_name, LoginToken.getUser(applicationContext).name.toString() + ":" + txt, group.g_idx, room.room_idx)
//                            val task = FcmSendMessageTask()
//                            task.execute(json)
                        }

                    }
                })
            }

        }
    }


    fun addMessage(dataSnapShot: DataSnapshot) {
        Log.d(LOG_TAG, "addMessage/" + dataSnapShot.toString())
        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
        val chatDataF = dataSnapShot.getValue<ChatMessageF>(ChatMessageF::class.java)
        val chatData = chatDataF!!.toChatMessage()



        if (chatData.type == ChatUtils.TYPE_MESSAGE
                || chatData.type == ChatUtils.TYPE_NOTICE
                || chatData.type == ChatUtils.TYPE_VOTE
                || chatData.type == ChatUtils.TYPE_LIGHT
                || chatData.type == ChatUtils.TYPE_ROLE) {
            chatData.setPhotoInfo(realm)

            var readCount = userList.size
            endPoints.iterator().forEach {
                if (chatData.chat_idx <= it.value)
                    readCount -= 1
            }
            chatData.count = readCount

        }

        dataList.add(chatData)
        updateReadCountEach(dataList.lastIndex)

        if (isShowReadLine) {
            if (chatData.chat_idx == recentChatIdx) {
                Log.d("$LOG_TAG/showReadLine", "Yes!" + chatData.chat_idx + "/" + recentChatIdx.toString())
                dataList.add(ChatMessage(chatData.chat_idx, ChatUtils.TYPE_READLINE, null, null, null))
                isShowReadLine = false
            }
        }


        realm.beginTransaction()
        realm.copyToRealmOrUpdate(chatData.toChatMessageR())
        realm.commitTransaction()


        if (chatData.chat_idx == pick_idx)
            scrollToPosition(listView_chat, dataList.size - 1)
        if (chatData.chat_idx == positionNow && pick_idx == -1)
            scrollToPosition(listView_chat, dataList.size - 1)


        if (chatData.type == ChatUtils.TYPE_LEAVE) {
            val idx = chatData.u_idx
            endPoints.remove(idx)
            updateReadCount()
        }
        if (chatData.type == ChatUtils.TYPE_INVITE) {
            endPoints[chatData.u_idx!!] = lastChatIdx
            updateReadCount()
        }

        adapter_chat.notifyDataSetChanged()
    }

    fun updateReadCount() {
        Log.d(LOG_TAG, "update Count")


        Log.d("$LOG_TAG/EndPoints_size", endPoints.size.toString())
        endPoints.forEach {
            Log.d("$LOG_TAG/EndPoints ", it.key.toString() + "/" + it.value.toString())
        }
        for (i in 0 until dataList.size) {
            updateReadCountEach(i)
        }
        adapter_chat.notifyDataSetChanged()

    }

    private fun updateReadCountEach(i: Int) {
        var readCount = endPoints.size - 1

        Log.d("$LOG_TAG/chat_idx ", dataList[i].chat_idx.toString())
        endPoints.forEach {
            if (dataList[i].chat_idx <= it.value && it.key != LoginToken.getUserIdx(applicationContext))
                readCount -= 1
        }
        dataList[i].count = readCount
        adapter_chat.notifyDataSetChanged()
    }


    var isUpdateJoined: IsUpdateR by Delegates.notNull()

    private fun addChangeJoinedListener() {

        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
        isUpdateJoined = realm.where(IsUpdateR::class.java).equalTo("what", StatusCode.joinedRoomChange).findFirst() ?: makeUpdateR(realm, StatusCode.joinedRoomChange)

        isUpdateJoined.addChangeListener<IsUpdateR> { t: IsUpdateR, _ ->
            if (t.isUpdate) {
                updateUserList()
                realm.executeTransaction {
                    t.isUpdate = false
                }
            }
        }

    }

    fun makeUpdateR(realm: Realm, code: Int): IsUpdateR {

        realm.beginTransaction()
        val isUpdate = realm.createObject(IsUpdateR::class.java, code)
        isUpdate.isUpdate = false
        realm.commitTransaction()
        return isUpdate

    }

    override fun onResume() {
        super.onResume()
        getUserList()
        updateUserList()
        Log.d(LOG_TAG, "onResume")

//        addFirebaseListener()
//        addChangeJoinedListener()
    }


    override fun onPause() {
        super.onPause()
        Log.d(LOG_TAG, "onPause")

        if (lastChatIdx > DatabaseHelpUtils.getRecentChatIdx(applicationContext, room.room_idx))
            DatabaseHelpUtils.setRecentChatIdx(applicationContext, room.room_idx, lastChatIdx)
//        removeFirebaseListener()
//        isUpdateJoined.removeAllChangeListeners()


    }


    override fun onDestroy() {
        super.onDestroy()
        leaveRoomSocket()
        disconnectSocket()
    }


    var mEndPointListener = object : ValueEventListener {
        override fun onDataChange(dataSnapShot: DataSnapshot) {
            Log.d(LOG_TAG, "endPoint onDataChanged")
            val ep = dataSnapShot.children

            var max = 0
            ep.iterator().forEach {

                val key: String = it.key!!
                val value: Int = it.getValue(Int::class.java)!!

                if (max < value) max = value
                endPoints[key.toInt()] = value

            }
            if (recentChatIdx < max)
                DatabaseHelpUtils.setRecentChatIdx(applicationContext, room.room_idx, max)

            updateReadCount()
        }

        override fun onCancelled(p0: DatabaseError) {
        }


    }

    private var mMessageAddListener = object : ChildEventListener {
        override fun onChildChanged(dataSnapShot: DataSnapshot, p1: String?) {
            Log.d(LOG_TAG, "message onChildChanged")
        }

        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
            Log.d(LOG_TAG, "message onChildAdded")
            addMessage(dataSnapshot)

        }


        override fun onChildRemoved(p0: DataSnapshot) {
        }
    }

    fun removeFirebaseListener() {
        if (isAddedFirebaseListener) {
            Log.d(LOG_TAG, "removeListener")
            dataBaseEndpoints.removeEventListener(mEndPointListener)
            dataBaseMessages.removeEventListener(mMessageAddListener)
            dataBaseLastMessage!!.child(ARG_CHAT_IDX).removeEventListener(lastChatIdxListener)
            isAddedFirebaseListener = false
        }
    }

    private fun addFirebaseListener() {
        if (!isAddedFirebaseListener) {
            Log.d(LOG_TAG, "addListener")
            dataList.clear()
            dataBaseEndpoints.addValueEventListener(mEndPointListener)
            dataBaseMessages.addChildEventListener(mMessageAddListener)
            dataBaseLastMessage!!.child(ARG_CHAT_IDX).addValueEventListener(lastChatIdxListener)
            isAddedFirebaseListener = true
        }
    }


    fun makeDialog(position: Int) {
        val dialog = ChooseWorkDialog(this)
        dialog.show()
        dialog.setOnClickListener(View.OnClickListener { p0 ->
            when (p0.id) {
                R.id.btn_copy -> {
                    val clipboardManager = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText(getString(R.string.app_name), dataList[position].content)
                    clipboardManager.primaryClip = clipData
                    Toast.makeText(applicationContext, dataList[position].content + "\n가 복사되었습니다.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                R.id.btn_delete -> {

                }
                R.id.btn_share -> {

                }

                R.id.btn_signal -> {
                    makeSignal(position)
                    dialog.dismiss()
                }
                R.id.btn_notice -> {
                    makeNotice(position)
                    dialog.dismiss()
                }
                R.id.btn_pick -> {
                    val realm = getRealmDefault(applicationContext)

                    realm.beginTransaction()
                    val pickR = realm.createObject(PickR::class.java)
                    pickR.content = dataList[position].content
                    pickR.chat_idx = position
                    pickR.write_time = Utils.getNow()
                    pickR.u_idx = dataList[position].u_idx
                    pickR.g_idx = group.g_idx
                    pickR.room_idx = room.room_idx
                    realm.commitTransaction()

                    dialog.dismiss()

                    var txt: String = dataList[position].content!!
                    if (txt.length > 10) txt = txt.substring(0, 10) + "..."
                    Toast.makeText(applicationContext, txt + "내용을 픽! 했습니다", Toast.LENGTH_SHORT).show()

                }
                R.id.btn_search -> {

                }
            }
        })

    }

    private var makeContent: String? = null
    private var makeType: Int by Delegates.notNull()
    private fun makeSignal(position: Int) {

        makeContent = dataList[position].content
        makeType = ChatUtils.TYPE_LIGHT
        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_MAKE_SIGNAL_PARAM_UID, LoginToken.getUserIdx(applicationContext))
            jsonParam.put(URL_MAKE_SIGNAL_PARAM_CHATID, dataList.get(position).chat_idx)
            jsonParam.put(URL_MAKE_SIGNAL_PARAM_ROOM_IDX, room.room_idx)
            jsonParam.put(URL_MAKE_SIGNAL_PARAM_CONTENT, dataList.get(position).content)
            jsonParam.put(URL_MAKE_SIGNAL_PARAM_OPENSTATUS, OPEN_STATUS_OPEN)
            jsonParam.put(URL_MAKE_SIGNAL_PARAM_ENTIRESTATUS, ENTIRE_STATUS_ENTIRE)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val task = GetMessageTask(applicationContext, HandlerMake(this), LoginToken.getToken(applicationContext))
        task.execute(URL_MAKE_SIGNAL, METHOD_POST, jsonParam.toString())
    }

    private fun makeNotice(position: Int) {

        val jsonParam = JSONObject()

        makeContent = dataList[position].content
        makeType = ChatUtils.TYPE_NOTICE
        try {
            jsonParam.put(USGS_REQUEST_URL.URL_MAKE_NOTICE_PARAM_UID, LoginToken.getUserIdx(applicationContext))
            jsonParam.put(USGS_REQUEST_URL.URL_MAKE_NOTICE_PARAM_CHATID, dataList[position].chat_idx)
            jsonParam.put(USGS_REQUEST_URL.URL_MAKE_NOTICE_PARAM_ROOM_IDX, room.room_idx)
            jsonParam.put(USGS_REQUEST_URL.URL_MAKE_NOTICE_PARAM_CONTENT, dataList[position].content)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        val task = GetMessageTask(applicationContext, HandlerMake(this), LoginToken.getToken(applicationContext))
        task.execute(USGS_REQUEST_URL.URL_MAKE_NOTICE,METHOD_POST , jsonParam.toString())
    }

    private class HandlerLeaveFirebase(activity: ChatActivity) : Handler() {
        private val mActivity: WeakReference<ChatActivity> = WeakReference<ChatActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {

                        val LOG_TAG = activity.LOG_TAG
                        val isUpdateJoined = activity.isUpdateJoined
                        val group = activity.group
                        isUpdateJoined.removeAllChangeListeners()

                        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
                        realm.beginTransaction()
                        realm.where(JoinedRoomR::class.java).equalTo(ARG_G_IDX, group.g_idx).equalTo(ARG_U_IDX, LoginToken.getUserIdx(activity.applicationContext)).findAll().deleteAllFromRealm()
                        realm.where(GroupR::class.java).equalTo(ARG_G_IDX, group.g_idx).findAll().deleteAllFromRealm()

                        val isUpdateR: IsUpdateR = realm.where(IsUpdateR::class.java).equalTo(ARG_WHAT, StatusCode.joinedRoomChange).findFirst()
                                ?: activity.makeUpdateR(realm, StatusCode.joinedRoomChange)
                        isUpdateR.isUpdate = true

                        realm.commitTransaction()

                        dataBaseEndpoints.child(LoginToken.getUserIdx(activity.applicationContext).toString()).removeValue()
                        activity.removeFirebaseListener()



                        Log.d("$LOG_TAG/firebaseDB", "remove endpoint of " + LoginToken.getUser(activity.applicationContext).name)

                        val endPoints = activity.endPoints
                        Log.d("$LOG_TAG/endPoint", endPoints.size.toString() + "," + endPoints.toString())
                        if (endPoints.size == 1 && endPoints.containsKey(LoginToken.getUserIdx(activity.applicationContext))) {
                            Log.d("$LOG_TAG/firebaseDB", "remove group(" + group.real_name + ")")
                            dataBaseGroup.removeValue()
                        } else
                            activity.sendLeaveMessage()
                        Toast.makeText(activity.applicationContext, "그룹을 나갔습니다.", Toast.LENGTH_SHORT).show()

                        val intent = Intent(activity.applicationContext, MainActivity::class.java)
                        intent.putExtra("leave_from_chat", true)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                    else -> {
                        Toast.makeText(activity.applicationContext, "잠시 후 다시 시도해주십시오. ", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    private class HandlerLongClick(activity: ChatActivity) : Handler() {
        private val mActivity: WeakReference<ChatActivity> = WeakReference<ChatActivity>(activity)

        override fun handleMessage(msg: Message) {
            val position = msg.what
            mActivity.get()?.makeDialog(position)
        }
    }


    private class HandlerMake(activity: ChatActivity) : Handler() {
        private val mActivity: WeakReference<ChatActivity> = WeakReference<ChatActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        val obj = msg.obj as String
                        val idx = obj.toInt()
                        FirebaseMessageUtils.sendMessage(activity.makeType, idx, activity.makeContent!!, activity.group, activity.room, LoginToken.getUserIdx(activity.applicationContext), activity)
                    }
                    else -> {
                        val result = msg.data.getString("message")
                        Toast.makeText(activity.applicationContext, result, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private class HandlerGetLastMsg(activity: ChatActivity) : Handler() {
        private val mActivity: WeakReference<ChatActivity> = WeakReference<ChatActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                val lastChatIdx = msg.what

                activity.isShowReadLine = lastChatIdx > activity.recentChatIdx
                Log.d("$activity.LOG_TAG/isShowReadLine", activity.isShowReadLine.toString())

            }
        }
    }

    private class HandlerGet(activity: ChatActivity) : Handler() {
        private val mActivity: WeakReference<ChatActivity> = WeakReference<ChatActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                NetworkUtils.connectJoinedRoomList(activity.applicationContext, null)
            }
        }
    }


    /* 소켓 관련 함수 */

    private fun connectSocket() {
        val socket = ChatApplication.getSocket(group.g_idx)
        if (socket == null) finish()

        mSocket = socket!!
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        mSocket.on(Constants.UPDATE_CHAT, onUpdateChat)
        mSocket.on(Constants.ENTER_RESULT, onEnterResult)
        mSocket.on(Constants.LEAVE_RESULT, onLeaveResult)
        mSocket.connect()

    }

    private fun disconnectSocket() {
        mSocket.disconnect()
        mSocket.off(Socket.EVENT_CONNECT, onConnect)
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        mSocket.off(Constants.UPDATE_CHAT, onUpdateChat)
        mSocket.off(Constants.ENTER_RESULT, onEnterResult)
        mSocket.off(Constants.LEAVE_RESULT, onLeaveResult)
    }


    private fun enterRoomSocket() {
        val jsonObj = JSONObject()
        jsonObj.put(Constants.JSON_U_IDX, LoginToken.getUserIdx(applicationContext))
        jsonObj.put(Constants.JSON_ROOM_IDX, room.room_idx)
        mSocket.emit(Constants.ENTER_ROOM, jsonObj.toString())

        Log.d("$LOG_TAG/Socket", "try enterRoom with $jsonObj")

    }

    private fun leaveRoomSocket() {
        val jsonObj = JSONObject()
        jsonObj.put(Constants.JSON_U_IDX, LoginToken.getUserIdx(applicationContext))
        jsonObj.put(Constants.JSON_ROOM_IDX, room.room_idx)
        mSocket.emit(Constants.LEAVE_ROOM, jsonObj.toString())
        Log.d("$LOG_TAG/Socket", "try leaveRoom with $jsonObj")
    }


    private fun sendMessageSocket(txt: String) {
        val jsonObj = JSONObject()
        jsonObj.put(Constants.JSON_U_IDX, LoginToken.getUserIdx(applicationContext))
        jsonObj.put(Constants.JSON_ROOM_IDX, room.room_idx)
        jsonObj.put(Constants.JSON_CONTENT, txt)
        jsonObj.put(Constants.JSON_TYPE, ChatUtils.TYPE_MESSAGE)
        mSocket.emit(Constants.SEND_CHAT, jsonObj.toString())
        Log.d("$LOG_TAG/Socket", "try sendMessage with $jsonObj")
    }


    private val onConnect = Emitter.Listener {

        this.runOnUiThread(Runnable {
            if (!isConnected) {

                enterRoomSocket()
                Log.i(LOG_TAG + "/Socket onConnect/", "connected")
                isConnected = true

            }
        })
    }

    private val onDisconnect = Emitter.Listener {
        this.runOnUiThread(Runnable {
            Log.i(LOG_TAG + "/Socket onDisconnect", "disconnected")
            isConnected = false
            Toast.makeText(applicationContext,
                    R.string.disconnect, Toast.LENGTH_LONG).show()
        })
    }

    private val onConnectError = Emitter.Listener {
        this.runOnUiThread(Runnable {
            Log.e(LOG_TAG + "/Socket ConnectError", "Error connecting")
            Toast.makeText(applicationContext,
                    R.string.error_connect, Toast.LENGTH_LONG).show()
        })
    }

    private val onEnterResult = Emitter.Listener { args ->
        this.runOnUiThread(Runnable {

//            Toast.makeText(applicationContext, args[0].toString(), Toast.LENGTH_SHORT).show()

            Log.d("$LOG_TAG/Socket onEnterResult", args[0].toString())

            val dataArray: JSONArray = JSONArray(args[0].toString())


            for (i in 0 until dataArray.length()) {
                val data = dataArray.getJSONObject(i)

                addChatFromJSON(data)
            }

            adapter_chat.notifyDataSetChanged()
            if(pick_idx ==-1)
                scrollToPosition(listView_chat, dataList.size - 1)
            isConnectedRoom = true
        })
    }

    private val onLeaveResult = Emitter.Listener { args ->
        this.runOnUiThread(Runnable {

            Toast.makeText(applicationContext, args[0].toString(), Toast.LENGTH_SHORT).show()

            Log.d("$LOG_TAG/Socket onLeaveResult", args[0].toString())
            if (args[0].toString() == "true")
                finish()
            else
                Toast.makeText(applicationContext, "잠시 후 다시 도전해주세요", Toast.LENGTH_SHORT).show()
        })
    }
    private val onUpdateChat = Emitter.Listener { args ->
        this.runOnUiThread(Runnable {

            Log.d("$LOG_TAG/Socket onUpdateChat", args[0].toString())
           addChatFromJSON(org.json.JSONObject(args[0].toString()))

            if(pick_idx ==-1)
                scrollToPosition(listView_chat, dataList.size - 1)
            adapter_chat.notifyDataSetChanged()

        })
    }

    private fun addChatFromJSON(data : JSONObject){

        val chatIdx = data.getInt(Constants.JSON_CHAT_IDX)
        val message: String = data.getString(JSON_CONTENT)
        val uIdx = data.getInt(JSON_U_IDX)
        val type = data.getInt(Constants.JSON_TYPE)
        val writeTime = data.getString(Constants.JSON_WRITE_TIME)
        val count : Int =data.getInt(Constants.JSON_COUNT)

        val chatData = ChatMessage(chatIdx, type, uIdx, message, writeTime)
        chatData.count=count
        chatData.setPhotoInfo(applicationContext)
        dataList.add(chatData)
    }


}