package org.teamfairy.sopt.teamkerbell.activities.group.invite

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_back.*
import kotlinx.android.synthetic.main.content_invite_mail.*
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import kotlin.properties.Delegates

class InviteMailActivity : AppCompatActivity() {

    var group: Team by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_mail)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra<Team>(INTENT_GROUP)

        btn_send.setOnClickListener {
            finish()
        }

        btn_back.setOnClickListener {
            finish()
        }
    }
}
