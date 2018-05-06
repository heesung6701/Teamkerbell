package org.teamfairy.sopt.teamkerbell.activities.items.signal

import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_signal_list.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.listview.adapter.ListDataAdapter
import org.teamfairy.sopt.teamkerbell.model.data.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.SignalR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_DETAIL_LIGHTS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_DETAIL_PARAM_GID
import org.teamfairy.sopt.teamkerbell.network.info.SignalListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_RESPONDED
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_SIGNAL
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.properties.Delegates

/**
 * A placeholder fragment containing a simple view.
 */
class SignalListFragment : Fragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectSignalList()
        mSwipeRefreshLayout.isRefreshing = false
    }

    var group: Team by Delegates.notNull()
    var state = Utils.SIGNAL_RECEIVE

    private var signalList: ArrayList<Signal> = arrayListOf<Signal>()

    private var dataList: ArrayList<ListDataInterface> = arrayListOf<ListDataInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()
    private var adapter: ListDataAdapter by Delegates.notNull()

    private var signFilter: Byte = ALL

    companion object {
        const val RED: Byte = 1
        const val YELLOW: Byte = 2
        const val GREEN: Byte = 4
        val ALL: Byte = RED or YELLOW or GREEN
        const val DEFAULT: Byte = 8
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_signal_list, container, false)


        recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        adapter = ListDataAdapter(dataList, activity.applicationContext)
        adapter.setOnItemClick(this)
        recyclerView.adapter = adapter


        val signBar = v.findViewById<LinearLayout>(R.id.sign_bar)

        if (state == Utils.SIGNAL_RECEIVE) {
            signBar.visibility = View.VISIBLE
            activeSignBar(v)
        } else signBar.visibility =  View.GONE

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
            signFilter = if (signFilter == RED) DEFAULT
            else RED
            updateColorFilter()
            updateList()
        }

        signYellow.setOnClickListener {
            signFilter = if (signFilter == YELLOW) DEFAULT
            else YELLOW
            updateColorFilter()
            updateList()

        }

        signGreen.setOnClickListener {
            signFilter = if (signFilter == GREEN) DEFAULT
            else GREEN

            updateColorFilter()
            updateList()
        }

    }

    private fun updateColorFilter() {

        if (signFilter and RED != RED) iv_focus_red.visibility = View.GONE else iv_focus_red.visibility = View.VISIBLE


        if (signFilter and YELLOW == YELLOW) iv_focus_yellow.visibility = View.VISIBLE else iv_focus_yellow.visibility = View.GONE

        if (signFilter and GREEN == GREEN) iv_focus_green.visibility = View.VISIBLE else iv_focus_green.visibility = View.GONE

        if (dataList.size > 0)
            recyclerView.scrollToPosition(0)

    }

    private fun updateList() {
        dataList.clear()
        signalList.forEach {
            val c: Byte = when (it.color) {
                "r" -> RED
                "g" -> GREEN
                "y" -> YELLOW
                "a" -> ALL
                else -> DEFAULT
            }
            if (c and signFilter != 0.toByte())
                dataList.add(it)
        }

        signalList.forEach {
            val c: Byte = when (it.color) {
                "r" -> RED
                "g" -> GREEN
                "y" -> YELLOW
                "a" -> ALL
                else -> DEFAULT
            }
            if (c and signFilter == 0.toByte())
                dataList.add(it)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        val signal: Signal? = dataList[pos] as Signal

        val intent = Intent(activity, SignalActivity::class.java)
        intent.putExtra(INTENT_SIGNAL, signal)
        intent.putExtra(INTENT_GROUP, group)
        intent.putExtra(INTENT_RESPONDED, (signal!!.color.equals("g") || state == Utils.SIGNAL_REQUEST))
        startActivity(intent)
    }


    private fun connectSignalList() {
        dataList.clear()
        adapter.notifyDataSetChanged()

        var url = ""

        val task = SignalListTask(activity.applicationContext, HandlerGet(this), LoginToken.getToken(activity.applicationContext))

        val builtUri = Uri.parse(URL_DETAIL_LIGHTS)
                .buildUpon()
                .appendQueryParameter(URL_DETAIL_PARAM_GID, group.g_idx.toString())
                .build()
        url = builtUri.toString()

        task.execute(url)
    }


    private fun getSignalListFromRealm() {

        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)


        var result: RealmResults<SignalR>? = null
        when (state) {
            Utils.SIGNAL_ALL ->
                result = realm.where(SignalR::class.java).equalTo("g_idx", group.g_idx).sort("write_time", Sort.DESCENDING).findAll()
            Utils.SIGNAL_RECEIVE ->
                result = realm.where(SignalR::class.java).equalTo("g_idx", group.g_idx).notEqualTo("u_idx", LoginToken.getUserIdx(activity.applicationContext)).sort("write_time", Sort.DESCENDING).findAll()
            Utils.SIGNAL_REQUEST ->
                result = realm.where(SignalR::class.java).equalTo("g_idx", group.g_idx).equalTo("u_idx", LoginToken.getUserIdx(activity.applicationContext)).sort("write_time", Sort.DESCENDING).findAll()
        }
        signalList.clear()
        result!!.iterator().forEach {
            signalList.add(it.toSignal(realm))
        }

        updateList()


    }

    private class HandlerGet(fragment: SignalListFragment) : Handler() {
        private val mFragment: WeakReference<SignalListFragment> = WeakReference<SignalListFragment>(fragment)

        override fun handleMessage(msg: Message) {
            mFragment.get()?.getSignalListFromRealm()
        }
    }


}
