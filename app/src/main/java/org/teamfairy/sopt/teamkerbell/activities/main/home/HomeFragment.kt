package org.teamfairy.sopt.teamkerbell.activities.main.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_home.view.*

import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.group.invite.InviteActivity
import org.teamfairy.sopt.teamkerbell.activities.main.MainActivity
import org.teamfairy.sopt.teamkerbell.activities.main.interfaces.HasGroupFragment
import org.teamfairy.sopt.teamkerbell.activities.items.notice.NoticeCardActivity
import org.teamfairy.sopt.teamkerbell.activities.items.pick.PickListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.role.RoleListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.VoteListActivity
import org.teamfairy.sopt.teamkerbell.activities.main.dialog.ShowUserDialog
import org.teamfairy.sopt.teamkerbell.activities.setting.SettingActivity
import org.teamfairy.sopt.teamkerbell.listview.adapter.TextListAdapter
import org.teamfairy.sopt.teamkerbell.model.interfaces.GroupInterface
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedGroupR
import org.teamfairy.sopt.teamkerbell.model.realm.BadgeCnt
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import kotlin.properties.Delegates


class HomeFragment : Fragment(), View.OnClickListener, HasGroupFragment {

    val TAG = this::class.java.simpleName

    private var adapterGroup: TextListAdapter by Delegates.notNull()
    private var dataListGroup = ArrayList<GroupInterface>()
    private var recyclerView: RecyclerView by Delegates.notNull()


    override var group: Team by Delegates.notNull()


    private var ivDropDown: ImageView by Delegates.notNull()
    private var tvTeamName: TextView by Delegates.notNull()
    var tvCount: TextView by Delegates.notNull()


    private var badgeNotice : TextView by Delegates.notNull()
    private var badgeSignal : TextView by Delegates.notNull()
    private var badgeVote : TextView by Delegates.notNull()
    private var badgeRole : TextView by Delegates.notNull()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        tvTeamName = v.findViewById(R.id.tv_teamName)
        tvCount = v.findViewById(R.id.tv_count)
        ivDropDown = v.findViewById(R.id.iv_drop_down)

        showGroupInfo()

        badgeNotice=v.tv_notice_sign
        badgeSignal=v.tv_signal_sign
        badgeVote=v.tv_vote_sign
        badgeRole=v.tv_role_sign

        v.btn_notice.setOnClickListener { _ ->
            activity?.let {
                BadgeCnt.clear(it.applicationContext,BadgeCnt.WHAT_NOTICE,group.g_idx)
                val i = Intent(it.applicationContext, NoticeCardActivity::class.java)
                i.putExtra(INTENT_GROUP, group)
                startActivity(i)
            }

        }

        v.btn_pick.setOnClickListener { _ ->
            activity?.let {
                val i = Intent(it.applicationContext, PickListActivity::class.java)
                i.putExtra(INTENT_GROUP, group)
                startActivity(i)
            }
        }

        v.btn_signal.setOnClickListener { _ ->
            activity?.let {
                BadgeCnt.clear(it.applicationContext, BadgeCnt.WHAT_SIGNAL, group.g_idx)
                val i = Intent(it.applicationContext, SignalListActivity::class.java)
                i.putExtra(INTENT_GROUP, group)
                startActivity(i)
            }
        }


        v.btn_vote.setOnClickListener { _ ->
            activity?.let {
                BadgeCnt.clear(it.applicationContext, BadgeCnt.WHAT_VOTE, group.g_idx)
                val i = Intent(it.applicationContext, VoteListActivity::class.java)
                i.putExtra(INTENT_GROUP, group)
                startActivity(i)
            }
        }

        v.btn_role.setOnClickListener { _ ->
            activity?.let {
                BadgeCnt.clear(it.applicationContext, BadgeCnt.WHAT_ROLE, group.g_idx)
                val i = Intent(it.applicationContext, RoleListActivity::class.java)
                i.putExtra(INTENT_GROUP, group)
                startActivity(i)
            }
        }
        v.btn_setting.setOnClickListener { _ ->
            activity?.let {
                val i = Intent(it.applicationContext, SettingActivity::class.java)
                i.putExtra(INTENT_GROUP, group)
                startActivity(i)
            }

        }

        adapterGroup = TextListAdapter(dataListGroup, activity!!.applicationContext)
        adapterGroup.setOnItemClickListener(this)
        adapterGroup.currentIdx = group.g_idx

        recyclerView = v.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapterGroup

        addChangeBadgeCntListener()



        v.layout_select_team.setOnClickListener {
            if (recyclerView.visibility == View.VISIBLE) closeGroupList()
            else openGroupList()
        }
        v.tv_count.setOnClickListener {
            showUserDialog()
        }
        v.layout_parent.setOnClickListener {
            closeGroupList()
        }


        return v
    }

    private fun showGroupInfo() {
        activity?.let {
            val realm = DatabaseHelpUtils.getRealmDefault(it.applicationContext)
            tvTeamName.text = group.real_name
            tvCount.text = ("${realm.where(JoinedGroupR::class.java).equalTo(Team.ARG_G_IDX, group.g_idx).findAll().size}명")
            realm.close()
        }
    }


    override fun changeGroup(g: Team) {
        group = g
        showGroupInfo()

        adapterGroup.currentIdx = group.g_idx
        adapterGroup.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        showGroupInfo()
    }

    private fun showUserDialog() {

        val dialog = ShowUserDialog(context, group)
        dialog.show()

        dialog.setOnClickListener(View.OnClickListener { p0 ->
            when (p0.id) {
                R.id.btn_add -> {
                    activity?.let{
                        val i = Intent(it.applicationContext, InviteActivity::class.java)
                        i.putExtra(INTENT_GROUP, group)
                        startActivity(i)
                        dialog.dismiss()
                    }
                }
            }
        })
    }


    private fun closeGroupList() {
        if (recyclerView.visibility != View.GONE) {
            recyclerView.visibility = View.GONE
            ivDropDown.rotation = 0.0f
        }

    }

    private fun openGroupList() {

        if (recyclerView.visibility != View.VISIBLE) {
            recyclerView.visibility = View.VISIBLE
            ivDropDown.rotation = 180.0f


            activity?.let {
                DatabaseHelpUtils.getGroupListFromRealm(it.applicationContext, dataListGroup as ArrayList<Team>, adapterGroup as RecyclerView.Adapter<*>, group)
            }
        }

    }

    private fun changeGroup() {

        activity?.let {
        val realm = DatabaseHelpUtils.getRealmDefault(it.applicationContext)
        tvTeamName.text = group.real_name
        tvCount.text = ("${realm.where(JoinedGroupR::class.java).equalTo(Team.ARG_G_IDX, group.g_idx).findAll().size}명")
        realm.close()

        adapterGroup.currentIdx = group.g_idx
        adapterGroup.notifyDataSetChanged()

        val activity = activity as MainActivity
        activity.changeGroup(group)


        removeBadgeCntListener()
        addChangeBadgeCntListener()
        }
    }


    override fun onClick(p0: View) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        group = dataListGroup[pos] as Team
        changeGroup()
        closeGroupList()
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)


        activity?.let {
            NetworkUtils.connectGroupList(it.applicationContext, null)
            NetworkUtils.connectUserList(it.applicationContext, null)
        }
    }

    private var badgeCnts = ArrayList<BadgeCnt>()
    private fun addChangeBadgeCntListener(){
        activity?.let {
        val realm = DatabaseHelpUtils.getRealmDefault(it.applicationContext)

        removeBadgeCntListener()
        Log.d("$TAG /BadgeCnt", "g_idx: ${group.g_idx} add listener")
        for(key in BadgeCnt.WHAT_NOTICE..BadgeCnt.WHAT_ROLE){
            var badgeCnt = realm.where(BadgeCnt::class.java).equalTo(BadgeCnt.ARG_WHAT, key).equalTo(BadgeCnt.ARG_G_IDX,group.g_idx).findFirst()
            if(badgeCnt==null){
                Log.d("$TAG /BadgeCnt ", "[key:$key,g_idx: ${group.g_idx}] create")
                realm.beginTransaction()
                badgeCnt= realm.createObject(BadgeCnt::class.java)
                badgeCnt!!.what=key
                badgeCnt.g_idx=group.g_idx
                realm.commitTransaction()
            }

            Log.d("$TAG /BadgeCnt", "g_idx: ${group.g_idx} ${badgeCnt.cnt}")
            setBadgeCnt(key,badgeCnt.cnt)

            badgeCnts.add(badgeCnt)
            badgeCnt.addChangeListener<BadgeCnt> { t: BadgeCnt ->
                Log.d("$TAG /BadgeCnt", "[key:${t.what},g_idx: ${t.g_idx}] is ${t.cnt}")
                setBadgeCnt(t.what,t.cnt)
            }
        }
        }
    }
    private fun removeBadgeCntListener(){
        if(badgeCnts.size>0)
            Log.d("$TAG /BadgeCnt", "g_idx: ${group.g_idx} remove listener")
        badgeCnts.forEach {
            it.removeAllChangeListeners()
        }
    }

    private fun setBadgeCnt(key : Int, value : Int){
        when(key){
            BadgeCnt.WHAT_NOTICE->{
                badgeNotice.text=when(value){
                    in Int.MIN_VALUE..0 -> ""
                    in 999 downTo 0 -> value.toString()
                    else-> "999+"
                }
                badgeNotice.visibility=if(value>0) View.VISIBLE else View.INVISIBLE
            }
            BadgeCnt.WHAT_SIGNAL->{
                badgeSignal.text=when(value){
                    in Int.MIN_VALUE..0 -> ""
                    in 999 downTo 0 -> value.toString()
                    else-> "999+"
                }
                badgeSignal.visibility=if(value>0) View.VISIBLE else View.INVISIBLE


            }
            BadgeCnt.WHAT_VOTE->{

                badgeVote.text=when(value){
                    in Int.MIN_VALUE..0 -> ""
                    in 999 downTo 0 -> value.toString()
                    else-> "999+"
                }
                badgeVote.visibility=if(value>0) View.VISIBLE else View.INVISIBLE

            }
            BadgeCnt.WHAT_ROLE->{
                badgeRole.text=when(value){
                    in Int.MIN_VALUE..0 -> ""
                    in 999 downTo 0 -> value.toString()
                    else-> "999+"
                }
                badgeRole.visibility=if(value>0) View.VISIBLE else View.INVISIBLE

            }
        }

    }

}
