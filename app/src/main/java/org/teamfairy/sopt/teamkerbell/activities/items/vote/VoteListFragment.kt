package org.teamfairy.sopt.teamkerbell.activities.items.vote

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
import android.widget.TextView
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_vote_list.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter.VoteListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.*
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Team.Companion.ARG_G_IDX
import org.teamfairy.sopt.teamkerbell.model.data.User.Companion.ARG_U_IDX
import org.teamfairy.sopt.teamkerbell.model.realm.VoteR
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_GET
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_GROUP_VOTE_RECEIVER
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_GROUP_VOTE_SENDER
import org.teamfairy.sopt.teamkerbell.network.info.VoteListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_VOTE
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * A placeholder fragment containing a simple view.
 */
class VoteListFragment : Fragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, RoomActivityInterface {


    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectVoteList()
        mSwipeRefreshLayout.isRefreshing = false
    }

    override var group: Team by Delegates.notNull()
    override var room: Room? = null

    override fun changeRoom(room: Room) {
        this.room = room
        updateVoteList()
    }

    var state = Utils.VOTE_RECEIVER


    private var showFinished = false
    private var voteList: ArrayList<Vote> = arrayListOf<Vote>()
    private var tvShowFinished: TextView by Delegates.notNull()

    private var dataList: ArrayList<ListDataInterface> = arrayListOf<ListDataInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()
    private var adapter: VoteListAdapter by Delegates.notNull()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_vote_list, container, false)


        recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        adapter = VoteListAdapter(activity.applicationContext,dataList)
        adapter.setOnItemClick(this)
        recyclerView.adapter = adapter

        val divider = DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
        )
        divider.setDrawable(ContextCompat.getDrawable(activity.baseContext, R.drawable.shape_line_divider))
        recyclerView.addItemDecoration(divider)

        mSwipeRefreshLayout = v.findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)


        tvShowFinished=v.findViewById(R.id.tv_show_finished)
        tvShowFinished.setOnClickListener {
            showFinished = !showFinished
            updateVoteList()
        }

        return v
    }


    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        val i = Intent(activity.applicationContext, VoteActivity::class.java)
        i.putExtra(INTENT_VOTE, dataList[pos] as Vote)
        i.putExtra(INTENT_GROUP, group)
        startActivity(i)
    }

    private fun connectVoteList() {
        dataList.clear()
        adapter.notifyDataSetChanged()

        val task: NetworkTask = VoteListTask(activity.applicationContext, HandlerGet(this), LoginToken.getToken(activity.applicationContext))

        val url = if (state == Utils.VOTE_SENDER) URL_GROUP_VOTE_SENDER else URL_GROUP_VOTE_RECEIVER
        task.execute("$url/${group.g_idx}", METHOD_GET)
    }

    private fun updateVoteList() {
        dataList.clear()
        voteList.forEach {
            if (it.isFinished() == showFinished) {
                if (it.room_idx == room?.room_idx ?: it.room_idx)
                    dataList.add(it)
            }
        }
        adapter.notifyDataSetChanged()
        tvShowFinished.text = if (!showFinished) getString(R.string.action_show_finished)+"(${voteList.size-dataList.size})" else getString(R.string.action_show_not_finished)+"(${voteList.size-dataList.size})"
    }


    override fun onResume() {
        super.onResume()
        connectVoteList()
    }

    fun getVoteListFromRealm() {
        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)


        var result: RealmResults<VoteR>? = null
        when (state) {
            Utils.VOTE_ALL ->
                result = realm.where(VoteR::class.java).equalTo(ARG_G_IDX, group.g_idx).sort(Vote.ARG_WRITETIME, Sort.DESCENDING).findAll()
            Utils.VOTE_RECEIVER ->
                result = realm.where(VoteR::class.java).equalTo(ARG_G_IDX, group.g_idx).notEqualTo(ARG_U_IDX, LoginToken.getUserIdx(activity.applicationContext)).sort("write_time", Sort.DESCENDING).findAll()
            Utils.VOTE_SENDER ->
                result = realm.where(VoteR::class.java).equalTo(ARG_G_IDX, group.g_idx).equalTo(ARG_U_IDX, LoginToken.getUserIdx(activity.applicationContext)).sort("write_time", Sort.DESCENDING).findAll()
        }
        voteList.clear()
        result!!.iterator().forEach {
            voteList.add(it.toVote(realm))
        }

        updateVoteList()
    }


    fun getVoteList(result: ArrayList<Vote>) {

        voteList.clear()

        when (state) {
            Utils.VOTE_ALL ->
                result.forEach {
                    it.setGroupInfo(activity.applicationContext)
                    it.setPhotoInfo(activity.applicationContext)
                    voteList.add(it)
                }
            Utils.VOTE_RECEIVER ->
                result.filter { it.u_idx != LoginToken.getUserIdx(activity.applicationContext) }.forEach {

                    it.setGroupInfo(activity.applicationContext)
                    it.setPhotoInfo(activity.applicationContext)
                    voteList.add((it))
                }
            Utils.VOTE_SENDER ->
                result.filter { it.u_idx == LoginToken.getUserIdx(activity.applicationContext) }.forEach {

                    it.setGroupInfo(activity.applicationContext)
                    it.setPhotoInfo(activity.applicationContext)
                    voteList.add((it))
                }
        }
        updateVoteList()
    }

    private class HandlerGet(fragment: VoteListFragment) : Handler() {
        private val mFragment: WeakReference<VoteListFragment> = WeakReference<VoteListFragment>(fragment)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Utils.MSG_SUCCESS -> {
                    val fragment = mFragment.get()
                    if (fragment == null || fragment.activity == null) return
                    mFragment.get()?.getVoteList(msg.obj as ArrayList<Vote>)
//                    mFragment.get()?.getVoteListFromRealm()

                }
                else -> {

                }

            }
        }
    }


}
