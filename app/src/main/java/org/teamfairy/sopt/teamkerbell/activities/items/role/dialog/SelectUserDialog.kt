package org.teamfairy.sopt.teamkerbell.activities.items.role.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.Window
import android.widget.Button
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.list.UserCheckData
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-05-20.
 */
class SelectUserDialog(context: Context?, var room: Room, private var userIdArray: IntArray) : Dialog(context) {

    private var adapter: UserListAdapter by Delegates.notNull()
    var dataList = ArrayList<UserCheckData>()
    private var dataListOrigin = ArrayList<User>()
    private var recyclerView: RecyclerView by Delegates.notNull()

    private var btnComplete: Button by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        setContentView(R.layout.dialog_select_user)

        recyclerView = findViewById<RecyclerView>(R.id.dialog_recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context.applicationContext)
        adapter = UserListAdapter(dataList as ArrayList<User>, context.applicationContext)
        recyclerView.adapter = adapter

        updateUserList()

        btnComplete = findViewById(R.id.btn_complete)
    }
    fun setOnClickListener(l: View.OnClickListener) {
        btnComplete.setOnClickListener(l)
    }
    private fun updateUserList() {
        dataList.clear()

        val roomMemberList = ArrayList<User>()
        DatabaseHelpUtils.getRoomUserListFromRealm(context, roomMemberList, room)

        roomMemberList.forEach {
            dataList.add(it.toUserCheckData(userIdArray.contains(it.u_idx)))
        }
        adapter.notifyDataSetChanged()
    }
}