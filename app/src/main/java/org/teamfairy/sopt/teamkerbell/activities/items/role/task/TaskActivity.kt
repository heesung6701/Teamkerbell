package org.teamfairy.sopt.teamkerbell.activities.items.role.task

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_task.*
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_back.*
import kotlinx.android.synthetic.main.content_recyclerview.*
import kotlinx.android.synthetic.main.content_task.*
import org.json.JSONArray
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R.id.btn_take_role
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.activities.group.invite.InviteActivity
import org.teamfairy.sopt.teamkerbell.activities.items.role.adapter.ResponseListAdapter
import org.teamfairy.sopt.teamkerbell.activities.items.role.dialog.SelectUserDialog
import org.teamfairy.sopt.teamkerbell.activities.main.dialog.ShowUserDialog
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.*
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedGroupR
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_SHOW_RESPONSE
import org.teamfairy.sopt.teamkerbell.network.info.TaskResponseListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Build
import android.content.DialogInterface
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROLE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_TASK
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_TASK_RESPONSE


class TaskActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectResponseList()
        mSwipeRefreshLayout.isRefreshing = false
    }


    var group: Team by Delegates.notNull()
    var role: Role by Delegates.notNull()
    var roleTask: RoleTask by Delegates.notNull()
    var room: Room by Delegates.notNull()


    private var chkUser = HashMap<Int, Boolean>()
    private var adapterUser: UserListAdapter  by Delegates.notNull()
    private var dataListUser = ArrayList<User>()

    private var isMaster = false


    var dataListResponse = ArrayList<TaskResponse>()
    var adapter: ResponseListAdapter by Delegates.notNull()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(IntentTag.INTENT_GROUP)
        role = intent.getParcelableExtra(IntentTag.INTENT_ROLE)
        room = intent.getParcelableExtra(IntentTag.INTENT_ROOM)
        roleTask = intent.getParcelableExtra(IntentTag.INTENT_TASK)
        role.setPhotoInfo(applicationContext)

        isMaster = (role.master_idx == LoginToken.getUserIdx(applicationContext))

        supportActionBar!!.title = role.title



        tv_chat_name.text = room.real_name

        tv_task_name.text = roleTask.content

        tv_name.text = ("작성자 : ${role.name}")
        tv_time.text = role.getTime()



        getTakeUserArray()

        recyclerView_horizon.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapterUser = UserListAdapter(dataListUser, applicationContext)
        recyclerView_horizon.adapter = adapterUser

        setResponseList()


        btn_take_role.setOnClickListener {
            if (isMaster) {
                showUserDialog()
            } else {
                if (chkUser[LoginToken.getUserIdx(applicationContext)] == true)
                    connectUpdateUser(-1)
                else
                    connectUpdateUser(1)
            }
        }

        if (chkUser[LoginToken.getUserIdx(applicationContext)] == true) {
            btn_take_role.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.icon_floating_minus))
        } else
            btn_take_role.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_add))


        btn_back.setOnClickListener {
            onBackPressed()
        }
        fab.setOnClickListener {

            if(roleTask.userIdArray.contains(LoginToken.getUserIdx(applicationContext))) {
                val i = Intent(applicationContext, MakeTaskResponseActivity::class.java)
                i.putExtra(IntentTag.INTENT_TASK, roleTask)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
            }else{
                Toast.makeText(applicationContext,"맡은일에 최선을 다하자",Toast.LENGTH_SHORT).show()
            }
        }


        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

    }

    private fun setResponseList() {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager
        adapter = ResponseListAdapter(dataListResponse, HandlerClick(this), this)
        recyclerView.adapter = adapter

    }

    private fun showUserDialog() {

        val dialog = SelectUserDialog(this, room,roleTask.userIdArray)
        dialog.show()

        dialog.setOnClickListener(View.OnClickListener { p0 ->
            when (p0.id) {
                R.id.btn_complete -> {
                    val plusArray = JSONArray()
                    val minusArray = JSONArray()

                    dialog.dataList.iterator().forEach {
                        if (it.isChecked && !roleTask.userIdArray.contains(it.u_idx))
                            plusArray.put(it.u_idx)
                        else if (!it.isChecked && roleTask.userIdArray.contains(it.u_idx))
                            minusArray.put(it.u_idx)
                    }

                    connectUpdateUser(plusArray,minusArray)
                    dialog.dismiss()
                }
            }
        })
    }

    private fun getTakeUserArray() {
        dataListUser.clear()
        roleTask.userIdArray.forEach {
            val user = DatabaseHelpUtils.getUser(applicationContext, it)
            chkUser[user.u_idx] = true
            dataListUser.add(user)
        }
    }


    private fun connectUpdateUser(plusArrayJson : JSONArray, minusArrayJson : JSONArray) {
        val task = GetMessageTask(applicationContext, HandlerUpdateUser(this), LoginToken.getToken(applicationContext))

        val jsonParam = JSONObject()
        jsonParam.put(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER_PARAM_ROLE_IDX, roleTask.role_idx)
        jsonParam.put(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER_PARAM_TASK_IDX, roleTask.task_idx)
        jsonParam.put(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER_PARAM_PLUS_ARRAY, plusArrayJson)
        jsonParam.put(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER_PARAM_MINUS_ARRAY, minusArrayJson)
        task.execute(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER, jsonParam.toString())

    }

    fun getUserArrayFromJson(str: String) {
        dataListUser.clear()
        chkUser.clear()


        val jsonArray = JSONArray(str)
        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
        for (i in 0 until jsonArray.length()) {
            val uIdx = jsonArray.get(i).toString().toInt()
            val user: UserR = realm.where(UserR::class.java).equalTo(JSON_U_IDX, uIdx).findFirst()
                    ?: UserR()
            dataListUser.add(user.toUser())
            chkUser[user.u_idx] = true
        }
        realm.close()


        if (chkUser[LoginToken.getUserIdx(applicationContext)] == true) {
            if (isMaster) {
                dataListUser.add(LoginToken.getUser(applicationContext))
                chkUser[LoginToken.getUserIdx(applicationContext)] = true
            }
            btn_take_role.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.icon_floating_minus))
        } else {
            btn_take_role.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_add))
        }

        adapterUser.notifyDataSetChanged()

    }


    override fun onResume() {
        super.onResume()
        connectResponseList()
    }



    private fun connectUpdateUser(status: Int) {
        val task = GetMessageTask(applicationContext, HandlerUpdateUser(this), LoginToken.getToken(applicationContext))

        val jsonParam = JSONObject()

        jsonParam.put(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER_PARAM_ROLE_IDX, roleTask.role_idx)
        jsonParam.put(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER_PARAM_TASK_IDX, roleTask.task_idx)
        if (isMaster) {
            when (status) {
                1 -> {
                    val plusArrayJson = JSONArray()
                    plusArrayJson.put(LoginToken.getUserIdx(applicationContext))
                    jsonParam.put(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER_PARAM_PLUS_ARRAY, plusArrayJson)
                }
                -1 -> {
                    val minusArrayJson = JSONArray()
                    minusArrayJson.put(LoginToken.getUserIdx(applicationContext))
                    jsonParam.put(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER_PARAM_MINUS_ARRAY, minusArrayJson)
                }
            }

        } else jsonParam.put(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER_PARAM_ROLE_STATUS, status)
        task.execute(USGS_REQUEST_URL.URL_MODIFY_ROLE_USER, jsonParam.toString())

    }

    private class HandlerUpdateUser(activity: TaskActivity) : Handler() {
        private val mActivity: WeakReference<TaskActivity> = WeakReference<TaskActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        if (msg.obj != null) {
                            activity.getUserArrayFromJson(msg.obj.toString())
                            Toast.makeText(activity.applicationContext, "수정되었습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        Toast.makeText(activity.applicationContext, "잠시후 다시 도전해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun connectResponseList() {
        val task = TaskResponseListTask(applicationContext, HandlerGetResponse(this), LoginToken.getToken(applicationContext))
        task.execute(URL_ROLE_SHOW_RESPONSE + "/" + roleTask.task_idx)
    }

    private class HandlerGetResponse(activity: TaskActivity) : Handler() {
        private val mActivity: WeakReference<TaskActivity> = WeakReference<TaskActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {

                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        val datas: ArrayList<TaskResponse> = msg.obj as ArrayList<TaskResponse>
                        activity.dataListResponse.clear()
                        for (i in 0 until datas.size) {
                            datas[i].setPhotoInfo(activity.applicationContext)
                            activity.dataListResponse.add(datas[i])
                        }

                        activity.adapter.notifyDataSetChanged()
                        activity.recyclerView.smoothScrollToPosition(datas.size)
                    }
                }
            }
        }
    }


    private class HandlerClick(activity: TaskActivity) : Handler() {
        private val mActivity: WeakReference<TaskActivity> = WeakReference<TaskActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                val pos = msg.what

                val intent = Intent(activity.applicationContext, TaskResponseActivity::class.java)
                intent.putExtra(INTENT_TASK_RESPONSE, activity.dataListResponse[pos])
                intent.putExtra(INTENT_TASK, activity.roleTask)
                intent.putExtra(INTENT_ROLE, activity.role)
                intent.putExtra(INTENT_ROOM, activity.room)
                activity.startActivity(intent)

            }
        }
    }


}
