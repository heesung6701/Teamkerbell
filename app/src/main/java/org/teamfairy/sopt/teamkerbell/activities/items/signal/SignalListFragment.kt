package org.teamfairy.sopt.teamkerbell.activities.items.signal

import android.content.Intent
import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.fragment_signal_list.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.listview.adapter.ListDataAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_GROUP_LIGHT_RECEIVER
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_GROUP_LIGHT_SENDER
import org.teamfairy.sopt.teamkerbell.network.info.SignalListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_RESPONDED
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_SIGNAL
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.properties.Delegates

/**
 * A placeholder fragment containing a simple view.
 */
class SignalListFragment : Fragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, RoomActivityInterface {

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectSignalList()
        mSwipeRefreshLayout.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        connectSignalList()
    }

    override var group: Team by Delegates.notNull()
    override var room: Room? = null

    override fun changeRoom(room: Room) {
        this.room = room
        updateList()
    }


    var state = Utils.SIGNAL_RECEIVER

    private var signalList: ArrayList<Signal> = arrayListOf<Signal>()

    private var dataList: ArrayList<ListDataInterface> = arrayListOf<ListDataInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()
    private var adapter: ListDataAdapter by Delegates.notNull()

    private var signFilter: Byte = Signal.ALL



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_signal_list, container, false)

        recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        adapter = ListDataAdapter(dataList, activity.applicationContext)
        adapter.setOnItemClick(this)
        recyclerView.adapter = adapter

        val divider = DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
        )
        divider.setDrawable(ContextCompat.getDrawable(activity.baseContext, R.drawable.shape_line_divider))
        recyclerView.addItemDecoration(divider)


        val signBar = v.findViewById<LinearLayout>(R.id.sign_bar)

        if (state == Utils.SIGNAL_RECEIVER) {
            signBar.visibility = View.VISIBLE
            activeSignBar(v)
        } else signBar.visibility = View.GONE

        mSwipeRefreshLayout = v.findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        connectSignalList()



        return v
    }

    private fun activeSignBar(v: View) {

        val signRed: RelativeLayout = v.findViewById(R.id.btn_red)
        val signYellow: RelativeLayout = v.findViewById(R.id.btn_yellow)
        val signGreen: RelativeLayout = v.findViewById(R.id.btn_green)

        signRed.setOnClickListener {
            signFilter = if (signFilter == Signal.RED) Signal.DEFAULT
            else Signal.RED
            updateColorFilter(iv_focus_red, iv_focus_yellow, iv_focus_green)
            updateList()
        }

        signYellow.setOnClickListener {
            signFilter = if (signFilter == Signal.YELLOW) Signal.DEFAULT
            else Signal.YELLOW
            updateColorFilter(iv_focus_red, iv_focus_yellow, iv_focus_green)
            updateList()

        }

        signGreen.setOnClickListener {
            signFilter = if (signFilter == Signal.GREEN) Signal.DEFAULT
            else Signal.GREEN

            updateColorFilter(iv_focus_red, iv_focus_yellow, iv_focus_green)
            updateList()
        }

    }

    private fun updateColorFilter(ivFocusRed: ImageView, ivFocusYellow: ImageView, ivFocusGreen: ImageView) {

        if (signFilter and Signal.RED != Signal.RED) ivFocusRed.visibility = View.GONE else ivFocusRed.visibility = View.VISIBLE

        if (signFilter and Signal.YELLOW == Signal.YELLOW) ivFocusYellow.visibility = View.VISIBLE else ivFocusYellow.visibility = View.GONE

        if (signFilter and Signal.GREEN == Signal.GREEN) ivFocusGreen.visibility = View.VISIBLE else ivFocusGreen.visibility = View.GONE

        if (dataList.size > 0)
            recyclerView.scrollToPosition(0)

    }

    private fun updateList() {
        dataList.clear()
        signalList.forEach {
            val c: Byte = Signal.colorStrToByte(it.responseColor)

            if (c and signFilter != 0.toByte()) {
                if(it.room_idx==room?.room_idx ?: it.room_idx || room?.room_idx==Room.ARG_ALL_IDX)
                    dataList.add(it)
            }
        }

        signalList.forEach {
            val c: Byte = Signal.colorStrToByte(it.responseColor)

            if (c and signFilter == 0.toByte()) {
                if(it.room_idx==room?.room_idx ?: it.room_idx || room?.room_idx==Room.ARG_ALL_IDX)
                    dataList.add(it)
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        val signal: Signal? = dataList[pos] as Signal

        val intent = Intent(activity, SignalActivity::class.java)
        intent.putExtra(INTENT_SIGNAL, signal)
        intent.putExtra(INTENT_GROUP, group)
        intent.putExtra(INTENT_ROOM, room)
        intent.putExtra(INTENT_RESPONDED, (signal!!.responseColor.equals("g") || state == Utils.SIGNAL_SENDER))
        startActivity(intent)
    }


    private fun connectSignalList() {
        dataList.clear()
        adapter.notifyDataSetChanged()

        var url = ""

        val task = SignalListTask(activity.applicationContext, HandlerGet(this), LoginToken.getToken(activity.applicationContext))
        url = if (state == Utils.SIGNAL_SENDER) URL_GROUP_LIGHT_SENDER else URL_GROUP_LIGHT_RECEIVER
        task.execute("$url/${group.g_idx}")
    }

    fun getSignalList(result: ArrayList<Signal>) {
        signalList.clear()
        result.iterator().forEach {
            when (state) {
                Utils.SIGNAL_ALL -> {
                    it.setPhotoInfo(activity.applicationContext)
                    it.setGroupInfo(activity.applicationContext)
                    signalList.add(it)
                }
                Utils.SIGNAL_RECEIVER -> {
                    it.setPhotoInfo(activity.applicationContext)
                    it.setGroupInfo(activity.applicationContext)
                    if (it.u_idx != LoginToken.getUserIdx(activity.applicationContext))
                        signalList.add(it)
                }
                Utils.SIGNAL_SENDER -> {
                    if (it.u_idx == LoginToken.getUserIdx(activity.applicationContext)) {
                        it.setPhotoInfo(activity.applicationContext)
                        it.setGroupInfo(activity.applicationContext)
                        signalList.add(it)
                    }
                }
            }
        }
        updateList()
    }

    private class HandlerGet(fragment: SignalListFragment) : Handler() {
        private val mFragment: WeakReference<SignalListFragment> = WeakReference<SignalListFragment>(fragment)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Utils.MSG_SUCCESS -> {
                    val fragment = mFragment.get()
                    if (fragment == null || fragment.activity == null) return
                    mFragment.get()?.getSignalList(msg.obj as ArrayList<Signal>)
                }
                else -> {
                }
            }
        }
    }
}