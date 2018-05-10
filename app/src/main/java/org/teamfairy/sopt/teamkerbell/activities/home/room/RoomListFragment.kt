package org.teamfairy.sopt.teamkerbell.activities.home.room


import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_G_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import java.io.File
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass.
 * Use the [RoomListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoomListFragment : Fragment(),View.OnClickListener ,SwipeRefreshLayout.OnRefreshListener, HasGroupFragment{

    private val TAG = this::class.java.simpleName

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()

    override fun onRefresh() {
        NetworkUtils.connectRoomList(activity.applicationContext,null,true)
        NetworkUtils.connectJoinedRoomList(activity.applicationContext,null,true)
        mSwipeRefreshLayout.isRefreshing = false
    }


    override  var group: Team by Delegates.notNull()

    var adapter: RoomListAdapter by Delegates.notNull()
    var dataList: ArrayList<Room> = arrayListOf<Room>()
    var recyclerView : RecyclerView by Delegates.notNull()

    var file: File? = null

    var isUpdateJoined: IsUpdateR? = null

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
        recyclerView.adapter = adapter

        val fab = v.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val i = Intent(activity.applicationContext,MakeRoomActivity::class.java)
            i.putExtra(IntentTag.INTENT_GROUP,group)
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
        updateRoomList()
        addChangeJoinedListener()
    }

    override fun onStop() {
        super.onStop()
        isUpdateJoined?.removeAllChangeListeners()
    }

    private fun updateRoomList() {

        val realm = getRealmDefault(activity.applicationContext)

        dataList.clear()
        adapter.notifyDataSetChanged()
        var i = 0
        val groupR = realm.where(JoinedRoomR::class.java).equalTo(JSON_G_IDX,group.g_idx).findAll()
        groupR.forEach {
            val roomR = realm.where(RoomR::class.java).equalTo(JSON_ROOM_IDX,it.room_idx).findFirst()?:RoomR()
            dataList.add(roomR.toChatRoom())
//            updateRecentMessage(it.toChatRoom(), i)
            i++
        }
        adapter.notifyDataSetChanged()
    }

    override fun onClick(p0: View?) {
        val pos= recyclerView.getChildAdapterPosition(p0)
    }

    private fun addChangeJoinedListener() {

        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
        isUpdateJoined = realm.where(IsUpdateR::class.java).equalTo("what", StatusCode.joinedGroupChange).findFirst()
        if (isUpdateJoined == null) {
            realm.beginTransaction()
            isUpdateJoined = realm.createObject(IsUpdateR::class.java, StatusCode.joinedGroupChange)
            isUpdateJoined!!.isUpdate = false
            realm.commitTransaction()
        }
        isUpdateJoined!!.addChangeListener<IsUpdateR> { t: IsUpdateR, _ ->
            if (t.isUpdate) {
                Log.d("$TAG /isUpdateJoined","is ${t.isUpdate}")
                updateRoomList()
                realm.executeTransaction {
                    t.isUpdate = false
                }
            }
        }

    }

    override fun changeGroup(g: Team) {
        group=g
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
