package org.teamfairy.sopt.teamkerbell.activities.items.notice

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_back.*
import kotlinx.android.synthetic.main.content_notice.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.model.data.Notice.Companion.ARG_STATUS_READ
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_NOTICE_PARAM_NOTICEID
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_NOTICE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference


class NoticeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
        setSupportActionBar(toolbar)

        val notice = intent.getParcelableExtra<Notice>(INTENT_NOTICE)
        notice.setPhotoInfo(applicationContext)
        notice.setGroupInfo(applicationContext)


        tv_title.text=notice.getMainTitle()
        tv_content.text=notice.content
        if (NetworkUtils.getBitmapList(notice.photo, iv_profile, applicationContext,"$INTENT_USER/${notice.u_idx}"))
            iv_profile.setImageResource(R.drawable.icon_profile_default_png)
        tv_name.text=notice.name
        tv_time.text=notice.getSubTitle()

        if(notice.status==ARG_STATUS_READ)
            btn_commit.visibility= View.GONE
        else
            btn_commit.visibility=View.VISIBLE

        btn_commit.setOnClickListener {
            attemptResponse(notice)
        }
        btn_back.setOnClickListener {
            onBackPressed()
        }

    }
    private fun attemptResponse(notice : Notice){
        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_RESPONSE_NOTICE_PARAM_NOTICEID, notice.notice_idx)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val task = GetMessageTask(applicationContext, HandlerNoticeResponse(this), LoginToken.getToken(applicationContext))
        task.execute(URL_RESPONSE_NOTICE,METHOD_POST, jsonParam.toString())
    }

    private class HandlerNoticeResponse(activity: NoticeActivity) : Handler() {
        private val mActivity: WeakReference<NoticeActivity> = WeakReference<NoticeActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        Toast.makeText(activity.applicationContext, "확인했습니다.", Toast.LENGTH_SHORT).show()
                        activity.finish()
                    }
                    else -> {
                        val message = msg.data.getString("message")
                        Toast.makeText(activity.applicationContext, "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}
