package org.teamfairy.sopt.teamkerbell.activities.main.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.Window
import android.widget.ImageButton
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.listview.adapter.UserListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-05-20.
 */
class ShowUserDialog(context: Context?, var group: Team) : Dialog(context) {

    private var adapterUser: UserListAdapter by Delegates.notNull()
    private var dataListUser = ArrayList<User>()
    private var recyclerView: RecyclerView by Delegates.notNull()

    private var btnAdd: ImageButton by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        setContentView(R.layout.dialog_user_list)

        recyclerView = findViewById<RecyclerView>(R.id.dialog_recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context.applicationContext)
        adapterUser = UserListAdapter(dataListUser, context.applicationContext)
        recyclerView.adapter = adapterUser

        DatabaseHelpUtils.getUserListFromRealm(context.applicationContext, dataListUser, adapterUser as RecyclerView.Adapter<*>, group)

        btnAdd = findViewById(R.id.btn_add)
    }
    fun setOnClickListener(l: View.OnClickListener) {
        btnAdd.setOnClickListener(l)
    }
}