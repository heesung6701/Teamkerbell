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
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_more.*
import org.teamfairy.sopt.teamkerbell.activities.items.role.adapter.RoleListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Role
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.info.RoleListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROLE
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


class RoleListActivity : AppCompatActivity(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectRoleList()
        mSwipeRefreshLayout.isRefreshing = false
    }


    var group: Team by Delegates.notNull()

    private var dataList: ArrayList<Role> = arrayListOf<Role>()
    private var recyclerView: RecyclerView by Delegates.notNull()
    private var adapter: RoleListAdapter by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_list)
        setSupportActionBar(toolbar)


        group = intent.getParcelableExtra(INTENT_GROUP)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RoleListAdapter(dataList)
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

        btn_back.setOnClickListener {
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        connectRoleList()
    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        val i = Intent(applicationContext,RoleActivity::class.java)
        i.putExtra(INTENT_GROUP, group)
        i.putExtra(INTENT_ROLE, dataList[pos])
        startActivity(i)
    }

    private fun connectRoleList() {
        val task = RoleListTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
        task.execute(USGS_REQUEST_URL.URL_ROLE_SHOW)
    }

    private class HandlerGet(activity: RoleListActivity) : Handler() {
        private val mActivity: WeakReference<RoleListActivity> = WeakReference<RoleListActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                val datas=msg.obj as ArrayList<Role>
                activity.dataList.clear()
                datas.forEach {
                    it.setPhotoInfo(activity.applicationContext)
                    activity.dataList.add(it)
                }
                activity.adapter.notifyDataSetChanged()
            }
        }
    }
}
