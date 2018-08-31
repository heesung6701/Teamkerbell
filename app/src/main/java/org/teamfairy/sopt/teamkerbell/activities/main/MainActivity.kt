package org.teamfairy.sopt.teamkerbell.activities.main

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import kotlin.properties.Delegates
import android.support.v4.view.ViewPager
import org.teamfairy.sopt.teamkerbell.activities.main.adapter.MainTabAdapter
import org.teamfairy.sopt.teamkerbell.activities.main.contact.ContactFragment
import org.teamfairy.sopt.teamkerbell.activities.main.home.HomeFragment
import org.teamfairy.sopt.teamkerbell.activities.main.room.RoomListFragment
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_CONTACT
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_HOME
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_ROOM


class MainActivity : AppCompatActivity() {


    var group: Team by Delegates.notNull()


    var tabAdapter : MainTabAdapter by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        group = intent.getParcelableExtra<Team>(INTENT_GROUP)


        main_tab.addTab(main_tab.newTab().setIcon(R.drawable.icon_tab_contact))
        main_tab.addTab(main_tab.newTab().setIcon(R.drawable.icon_tab_chat))
        main_tab.addTab(main_tab.newTab().setIcon(R.drawable.icon_tab_home))

        main_tab.tabGravity = TabLayout.GRAVITY_FILL


        tabAdapter = MainTabAdapter(supportFragmentManager, main_tab.tabCount,group)

        viewPager.adapter = tabAdapter

        main_tab.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(main_tab))
        viewPager.setCurrentItem(TAB_HOME,false)

        viewPager.addOnPageChangeListener(object  : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                when(position){
                    TAB_CONTACT->{
                        fab.hide()
                    }
                    TAB_ROOM->{
                        fab.show()
                    }
                    TAB_HOME->{
                        fab.hide()
                    }
                }
            }

        })

    }

    fun changeGroup(g: Team){
        group=g
        tabAdapter.changeGroup(g)
    }



}
