package org.teamfairy.sopt.teamkerbell.activities.items.notice

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_commit.*
import kotlinx.android.synthetic.main.content_make_notice.*
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.listview.adapter.TextListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.GroupInterface
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_CHATROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import kotlin.properties.Delegates

class MakeNoticeActivity : AppCompatActivity(), View.OnClickListener {

    var group: Team by Delegates.notNull()
    var room: Room? = null


    private var adapter: TextListAdapter by Delegates.notNull()
    private var dataListRoom = ArrayList<GroupInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_notice)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)
        room = intent.getParcelableExtra(INTENT_CHATROOM)

        NetworkUtils.connectRoomList(applicationContext,null,true,group.g_idx)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TextListAdapter(dataListRoom, applicationContext)
        adapter.setOnItemClickListener(this)
        adapter.currentIdx = room?.room_idx ?: -1
        recyclerView.adapter = adapter

        layout_select_team.setOnClickListener {
            if (recyclerView.visibility != View.VISIBLE)
                openRoomList()
            else
                closeRoomList()
        }

        edt_response.setOnFocusChangeListener { _, b ->
            if (b) {
                recyclerView.visibility = View.GONE
                iv_drop_down.rotation = 0.0f
            }
        }
        btn_back.setOnClickListener {
            onBackPressed()
        }
        btn_commit.setOnClickListener {

        }

    }


    private fun closeRoomList() {
        if(recyclerView.visibility!=View.GONE) {
            recyclerView.visibility = View.GONE
            iv_drop_down.rotation = 0.0f
        }

    }

    private fun openRoomList() {

        if(recyclerView.visibility!=View.VISIBLE) {
            recyclerView.visibility = View.VISIBLE
            iv_drop_down.rotation = 180.0f

            DatabaseHelpUtils.getRoomListFromRealm(applicationContext, dataListRoom as ArrayList<Room>, adapter as RecyclerView.Adapter<*>, group)
        }
    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)

        room = dataListRoom[pos] as Room
        adapter.currentIdx = room?.room_idx ?: -1
        closeRoomList()
    }

}
