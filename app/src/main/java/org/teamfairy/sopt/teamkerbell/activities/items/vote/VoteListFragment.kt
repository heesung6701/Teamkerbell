package org.teamfairy.sopt.teamkerbell.activities.items.vote

import android.content.Intent
import android.net.Uri
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
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_vote_list.view.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter.VoteListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.Vote
import org.teamfairy.sopt.teamkerbell.model.realm.VoteR
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_DETAIL_PARAM_GID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_DETAIL_VOTE
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
class VoteListFragment : Fragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectVoteList()
        mSwipeRefreshLayout.isRefreshing = false
    }

    var group: Team by Delegates.notNull()
    var state = Utils.VOTE_RECEIVE


    private var showFinished = false
    private var voteList: ArrayList<Vote> = arrayListOf<Vote>()

    private var dataList: ArrayList<ListDataInterface> = arrayListOf<ListDataInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()
    private var adapter: VoteListAdapter by Delegates.notNull()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_vote_list, container, false)


        recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        adapter = VoteListAdapter(dataList)
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


        connectVoteList()

        v.tv_show_finished.setOnClickListener {
            showFinished=!showFinished
            v.tv_show_finished.text = if(!showFinished) getString(R.string.action_show_finished) else getString(R.string.action_show_not_finished)
            updateVoteList()
        }

        return v
    }


    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        val i = Intent(activity.applicationContext,VoteActivity::class.java)
        i.putExtra(INTENT_VOTE,dataList[pos] as Vote)
        i.putExtra(INTENT_GROUP,group)
        startActivity(i)
    }

    private fun connectVoteList() {

        dataList.clear()
        adapter.notifyDataSetChanged()

        val task: NetworkTask = VoteListTask(activity.applicationContext, HandlerGet(this), LoginToken.getToken(activity.applicationContext))

        task.execute("$URL_DETAIL_VOTE/${group.g_idx}")
    }

    private fun updateVoteList(){
        dataList.clear()
        voteList.forEach {
            if(it.isFinished() == showFinished)
                dataList.add(it)
        }
        adapter.notifyDataSetChanged()
    }
    fun getVoteListFromRealm() {
        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)


        var result: RealmResults<VoteR>? = null
        when (state) {
            Utils.VOTE_ALL ->
                result = realm.where(VoteR::class.java).equalTo("g_idx", group.g_idx).sort("write_time", Sort.DESCENDING).findAll()
            Utils.VOTE_RECEIVE ->
                result = realm.where(VoteR::class.java).equalTo("g_idx", group.g_idx).notEqualTo("u_idx", LoginToken.getUserIdx(activity.applicationContext)).sort("write_time", Sort.DESCENDING).findAll()
            Utils.VOTE_REQUEST ->
                result = realm.where(VoteR::class.java).equalTo("g_idx", group.g_idx).equalTo("u_idx", LoginToken.getUserIdx(activity.applicationContext)).sort("write_time", Sort.DESCENDING).findAll()
        }
        voteList.clear()
        result!!.iterator().forEach {
            voteList.add(it.toVote(realm))
        }

        updateVoteList()
    }


    private class HandlerGet(fragment: VoteListFragment) : Handler() {
        private val mFragment: WeakReference<VoteListFragment> = WeakReference<VoteListFragment>(fragment)

        override fun handleMessage(msg: Message) {
            val fragment = mFragment.get()
            if (fragment != null) {
                fragment.getVoteListFromRealm()
            }
        }
    }


}
