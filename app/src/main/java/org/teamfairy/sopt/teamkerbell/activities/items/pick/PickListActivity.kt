package org.teamfairy.sopt.teamkerbell.activities.items.pick

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_filter.*
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.activities.chat.ChatActivity
import org.teamfairy.sopt.teamkerbell.activities.items.filter.FilterFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.activities.items.pick.adapter.PickListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Pick
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ALL_IDX
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_NULL_IDX
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.Team.Companion.ARG_G_IDX
import org.teamfairy.sopt.teamkerbell.model.realm.PickR
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_PICK_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import kotlin.properties.Delegates

class PickListActivity : AppCompatActivity(), RoomActivityInterface, View.OnClickListener {
    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        val pick = dataList[pos]

        val pRoom : Room= DatabaseHelpUtils.getRoom(applicationContext,pick.room_idx)
        if(pRoom.room_idx== ARG_NULL_IDX){
            Toast.makeText(applicationContext,"해당 채팅방은 존재하지 않습니다.",Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(INTENT_GROUP, group)
        intent.putExtra(INTENT_ROOM, pRoom)
        intent.putExtra(INTENT_PICK_IDX, pick.chat_idx)
        startActivity(intent)
    }


    private var adapter: PickListAdapter by Delegates.notNull()
    var dataList: ArrayList<Pick> = arrayListOf<Pick>()
    private var recyclerView: RecyclerView by Delegates.notNull()

    override var group: Team by Delegates.notNull()
    override var room: Room? = null

    override fun changeRoom(room: Room) {
        this.room = room
        getPickList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_list)
        setSupportActionBar(toolbar)
        tv_title.text = supportActionBar!!.title

        group = intent.getParcelableExtra(IntentTag.INTENT_GROUP)
        room = intent.getParcelableExtra(IntentTag.INTENT_ROOM) ?: Room()

        adapter = PickListAdapter(dataList, applicationContext)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.setOnItemClick(this)


        val divider = DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
        )
        divider.setDrawable(ContextCompat.getDrawable(baseContext, R.drawable.shape_line_divider))
        recyclerView.addItemDecoration(divider)

        FilterFunc(this)


        btn_back.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onResume() {
        super.onResume()
        getPickList()
    }

    private fun getPickList() {

        dataList.clear()

        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
        val pickRs = realm.where(PickR::class.java).equalTo(ARG_G_IDX, group.g_idx)
        if (room?.room_idx?: ARG_ALL_IDX != ARG_ALL_IDX)
            pickRs.equalTo(ARG_ROOM_IDX, room!!.room_idx)
        pickRs.findAll().forEach {
            val pick : Pick = it.toPick(realm)
            dataList.add(pick)
        }
        adapter.notifyDataSetChanged()
    }


}
