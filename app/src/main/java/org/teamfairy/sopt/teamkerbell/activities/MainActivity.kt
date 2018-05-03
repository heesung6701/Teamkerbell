package org.teamfairy.sopt.teamkerbell.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell._utils.TagUtils.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.activities.home.TeamActivity

import org.teamfairy.sopt.teamkerbell.listview.adapter.TeamListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() ,View.OnClickListener{


    private var dataList = ArrayList<Team>()

    private var recyclerView : RecyclerView by Delegates.notNull()
    private  var adapter : TeamListAdapter by Delegates.notNull()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataList.add(Team(0,"",""))
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
        val intent = Intent(applicationContext, TeamActivity::class.java)
        intent.putExtra(INTENT_GROUP,dataList[pos])
        startActivity(intent)
    }

    private fun addItem(pos : Int ){

        Toast.makeText(applicationContext,pos.toString()+" add",Toast.LENGTH_SHORT).show()
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
        adapter.notifyDataSetChanged()
        var i = 0
        val groupR = realm.where(GroupR::class.java).findAll()
        groupR.forEach {
            dataList.add(it.toGroup())
        }
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
