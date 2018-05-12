package org.teamfairy.sopt.teamkerbell.activities.home.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_home.view.*

import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.group.invite.InviteActivity
import org.teamfairy.sopt.teamkerbell.activities.home.HomeActivity
import org.teamfairy.sopt.teamkerbell.activities.home.interfaces.HasGroupFragment
import org.teamfairy.sopt.teamkerbell.activities.home.interfaces.HasGroupFragment.Companion.ARG_GROUP
import org.teamfairy.sopt.teamkerbell.activities.items.notice.NoticeCardActivity
import org.teamfairy.sopt.teamkerbell.activities.items.role.RoleListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.VoteListActivity
import org.teamfairy.sopt.teamkerbell.listview.adapter.TextListAdapter
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.GroupInterface
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedGroupR
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import kotlin.properties.Delegates


class HomeFragment : Fragment() , View.OnClickListener, HasGroupFragment{

    private var adapterGroup: TextListAdapter by Delegates.notNull()
    private var dataListGroup = ArrayList<GroupInterface>()

    private var adapterUser: UserListAdapter by Delegates.notNull()
    private var dataListUser = ArrayList<User>()
    private  var recyclerView : RecyclerView by Delegates.notNull()

    override  var group: Team by Delegates.notNull()


    private var ivDropDown : ImageView by Delegates.notNull()
    private var tvTeamName : TextView by Delegates.notNull()
    var tvCount : TextView by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            group = arguments.getParcelable(ARG_GROUP)

    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater!!.inflate(R.layout.fragment_home, container, false)
        tvTeamName = v.findViewById(R.id.tv_teamName)
        tvCount = v.findViewById(R.id.tv_count)
        ivDropDown = v.findViewById(R.id.iv_drop_down)

        showGroupInfo()


        v.btn_notice.setOnClickListener {
            val i = Intent(activity.applicationContext, NoticeCardActivity::class.java)
            i.putExtra(INTENT_GROUP, group)
            startActivity(i)
        }


        v.btn_signal.setOnClickListener {
            val i = Intent(activity.applicationContext, SignalListActivity::class.java)
            i.putExtra(INTENT_GROUP, group)
            startActivity(i)
        }


        v.btn_vote.setOnClickListener {
            val i = Intent(activity.applicationContext, VoteListActivity::class.java)
            i.putExtra(INTENT_GROUP, group)
            startActivity(i)
        }

        v.btn_role.setOnClickListener {
            val i = Intent(activity.applicationContext, RoleListActivity::class.java)
            i.putExtra(INTENT_GROUP, group)
            startActivity(i)
        }


        recyclerView=v.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapterGroup = TextListAdapter(dataListGroup, activity.applicationContext)
        adapterGroup.setOnItemClickListener(this)
        adapterGroup.currentIdx=group.g_idx
        recyclerView.adapter = adapterGroup

        adapterUser = UserListAdapter(dataListUser, activity.applicationContext)


        v.layout_select_team.setOnClickListener {
            if (recyclerView.visibility == View.VISIBLE) closeGroupList()
            else openGroupList()
        }
        v.tv_count.setOnClickListener {
            makeUserDialog()
        }

        v.layout_parent.setOnClickListener {
            if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
            closeGroupList()
        }
        return v
    }

    private fun showGroupInfo() {
        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
        tvTeamName.text = group.real_name
        tvCount.text = ("${realm.where(JoinedGroupR::class.java).equalTo(Team.ARG_G_IDX,group.g_idx).findAll().size }명")
        realm.close()
    }


    override fun changeGroup(g: Team) {
        group=g
        showGroupInfo()

        adapterGroup.currentIdx = group.g_idx
        adapterGroup.notifyDataSetChanged()
    }
    override fun onResume() {
        super.onResume()

    }

    private var dialog: Dialog? = null

    private fun makeUserDialog() {
        DatabaseHelpUtils.getUserListFromRealm(activity.applicationContext,dataListUser,adapterUser as RecyclerView.Adapter<*>,group)

        val builder = AlertDialog.Builder(activity, R.style.CustomDialog)

        val dialogView = layoutInflater.inflate(R.layout.dialog_user_list, null)
        builder.setView(dialogView)
        dialog = builder.create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.dialog_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        recyclerView.adapter = adapterUser


        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val screenHeight = size.y
        val screenWidth = size.x

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog!!.window.attributes)
        lp.height = screenHeight / 2
        lp.width = (screenWidth * 0.7f).toInt()

        dialog!!.show()


        val window = dialog!!.window
        window.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val btnCreate = dialogView.findViewById<ImageButton>(R.id.btn_create)
        btnCreate.setOnClickListener {
            val i = Intent(activity.applicationContext, InviteActivity::class.java)
            i.putExtra(INTENT_GROUP,group)
            startActivity(i)

            dialog!!.dismiss()
        }

    }



    private fun closeGroupList() {
        if(recyclerView.visibility != View.GONE) {
            recyclerView.visibility = View.GONE
            ivDropDown.rotation = 0.0f
        }

    }

    private fun openGroupList() {

        if(recyclerView.visibility != View.VISIBLE) {
            recyclerView.visibility = View.VISIBLE
            ivDropDown.rotation = 180.0f


            DatabaseHelpUtils.getGroupListFromRealm(activity.applicationContext, dataListGroup as ArrayList<Team>, adapterGroup as RecyclerView.Adapter<*>, group)
        }

    }

    private fun changeGroup() {

        val realm = DatabaseHelpUtils.getRealmDefault(activity.applicationContext)
        tvTeamName.text = group.real_name
        tvCount.text = ("${realm.where(JoinedGroupR::class.java).equalTo(Team.ARG_G_IDX,group.g_idx).findAll().size}명")
        realm.close()

        adapterGroup.currentIdx = group.g_idx
        adapterGroup.notifyDataSetChanged()

        val activity = activity as HomeActivity
        activity.changeGroup(group)



    }





    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        group = dataListGroup[pos] as Team
        changeGroup()
        closeGroupList()
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)


        NetworkUtils.connectGroupList(activity.applicationContext,null)
        NetworkUtils.connectUserList(activity.applicationContext,null)
    }

    override fun onDetach() {
        super.onDetach()
    }


    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @group group group.
         * @return A new instance of fragment HomeFragment.
         */
        fun newInstance(group: Team): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putParcelable(ARG_GROUP, group)
            fragment.arguments = args
            return fragment
        }

    }

}
