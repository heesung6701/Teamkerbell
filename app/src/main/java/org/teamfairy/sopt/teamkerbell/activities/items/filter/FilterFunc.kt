package org.teamfairy.sopt.teamkerbell.activities.items.filter

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
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
class FilterFunc(activity: Activity) {

    private var adapter: TextListAdapter by Delegates.notNull()
    var dataList: ArrayList<GroupInterface> = arrayListOf<GroupInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()
    private var btnFilter: ImageButton by Delegates.notNull()

    private val mActivity: WeakReference<Activity> = WeakReference<Activity>(activity)

    init {
        if (mActivity.get() != null) {
            val activity = mActivity.get()!!
            recyclerView = activity.findViewById(R.id.recyclerView_room)

            recyclerView.layoutManager = LinearLayoutManager(activity.applicationContext)
            adapter = TextListAdapter(dataList, activity.applicationContext)
            adapter.setOnItemClickListener(View.OnClickListener { p0 ->
                val pos = recyclerView.getChildAdapterPosition(p0)
                changeRoom(dataList[pos] as Room)
                closeFilter(activity.applicationContext)
            })
            val room = (activity as RoomActivityInterface).room
            adapter.currentIdx = room?.room_idx?: ARG_ALL_IDX
            recyclerView.adapter = adapter

            val group = (activity as RoomActivityInterface).group

            btnFilter=activity.findViewById(R.id.btn_filter)
            btnFilter.setOnClickListener {
                if (recyclerView.visibility == View.VISIBLE) closeFilter(activity.applicationContext)
                else {
                    DatabaseHelpUtils.getRoomListFromRealm(activity.applicationContext, dataList as ArrayList<Room>, adapter as RecyclerView.Adapter<*>, group,true)
                    openFilter(activity.applicationContext)
                }
            }
        }

    }

    private fun openFilter(applicationContext: Context) {
        btnFilter.setColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColor))
        recyclerView.visibility = View.VISIBLE
    }

    private fun closeFilter(applicationContext: Context) {
        btnFilter.setColorFilter(ContextCompat.getColor(applicationContext, R.color.black))
        recyclerView.visibility = View.GONE
    }

    private fun changeRoom(room: Room) {

        adapter.currentIdx = room.room_idx
        if (mActivity.get() == null) return
        (mActivity.get() as RoomActivityInterface).changeRoom(room)

    }

}