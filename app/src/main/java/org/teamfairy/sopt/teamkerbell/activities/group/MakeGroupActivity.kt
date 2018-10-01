package org.teamfairy.sopt.teamkerbell.activities.group

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat.startActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_make_group.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.activities.main.MainActivity
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedGroupR
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_GROUP
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_GROUP_PARAM_NAME
import org.teamfairy.sopt.teamkerbell.network.make.MakeGroupTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.io.File
import java.lang.ref.WeakReference
import kotlin.properties.Delegates
import android.widget.EditText
import android.text.InputFilter
import org.teamfairy.sopt.teamkerbell.R.id.group_name
import java.util.regex.Pattern
import android.text.Spanned
import org.teamfairy.sopt.teamkerbell.utils.EditTextFilter.Companion.setFilter


class MakeGroupActivity : AppCompatActivity() {

    var user: User by Delegates.notNull()

    private var filePhoto : File?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_group)

        user = LoginToken.getUser(applicationContext)

        tv_welcome.text = ("${user.name}님, 어서오세요!")

        setFilter(group_name)

        btn_start.setOnClickListener {
            attemptMakeGroup()
        }
    }



    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
    }

    private fun attemptMakeGroup() {

        group_name.error=null

        val groupName = group_name.text.toString()

        var cancel = false

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(groupName)) {
            group_name.error = getString(R.string.error_field_required)
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            group_name.requestFocus()
        } else {
            makeGroup(groupName)
        }
    }
    private fun makeGroup(groupName : String){
        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_MAKE_GROUP_PARAM_NAME,groupName )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val makeGroupTask = MakeGroupTask(applicationContext, HandlerCreate(this), LoginToken.getToken(applicationContext))
        if (filePhoto != null) makeGroupTask.photo = filePhoto!!
        makeGroupTask.execute(URL_MAKE_GROUP,METHOD_POST, jsonParam.toString())
    }

    fun createSuccess(msg : Message){
        when (msg.what) {
            Utils.MSG_SUCCESS -> {

                if (msg.obj is Team) {
                    val group = msg.obj as Team

                    Toast.makeText(applicationContext, group.real_name + "이 만들어졌습니다.", Toast.LENGTH_SHORT).show()

                    val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
                    realm.beginTransaction()

                    val joinedR = realm.createObject(JoinedGroupR::class.java)
                    joinedR.u_idx = LoginToken.getUserIdx(applicationContext)
                    joinedR.g_idx = group.g_idx
                    Log.d("RealmDB/added", joinedR.toString())

                    Log.d("RealmDB/added", joinedR.toString())

                    realm.copyToRealmOrUpdate(group.toGroupR())
                    Log.d("RealmDB/added", group.toString())

                    realm.commitTransaction()

                    DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_ROOM,true)
                    DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_JOINED_ROOM,true)
                    NetworkUtils.connectRoomList(applicationContext,null)
                    NetworkUtils.connectJoinedRoomList(applicationContext,null)


                    val i = Intent(application,MainActivity::class.java)
                    i.putExtra(IntentTag.INTENT_GROUP,group)
                    startActivity(i)

                    finish()

                }
            }
            else -> {
                val result = msg.data.getString("message")
                Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class HandlerCreate(activity: MakeGroupActivity) : Handler() {
        private val mActivity: WeakReference<MakeGroupActivity> = WeakReference<MakeGroupActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            activity?.createSuccess(msg)
        }
    }
}
