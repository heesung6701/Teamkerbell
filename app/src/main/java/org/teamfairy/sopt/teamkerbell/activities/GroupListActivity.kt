package org.teamfairy.sopt.teamkerbell.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_group_list.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.activities.group.MakeGroupActivity
import org.teamfairy.sopt.teamkerbell.activities.main.MainActivity

import org.teamfairy.sopt.teamkerbell.listview.adapter.TeamListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedGroupR
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_DELETE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LEAVE_GROUP_PARAM_GID
import org.teamfairy.sopt.teamkerbell.utils.*
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class GroupListActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(it: View?) {
        val pos = recyclerView.getChildLayoutPosition(it)
        if (pos == dataList.lastIndex) {
            addItem()
            return
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(INTENT_GROUP, groupList[pos])
        startActivity(intent)

    }


    private val TAG = this::class.java.simpleName

    private var dataList = ArrayList<HashMap<String, String>>()
    private var groupList = ArrayList<Team>()

    private var recyclerView: RecyclerView by Delegates.notNull()
    private var adapter: TeamListAdapter by Delegates.notNull()


    var isUpdateRs: HashMap<Int, IsUpdateR> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_list)

        tv_hello.text = ("${LoginToken.getUser(applicationContext).name}님 안녕하세요!")

        adapter = TeamListAdapter(dataList, this)
//        adapter.setOnLongClickHandler(HandlerDelete(this))

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


    }


    private fun addItem() {

        val i = Intent(applicationContext, MakeGroupActivity::class.java)
        startActivity(i)
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
    }

    override fun onResume() {
        super.onResume()
        connectGroupList(false)
        addChangeListener(IsUpdateR.WHAT_GROUP)
        addChangeListener(IsUpdateR.WHAT_JOINED_GROUP)
    }

    override fun onPause() {
        super.onPause()
        isUpdateRs.forEach {
            it.value.removeAllChangeListeners()
        }
    }

//    private fun deleteGroup(group: Team) {
//        val task = GetMessageTask(applicationContext, HandlerDeleteSuccess(this), LoginToken.getToken(applicationContext))
//
//        val jsonParam = JSONObject()
//        try {
//            jsonParam.put(URL_LEAVE_GROUP_PARAM_GID, group.g_idx)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        task.execute(USGS_REQUEST_URL.URL_LEAVE_GROUP,METHOD_DELETE, jsonParam.toString())
//    }

    private fun connectGroupList(b: Boolean) {
        NetworkUtils.connectGroupList(applicationContext, HandlerGet(this), b)
    }

    private fun updateGroupList() {

        val realm = getRealmDefault(applicationContext)

        dataList.clear()
        groupList.clear()

        adapter.notifyDataSetChanged()
        val groupR = realm.where(GroupR::class.java).findAll()
        groupR.forEach {
            val h = HashMap<String, String>()
            h["name"] = it.real_name
            h["cnt"] = (realm.where(JoinedGroupR::class.java).equalTo(Team.ARG_G_IDX, it.g_idx).findAll().size).toString()
            dataList.add(h)
            groupList.add(it.toGroup())

        }
        dataList.add(HashMap<String, String>())
        adapter.notifyDataSetChanged()
    }


    private fun addChangeListener(what : Int) {
        val LOG_TAG = "$TAG /isUpdateR : User"

        Log.d(LOG_TAG, "add ChangeListener what : $what")

        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
        val isUpdateR = realm.where(IsUpdateR::class.java).equalTo(IsUpdateR.ARG_WHAT, what).findFirst()
                ?: IsUpdateR.create(realm,what)

        if (isUpdateR.isUpdate) {
            Log.d(LOG_TAG, "was true")
            updateGroupList()

            realm.executeTransaction {
                isUpdateR.isUpdate = false
                Log.d(LOG_TAG, "become false")
            }
        }
        isUpdateR.addChangeListener<IsUpdateR> { t: IsUpdateR, _ ->
            if (t.isUpdate) {
                Log.d(LOG_TAG, "is ${t.isUpdate} on addChangeListener")
                updateGroupList()
                realm.executeTransaction {
                    t.isUpdate = false
                    Log.d(LOG_TAG, "is updated on addChangeListener")
                }
            }
        }
        isUpdateRs[what]= isUpdateR
    }

    private class HandlerGet(activity: GroupListActivity) : Handler() {
        private val mActivity: WeakReference<GroupListActivity> = WeakReference<GroupListActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            activity?.updateGroupList()
        }
    }

//    private class HandlerDeleteSuccess(activity: GroupListActivity) : Handler() {
//        private val mActivity: WeakReference<GroupListActivity> = WeakReference<GroupListActivity>(activity)
//
//        override fun handleMessage(msg: Message) {
//            val activity = mActivity.get()
//            if (activity != null) {
//                when (msg.what) {
//                    Utils.MSG_SUCCESS -> {
//                        Toast.makeText(activity.applicationContext, activity.getString(R.string.txt_delete_success), Toast.LENGTH_SHORT).show()
//                        activity.connectGroupList(true)
//                    }
//                    else -> {
//                        Toast.makeText(activity.applicationContext, activity.getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//    }

//    private class HandlerDelete(activity: GroupListActivity) : Handler() {
//        private val mActivity: WeakReference<GroupListActivity> = WeakReference<GroupListActivity>(activity)
//
//        override fun handleMessage(msg: Message) {
//            val activity = mActivity.get()
//            activity?.deleteGroup(activity.groupList[msg.what])
//        }
//    }

    override fun onBackPressed() {
        val i = Intent(this, SplashActivity::class.java)
        i.putExtra(IntentTag.EXIT,true)
        i.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
        finish()
    }
}
