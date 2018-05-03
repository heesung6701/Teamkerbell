package org.teamfairy.sopt.teamkerbell.activities.items.signal

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_more.*
import kotlinx.android.synthetic.main.content_signal_list.*
import org.teamfairy.sopt.teamkerbell._utils.TagUtils
import org.teamfairy.sopt.teamkerbell._utils.TagUtils.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell._utils.TagUtils.Companion.INTENT_RESPONDED
import org.teamfairy.sopt.teamkerbell._utils.TagUtils.Companion.INTENT_SIGNAL
import org.teamfairy.sopt.teamkerbell.activities.items.signal.adapter.SignalTabAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.model.data.Team
import kotlin.properties.Delegates

class SignalListActivity : AppCompatActivity() {

    var group : Team by Delegates.notNull()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signal_list)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)



        top_tab.addTab(top_tab.newTab().setText("수신"))
        top_tab.addTab(top_tab.newTab().setText("발신"))

        val tabAdapter = SignalTabAdapter(supportFragmentManager, top_tab.tabCount,group)

        viewPager.adapter = tabAdapter

        top_tab.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(top_tab))


        btn_back.setOnClickListener {
            finish()
        }
    }

}
