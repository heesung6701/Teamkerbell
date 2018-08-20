package org.teamfairy.sopt.teamkerbell.activities.main.room

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_close.*
import kotlinx.android.synthetic.main.content_make_room.*
import org.json.JSONArray
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_ROOM
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.setPref_isUpdate
import org.teamfairy.sopt.teamkerbell._utils.FileUtils.Companion.getRealPathFromURI
import org.teamfairy.sopt.teamkerbell._utils.FileUtils.Companion.updatePhoto
import org.teamfairy.sopt.teamkerbell._utils.FirebaseMessageUtils
import org.teamfairy.sopt.teamkerbell._utils.StatusCode
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedRoomR
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_ROOM
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_ROOM_PARAM_G_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_ROOM_PARAM_NAME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_ROOM_PARAM_USERARRAY
import org.teamfairy.sopt.teamkerbell.network.make.MakeRoomTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class MakeRoomActivity : AppCompatActivity() {


    var group : Team by Delegates.notNull()
    var filePhoto : File?= null


    internal val SELECT_IMAGE = 10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_room)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)

        val spFilter = InputFilter { source, start, end, dest, dstart, dend ->

            var r: CharSequence? = null
            for (i in start until end) {
                if (!Character.isLetterOrDigit(source[i])) {
                    r = ""
                }
            }
            r
        }

        img_profile.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    } else {
                        requestExplorer()
                    }
                } else {
                    requestExplorer()
                }
            } else {
                requestExplorer()
            }
        }

        room_name.filters = arrayOf(spFilter)

        btn_start.setOnClickListener {
            attemptMakeRoom()
        }
        btn_close.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
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
            jsonParam.put(URL_MAKE_ROOM_PARAM_USERARRAY,JSONArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val makeRoomTask = MakeRoomTask(applicationContext, HandlerCreate(this), LoginToken.getToken(applicationContext),group.g_idx)
        if (filePhoto != null) makeRoomTask.file = filePhoto!!
        makeRoomTask.execute(URL_MAKE_ROOM, METHOD_POST, jsonParam.toString())
    }


    private fun requestExplorer() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, SELECT_IMAGE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    filePhoto = updatePhoto(getRealPathFromURI(data!!.data, contentResolver), img_profile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
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

                    finish()

                }
            }
            else -> {
                val result = msg.data.getString(JSON_MESSAGE)
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
