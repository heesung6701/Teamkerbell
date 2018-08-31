package org.teamfairy.sopt.teamkerbell.activities.group.invite

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_back.*
import kotlinx.android.synthetic.main.content_invite_phone.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.ChatUtils
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_JOINED_GROUP
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_USER
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.setPref_isUpdate
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessageF
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_INVITE_GROUP
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_INVITE_GROUP_PARAM_GID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_INVITE_GROUP_PARAM_NAME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_INVITE_GROUP_PARAM_PHONE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class InvitePhoneActivity : AppCompatActivity() {

    var group: Team by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_phone)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra<Team>(INTENT_GROUP)

        edt_phone.addTextChangedListener(object : TextWatcher {
            var beforeLength: Int = 0
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {


                if (editable.toString().length > beforeLength) {
                    if ((editable.toString().length == 3 || editable.toString().length == 8)) {
                        editable.append('-');
                    }
                } else {
                    if ((editable.toString().length == 3 || editable.toString().length == 8)) {
                        editable.delete(editable.length - 1, editable.length)
                    }
                }
                beforeLength = editable.toString().length
            }
        })

        btn_add.setOnClickListener {
            attemptInvite()
        }
        btn_back.setOnClickListener {
            finish()
        }
    }

    var name = ""
    var phone = ""
    private fun attemptInvite() {
        name = edt_name.text.toString()
        phone = edt_phone.text.toString()

        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_INVITE_GROUP_PARAM_GID, group.g_idx)
            jsonParam.put(URL_INVITE_GROUP_PARAM_NAME, name)
            jsonParam.put(URL_INVITE_GROUP_PARAM_PHONE, phone)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val inviteTask = GetMessageTask(applicationContext, HandlerInvite(this))
        inviteTask.execute(URL_INVITE_GROUP,METHOD_POST, jsonParam.toString())
    }

    fun invite(msg: Message) {
        when (msg.what) {
            MSG_SUCCESS -> {
                val realm = getRealmDefault(applicationContext)

                val uIdx: Int = msg.obj.toString().toInt()

                val userR: UserR = realm.where(UserR::class.java).equalTo("name", name).equalTo("phone", phone).findFirst()
                        ?: UserR()
                if (userR.u_idx == -2) //베짱이가 U_idx를 보내줘야됨
                    setPref_isUpdate(applicationContext, PREF_ISUPDATE_USER, true)

                setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_GROUP, true)

                Toast.makeText(applicationContext, name + "님을 초대했습니다.", Toast.LENGTH_SHORT).show()
                finish()

            }
            else -> {
                val result = msg.data.getString("message")
                when {
                    result.contains("Already") -> Toast.makeText(applicationContext, "이미 초대된 사용자입니다.", Toast.LENGTH_SHORT).show()
                    result.contains("Failed") -> Toast.makeText(applicationContext, "존재하지 않는 사용자입니다.", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private class HandlerInvite(activity: InvitePhoneActivity) : Handler() {
        private val mActivity: WeakReference<InvitePhoneActivity> = WeakReference<InvitePhoneActivity>(activity)

        override fun handleMessage(msg: Message) {
            mActivity.get()?.invite(msg)
        }
    }



}
