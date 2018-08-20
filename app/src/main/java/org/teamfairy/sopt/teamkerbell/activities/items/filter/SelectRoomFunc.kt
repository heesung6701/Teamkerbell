package org.teamfairy.sopt.teamkerbell.activities.items.filter

import android.app.Activity
import android.support.v4.content.res.TypedArrayUtils.getText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.R.id.*
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.listview.adapter.TextListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ALL_IDX
import org.teamfairy.sopt.teamkerbell.model.interfaces.GroupInterface
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-05-19.
 */
class SelectRoomFunc(var activity: Activity) : View.OnClickListener {

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        changeRoom(dataList[pos] as Room)
    }

    private val mActivity: WeakReference<Activity> = WeakReference<Activity>(activity)


    private var adapter: TextListAdapter by Delegates.notNull()
    private var dataList = ArrayList<GroupInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()


    private var layoutSelectRoom: LinearLayout by Delegates.notNull()
    private var ivDropDown : ImageView by Delegates.notNull()
    private var tvRoomName : TextView by Delegates.notNull()

    init {
        if (mActivity.get() != null) {
            activity = mActivity.get()!!
            recyclerView = activity.findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            adapter = TextListAdapter(dataList, activity.applicationContext)
            adapter.setOnItemClickListener(this)


            ivDropDown = activity.findViewById(R.id.iv_drop_down)
            tvRoomName = activity.findViewById(R.id.tv_room_name)

            val group = (activity as RoomActivityInterface).group
            val room = (activity as RoomActivityInterface).room
            adapter.currentIdx = room?.room_idx ?: -1
            recyclerView.adapter = adapter

            if(room!=null && room.room_idx!=ARG_ALL_IDX)
                tvRoomName.text=room.real_name




            layoutSelectRoom = activity.findViewById(R.id.layout_select_room)
            layoutSelectRoom.setOnClickListener {
                if (recyclerView.visibility != View.VISIBLE) {
                    openRoomList()
                    DatabaseHelpUtils.getRoomListFromRealm(activity.applicationContext, dataList as ArrayList<Room>, adapter as RecyclerView.Adapter<*>, group)
                }
                else
                    closeRoomList()
            }
        }


    }

    private fun openRoomList() {

        if (recyclerView.visibility != View.VISIBLE) {
            recyclerView.visibility = View.VISIBLE
            ivDropDown.rotation = 180.0f

        }
    }

    private fun closeRoomList() {
        if (recyclerView.visibility != View.GONE) {
            recyclerView.visibility = View.GONE
            ivDropDown.rotation = 0.0f
        }

    }

    private fun changeRoom(room: Room) {

        adapter.currentIdx = room.room_idx
        tvRoomName.text=room.real_name

        if (mActivity.get() == null) return
        (mActivity.get() as RoomActivityInterface).changeRoom(room)
        closeRoomList()

    }

}