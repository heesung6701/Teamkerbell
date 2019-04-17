package org.teamfairy.sopt.teamkerbell.activities.setting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.app_bar_back.*
import org.teamfairy.sopt.teamkerbell.R

class VersionInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_version_info)
        setSupportActionBar(toolbar)

        btn_back.setOnClickListener {
            onBackPressed()
        }
    }
}
