package org.teamfairy.sopt.teamkerbell.activities.items.notice

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.app_bar_back.*
import kotlinx.android.synthetic.main.content_notice.*
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell._utils.TagUtils
import org.teamfairy.sopt.teamkerbell.model.data.Notice

class NoticeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
        setSupportActionBar(toolbar)

        val notice = intent.getParcelableExtra<Notice>(TagUtils.INTENT_NOTICE)
        notice.setPhotoInfo(applicationContext)

        tv_title.text=notice.getMainTitle()
        tv_content.text=notice.content
        if (NetworkUtils.getBitmapList(notice.photo, iv_profile, applicationContext,"user"+notice.u_idx))
            iv_profile.setImageResource(R.drawable.icon_profile_default_png)
        tv_name.text=notice.name
        tv_time.text=notice.getSubTitle()

        btn_commit.setOnClickListener {
            finish()
        }
        btn_back.setOnClickListener {
            onBackPressed()
        }

    }

}
