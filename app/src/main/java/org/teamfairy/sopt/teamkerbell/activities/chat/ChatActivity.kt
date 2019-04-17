package org.teamfairy.sopt.teamkerbell.activities.chat

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.Toast
import io.realm.Realm
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.app_bar_chat.*
import kotlinx.android.synthetic.main.content_chat.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONArray
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.chat.adapter.ChatViewAdapter
import org.teamfairy.sopt.teamkerbell.activities.chat.dialog.ChooseWorkDialog
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.ChatApplication
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.Constants
import org.teamfairy.sopt.teamkerbell.activities.items.notice.MakeNoticeActivity
import org.teamfairy.sopt.teamkerbell.activities.items.notice.NoticeCardActivity
import org.teamfairy.sopt.teamkerbell.activities.items.pick.PickListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.role.MakeRoleActivity
import org.teamfairy.sopt.teamkerbell.activities.items.role.RoleListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.MakeSignalActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.MakeVoteActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.VoteListActivity
import org.teamfairy.sopt.teamkerbell.dialog.ConfirmDeleteDialog
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.data.User.Companion.ARG_U_IDX
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessage
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR.Companion.ARG_WHAT
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedRoomR
import org.teamfairy.sopt.teamkerbell.model.realm.PickR
import org.teamfairy.sopt.teamkerbell.model.realm.RoomR
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_DELETE
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LEAVE_ROOM_PARAM_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_CHATID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_ENTIRESTATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_OPENSTATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_UID
import org.teamfairy.sopt.teamkerbell.utils.*
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.utils.FileUtils.Companion.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_FILE
import org.teamfairy.sopt.teamkerbell.utils.FileUtils.Companion.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_IMAGE
import org.teamfairy.sopt.teamkerbell.utils.FileUtils.Companion.SELECT_FILE
import org.teamfairy.sopt.teamkerbell.utils.FileUtils.Companion.SELECT_IMAGE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_FROM_CHAT
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_PICK_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.ENTIRE_STATUS_ENTIRE
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.OPEN_STATUS_OPEN
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class ChatActivity() : AppCompatActivity(), View.OnClickListener, Parcelable {

    private val LOG_TAG = ChatActivity::class.java.simpleName

    private var isOpenDrawLayout: Boolean = false
    private var isExpanded: Boolean = false

    var group: Team by Delegates.notNull()
    var room: Room by Delegates.notNull()

    var dataList: ArrayList<ChatMessage> = arrayListOf<ChatMessage>()

    private var adapter_chat: ChatViewAdapter by Delegates.notNull()
    private var adapter_user: UserListAdapter by Delegates.notNull()

    private var userList = ArrayList<User>()

    private var lastChatIdx = -1
    private var lastChatPos = -1

    private var forFinish = false

    private var mSocket: Socket by Delegates.notNull()

    private var isConnectedRoom = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        getUserList()
        group = intent.getParcelableExtra(INTENT_GROUP)
        room = intent.getParcelableExtra(INTENT_ROOM)
        lastChatIdx = DatabaseHelpUtils.getRecentChatIdx(applicationContext, room.room_idx)
        supportActionBar!!.title = room.real_name
        val layoutManager = LinearLayoutManager(this)
        listView_chat.layoutManager = layoutManager
        adapter_chat = ChatViewAdapter(dataList, applicationContext, group, room)
        adapter_chat.setOnLongClickHandler(HandlerLongClick(this))
        listView_chat.adapter = adapter_chat
        adapter_chat.setPick(intent.getIntExtra(INTENT_PICK_IDX, -1))
        listView_chat.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (adapter_chat.isFixedScroll)
                        adapter_chat.setFixed(false)
                    if (adapter_chat.pickIdx != -1) {
                        adapter_chat.setPick(-1)
                        adapter_chat.notifyDataSetChanged()
                    }
                    clearReadLine()
                }
            }
        })

        if (adapter_chat.pickIdx != -1)
            adapter_chat.setFixed(true)

        tv_nav_room_name.text = room.real_name

        if (group.default_room_idx == room.room_idx) {
            btn_nav_leave.isEnabled = false
            btn_nav_leave.visibility = View.INVISIBLE
            tv_nav_room_name.text = ("${tv_nav_room_name.text}(기본 채팅방)")
        } else {
            btn_nav_leave.setOnClickListener(this)
        }
        edt_sendmessage.setOnFocusChangeListener { _, b ->
            if (b) {
                if (isExpanded) {
                    isExpanded = false
                    layout_expanded_menu.visibility = View.GONE
                    btn_expand.setImageDrawable(VectorDrawableCompat.create(resources, R.drawable.icon_chat_expand, null))
                }

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                imm.showSoftInput(edt_sendmessage, InputMethodManager.SHOW_IMPLICIT)

                scrollToPosition(listView_chat, dataList.lastIndex)
            }
        }
        btn_expand.setOnClickListener(this)

        btn_back.setOnClickListener(this)
        btn_invite.setOnClickListener(this)
        btn_draw_menu.setOnClickListener(this)
        btn_board.setOnClickListener(this)

        btn_gallery.setOnClickListener(this)
        btn_video.setOnClickListener(this)
        btn_file.setOnClickListener(this)
        btn_notice.setOnClickListener(this)
        btn_signal.setOnClickListener(this)
        btn_vote.setOnClickListener(this)
        btn_role.setOnClickListener(this)

        btn_sendMessage.setOnClickListener(this)

        btn_nav_notice.setOnClickListener(this)
        btn_nav_pick.setOnClickListener(this)
        btn_nav_role.setOnClickListener(this)
        btn_nav_signal.setOnClickListener(this)
        btn_nav_vote.setOnClickListener(this)

        btn_nav_cloud_drive.setOnClickListener(this)
        attachSocket()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_nav_cloud_drive -> {
                if (tv_nav_cloud_link.visibility == View.VISIBLE) {
                    tv_nav_cloud_link.visibility = View.GONE
                    edt_nav_cloud_link.visibility = View.VISIBLE

                    btn_nav_cloud_drive.setColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColor))
                } else {
                    if (!edt_nav_cloud_link.text.isNullOrBlank()) {
                        if (URLUtil.isValidUrl(edt_nav_cloud_link.text.toString()))
                            tv_nav_cloud_link.text = edt_nav_cloud_link.text.toString()
                        else
                            Toast.makeText(applicationContext, "유효하지 않는 URL 입니다.", Toast.LENGTH_SHORT).show()
                    }
                    tv_nav_cloud_link.visibility = View.VISIBLE
                    edt_nav_cloud_link.visibility = View.GONE

                    btn_nav_cloud_drive.setColorFilter(ContextCompat.getColor(applicationContext, R.color.black))
                }
            }
            R.id.btn_nav_leave -> {
                showDeleteDialog()
            }
            R.id.btn_nav_notice -> {
                val intent = Intent(this, NoticeCardActivity::class.java)
                intent.putExtra(INTENT_GROUP, group)
                intent.putExtra(INTENT_ROOM, room)
                startActivity(intent)
                drawer_layout.closeDrawer(Gravity.END)
            }
            R.id.btn_nav_pick -> {
                val intent = Intent(this, PickListActivity::class.java)
                intent.putExtra(INTENT_GROUP, group)
                intent.putExtra(INTENT_ROOM, room)

                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
                drawer_layout.closeDrawer(Gravity.END)
            }
            R.id.btn_nav_role -> {
                val intent = Intent(this, RoleListActivity::class.java)
                intent.putExtra(INTENT_GROUP, group)
                intent.putExtra(INTENT_ROOM, room)
                startActivity(intent)
                drawer_layout.closeDrawer(Gravity.END)
            }
            R.id.btn_nav_signal -> {
                val intent = Intent(this, SignalListActivity::class.java)
                intent.putExtra(INTENT_GROUP, group)
                intent.putExtra(INTENT_ROOM, room)
                startActivity(intent)
                drawer_layout.closeDrawer(Gravity.END)
            }
            R.id.btn_nav_vote -> {
                val intent = Intent(this, VoteListActivity::class.java)
                intent.putExtra(INTENT_GROUP, group)
                intent.putExtra(INTENT_ROOM, room)
                startActivity(intent)
                drawer_layout.closeDrawer(Gravity.END)
            }

            R.id.btn_role -> {
                val intent = Intent(this, MakeRoleActivity::class.java)
                intent.putExtra(INTENT_GROUP, group)
                intent.putExtra(INTENT_ROOM, room)
                intent.putExtra(INTENT_FROM_CHAT, true)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
            }
            R.id.btn_vote -> {
                val intent = Intent(this, MakeVoteActivity::class.java)
                intent.putExtra(INTENT_GROUP, group)
                intent.putExtra(INTENT_ROOM, room)
                intent.putExtra(INTENT_FROM_CHAT, true)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
            }
            R.id.btn_signal -> {
                val intent = Intent(this, MakeSignalActivity::class.java)
                intent.putExtra(INTENT_GROUP, group)
                intent.putExtra(INTENT_ROOM, room)
                intent.putExtra(INTENT_FROM_CHAT, true)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
            }
            R.id.btn_notice -> {
                val intent = Intent(this, MakeNoticeActivity::class.java)
                intent.putExtra(INTENT_GROUP, group)
                intent.putExtra(INTENT_ROOM, room)
                intent.putExtra(INTENT_FROM_CHAT, true)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
            }
            R.id.btn_file -> {
                if (checkPermissionREAD_EXTERNAL_STORAGE(this, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_FILE)) {
                    intentFile()
                }
            }

            R.id.btn_gallery -> {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                        } else {
                            requestExplorer()
                        }
                    } else {
                        requestExplorer()
                    }
                } else {
                    requestExplorer()
                }
            }

            R.id.btn_back -> {
                onBackPressed()
            }

            R.id.btn_sendMessage -> {

                if (!isConnectedRoom) {
                    Toast.makeText(applicationContext, "현재 채팅방에 연결할수 없습니다", Toast.LENGTH_SHORT).show()
                    return
                }
                val edt: EditText = edt_sendmessage
                if (edt.text.isNotEmpty()) {
                    val txt = edt.text.toString()
                    edt.setText("")
                    sendMessageSocket(txt)
                } else {
                    edt.requestFocus()
                }
            }

            R.id.btn_draw_menu -> {
                isOpenDrawLayout = !isOpenDrawLayout
                if (isOpenDrawLayout) {
//                window.attributes.softInputMode= WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                    hideKeyboard()
                    drawer_layout.openDrawer(Gravity.END)
                } else {
//                window.attributes.softInputMode= WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    drawer_layout.closeDrawer(Gravity.END)
                }
            }

            R.id.btn_board -> {
                //            val intent = Intent(this, BoardActivity::class.java)
//            intent.putExtra("group", group)
//            startActivity(intent)
                drawer_layout.closeDrawer(Gravity.END)
            }

            R.id.btn_invite -> {
                val intent = Intent(applicationContext, InviteUserActivity::class.java)
                intent.putExtra(INTENT_GROUP, group)
                intent.putExtra(INTENT_ROOM, room)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
                drawer_layout.closeDrawer(Gravity.END)
            }

            R.id.btn_expand -> {
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
        }
    }

    private fun requestExplorer() {
        if (checkPermissionREAD_EXTERNAL_STORAGE(this, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_IMAGE)) {
            intentImage()
        }
    }

    var dialog: ConfirmDeleteDialog? = null
    private fun showDeleteDialog() {
        if (group.default_room_idx == room.room_idx) {
            Toast.makeText(applicationContext, getString(R.string.txt_default_cant_leave), Toast.LENGTH_SHORT).show()
            return
        }
        dialog = ConfirmDeleteDialog(this, getString(R.string.txt_confirm_leave))
        dialog!!.show()

        dialog!!.setOnClickListenerYes(View.OnClickListener {
            forFinish = true
            attemptExitSocket()
//            attemptLeave()
        })
    }

    private fun hideKeyboard() {
        edt_sendmessage.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edt_sendmessage.windowToken, 0)
    }

    private fun scrollToPosition(recyclerView: RecyclerView, position: Int) {
        if (position < 0) return
//        Log.d(LOG_TAG ,"is Fixed? ${adapter_chat.isFixedScroll}")
//        Log.d(LOG_TAG ,"is pick_id? ${adapter_chat.pick_idx}")
        if (adapter_chat.pickIdx == dataList[position].chat_idx || (!adapter_chat.isFixedScroll && adapter_chat.pickIdx == -1)) {
            recyclerView.layoutManager = LinearLayoutManagerWithSmoothScroller(applicationContext)
            (recyclerView.layoutManager as LinearLayoutManagerWithSmoothScroller).offsetChildrenVertical(0)
            recyclerView.scrollToPosition(position)
        }
    }

    private fun attemptExitSocket() {
        val jsonObj = JSONObject()
        jsonObj.put(Constants.JSON_U_IDX, LoginToken.getUserIdx(applicationContext))
        jsonObj.put(Constants.JSON_ROOM_IDX, room.room_idx)
        mSocket.emit(Constants.EXIT_CHAT_ROOM, jsonObj.toString())
        Log.d("$LOG_TAG/Socket ${Constants.EXIT_CHAT_ROOM}", jsonObj.toString())
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

    private fun deleteRoomFromRealm() {
        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
        realm.beginTransaction()
        realm.where(JoinedRoomR::class.java).equalTo(ARG_ROOM_IDX, room.room_idx).equalTo(ARG_U_IDX, LoginToken.getUserIdx(applicationContext)).findAll().deleteAllFromRealm()
        realm.where(RoomR::class.java).equalTo(ARG_ROOM_IDX, room.room_idx).findAll().deleteAllFromRealm()

        val isUpdateR:
                IsUpdateR = realm.where(IsUpdateR::class.java).equalTo(ARG_WHAT, StatusCode.joinedRoomChange).findFirst()
                ?: makeUpdateR(realm, StatusCode.joinedRoomChange)
        isUpdateR.isUpdate = true

        realm.commitTransaction()
    }

    private fun leavedRoom(msg: Message) {
        when (msg.what) {
            MSG_SUCCESS -> {
                deleteRoomFromRealm()
                finish()
            }
            else ->
                Toast.makeText(applicationContext, getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
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

        // drawer layout
        listView_user!!.layoutManager = LinearLayoutManager(applicationContext)
        adapter_user = UserListAdapter(userList, applicationContext)
        listView_user.adapter = adapter_user
//        val divider = DividerItemDecoration(
//                listView_user.context,
//                DividerItemDecoration.VERTICAL
//        )
//        divider.setDrawable(ContextCompat.getDrawable(baseContext, R.drawable.shape_line_divider))
//        listView_user.addItemDecoration(divider)
        tv_numberOfUser.text = if (userList.size > 0) "(" + userList.size.toString() + ")" else ""
    }

    var isUpdateJoined: IsUpdateR by Delegates.notNull()

    private fun addChangeJoinedListener() {

        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
        isUpdateJoined = realm.where(IsUpdateR::class.java).equalTo(IsUpdateR.ARG_WHAT, StatusCode.joinedRoomChange).findFirst()
                ?: makeUpdateR(realm, StatusCode.joinedRoomChange)

        isUpdateJoined.addChangeListener<IsUpdateR> { t: IsUpdateR, _ ->
            if (t.isUpdate) {
                updateUserList()
                realm.executeTransaction {
                    t.isUpdate = false
                }
            }
        }
    }

    private fun makeUpdateR(realm: Realm, code: Int): IsUpdateR {
        realm.beginTransaction()
        val isUpdate = realm.createObject(IsUpdateR::class.java, code)
        isUpdate.isUpdate = false
        realm.commitTransaction()
        return isUpdate
    }

    var firstTimeResume = true
    override fun onResume() {
        super.onResume()
        getUserList()
        updateUserList()
//        Log.d(LOG_TAG, "onResume")
        if (mSocket.connected()) {
            if (!firstTimeResume)
                enterRoomSocket()
            firstTimeResume = false
        } else
            connectSocket()
        addChangeJoinedListener()
    }

    override fun onPause() {
        super.onPause()
//        Log.d(LOG_TAG, "onPause")

        if (dataList.last().chat_idx > DatabaseHelpUtils.getRecentChatIdx(applicationContext, room.room_idx))
            DatabaseHelpUtils.setRecentChatIdx(applicationContext, room.room_idx, dataList.last().chat_idx)

        isUpdateJoined.removeAllChangeListeners()
        leaveRoomSocket()
    }

    override fun onBackPressed() {

        if (!mSocket.connected()) {
            detachSocket()
            finish()
        } else if (isConnectedRoom) {
            forFinish = true
            finish()
        } else {
            if (mSocket.connected())
                detachSocket()
            super.onBackPressed()
        }
    }

    fun clearReadLine() {
        lastChatIdx = -1
        if (lastChatPos == -1) return

        dataList.removeAt(lastChatPos)
        scrollToPosition(listView_chat, lastChatPos - 1)

        listView_chat.adapter = adapter_chat
        adapter_chat.notifyDataSetChanged()
        lastChatPos = -1
//        Log.d("$LOG_TAG/lastChatPost", lastChatPos.toString())
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
                    dialog.dismiss()
                    Toast.makeText(applicationContext, dataList[position].content + "가 복사되었습니다.", Toast.LENGTH_SHORT).show()
                }
//                R.id.btn_delete -> {
//
//                }
                R.id.btn_share -> {

                    val intent = Intent(android.content.Intent.ACTION_SEND)
                    intent.type = "text/plain"

                    intent.putExtra(Intent.EXTRA_TEXT, dataList[position].content)

                    val chooser = Intent.createChooser(intent, "공유하기")
                    startActivity(chooser)
                    dialog.dismiss()
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
                    pickR.chat_idx = dataList[position].chat_idx
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

//                R.id.btn_search -> {
//
//                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val url = getPath(applicationContext, data!!.data)
                    val file = File(url)

                    attemptUploadStorage(requestCode, file)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val file = FileUtils.updatePhoto(FileUtils.getRealPathFromURI(data!!.data, contentResolver), null)
                    attemptUploadStorage(requestCode, file)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkPermissionREAD_EXTERNAL_STORAGE(context: Context, request: Int): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                                context as Activity,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            request)
                } else {
                    ActivityCompat
                            .requestPermissions(
                                    context,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    request)
                }
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

    fun showDialog(
            msg: String,
            context: Context,
            permission: String,
            request: Int
    ) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage("$msg permission is necessary")
        alertBuilder.setPositiveButton(android.R.string.yes) { _, _ ->
            ActivityCompat.requestPermissions(context as Activity,
                    arrayOf(permission),
                    request)
        }
        val alert = alertBuilder.create()
        alert.show()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_IMAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intentImage()
            } else {
                Toast.makeText(applicationContext, "GET_ACCOUNTS Denied",
                        Toast.LENGTH_SHORT).show()
            }
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_FILE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intentFile()
            } else {
                Toast.makeText(applicationContext, "GET_ACCOUNTS Denied",
                        Toast.LENGTH_SHORT).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions,
                    grantResults)
        }
    }

    private fun intentFile() {

        val intent = Intent(Intent.ACTION_GET_CONTENT)

//       구글 드라이브 등에서 가져오는기 막음
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        intent.type = "*/*"
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivityForResult(intent, SELECT_FILE)
    }

    private fun intentImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, SELECT_IMAGE)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            } // MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        } // File
        // MediaStore (and general)

        return null
    }

    fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null)
                cursor.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Docs
     */
    fun isGoogleDocssUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority
    }

    private fun attemptUploadStorage(type: Int, file: File) {
        val task = GetMessageTask(applicationContext, HandlerUpload(this), LoginToken.getToken(applicationContext))

        val jsonParam = JSONObject()
        try {
            jsonParam.put(USGS_REQUEST_URL.URL_PHOTO_SINGLE_PARAM_ROOM_IDX, room.room_idx)
            jsonParam.put(USGS_REQUEST_URL.URL_PHOTO_SINGLE_PARAM_U_IDX, LoginToken.getUserIdx(applicationContext))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var url = ""
        when (type) {
            SELECT_FILE -> {
                task.file = file
                url = USGS_REQUEST_URL.URL_FILE
            }
            SELECT_IMAGE -> {
                task.photo = file
                url = USGS_REQUEST_URL.URL_PHOTO_SINGLE
            }
        }

        task.execute(url, NetworkTask.METHOD_POST, jsonParam.toString())
    }

    private class HandlerUpload(activity: ChatActivity) : Handler() {
        private val mActivity: WeakReference<ChatActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        Toast.makeText(activity.applicationContext, "업로드 되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
//                        activity.isFailed()
                    }
                }
            }
        }
    }

    private var makeContent: String? = null
    private var makeType: Int by Delegates.notNull()
    private fun makeSignal(position: Int) {

        makeContent = dataList[position].content
        makeType = ChatUtils.TYPE_SIGNAL
        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_MAKE_SIGNAL_PARAM_UID, LoginToken.getUserIdx(applicationContext))
            jsonParam.put(URL_MAKE_SIGNAL_PARAM_CHATID, dataList[position].chat_idx)
            jsonParam.put(URL_MAKE_SIGNAL_PARAM_ROOM_IDX, room.room_idx)
            jsonParam.put(URL_MAKE_SIGNAL_PARAM_CONTENT, dataList[position].content)
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
        task.execute(USGS_REQUEST_URL.URL_MAKE_NOTICE, METHOD_POST, jsonParam.toString())
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
//                        val obj = msg.obj as String
                        activity.enterRoomSocket()
                    }
                    else -> {
                        val result = msg.data.getString(JSON_MESSAGE)
                        Toast.makeText(activity.applicationContext, result, Toast.LENGTH_SHORT).show()
                    }
                }
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
    private fun attachSocket() {

        val socket = ChatApplication.getSocket(group.g_idx)
        if (socket == null) finish()

        Log.d(LOG_TAG, "attach socket listener")
        mSocket = socket!!
        mSocket.on(Constants.UPDATE_CHAT, onUpdateChat)
        mSocket.on(Constants.ENTER_ROOM_RESULT, onEnterResult)
        mSocket.on(Constants.LEAVE_ROOM_RESULT, onLeaveResult)
        mSocket.on(Constants.EXIT_ROOM_RESULT, onExitResult)

        enterRoomSocket()
    }

    private fun detachSocket() {

        Log.d(LOG_TAG, "detach socket listener")
        mSocket.off(Constants.UPDATE_CHAT, onUpdateChat)
        mSocket.off(Constants.ENTER_ROOM_RESULT, onEnterResult)
        mSocket.off(Constants.LEAVE_ROOM_RESULT, onLeaveResult)
        mSocket.off(Constants.EXIT_ROOM_RESULT, onExitResult)
    }

    private fun connectSocket() {
        val socket = ChatApplication.getSocket(group.g_idx)
        if (socket == null) finish()

        mSocket = socket!!
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        mSocket.on(Constants.UPDATE_CHAT, onUpdateChat)
        mSocket.on(Constants.ENTER_ROOM_RESULT, onEnterResult)
        mSocket.on(Constants.LEAVE_ROOM_RESULT, onLeaveResult)
        mSocket.on(Constants.EXIT_ROOM_RESULT, onExitResult)
        mSocket.connect()
    }

    private fun disconnectSocket() {
        mSocket.disconnect()
        mSocket.off(Socket.EVENT_CONNECT, onConnect)
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        mSocket.off(Constants.UPDATE_CHAT, onUpdateChat)
        mSocket.off(Constants.ENTER_ROOM_RESULT, onEnterResult)
        mSocket.off(Constants.LEAVE_ROOM_RESULT, onLeaveResult)
        mSocket.off(Constants.EXIT_ROOM_RESULT, onExitResult)
        mSocket.close()
    }

    private fun enterRoomSocket() {
        if (!mSocket.connected()) {
            connectSocket()
            return
        }

        val jsonObj = JSONObject()
        jsonObj.put(Constants.JSON_U_IDX, LoginToken.getUserIdx(applicationContext))
        jsonObj.put(Constants.JSON_ROOM_IDX, room.room_idx)
        mSocket.emit(Constants.ENTER_ROOM, jsonObj.toString())
        Log.d("$LOG_TAG/Socket ${Constants.ENTER_ROOM}", jsonObj.toString())
    }

    private fun leaveRoomSocket(b: Boolean) {
        forFinish = b
        leaveRoomSocket()
    }

    private fun leaveRoomSocket() {
        val jsonObj = JSONObject()
        jsonObj.put(Constants.JSON_U_IDX, LoginToken.getUserIdx(applicationContext))
        jsonObj.put(Constants.JSON_ROOM_IDX, room.room_idx)
        mSocket.emit(Constants.LEAVE_ROOM, jsonObj.toString())
        Log.d("$LOG_TAG/Socket ${Constants.LEAVE_ROOM}", jsonObj.toString())
    }

    private fun sendMessageSocket(txt: String) {
        clearReadLine()
        val jsonObj = JSONObject()
        jsonObj.put(Constants.JSON_U_IDX, LoginToken.getUserIdx(applicationContext))
        jsonObj.put(Constants.JSON_ROOM_IDX, room.room_idx)
        jsonObj.put(Constants.JSON_CONTENT, txt)
        jsonObj.put(Constants.JSON_TYPE, ChatUtils.TYPE_MESSAGE)
        mSocket.emit(Constants.SEND_CHAT, jsonObj.toString())
        Log.d("$LOG_TAG/Socket ${Constants.SEND_CHAT}", jsonObj.toString())
    }

    private val onConnect = Emitter.Listener {

        Log.i("$LOG_TAG/Socket onConnect/", "connected")
        this.runOnUiThread(Runnable {
            enterRoomSocket()
        })
    }

    private val onDisconnect = Emitter.Listener {
        Log.i("$LOG_TAG/Socket onDisconnect", "disconnected")
        this.runOnUiThread(Runnable {
            detachSocket()
//            Toast.makeText(applicationContext,
//                    R.string.disconnect, Toast.LENGTH_LONG).show()
        })
    }

    private val onConnectError = Emitter.Listener {
        Log.e("$LOG_TAG/Socket ConnectError", "Error connecting")
        this.runOnUiThread(Runnable {
            Toast.makeText(applicationContext, R.string.error_connect, Toast.LENGTH_LONG).show()
        })
    }

    private val onEnterResult = Emitter.Listener { args ->

        if (args[0] == null || args[0] == 0) return@Listener

        Log.d("$LOG_TAG/Socket ${Constants.ENTER_ROOM_RESULT}", args[0].toString())
        this.runOnUiThread(Runnable {
            val dataArray: JSONArray = JSONArray(args[0].toString())

//            val firstTime = dataList.size==0 //이거 왜했지
            for (i in 0 until dataArray.length()) {
                val data = dataArray.getJSONObject(i)

                addChatFromJSON(data)
            }

            adapter_chat.notifyDataSetChanged()

//            if(firstTime)
            scrollToPosition(listView_chat, dataList.lastIndex)
            isConnectedRoom = true
        })
    }

    private val onLeaveResult = Emitter.Listener { args ->
        if (args[0] == null) return@Listener
        this.runOnUiThread(Runnable {

            Log.d("$LOG_TAG/Socket ${Constants.LEAVE_ROOM_RESULT}", args[0].toString())
            if (args[0].toString() == "true") {
                if (forFinish) {
                    detachSocket()
                    finish()
                }
            } else
                Toast.makeText(applicationContext, getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
        })
    }

    private val onExitResult = Emitter.Listener { args ->
        if (args[0] == null) return@Listener
        this.runOnUiThread(Runnable {

            Log.d("$LOG_TAG/Socket ${Constants.EXIT_ROOM_RESULT}", args[0].toString())
            if (args[0].toString().length > 1) {
                if (forFinish) {
                    forFinish = false
                    deleteRoomFromRealm()
                    detachSocket()
                    finish()
                } else {
                    addChatFromJSON(JSONObject(args[0].toString()))
                    adapter_chat.notifyDataSetChanged()
                    scrollToPosition(listView_chat, dataList.lastIndex)
                }
            } else {
                Toast.makeText(applicationContext, getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
            }
        })
    }

    private val onUpdateChat = Emitter.Listener { args ->
        if (args[0] == null) return@Listener
        Log.d("$LOG_TAG/Socket ${Constants.UPDATE_CHAT}", args[0].toString())
        this.runOnUiThread(Runnable {

            addChatFromJSON(org.json.JSONObject(args[0].toString()))

            adapter_chat.notifyDataSetChanged()
            scrollToPosition(listView_chat, dataList.lastIndex)
        })
    }

    constructor(parcel: Parcel) : this() {
        isOpenDrawLayout = parcel.readByte() != 0.toByte()
        isExpanded = parcel.readByte() != 0.toByte()
        lastChatIdx = parcel.readInt()
        lastChatPos = parcel.readInt()
        forFinish = parcel.readByte() != 0.toByte()
        isConnectedRoom = parcel.readByte() != 0.toByte()
        firstTimeResume = parcel.readByte() != 0.toByte()
        makeContent = parcel.readString()
    }

    private fun addChatFromJSON(data: JSONObject) {

        val chatIdx = data.getInt(Constants.JSON_CHAT_IDX)
        val message: String = data.getString(JSON_CONTENT)
        val uIdx = data.getInt(JSON_U_IDX)
        val type = data.getInt(Constants.JSON_TYPE)
        val writeTime = data.getString(Constants.JSON_WRITE_TIME)
        val count: Int = data.getInt(Constants.JSON_COUNT)

        val chatList = dataList.filter { it.chat_idx == chatIdx }

        val chatData: ChatMessage = if (chatList.isEmpty())
            ChatMessage(chatIdx, type, uIdx, message, writeTime)
        else chatList[0]

        if (lastChatIdx == chatData.chat_idx - 1) {
//            Log.d("$LOG_TAG/lastChatIdx==chat_idx", chatData.chat_idx.toString())
            val readChat = ChatMessage(0, ChatUtils.TYPE_READLINE, 0, "", "")
            dataList.add(readChat)
            lastChatPos = dataList.indexOf(readChat)
//            Log.d("$LOG_TAG/lastChatPost", lastChatPos.toString())
            lastChatIdx = -1
        }

        if (chatList.isEmpty())
            dataList.add(chatData)
        else chatData.update(type, uIdx, message, writeTime)

        if (chatData.chat_idx > DatabaseHelpUtils.getRecentChatIdx(applicationContext, room.room_idx))
            DatabaseHelpUtils.setRecentChatIdx(applicationContext, room.room_idx, chatData.chat_idx)

        chatData.count = count
        chatData.setPhotoInfo(applicationContext)

        if (chatIdx == adapter_chat.pickIdx) {
            scrollToPosition(listView_chat, dataList.indexOf(chatData))
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isOpenDrawLayout) 1 else 0)
        parcel.writeByte(if (isExpanded) 1 else 0)
        parcel.writeInt(lastChatIdx)
        parcel.writeInt(lastChatPos)
        parcel.writeByte(if (forFinish) 1 else 0)
        parcel.writeByte(if (isConnectedRoom) 1 else 0)
        parcel.writeByte(if (firstTimeResume) 1 else 0)
        parcel.writeString(makeContent)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatActivity> {
        override fun createFromParcel(parcel: Parcel): ChatActivity {
            return ChatActivity(parcel)
        }

        override fun newArray(size: Int): Array<ChatActivity?> {
            return arrayOfNulls(size)
        }
    }
}