package org.teamfairy.sopt.teamkerbell.activities.home.room

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_back.*
import kotlinx.android.synthetic.main.content_make_room.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils
import org.teamfairy.sopt.teamkerbell._utils.StatusCode
import org.teamfairy.sopt.teamkerbell.activities.group.MakeGroupActivity
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedGroupR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedRoomR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_GROUP
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_ROOM
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_ROOM_PARAM_G_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_ROOM_PARAM_NAME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_ROOM_PARAM_USERARRAY
import org.teamfairy.sopt.teamkerbell.network.make.MakeGroupTask
import org.teamfairy.sopt.teamkerbell.network.make.MakeRoomTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.io.File
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class MakeRoomActivity : AppCompatActivity() {


    var group : Team by Delegates.notNull()
    var file : File?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_room)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)


        btn_start.setOnClickListener {
            attemptMakeRoom()
        }
        btn_back.setOnClickListener {
            finish()
        }
    }

    private fun attemptMakeRoom() {

        room_name.error=null

        val roomName = room_name.text.toString()

        var cancel = false

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(roomName)) {
            room_name.error = getString(R.string.error_field_required)
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            room_name.requestFocus()
        } else {
            makeRoom(roomName)
        }
    }
    private fun makeRoom(roomName : String){
        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_MAKE_ROOM_PARAM_NAME,roomName )
            jsonParam.put(URL_MAKE_ROOM_PARAM_G_IDX,group.g_idx)
            jsonParam.put(URL_MAKE_ROOM_PARAM_USERARRAY,"[]")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val makeRoomTask = MakeRoomTask(applicationContext, HandlerCreate(this), LoginToken.getToken(applicationContext))
        if (file != null) makeRoomTask.file = file!!
        makeRoomTask.execute(URL_MAKE_ROOM, jsonParam.toString())
    }

    fun createSuccess(msg : Message){
        when (msg.what) {
            Utils.MSG_SUCCESS -> {

                if (msg.obj is Room) {
                    val room = msg.obj as Room

                    Toast.makeText(applicationContext, room.real_name + "이 만들어졌습니다.", Toast.LENGTH_SHORT).show()

                    val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
                    realm.beginTransaction()

                    val joinedR = realm.createObject(JoinedRoomR::class.java)
                    joinedR.u_idx = LoginToken.getUserIdx(applicationContext)
                    joinedR.g_idx = group.g_idx
                    joinedR.room_idx = room.room_idx
                    Log.d("RealmDB/added", joinedR.toString())

                    realm.copyToRealmOrUpdate(room.toChatRoomR())
                    Log.d("RealmDB/added", room.toString())


                    val isUpdateJoined = IsUpdateR()
                    isUpdateJoined.what=StatusCode.joinedRoomChange
                    isUpdateJoined.isUpdate = true
                    realm.copyToRealmOrUpdate(isUpdateJoined)

                    realm.commitTransaction()

                    FirebaseMessageUtils.setDatabaseGroup(group,room)
                    FirebaseMessageUtils.dataBaseEndpoints.child(LoginToken.getUserIdx(applicationContext).toString()).setValue(0)

                    finish()

                }
            }
            else -> {
                val result = msg.data.getString("message")
                Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class HandlerCreate(activity: MakeRoomActivity) : Handler() {
        private val mActivity: WeakReference<MakeRoomActivity> = WeakReference<MakeRoomActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            activity?.createSuccess(msg)
        }
    }

}
