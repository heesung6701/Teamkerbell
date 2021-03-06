package org.teamfairy.sopt.teamkerbell.activities.items.vote

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_vote_list.*
import kotlinx.android.synthetic.main.app_bar_filter.*
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.content_vote_list.*
import org.teamfairy.sopt.teamkerbell.activities.items.filter.FilterFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter.VoteTabAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.Utils
import kotlin.properties.Delegates

class VoteListActivity : AppCompatActivity() ,RoomActivityInterface{

    override fun changeRoom(room: Room) {
        this.room=room
        tabAdapter.changeRoom(room)
    }

    override var room: Room?=null
    override var group : Team by Delegates.notNull()

    private var tabAdapter : VoteTabAdapter by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote_list)
        setSupportActionBar(toolbar)
        tv_title.text = supportActionBar!!.title

        group = intent.getParcelableExtra(INTENT_GROUP)


        top_tab.addTab(top_tab.newTab().setText(getText(R.string.txt_receiver)))
        top_tab.addTab(top_tab.newTab().setText(getText(R.string.txt_sender)))

        tabAdapter = VoteTabAdapter(supportFragmentManager, top_tab.tabCount,group)

        viewPager.adapter = tabAdapter

        top_tab.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(top_tab))


        FilterFunc(this)

        btn_back.setOnClickListener {
            finish()
        }
        fab.setOnClickListener {
            val i = Intent(applicationContext,MakeVoteActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            i.putExtra(INTENT_GROUP,group)
            i.putExtra(INTENT_ROOM,room)
            startActivity(i)
            viewPager.currentItem = Utils.TAB_REQUEST
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)

        }
    }

}
