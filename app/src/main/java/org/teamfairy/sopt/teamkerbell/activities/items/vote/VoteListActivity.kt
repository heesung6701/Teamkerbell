package org.teamfairy.sopt.teamkerbell.activities.items.vote

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_vote_list.*
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_more.*
import kotlinx.android.synthetic.main.content_vote_list.*
import org.teamfairy.sopt.teamkerbell._utils.TagUtils
import org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter.VoteTabAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import kotlin.properties.Delegates

class VoteListActivity : AppCompatActivity() {

    var group : Team by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote_list)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)


        top_tab.addTab(top_tab.newTab().setText("수신"))
        top_tab.addTab(top_tab.newTab().setText("발신"))

        val tabAdapter = VoteTabAdapter(supportFragmentManager, top_tab.tabCount,group)

        viewPager.adapter = tabAdapter

        top_tab.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(top_tab))


        btn_back.setOnClickListener {
            finish()
        }
        fab.setOnClickListener {
            val i = Intent(applicationContext,MakeVoteActivity::class.java)
            i.putExtra(INTENT_GROUP,group)
            startActivity(i)

        }
    }

}
