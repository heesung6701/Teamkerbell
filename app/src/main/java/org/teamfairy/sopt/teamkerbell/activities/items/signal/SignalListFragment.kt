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
import android.widget.ImageView
import android.widget.LinearLayout
import io.realm.RealmResults
import io.realm.Sort
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell._utils.TagUtils
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
        const val DEFAULT: Byte = 0
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
        } else View.GONE

        mSwipeRefreshLayout = v.findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        connectSignalList()

        return v
    }

    private fun activeSignBar(v: View) {
        val signRed = v.findViewById<ImageView>(R.id.iv_sign_red)
        val signYellow = v.findViewById<ImageView>(R.id.iv_sign_yellow)
        val signGreen = v.findViewById<ImageView>(R.id.iv_sign_green)

        signRed.setOnClickListener {
            signFilter = signFilter xor RED
            if (signFilter and RED == RED)
                signRed.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.red))
            else
                signRed.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.grey))
            updateList()
        }

        signYellow.setOnClickListener {
            signFilter = signFilter xor YELLOW
            if (signFilter and YELLOW == YELLOW)
                signYellow.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.yellow))
            else
                signYellow.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.grey))
            updateList()

        }

        signGreen.setOnClickListener {
            signFilter = signFilter xor GREEN
            if (signFilter and GREEN == GREEN)
                signGreen.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.green))
            else
                signGreen.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.grey))

            updateList()
        }

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
