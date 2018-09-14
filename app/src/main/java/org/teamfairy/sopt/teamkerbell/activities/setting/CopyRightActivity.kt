package org.teamfairy.sopt.teamkerbell.activities.setting

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.activity_copy_right.*
import kotlinx.android.synthetic.main.app_bar_back.*

class CopyRightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_copy_right)
        setSupportActionBar(toolbar)

        btn_back.setOnClickListener {
            onBackPressed()
        }

    }

}
