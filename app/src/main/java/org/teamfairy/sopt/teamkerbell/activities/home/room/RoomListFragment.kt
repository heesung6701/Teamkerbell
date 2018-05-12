package org.teamfairy.sopt.teamkerbell.activities.home.room


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import org.json.JSONObject

import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell._utils.StatusCode
import org.teamfairy.sopt.teamkerbell.activities.home.interfaces.HasGroupFragment
import org.teamfairy.sopt.teamkerbell.activities.home.interfaces.HasGroupFragment.Companion.ARG_GROUP
import org.teamfairy.sopt.teamkerbell.activities.home.room.adapter.RoomListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedRoomR
import org.teamfairy.sopt.teamkerbell.model.realm.RoomR
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LEAVE_ROOM_PARAM_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.io.File
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass.
 * Use the [RoomListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoomListFragment : Fragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, HasGroupFragment {

    private val TAG = this::class.java.simpleName

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()

    override fun onRefresh() {
        NetworkUtils.connectRoomList(activity.applicationContext, null, true)
        NetworkUtils.connectJoinedRoomList(activity.applicationContext, null, true)
        mSwipeRefreshLayout.isRefreshing = false
    }


    override var group: Team by Delegates.notNull()

    var adapter: RoomListAdapter by Delegates.notNull()
    var dataList: ArrayList<Room> = arrayListOf<Room>()
    var recyclerView: RecyclerView by Delegates.notNull()

    var file: File? = null

    var isUpdateJoined: IsUpdateR? = null


    var fab : FloatingActionButton by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        group = arguments.getParcelable(ARG_GROUP)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_room_list, container, false)

        recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        dataList = arrayListOf<Room>()
        adapter = RoomListAdapter(dataList, activity.applicationContext)
        adapter.setOnItemClickListener(this)
        adapter.setOnLongClickHandler(HandlerDelete(this))
        recyclerView.adapter = adapter

        fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val i = Intent(activity.applicationContext, MakeRoomActivity::class.java)
            i.putExtra(IntentTag.INTENT_GROUP, group)
            startActivity(i)
        }


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility == View.GONE) {
                    fab.show()
                }
            }
        })


        mSwipeRefreshLayout = v.findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        updateRoomList()
        return v
    }

    override fun onResume() {
        super.onResume()

        NetworkUtils.connectRoomList(activity.applicationContext, HandlerGet(this))
        NetworkUtils.connectJoinedRoomList(activity.applicationContext, HandlerGet(this))

        updateRoomList()
        addChangeJoinedRoomListener()
    }

    override fun onStop() {
        super.onStop()
        isUpdateJoined?.removeAllChangeListeners()
    }


    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
    }

    private fun addChangeJoinedRoomListener() {

        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
        isUpdateJoined = realm.where(IsUpdateR::class.java).equalTo("what", StatusCode.joinedRoomChange).findFirst()
        if (isUpdateJoined == null) {
            realm.beginTransaction()
            isUpdateJoined = realm.createObject(IsUpdateR::class.java, StatusCode.joinedRoomChange)
            isUpdateJoined!!.isUpdate = false
            realm.commitTransaction()
        } else {
            if (isUpdateJoined?.isUpdate == true) {
                realm.beginTransaction()
                updateRoomList()
                isUpdateJoined!!.isUpdate = false
                realm.commitTransaction()
            }
        }
        isUpdateJoined!!.addChangeListener<IsUpdateR> { t: IsUpdateR, _ ->
            if (t.isUpdate) {
                Log.d("$TAG /isUpdateJoinedRoom", "is ${t.isUpdate}")

                updateRoomList()
                realm.executeTransaction {
                    t.isUpdate = false
                }
            }
        }

    }

    private fun deleteRoom(room: Room) {
        val task = GetMessageTask(activity.applicationContext, HandlerDeleteSuccess(this, room), LoginToken.getToken(activity.applicationContext))

        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_LEAVE_ROOM_PARAM_ROOM_IDX, room.room_idx)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        task.execute(USGS_REQUEST_URL.URL_LEAVE_ROOM, jsonParam.toString())
    }

    private fun updateRoomList() {

        val realm = getRealmDefault(activity.applicationContext)

        dataList.clear()
        adapter.notifyDataSetChanged()
        var i = 0
        val groupR = realm.where(JoinedRoomR::class.java).equalTo(Team.ARG_G_IDX, group.g_idx).findAll()
        groupR.forEach {
            val roomR = realm.where(RoomR::class.java).equalTo(Room.ARG_ROOM_IDX, it.room_idx).findFirst()
                    ?: RoomR()

            dataList.add(roomR.toChatRoom())
//            updateRecentMessage(it.toChatRoom(), i)
            i++
        }
        adapter.notifyDataSetChanged()
    }

    private fun connectRoomList(b: Boolean) {
        NetworkUtils.connectRoomList(activity.applicationContext, HandlerGet(this), b)
    }

    fun successDelete(room: Room) {
        Toast.makeText(activity.applicationContext, "채팅방이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
        realm.executeTransactionAsync {
            it.where(JoinedRoomR::class.java).equalTo(Team.ARG_G_IDX, group.g_idx).equalTo(Room.ARG_ROOM_IDX, room.room_idx).findFirst()?.deleteFromRealm()
        }
        connectRoomList(true)
    }

    private class HandlerGet(fragment: RoomListFragment) : Handler() {
        private val mFragment: WeakReference<RoomListFragment> = WeakReference<RoomListFragment>(fragment)

        override fun handleMessage(msg: Message) {
            val fragment = mFragment.get()
            if (fragment != null) {
                fragment.updateRoomList()
            }
        }
    }

    private class HandlerDelete(fragment: RoomListFragment) : Handler() {
        private val mFragment: WeakReference<RoomListFragment> = WeakReference<RoomListFragment>(fragment)

        override fun handleMessage(msg: Message) {
            val fragment = mFragment.get()
            if (fragment != null) {
                fragment.deleteRoom(fragment.dataList[msg.what])
            }
        }
    }

    private class HandlerDeleteSuccess(fragment: RoomListFragment, var room: Room) : Handler() {
        private val mFragment: WeakReference<RoomListFragment> = WeakReference<RoomListFragment>(fragment)

        override fun handleMessage(msg: Message) {
            val fragment = mFragment.get()
            if (fragment != null) {
                val activity = fragment.activity
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        fragment.successDelete(room)
                    }
                    else -> {
                        Toast.makeText(activity.applicationContext, "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun changeGroup(g: Team) {
        group = g
        updateRoomList()

    }


    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param group
         * @return A new instance of fragment RoomListFragment.
         */
        fun newInstance(group: Team): RoomListFragment {
            val fragment = RoomListFragment()
            val args = Bundle()
            args.putParcelable(ARG_GROUP, group)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
