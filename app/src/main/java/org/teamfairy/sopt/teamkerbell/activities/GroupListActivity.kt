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
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell._utils.StatusCode
import org.teamfairy.sopt.teamkerbell.activities.group.MakeGroupActivity
import org.teamfairy.sopt.teamkerbell.activities.main.MainActivity
import org.teamfairy.sopt.teamkerbell.activities.unperformed.UnperformedActivity

import org.teamfairy.sopt.teamkerbell.listview.adapter.TeamListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedGroupR
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LEAVE_GROUP_PARAM_GID
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class GroupListActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(it: View?) {
        val pos = recyclerView.getChildLayoutPosition(it)
        if (pos == dataList.lastIndex) {
            addItem(pos)
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

    var isUpdateJoined: IsUpdateR? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_list)

        tv_hello.text = ("${LoginToken.getUser(applicationContext).name}님 안녕하세요!")

        adapter = TeamListAdapter(dataList, this)
        adapter.setOnLongClickHandler(HandlerDelete(this))

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


    }


    private fun addItem(pos: Int) {

        val i = Intent(applicationContext, MakeGroupActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(i)
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
    }

    override fun onResume() {
        super.onResume()
        connectGroupList(false)
        addChangeJoinedGroupListener()
    }

    override fun onPause() {
        super.onPause()
        isUpdateJoined?.removeAllChangeListeners()
    }

    private fun deleteGroup(group: Team) {
        val task = GetMessageTask(applicationContext, HandlerDeleteSuccess(this), LoginToken.getToken(applicationContext))

        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_LEAVE_GROUP_PARAM_GID, group.g_idx)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        task.execute(USGS_REQUEST_URL.URL_LEAVE_GROUP, jsonParam.toString())
    }

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


    private fun addChangeJoinedGroupListener() {

        val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
        isUpdateJoined = realm.where(IsUpdateR::class.java).equalTo("what", StatusCode.joinedGroupChange).findFirst()
        if (isUpdateJoined == null) {
            realm.beginTransaction()
            isUpdateJoined = realm.createObject(IsUpdateR::class.java, StatusCode.joinedGroupChange)
            isUpdateJoined!!.isUpdate = false
            realm.commitTransaction()
        } else {
            if (isUpdateJoined?.isUpdate == true) {
                realm.beginTransaction()
                updateGroupList()
                isUpdateJoined!!.isUpdate = false
                realm.commitTransaction()
            }
        }
        isUpdateJoined!!.addChangeListener<IsUpdateR> { t: IsUpdateR, _ ->
            if (t.isUpdate) {
                Log.d("$TAG/isUpdateJoinedGroup", "is ${t.isUpdate}")
                updateGroupList()
                realm.executeTransaction {
                    t.isUpdate = false
                }
            }
        }

    }

    private class HandlerGet(activity: GroupListActivity) : Handler() {
        private val mActivity: WeakReference<GroupListActivity> = WeakReference<GroupListActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            activity?.updateGroupList()
        }
    }

    private class HandlerDeleteSuccess(activity: GroupListActivity) : Handler() {
        private val mActivity: WeakReference<GroupListActivity> = WeakReference<GroupListActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        Toast.makeText(activity.applicationContext, "그룹이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        activity.connectGroupList(true)
                    }
                    else -> {
                        Toast.makeText(activity.applicationContext, "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private class HandlerDelete(activity: GroupListActivity) : Handler() {
        private val mActivity: WeakReference<GroupListActivity> = WeakReference<GroupListActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            activity?.deleteGroup(activity.groupList[msg.what])
        }
    }
}
