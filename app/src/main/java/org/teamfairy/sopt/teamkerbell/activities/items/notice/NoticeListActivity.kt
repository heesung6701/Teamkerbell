package org.teamfairy.sopt.teamkerbell.activities.items.notice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import io.realm.Sort
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_more.*
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell._utils.TagUtils
import org.teamfairy.sopt.teamkerbell._utils.TagUtils.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.listview.adapter.ListDataAdapter
import org.teamfairy.sopt.teamkerbell.model.data.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.NoticeR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.info.NoticeListTask
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class NoticeListActivity : AppCompatActivity(),View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectNoticeList()
        mSwipeRefreshLayout.isRefreshing = false
    }


    private var recyclerView : RecyclerView by Delegates.notNull()

    var adapter: ListDataAdapter by Delegates.notNull()
    var dataList: ArrayList<ListDataInterface> = arrayListOf<ListDataInterface>()

    var group : Team by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_list)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)

        adapter = ListDataAdapter(dataList,applicationContext)
        adapter.setOnItemClick(this)

        recyclerView  = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter=adapter

        getNoticeListFromRealm()


        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)


        btn_back.setOnClickListener { onBackPressed() }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)

        val i = Intent(applicationContext,NoticeActivity::class.java)
        i.putExtra(TagUtils.INTENT_NOTICE,dataList[pos] as Notice)
        startActivity(i)
    }


    fun getNoticeListFromRealm() {

        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)

        val result = realm.where(NoticeR::class.java).equalTo("g_idx", group.g_idx).sort("write_time", Sort.DESCENDING).findAll()
        dataList.clear()
        result.iterator().forEach {
            dataList.add(it.toNotice(realm))
        }
        adapter.notifyDataSetChanged()

    }

    private fun connectNoticeList() {

        dataList.clear()
        adapter.notifyDataSetChanged()

        val task = NoticeListTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
        task.g_idx = group.g_idx

        val builtUri = Uri.parse(USGS_REQUEST_URL.URL_DETAIL_NOTICE)
                .buildUpon()
                .appendQueryParameter(USGS_REQUEST_URL.URL_DETAIL_PARAM_GID, group.g_idx.toString())
                .build()

        task.execute(builtUri.toString())

    }

    private class HandlerGet(fragment: NoticeListActivity) : Handler() {
        private val mFragment: WeakReference<NoticeListActivity> = WeakReference<NoticeListActivity>(fragment)

        override fun handleMessage(msg: Message) {
            mFragment.get()?.getNoticeListFromRealm()
        }
    }
}
