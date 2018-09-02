package org.teamfairy.sopt.teamkerbell.activities.items.notice

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_commit.*
import kotlinx.android.synthetic.main.content_make_notice.*
import kotlinx.android.synthetic.main.content_select_room.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.items.filter.SelectRoomFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalActivity
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_NOTICE_PARAM_CHATID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_NOTICE_PARAM_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_NOTICE_PARAM_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_FROM_CHAT
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_NOTICE_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_SIGNAL_IDX
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class MakeNoticeActivity : AppCompatActivity(), RoomActivityInterface {
    override fun changeRoom(room: Room) {
        this.room = room
    }


    val LOG_TAG = this::class.java.name


    override var group: Team by Delegates.notNull()
    override var room: Room? = null

    private var isConnecting: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_notice)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)
        room = intent.getParcelableExtra(INTENT_ROOM) ?: null

        NetworkUtils.connectRoomList(applicationContext, null, true)

        SelectRoomFunc(this)

        edt_response.setOnFocusChangeListener { _, b ->
            if (b) {
                recyclerView.visibility = View.GONE
                iv_drop_down.rotation = 0.0f
            }
        }
        btn_back.setOnClickListener {
            onBackPressed()
        }
        btn_commit.setOnClickListener {
            attemptMake()
        }

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
    }


    private fun attemptMake() {
        if (isConnecting) return
        if(room==null || room!!.room_idx<0){
            Toast.makeText(applicationContext, getText(R.string.txt_select_room), Toast.LENGTH_SHORT).show()
            return
        }
        val content = edt_response!!.text.toString()

        if (content.isEmpty()) {
            Toast.makeText(applicationContext, getText(R.string.txt_enter_content), Toast.LENGTH_SHORT).show()
            edt_response.requestFocus()
            return
        }

        val jsonParam = JSONObject()

        try {
            jsonParam.put(URL_MAKE_NOTICE_PARAM_CHATID, group.g_idx)
            jsonParam.put(URL_MAKE_NOTICE_PARAM_ROOM_IDX, room!!.room_idx)
            jsonParam.put(URL_MAKE_NOTICE_PARAM_CONTENT, content)


        } catch (e: Exception) {
            e.printStackTrace()
        }

        val registTask = GetMessageTask(applicationContext, HandlerMake(this), LoginToken.getToken(applicationContext))
        isConnecting = true
        registTask.execute(URL_MAKE_NOTICE,METHOD_POST, jsonParam.toString())

    }


    private fun makeSuccess(msg: Message) {
        when (msg.what) {
            Utils.MSG_SUCCESS -> {
                Toast.makeText(applicationContext, "공지 등록되었습니다", Toast.LENGTH_SHORT).show()


                val obj = msg.obj as String
                val idx = obj.toInt()

                if(!intent.getBooleanExtra(INTENT_FROM_CHAT,false)){
                    val intent = Intent(applicationContext, NoticeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.putExtra(INTENT_GROUP, group)
                    intent.putExtra(INTENT_NOTICE_IDX, idx.toInt())
                    startActivity(intent)
                }
                finish()


            }
            else -> {

                val result = msg.data.getString("message")

                if (result.contains("Failed")) {
                    isConnecting = false
                    Toast.makeText(applicationContext, "에러 : $result", Toast.LENGTH_SHORT).show()
                } else {
                    isConnecting = false
                    Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    private class HandlerMake(activity: MakeNoticeActivity) : Handler() {
        private val mActivity: WeakReference<MakeNoticeActivity> = WeakReference<MakeNoticeActivity>(activity)

        override fun handleMessage(msg: Message) {
            mActivity.get()?.makeSuccess(msg)
        }
    }

}
