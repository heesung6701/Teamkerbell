package org.teamfairy.sopt.teamkerbell.activities.items.notice

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_more.*
import kotlinx.android.synthetic.main.content_notice.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.activities.items.filter.MenuFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.MenuActionInterface
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.model.data.Notice.Companion.ARG_STATUS_READ
import org.teamfairy.sopt.teamkerbell.model.data.Vote
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_DELETE
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_GET
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_PUT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REMOVE_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_NOTICE_PARAM_NOTICEID
import org.teamfairy.sopt.teamkerbell.network.info.NoticeTask
import org.teamfairy.sopt.teamkerbell.network.info.VoteResponseTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_NOTICE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_NOTICE_IDX
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_VOTE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_VOTE_IDX
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


class NoticeActivity : AppCompatActivity(), MenuActionInterface {
    override fun menuEdit() {
    }

    override fun menuDelete() {
        notice?.let { attemptDelete(it) }
    }


    var notice : Notice? = null
    private var noticeIdx : Int by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
        setSupportActionBar(toolbar)



        when {
            intent.hasExtra(INTENT_NOTICE) -> {
                notice = intent.getParcelableExtra<Notice>(INTENT_NOTICE)
                notice!!.setPhotoInfo(applicationContext)
                notice!!.setGroupInfo(applicationContext)
                setNoticeInfo()



            }
            intent.hasExtra(INTENT_NOTICE_IDX) -> {
                noticeIdx=intent.getIntExtra(INTENT_NOTICE_IDX,0)

                val task = NoticeTask(applicationContext, HandlerGetNotice(this), LoginToken.getToken(applicationContext))
                task.execute(USGS_REQUEST_URL.URL_DETAIL_SINGLE_NOTICE + "/" + noticeIdx,METHOD_GET)

            }
            else -> finish()
        }

    }
    private fun setNoticeInfo(){

        val notice = this.notice!!

        if(notice.u_idx==LoginToken.getUserIdx(applicationContext))
            MenuFunc(this, MenuFunc.MENU_OPT.DELETE_ONLY)

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


    private fun attemptDelete(notice : Notice){
        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_RESPONSE_NOTICE_PARAM_NOTICEID, notice.notice_idx)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val task = GetMessageTask(applicationContext, HandlerDelete(this), LoginToken.getToken(applicationContext))
        task.execute(URL_REMOVE_NOTICE, METHOD_DELETE, jsonParam.toString())
    }
    private class HandlerGetNotice(activity: NoticeActivity) : Handler() {
        private val mActivity: WeakReference<NoticeActivity> = WeakReference<NoticeActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        activity.notice=msg.obj as Notice
                        activity.setNoticeInfo()
                    }
                    else -> {
                        val message = msg.data.getString("message")
                        Toast.makeText(activity.applicationContext, "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
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

    private class HandlerDelete(activity: NoticeActivity) : Handler() {
        private val mActivity: WeakReference<NoticeActivity> = WeakReference<NoticeActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        Toast.makeText(activity.applicationContext, "삭제 되었습니다.", Toast.LENGTH_SHORT).show()
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
