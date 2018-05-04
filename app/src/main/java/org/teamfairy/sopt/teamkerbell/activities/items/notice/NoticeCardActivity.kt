package org.teamfairy.sopt.teamkerbell.activities.items.notice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import io.realm.Sort
import kotlinx.android.synthetic.main.app_bar_more.*
import kotlinx.android.synthetic.main.content_notice_card.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.activities.items.notice.adapter.CardListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.NoticeR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_DETAIL_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_DETAIL_PARAM_GID
import org.teamfairy.sopt.teamkerbell.network.info.NoticeListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_NOTICE
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


class NoticeCardActivity : AppCompatActivity() ,View.OnClickListener{


    private var recyclerView : RecyclerView by Delegates.notNull()

    private var adapter: CardListAdapter by Delegates.notNull()
    private var dataList: ArrayList<Notice> = arrayListOf<Notice>()

    var group : Team by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_card)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)

        adapter = CardListAdapter(dataList,applicationContext,this)

        recyclerView  = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        recyclerView.adapter=adapter


        btn_show_list.setOnClickListener {
            val i = Intent(applicationContext,NoticeListActivity::class.java)
            i.putExtra(INTENT_GROUP,group)
            startActivity(i)
        }

        btn_back.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        connectNoticeList()
    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)

        val i = Intent(applicationContext,NoticeActivity::class.java)
        i.putExtra(INTENT_NOTICE,dataList[pos])
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

        val builtUri = Uri.parse(URL_DETAIL_NOTICE)
                .buildUpon()
                .appendQueryParameter(URL_DETAIL_PARAM_GID, group.g_idx.toString())
                .build()

        task.execute(builtUri.toString())

    }

    private class HandlerGet(fragment: NoticeCardActivity) : Handler() {
        private val mFragment: WeakReference<NoticeCardActivity> = WeakReference<NoticeCardActivity>(fragment)

        override fun handleMessage(msg: Message) {
            mFragment.get()?.getNoticeListFromRealm()
        }
    }
}
