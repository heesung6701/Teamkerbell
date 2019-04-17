package org.teamfairy.sopt.teamkerbell.activities.setting

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_back.*
import kotlinx.android.synthetic.main.content_setting.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.activities.GroupListActivity
import org.teamfairy.sopt.teamkerbell.activities.SplashActivity
import org.teamfairy.sopt.teamkerbell.activities.main.contact.ProfileActivity
import org.teamfairy.sopt.teamkerbell.dialog.ConfirmDeleteDialog
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.utils.*
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class SettingActivity : AppCompatActivity() {

    var group: Team by Delegates.notNull()
    var user: User by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(IntentTag.INTENT_GROUP)
        user = LoginToken.getUser(applicationContext)

        tv_group_name.text = group.real_name

        if (NetworkUtils.getBitmapList(group.photo, iv_group_profile, applicationContext, "group${group.g_idx}"))
            iv_group_profile.setImageResource(R.drawable.icon_profile_default)

        if (NetworkUtils.getBitmapList(user.photo, iv_user_profile, applicationContext, "user${user.u_idx}"))
            iv_user_profile.setImageResource(R.drawable.icon_profile_default)

        val settingNotificationAll = DatabaseHelpUtils.getSettingPush(applicationContext)
        swc_setting_allow_noti.isChecked = settingNotificationAll
        swc_setting_allow_noti_group.isEnabled = settingNotificationAll

        swc_setting_allow_noti_group.isChecked = DatabaseHelpUtils.getSettingPush(applicationContext, group.g_idx)

        layout_profile.setOnClickListener {
            val i = Intent(this, ProfileActivity::class.java)
            startActivity(i)
        }

        // 그룹 설정
        swc_setting_allow_noti_group.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(applicationContext, "${group.real_name}팀의 메세지 알림이 ${if (isChecked) "설정" else "해제"}되었습니다.", Toast.LENGTH_SHORT).show()
            DatabaseHelpUtils.setSettingPush(applicationContext, group.g_idx, isChecked)
        }
        layout_leave.setOnClickListener {
            val dialog = ConfirmDeleteDialog(this, getString(R.string.txt_confirm_leave))
            dialog.show()
            dialog.setOnClickListenerYes(View.OnClickListener {
                deleteGroup(group)
            })
        }

        // 앱설정
        swc_setting_allow_noti.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(applicationContext, "메세지 알림이 ${if (isChecked) "설정" else "해제"}되었습니다.", Toast.LENGTH_SHORT).show()
            DatabaseHelpUtils.setSettingPush(applicationContext, isChecked)

            swc_setting_allow_noti_group.isEnabled = isChecked
        }
        layout_version.setOnClickListener {
            val i = Intent(this, VersionInfoActivity::class.java)
            startActivity(i)
        }
        layout_copyright.setOnClickListener {
            val i = Intent(this, CopyRightActivity::class.java)
            startActivity(i)
        }
        layout_sign_out.setOnClickListener {
            val dialog = ConfirmDeleteDialog(this, getString(R.string.txt_confirm_sign_out))
            dialog.show()
            dialog.setOnClickListenerYes(View.OnClickListener {
                LoginToken.signOut(applicationContext)

                val i = Intent(this, SplashActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                finishAffinity()
                startActivity(i)
            })
        }

        btn_back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun successLeave() {
        val i = Intent(this, GroupListActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
    }
    private fun deleteGroup(group: Team) {
        val task = GetMessageTask(applicationContext, HandlerDeleteSuccess(this), LoginToken.getToken(applicationContext))

        val jsonParam = JSONObject()
        try {
            jsonParam.put(USGS_REQUEST_URL.URL_LEAVE_GROUP_PARAM_GID, group.g_idx)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        task.execute(USGS_REQUEST_URL.URL_LEAVE_GROUP, NetworkTask.METHOD_DELETE, jsonParam.toString())
    }

    private class HandlerDeleteSuccess(activity: SettingActivity) : Handler() {
        private val mActivity: WeakReference<SettingActivity> = WeakReference<SettingActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        Toast.makeText(activity.applicationContext, activity.getString(R.string.txt_delete_success), Toast.LENGTH_SHORT).show()
                        activity.successLeave()
                    }
                    else -> {
                        Toast.makeText(activity.applicationContext, activity.getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
