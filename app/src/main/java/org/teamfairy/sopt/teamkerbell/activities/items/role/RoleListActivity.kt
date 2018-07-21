package org.teamfairy.sopt.teamkerbell.activities.items.role

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.activity_role_list.*
import kotlinx.android.synthetic.main.app_bar_filter.*
import org.teamfairy.sopt.teamkerbell.R

import org.teamfairy.sopt.teamkerbell.activities.items.filter.FilterFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.activities.items.role.adapter.RoleListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Role
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_GET
import org.teamfairy.sopt.teamkerbell.network.info.RoleListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROLE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


class RoleListActivity : AppCompatActivity(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, RoomActivityInterface {
    override fun changeRoom(room: Room) {
        this.room=room
        updateList()
    }

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectRoleList()
        mSwipeRefreshLayout.isRefreshing = false
    }


    override var group: Team by Delegates.notNull()
    override var room : Room?=null

    private var dataList: ArrayList<Role> = arrayListOf<Role>()
    private var roleList: ArrayList<Role> = arrayListOf<Role>()
    private var recyclerView: RecyclerView by Delegates.notNull()
    private var adapter: RoleListAdapter by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_list)
        setSupportActionBar(toolbar)
        tv_title.text = supportActionBar!!.title


        group = intent.getParcelableExtra(INTENT_GROUP)
        room =intent.getParcelableExtra(INTENT_ROOM)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RoleListAdapter(dataList,applicationContext)
        adapter.setOnItemClick(this)
        recyclerView.adapter = adapter

        val divider = DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
        )
        divider.setDrawable(ContextCompat.getDrawable(baseContext, R.drawable.shape_line_divider))
        recyclerView.addItemDecoration(divider)

        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)


        FilterFunc(this)

        btn_back.setOnClickListener {
            finish()
        }


        fab.setOnClickListener {
            val i = Intent(applicationContext, MakeRoleActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            i.putExtra(INTENT_GROUP,group)
            i.putExtra(INTENT_ROOM,room)
            startActivity(i)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }
    }

    override fun onResume() {
        super.onResume()
        connectRoleList()
    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        val i = Intent(applicationContext, RoleActivity::class.java)
        i.putExtra(INTENT_GROUP, group)
        i.putExtra(INTENT_ROLE, dataList[pos])
        startActivity(i)
    }

    private fun connectRoleList() {
        val task = RoleListTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
        val roomIdx = room?.room_idx?:""
        if(roomIdx== Room.ARG_ALL_IDX || roomIdx==""){
            task.execute("$URL_ROLE_GET/g/${group.g_idx}")
        }else{
            task.execute("$URL_ROLE_GET/c/$roomIdx")
        }

    }
    private fun updateList(){
        dataList.clear()
        roleList.forEach {
            if(it.room_idx==room?.room_idx ?: it.room_idx || room?.room_idx==Room.ARG_ALL_IDX)
                dataList.add(it)
        }
        adapter.notifyDataSetChanged()
    }

    private  fun successGetRoleList(msg : Message){
        when (msg.what) {
            MSG_SUCCESS -> {
                val datas = msg.obj as ArrayList<Role>
                roleList.clear()
                datas.forEach {
//                    it.setPhotoInfo(applicationContext)
                    roleList.add(it)
                }
                updateList()
            }
            else -> {
            }
        }
    }

    private class HandlerGet(activity: RoleListActivity) : Handler() {
        private val mActivity: WeakReference<RoleListActivity> = WeakReference<RoleListActivity>(activity)

        override fun handleMessage(msg: Message) {
            mActivity.get()?.successGetRoleList(msg)
        }
    }
}
