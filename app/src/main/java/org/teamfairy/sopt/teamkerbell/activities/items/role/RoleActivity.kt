package org.teamfairy.sopt.teamkerbell.activities.items.role

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_more.*
import kotlinx.android.synthetic.main.content_role.*
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.activities.items.filter.MenuFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.MenuActionInterface
import org.teamfairy.sopt.teamkerbell.activities.items.role.adapter.TaskListAdapter
import org.teamfairy.sopt.teamkerbell.activities.items.role.task.TaskActivity
import org.teamfairy.sopt.teamkerbell.model.data.Role
import org.teamfairy.sopt.teamkerbell.model.data.RoleTask
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_GET
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.info.RoleTaskListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROLE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_TASK
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class RoleActivity : AppCompatActivity(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, MenuActionInterface {
    override fun menuEdit() {
        attemptEdit()
    }

    override fun menuDelete() {
        attemptDelete()
    }

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectTaskList()
        mSwipeRefreshLayout.isRefreshing = false
    }


    var group: Team by Delegates.notNull()
    var role: Role by Delegates.notNull()
    var room: Room by Delegates.notNull()

    private var dataList: ArrayList<RoleTask> = arrayListOf<RoleTask>()
    private var recyclerView: RecyclerView by Delegates.notNull()
    private var adapter: TaskListAdapter by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)
        role = intent.getParcelableExtra(INTENT_ROLE)
        room = DatabaseHelpUtils.getRoom(applicationContext, role.room_idx)

        supportActionBar!!.title = role.title
        tv_chat_name.text = room.real_name

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TaskListAdapter(dataList, applicationContext)
        adapter.setOnItemClick(this)
        recyclerView.adapter = adapter


        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        btn_back.setOnClickListener {
            finish()
        }

        MenuFunc(this)


    }

    override fun onResume() {
        super.onResume()

        connectTaskList()
    }

    private fun attemptEdit(){
        val i = Intent(applicationContext, EditRoleActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        i.putExtra(INTENT_GROUP, group)
        i.putExtra(INTENT_ROOM, room)
        i.putExtra(INTENT_ROLE, role)
        i.putExtra(INTENT_TASK, dataList)
        startActivity(i)
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
    }

    private fun attemptDelete(){
        //삭제하기
    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        val i = Intent(applicationContext, TaskActivity::class.java)
        i.putExtra(INTENT_GROUP, group)
        i.putExtra(INTENT_ROLE, role)
        i.putExtra(INTENT_ROOM, room)
        i.putExtra(INTENT_TASK, dataList[pos])
        startActivity(i)
    }


    private fun connectTaskList() {
        dataList.clear()
        adapter.notifyDataSetChanged()

        val task = RoleTaskListTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
        task.execute(USGS_REQUEST_URL.URL_ROLE_TASK + "/" + role.role_idx, METHOD_GET)

    }

    fun connectedTaskList(msg: Message) {
        when (msg.what) {
            Utils.MSG_SUCCESS -> {
                val datas = msg.obj as ArrayList<RoleTask>
                dataList.clear()
                datas.forEach {
                    dataList.add(it)
                }
                adapter.notifyDataSetChanged()
            }
            else -> {
                Toast.makeText(applicationContext,"잠시 후 시도 해주세요.",Toast.LENGTH_SHORT).show()

            }
        }
    }

    private class HandlerGet(activity: RoleActivity) : Handler() {
        private val mActivity: WeakReference<RoleActivity> = WeakReference<RoleActivity>(activity)

        override fun handleMessage(msg: Message) {
            mActivity.get()?.connectedTaskList(msg)
        }
    }

}
