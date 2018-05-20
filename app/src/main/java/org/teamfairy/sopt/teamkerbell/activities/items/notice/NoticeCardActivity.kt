package org.teamfairy.sopt.teamkerbell.activities.items.notice

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.activity_notice_card.*
import kotlinx.android.synthetic.main.app_bar_filter.*
import kotlinx.android.synthetic.main.content_notice_card.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.R.id.tv_title
import org.teamfairy.sopt.teamkerbell.activities.items.filter.FilterFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.activities.items.notice.adapter.CardListAdapter
import org.teamfairy.sopt.teamkerbell.listview.adapter.ListDataAdapter
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_GROUP_NOTICE
import org.teamfairy.sopt.teamkerbell.network.info.NoticeListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_NOTICE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


class NoticeCardActivity : AppCompatActivity(), View.OnClickListener, RoomActivityInterface {
    override fun changeRoom(room: Room) {
        this.room=room
        connectNoticeList()
    }


    private var adapterCard: CardListAdapter by Delegates.notNull()

    private var adapterList: ListDataAdapter by Delegates.notNull()
    var dataList: ArrayList<ListDataInterface> = arrayListOf<ListDataInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()



    override var group: Team by Delegates.notNull()
    override var room: Room?=null

    private var showCard = true

    private var divider: DividerItemDecoration by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_card)
        setSupportActionBar(toolbar)
        tv_title.text = supportActionBar!!.title

        group = intent.getParcelableExtra(INTENT_GROUP)
        room = intent.getParcelableExtra(INTENT_ROOM) ?: Room()


        adapterCard = CardListAdapter(dataList, applicationContext, this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapterCard


        adapterList = ListDataAdapter(dataList, applicationContext)
        adapterList.setOnItemClick(this)


        divider = DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
        )
        divider.setDrawable(ContextCompat.getDrawable(baseContext, R.drawable.shape_line_divider))

        FilterFunc(this)


        tv_show_list.setOnClickListener { changeMode() }


        fab.setOnClickListener { _ ->
            val i = Intent(applicationContext, MakeNoticeActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            i.putExtra(INTENT_GROUP, group)
            i.putExtra(INTENT_ROOM, room)
            startActivity(i)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }

        btn_back.setOnClickListener { onBackPressed() }


    }



    private fun changeMode() {
        showCard = !showCard
        if (showCard) {
            tv_show_list.text = getString(R.string.action_show_card)
            recyclerView.setPadding(0, 0, 0, 0)
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = adapterCard

            recyclerView.removeItemDecoration(divider)

        } else {
            tv_show_list.text = getString(R.string.action_show_list)
            recyclerView.setPadding(16, 16, 16, 16)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapterList

            recyclerView.addItemDecoration(divider)
        }

    }

    override fun onResume() {
        super.onResume()
        connectNoticeList()
    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)

        val i = Intent(applicationContext, NoticeActivity::class.java)
        i.putExtra(INTENT_NOTICE, dataList[pos] as Notice)
        startActivity(i)
    }

    fun getNoticeList(result: ArrayList<Notice>) {

        dataList.clear()
        result.iterator().forEach {
            it.setPhotoInfo(applicationContext)
            it.setGroupInfo(applicationContext)
            dataList.add(it)
        }
        adapterCard.notifyDataSetChanged()

    }

    private fun connectNoticeList() {

        dataList.clear()
        adapterCard.notifyDataSetChanged()

        val task = NoticeListTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
        task.g_idx = group.g_idx
        task.execute(URL_GROUP_NOTICE.plus("/${group.g_idx}"))

    }

    private class HandlerGet(fragment: NoticeCardActivity) : Handler() {
        private val mActivity: WeakReference<NoticeCardActivity> = WeakReference<NoticeCardActivity>(fragment)

        override fun handleMessage(msg: Message) {
            if (msg.obj is ArrayList<*>) {
                mActivity.get()?.getNoticeList(msg.obj as ArrayList<Notice>)
            }
        }
    }


}
