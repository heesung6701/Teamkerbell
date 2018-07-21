package org.teamfairy.sopt.teamkerbell.activities.chat

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_commit.*
import kotlinx.android.synthetic.main.content_invite_user.*
import org.json.JSONArray
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.list.UserCheckData
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_INVITE_ROOM_PARAM_G_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_INVITE_ROOM_PARAM_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_INVITE_ROOM_PARAM_USER_ARRAY
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class InviteUserActivity : AppCompatActivity() , SwipeRefreshLayout.OnRefreshListener {

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        updateUserList()
        mSwipeRefreshLayout.isRefreshing = false
    }



    var group : Team by Delegates.notNull()
    var room : Room by Delegates.notNull()

    var adapter: UserListAdapter by Delegates.notNull()
    var dataList: ArrayList<UserCheckData> = arrayListOf<UserCheckData>()
    var dataListOrigin: ArrayList<User> = arrayListOf<User>()
    var recyclerView: RecyclerView by Delegates.notNull()

    var txtSearch : String = ""


    private val whoCheck = HashMap<Int,Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_user)
        setSupportActionBar(toolbar)
        group  = intent.getParcelableExtra<Team>(INTENT_GROUP)
        room  = intent.getParcelableExtra<Room>(INTENT_ROOM)


        recyclerView = findViewById(R.id.recyclerView)
        adapter = UserListAdapter(dataList as ArrayList<User>,applicationContext)
        recyclerView.layoutManager= LinearLayoutManager(this)
        recyclerView.adapter=adapter

        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)


        btn_back.setOnClickListener {
            onBackPressed()
        }

        btn_commit.setOnClickListener {

            val checkedUserArray = dataList.filter { it.isChecked }
            if(checkedUserArray.isNotEmpty())
                attemptCommit(checkedUserArray)
            else
                Toast.makeText(applicationContext,"선택된 사용자가 없습니다", Toast.LENGTH_SHORT).show()
        }

        edt_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                txtSearch = p0.toString().trim()
                getUserList()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    override fun onResume() {
        super.onResume()
        getUserList()
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
    }


    fun getUserList(){
        DatabaseHelpUtils.getUserListFromRealm(applicationContext,dataListOrigin,adapter as RecyclerView.Adapter<*>,group,true)
        updateUserList()

    }
    private fun updateUserList(){
        dataList.forEach {
            whoCheck[it.u_idx]=it.isChecked
        }
        dataList.clear()

        val roomMemberList = ArrayList<Int>()
        DatabaseHelpUtils.getRoomUIdxListFromRealm(applicationContext,roomMemberList,room)

        dataListOrigin.forEach {
            if(it.name!!.contains(txtSearch) && !roomMemberList.contains(it.u_idx)) {
                dataList.add(it.toUserCheckData(whoCheck[it.u_idx]?:false))
            }
        }
        adapter.notifyDataSetChanged()
    }

    fun invitedUser(msg : Message){
        when(msg.what){
            Utils.MSG_SUCCESS->
                    finish()
            else->
                    Toast.makeText(applicationContext,"잠시 후 다시 시도해주세요",Toast.LENGTH_SHORT).show()
        }

    }
    private fun attemptCommit(userCheckArray : List<UserCheckData>){
        val task = GetMessageTask(applicationContext,HandlerInvite(this))
        val jsonParam = JSONObject()

        try {
            jsonParam.put(URL_INVITE_ROOM_PARAM_G_IDX, room.room_idx)
            jsonParam.put(URL_INVITE_ROOM_PARAM_ROOM_IDX, room.room_idx)
            val jsonArray = JSONArray()
            userCheckArray.forEach {
                jsonArray.put(it.u_idx)
            }
            jsonParam.put(URL_INVITE_ROOM_PARAM_USER_ARRAY, jsonArray)


        } catch (e: Exception) {
            e.printStackTrace()
        }
        task.execute(USGS_REQUEST_URL.URL_INVITE_ROOM,jsonParam.toString())

    }

    private class HandlerInvite(activity: InviteUserActivity) : Handler() {
        private val mActivity= WeakReference<InviteUserActivity>(activity)

        override fun handleMessage(msg: Message) {
            mActivity.get()?.invitedUser(msg)
        }
    }
}
