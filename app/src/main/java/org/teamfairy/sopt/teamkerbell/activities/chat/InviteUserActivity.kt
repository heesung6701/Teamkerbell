package org.teamfairy.sopt.teamkerbell.activities.chat

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.activity_invite_user.*
import kotlinx.android.synthetic.main.app_bar_commit.*
import kotlinx.android.synthetic.main.content_invite_user.*
import kotlinx.android.synthetic.main.fragment_contact.view.*
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.activities.main.contact.adapter.ContactListAdapter
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.data.User.Companion.ARG_U_IDX
import org.teamfairy.sopt.teamkerbell.model.list.UserCheckData
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedRoomR
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import kotlin.properties.Delegates

class InviteUserActivity : AppCompatActivity() {

    var group : Team by Delegates.notNull()
    var room : Room by Delegates.notNull()

    var adapter: UserListAdapter by Delegates.notNull()
    var dataList: ArrayList<UserCheckData> = arrayListOf<UserCheckData>()
    var dataListOrigin: ArrayList<User> = arrayListOf<User>()
    var recyclerView: RecyclerView by Delegates.notNull()

    var txtSearch : String = ""


    val whoCheck = HashMap<Int,Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_user)
        setSupportActionBar(toolbar)
        group  = intent.getParcelableExtra<Team>(INTENT_GROUP)
        room  = intent.getParcelableExtra<Room>(INTENT_ROOM)


        recyclerView = findViewById(R.id.recyclerView)
        adapter = UserListAdapter(dataList as ArrayList<User>,applicationContext)
        recyclerView.layoutManager= LinearLayoutManager(this)
        recyclerView.adapter=adapter


        btn_back.setOnClickListener {
            onBackPressed()
        }

        btn_commit.setOnClickListener {
            attemptCommit()
        }

        edt_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                txtSearch = p0.toString().trim()
                getUserList()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    override fun onResume() {
        super.onResume()
        getUserList()
    }
    fun getUserList(){
        DatabaseHelpUtils.getUserListFromRealm(applicationContext,dataListOrigin,adapter as RecyclerView.Adapter<*>,group,true)
        updateUserList()

    }
    private fun updateUserList(){
        dataList.forEach {
            whoCheck[it.u_idx]=it.isChecked
        }
        dataList.clear()

        val roomMemberList = ArrayList<Int>()
        DatabaseHelpUtils.getRoomUIdxListFromRealm(applicationContext,roomMemberList,room)

        dataListOrigin.forEach {
            if(it.name!!.contains(txtSearch) && !roomMemberList.contains(it.u_idx)) {
                dataList.add(it.toUserCheckData(whoCheck[it.u_idx]?:false))
            }
        }
        adapter.notifyDataSetChanged()
    }
    fun attemptCommit(){

    }

}
