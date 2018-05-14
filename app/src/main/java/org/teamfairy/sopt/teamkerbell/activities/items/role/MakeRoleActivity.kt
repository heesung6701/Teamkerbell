package org.teamfairy.sopt.teamkerbell.activities.items.role

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_commit.*
import kotlinx.android.synthetic.main.content_make_role.*
import kotlinx.android.synthetic.main.content_select_room.*
import org.json.JSONArray
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.listview.adapter.TextListAdapter
import org.teamfairy.sopt.teamkerbell.model.interfaces.GroupInterface
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class MakeRoleActivity : AppCompatActivity() , View.OnClickListener {
    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)

        room = dataListRoom[pos] as Room
        adapter.currentIdx = room?.room_idx ?: -1
        tv_room_name.text = room?.real_name ?: getText(R.string.txt_select_room)
        closeRoomList()
    }

    var group: Team by Delegates.notNull()
    var room: Room? = null


    private var adapter: TextListAdapter by Delegates.notNull()
    private var dataListRoom = java.util.ArrayList<GroupInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()


    var edtTaskList = arrayListOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_role)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)
        room = intent.getParcelableExtra(INTENT_ROOM)

        setRoomListInit()

        layout_add_task.setOnClickListener {
            val edtView = layoutInflater.inflate(R.layout.item_vote_example_edt, null, false)
            edtTaskList.add(edtView.findViewById(R.id.edt_vote_example))
            layout_role_tasks.addView(edtView)
        }

        btn_commit.setOnClickListener {
            if (edt_role_name.text.isEmpty()) {
                Toast.makeText(applicationContext, "역할 제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (room == null) {
                Toast.makeText(applicationContext, "그룹을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val title = edt_role_name.text.toString()
            val jsonParam = JSONObject()
            try {
                jsonParam.put(USGS_REQUEST_URL.URL_ROLE_REGISTER_PARAM_ROOM_IDX, room!!.room_idx)
                jsonParam.put(USGS_REQUEST_URL.URL_ROLE_REGISTER_PARAM_TITLE, title)
                val jsonArray = JSONArray()
                edtTaskList.forEach {
                    jsonArray.put(it.text.toString())
                }
                jsonParam.put(USGS_REQUEST_URL.URL_ROLE_REGISTER_PARAM_TASK_ARRAY, jsonArray)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val task = GetMessageTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
            task.execute(USGS_REQUEST_URL.URL_ROLE_REGISTER, jsonParam.toString())


        }

        btn_back.setOnClickListener {
            onBackPressed()
        }
    }


    private fun setRoomListInit() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TextListAdapter(dataListRoom, applicationContext)
        adapter.setOnItemClickListener(this)
        adapter.currentIdx = room?.room_idx ?: -1
        recyclerView.adapter = adapter

        layout_select_room.setOnClickListener {
            if (recyclerView.visibility != View.VISIBLE)
                openRoomList()
            else
                closeRoomList()
        }
    }

    private fun openRoomList() {

        if (recyclerView.visibility != View.VISIBLE) {
            recyclerView.visibility = View.VISIBLE
            iv_drop_down.rotation = 180.0f

            DatabaseHelpUtils.getRoomListFromRealm(applicationContext, dataListRoom as ArrayList<Room>, adapter as RecyclerView.Adapter<*>, group)
        }
    }

    private fun closeRoomList() {
        if (recyclerView.visibility != View.GONE) {
            recyclerView.visibility = View.GONE
            iv_drop_down.rotation = 0.0f
        }

    }

    override fun finish() {

        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
    }



    private class HandlerGet(activity: MakeRoleActivity) : Handler() {
        private val mActivity: WeakReference<MakeRoleActivity> = WeakReference<MakeRoleActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        val intent = Intent(activity.applicationContext, RoleListActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        activity.startActivity(intent)

                    }
                    else -> {
                        Toast.makeText(activity.applicationContext, "잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show()


                    }
                }
            }
        }
    }

}