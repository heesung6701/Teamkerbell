package org.teamfairy.sopt.teamkerbell.activities.items.signal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_commit.*
import kotlinx.android.synthetic.main.content_make_signal.*
import org.json.JSONArray
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.activities.items.filter.SelectRoomFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.data.User.Companion.ARG_U_IDX
import org.teamfairy.sopt.teamkerbell.model.list.UserCheckData
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedRoomR
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_CHATID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_ENTIRESTATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_OPENSTATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_UID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL_PARAM_USERARRAY
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_FROM_CHAT
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_SIGNAL_IDX
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.ENTIRE_STATUS_CHOSE
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.ENTIRE_STATUS_ENTIRE
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.OPEN_STATUS_OPEN
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.OPEN_STATUS_SECRET
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class MakeSignalActivity : AppCompatActivity(), RoomActivityInterface {
    override fun changeRoom(room: Room) {
        this.room = room
        getUserListFromRealm()
    }

    val LOG_TAG = this::class.java.name

    private var recyclerView: RecyclerView by Delegates.notNull()
    var adapter: UserListAdapter by Delegates.notNull()
    private var userList: ArrayList<UserCheckData> = arrayListOf<UserCheckData>()
    var dataList: ArrayList<Team>? = arrayListOf()

    var isConnecting: Boolean = false

    private var openStatus: Int = OPEN_STATUS_OPEN
    private var entireStatus: Int = ENTIRE_STATUS_ENTIRE
    var content: String = ""

    override var group: Team by Delegates.notNull()
    override var room: Room? = null

    private val whoCheck = HashMap<Int, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_signal)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra<Team>(INTENT_GROUP)
        room = intent.getParcelableExtra<Room>(INTENT_ROOM)

        SelectRoomFunc(this)
        setUserListInit()

        chk_particular.setOnCheckedChangeListener { _, p1 ->
            if (room == null || room!!.room_idx <0) {
                Toast.makeText(applicationContext, getString(R.string.txt_select_room), Toast.LENGTH_SHORT).show()
                chk_particular.isChecked = false
                return@setOnCheckedChangeListener
            }

            if (p1) {
                recyclerView.visibility = View.VISIBLE
            } else
                recyclerView.visibility = View.GONE
            adapter.notifyDataSetChanged()
        }

        btn_back.setOnClickListener {
            finish()
        }

        btn_commit.setOnClickListener {

            if (isConnecting) return@setOnClickListener

            if (room == null || room!!.room_idx < 0) {
                Toast.makeText(applicationContext, getString(R.string.txt_select_room), Toast.LENGTH_SHORT).show()
                layout_select_room.requestFocus()
                return@setOnClickListener
            }
            content = edt_content.text.toString()
            if (content.isEmpty()) {
                Toast.makeText(applicationContext, getString(R.string.txt_enter_content), Toast.LENGTH_SHORT).show()
                edt_content.requestFocus()
                return@setOnClickListener
            }

            openStatus = if (!chk_secret.isChecked) OPEN_STATUS_OPEN else OPEN_STATUS_SECRET
            entireStatus = if (!chk_particular.isChecked) ENTIRE_STATUS_ENTIRE else ENTIRE_STATUS_CHOSE

            val jsonParam = JSONObject()
            try {
                jsonParam.put(URL_MAKE_SIGNAL_PARAM_UID, LoginToken.getUserIdx(applicationContext))
                jsonParam.put(URL_MAKE_SIGNAL_PARAM_CHATID, group.g_idx)
                jsonParam.put(URL_MAKE_SIGNAL_PARAM_ROOM_IDX, room!!.room_idx)
                jsonParam.put(URL_MAKE_SIGNAL_PARAM_CONTENT, content)
                jsonParam.put(URL_MAKE_SIGNAL_PARAM_OPENSTATUS, openStatus)
                jsonParam.put(URL_MAKE_SIGNAL_PARAM_ENTIRESTATUS, entireStatus)
                val jsonArray = JSONArray()
                if (entireStatus == ENTIRE_STATUS_CHOSE) {
                    userList.iterator().forEach {
                        if (it.isChecked)
                            jsonArray.put(it.u_idx)
                    }
                }
                jsonParam.put(URL_MAKE_SIGNAL_PARAM_USERARRAY, jsonArray)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val task = GetMessageTask(applicationContext, HandlerMake(this), LoginToken.getToken(applicationContext))
            isConnecting = true
            task.execute(URL_MAKE_SIGNAL, METHOD_POST, jsonParam.toString())
        }
    }

    private fun setUserListInit() {
        recyclerView = findViewById(R.id.recyclerView_user)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        adapter = UserListAdapter(userList as ArrayList<User>, applicationContext)
        recyclerView.adapter = adapter
    }

    private fun getUserListFromRealm() {
        if (room == null) return

        val realm = getRealmDefault(applicationContext)

        val joinedRs = realm.where(JoinedRoomR::class.java).equalTo(Room.ARG_ROOM_IDX, room!!.room_idx).findAll()

        userList.forEach {
            whoCheck[it.u_idx] = it.isChecked
        }

        userList.clear()
        joinedRs.iterator().forEach {
            if (it.u_idx != LoginToken.getUserIdx(applicationContext)) {
                val u = (realm.where(UserR::class.java).equalTo(ARG_U_IDX, it.u_idx).findFirst()
                        ?: UserR()).toUser()
                userList.add(u.toUserCheckData(whoCheck[it.u_idx] ?: false))
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
    }

    private class HandlerMake(activity: MakeSignalActivity) : Handler() {
        private val mActivity: WeakReference<MakeSignalActivity> = WeakReference<MakeSignalActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        Toast.makeText(activity.applicationContext, "신호등이 만들어졌습니다.", Toast.LENGTH_SHORT).show()
                        val obj = msg.obj as String
                        val idx = obj.toInt()

                        Handler().postDelayed(Runnable {
                            if (!activity.intent.getBooleanExtra(INTENT_FROM_CHAT, false)) {
                                val intent = Intent(activity.applicationContext, SignalActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                intent.putExtra(INTENT_GROUP, activity.group)
                                intent.putExtra(INTENT_SIGNAL_IDX, idx.toInt())
                                activity.startActivity(intent)
                            }
                            activity.finish()
                        }, 500)
                    }
                    else -> {
                        val result = msg.data.getString("message")
                        activity.isConnecting = false
                        Toast.makeText(activity.applicationContext, result, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
