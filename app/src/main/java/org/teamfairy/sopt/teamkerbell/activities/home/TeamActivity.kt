package org.teamfairy.sopt.teamkerbell.activities.home

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_team.*
import kotlinx.android.synthetic.main.content_team.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.activities.items.notice.NoticeCardActivity
import org.teamfairy.sopt.teamkerbell.activities.items.role.RoleListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.VoteListActivity
import org.teamfairy.sopt.teamkerbell.listview.adapter.TextListAdapter
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedR
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import kotlin.properties.Delegates

class TeamActivity : AppCompatActivity(), View.OnClickListener {


    private var adapterGroup: TextListAdapter by Delegates.notNull()
    private var adapterUser: UserListAdapter by Delegates.notNull()
    private var dataListGroup = ArrayList<Team>()
    private var dataListUser = ArrayList<User>()

    var group: Team by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)

        group = intent.getParcelableExtra<Team>(INTENT_GROUP)

        tv_teamName.text = group.real_name
        tv_count.text = "${group.g_idx}명"

        main_tab.addTab(main_tab.newTab().setIcon(R.drawable.icon_tab_contact))
        main_tab.addTab(main_tab.newTab().setIcon(R.drawable.icon_tab_home))
        main_tab.addTab(main_tab.newTab().setIcon(R.drawable.icon_tab_chat))

        main_tab.tabGravity = TabLayout.GRAVITY_FILL

        btn_notice.setOnClickListener {
            val i = Intent(applicationContext, NoticeCardActivity::class.java)
            i.putExtra(INTENT_GROUP, group)
            startActivity(i)
        }


        btn_signal.setOnClickListener {
            val i = Intent(applicationContext, SignalListActivity::class.java)
            i.putExtra(INTENT_GROUP, group)
            startActivity(i)
        }


        btn_vote.setOnClickListener {
            val i = Intent(applicationContext, VoteListActivity::class.java)
            i.putExtra(INTENT_GROUP, group)
            startActivity(i)
        }

        btn_role.setOnClickListener {
            val i = Intent(applicationContext, RoleListActivity::class.java)
            i.putExtra(INTENT_GROUP, group)
            startActivity(i)
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        adapterGroup = TextListAdapter(dataListGroup, applicationContext)
        adapterGroup.setOnItemClickListener(this)
        recyclerView.adapter = adapterGroup

        adapterUser = UserListAdapter(dataListUser, applicationContext)

        getGroupListFromRealm()
        getUserListFromRealm()

        layout_team_select.setOnClickListener {
            recyclerView.visibility = if (recyclerView.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        tv_count.setOnClickListener {
            makeUserDialog()
        }

        layout_parent.setOnClickListener {
            if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
            recyclerView.visibility = View.GONE
        }
    }


    private var dialog: Dialog? = null

    private fun makeUserDialog() {

        val builder = AlertDialog.Builder(this, R.style.CustomDialog)

        val dialogView = layoutInflater.inflate(R.layout.dialog_user_list, null)
        builder.setView(dialogView)
        dialog = builder.create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.dialog_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = adapterUser


        val display = windowManager.defaultDisplay
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
            Toast.makeText(applicationContext, "??", Toast.LENGTH_SHORT).show()
        }

    }


    private fun changeGroup() {
        tv_teamName.text = group.real_name
        tv_count.text = "${group.g_idx}명"
        adapterGroup.currentGIdx = group.g_idx
        adapterGroup.notifyDataSetChanged()
    }

    private fun getGroupListFromRealm() {

        val realm = getRealmDefault(applicationContext)

        dataListGroup.clear()
        adapterGroup.notifyDataSetChanged()
        val groupR = realm.where(GroupR::class.java).findAll()
        groupR.iterator().forEach {
            dataListGroup.add(it.toGroup())
        }

        adapterGroup.currentGIdx = group.g_idx

        adapterGroup.notifyDataSetChanged()
    }

    private fun getUserListFromRealm() {

        val realm = getRealmDefault(applicationContext)

        dataListUser.clear()
        adapterUser.notifyDataSetChanged()
        val joinedRs = realm.where(JoinedR::class.java).equalTo("g_idx", group.g_idx).notEqualTo("u_idx", LoginToken.getUserIdx(applicationContext)).findAll()
        joinedRs.iterator().forEach {
            val userR: UserR = realm.where(UserR::class.java).equalTo("u_idx", it.u_idx).findFirst()
                    ?: UserR()
            dataListUser.add(userR.toUser())
        }
        adapterUser.notifyDataSetChanged()
    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
        group = dataListGroup[pos]
        changeGroup()
        recyclerView.visibility = View.GONE
    }


}
