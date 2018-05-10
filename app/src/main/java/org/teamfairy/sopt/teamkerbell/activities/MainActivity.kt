package org.teamfairy.sopt.teamkerbell.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.group.MakeGroupActivity
import org.teamfairy.sopt.teamkerbell.activities.home.HomeActivity

import org.teamfairy.sopt.teamkerbell.listview.adapter.TeamListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedGroupR
import org.teamfairy.sopt.teamkerbell.utils.CurrentGroup
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() ,View.OnClickListener{


    private var dataList = ArrayList<HashMap<String,String>>()
    private var groupList = ArrayList<Team>()

    private var recyclerView : RecyclerView by Delegates.notNull()
    private  var adapter : TeamListAdapter by Delegates.notNull()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_hello.text= ("${LoginToken.getUser(applicationContext).name}님 안녕하세요!")

        adapter =  TeamListAdapter(dataList,this)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager=LinearLayoutManager(this)
        recyclerView.adapter = adapter

    }

    override fun onClick(it: View?) {
        val pos = recyclerView.getChildLayoutPosition(it)
        if(pos==dataList.lastIndex){
            addItem(pos)
            return
        }
        val intent = Intent(applicationContext, HomeActivity::class.java)
//        CurrentGroup.setGroup(groupList[pos])
        intent.putExtra(INTENT_GROUP,groupList[pos])
        startActivity(intent)
    }

    private fun addItem(pos : Int ){

        val i = Intent(applicationContext, MakeGroupActivity::class.java)
        startActivity(i)
    }

    override fun onResume() {
        super.onResume()
        connectGroupList()
    }

    private fun connectGroupList() {
        NetworkUtils.connectGroupList(applicationContext,HandlerGet(this))
    }

    private fun updateGroupList() {

        val realm = getRealmDefault(applicationContext)

        dataList.clear()
        groupList.clear()

        adapter.notifyDataSetChanged()
        var i = 0
        val groupR = realm.where(GroupR::class.java).findAll()
        groupR.forEach {
            val h = HashMap<String,String>()
            h["name"] = it.real_name
            h["cnt"] = (realm.where(JoinedGroupR::class.java).equalTo("g_idx",it.g_idx).findAll().size?:0 ).toString()
            dataList.add(h)
            groupList.add(it.toGroup())

        }
        dataList.add(HashMap<String,String>())
        adapter.notifyDataSetChanged()
    }


    private class HandlerGet(activity: MainActivity) : Handler() {
        private val mActivity: WeakReference<MainActivity> = WeakReference<MainActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            activity?.updateGroupList()
        }
    }
}
