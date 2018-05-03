package org.teamfairy.sopt.teamkerbell.activities.home

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import kotlinx.android.synthetic.main.activity_team.*
import kotlinx.android.synthetic.main.content_team.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.TagUtils.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.activities.items.notice.NoticeCardActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalListActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.VoteListActivity
import org.teamfairy.sopt.teamkerbell.model.data.Team

class TeamActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)

        val group = intent.getParcelableExtra<Team>(INTENT_GROUP)

        tv_teamName.text=group.real_name
        tv_count.text="${group.g_idx}명"

        main_tab.addTab(main_tab.newTab().setIcon(R.drawable.icon_tab_contact))
        main_tab.addTab(main_tab.newTab().setIcon(R.drawable.icon_tab_home))
        main_tab.addTab(main_tab.newTab().setIcon(R.drawable.icon_tab_chat))

        main_tab.tabGravity= TabLayout.GRAVITY_FILL

        btn_notice.setOnClickListener {
            val i = Intent(applicationContext,NoticeCardActivity::class.java)
            i.putExtra(INTENT_GROUP,group)
            startActivity(i)
        }


        btn_signal.setOnClickListener {
            val i = Intent(applicationContext,SignalListActivity::class.java)
            i.putExtra(INTENT_GROUP,group)
            startActivity(i)
        }


        btn_vote.setOnClickListener {
            val i = Intent(applicationContext, VoteListActivity::class.java)
            i.putExtra(INTENT_GROUP,group)
            startActivity(i)
        }
    }
}
