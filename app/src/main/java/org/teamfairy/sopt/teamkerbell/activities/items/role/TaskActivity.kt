package org.teamfairy.sopt.teamkerbell.activities.items.role

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_more.*
import kotlinx.android.synthetic.main.content_task.*
import org.json.JSONArray
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.*
import org.teamfairy.sopt.teamkerbell.model.list.UserCheckData
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedR
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class TaskActivity : AppCompatActivity(){

    var group: Team by Delegates.notNull()
    var role : Role by Delegates.notNull()
    var roleTask : RoleTask by Delegates.notNull()


    private var chkUser = HashMap<Int, Boolean>()
    private var adapterUser: UserListAdapter  by Delegates.notNull()
    var dataListUser = ArrayList<User>()

    var isMaster = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(IntentTag.INTENT_GROUP)
        role = intent.getParcelableExtra(IntentTag.INTENT_ROLE)
        roleTask = intent.getParcelableExtra(IntentTag.INTENT_TASK)
        role.setPhotoInfo(applicationContext)

        isMaster = (role.master_idx == LoginToken.getUserIdx(applicationContext))

        supportActionBar!!.title=role.title + if(isMaster) " [마스터] " else ""



        tv_chat_name.text = "채팅방 이름"

        tv_task_name.text = roleTask.content

        tv_name.text = ("작성자 : ${role.name}")
        tv_time.text=role.getTime()



        getTakeUserArray()

        recyclerView_horizon.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapterUser = UserListAdapter(dataListUser, applicationContext)
        recyclerView_horizon.adapter = adapterUser


        btn_take_role.setOnClickListener {
            if(chkUser[LoginToken.getUserIdx(applicationContext)] == true)
                connectUpdateUser(-1)
            else
                connectUpdateUser(1)
        }

        if(chkUser[LoginToken.getUserIdx(applicationContext)] == true){
            btn_take_role.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.icon_floating_minus))
        }else
            btn_take_role.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.ic_add))

    }

    private fun getTakeUserArray(){
        dataListUser.clear()
        roleTask.userIdArray.forEach {

            val user = DatabaseHelpUtils.getUser(applicationContext, it)
            chkUser[user.u_idx] = true

            dataListUser.add(user)
        }
    }

    private fun getUserArray() {

        dataListUser.clear()

        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
        val joinedR = realm.where(JoinedR::class.java).equalTo("g_idx", group.g_idx).findAll()
        joinedR.forEach{
            val uIdx = it.u_idx
            val user: User = (realm.where(UserR::class.java).equalTo("u_idx", uIdx).findFirst()
                    ?: UserR()).toUser()
            dataListUser.add(user)
        }
        adapterUser.notifyDataSetChanged()
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
    fun getUserArrayFromJson(str: String) {
        dataListUser.clear()
        chkUser.clear()

        val jsonArray = JSONArray(str)
        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
        for (i in 0 until jsonArray.length()) {
            val uIdx = jsonArray.get(i).toString().toInt()
            val user: UserR = realm.where(UserR::class.java).equalTo("u_idx", uIdx).findFirst()
                    ?: UserR()
            dataListUser.add(user.toUser())
            chkUser[user.u_idx] = true
        }
        realm.close()


        if(chkUser[LoginToken.getUserIdx(applicationContext)] == true){
            btn_take_role.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.icon_floating_minus))
        }else
            btn_take_role.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.ic_add))

        adapterUser.notifyDataSetChanged()

    }


}
