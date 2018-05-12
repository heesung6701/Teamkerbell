package org.teamfairy.sopt.teamkerbell.activities.home

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import kotlinx.android.synthetic.main.activity_home.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.Utils
import kotlin.properties.Delegates
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import org.teamfairy.sopt.teamkerbell.activities.home.adapter.MainTabAdapter
import org.teamfairy.sopt.teamkerbell.activities.home.fragment.ContactFragment
import org.teamfairy.sopt.teamkerbell.activities.home.fragment.HomeFragment
import org.teamfairy.sopt.teamkerbell.activities.home.interfaces.HasGroupFragment
import org.teamfairy.sopt.teamkerbell.activities.home.room.MakeRoomActivity
import org.teamfairy.sopt.teamkerbell.activities.home.room.RoomListFragment
import org.teamfairy.sopt.teamkerbell.activities.items.signal.adapter.SignalTabAdapter
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_CONTACT
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_HOME
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_ROOM


class HomeActivity : AppCompatActivity() {
    companion object {
        private const val ARG_GROUP = "GROUP"

    }

    var group: Team by Delegates.notNull()


    var contactTab: ContactFragment by Delegates.notNull()
    var roomListTab: RoomListFragment by Delegates.notNull()
    var homeTab: HomeFragment by Delegates.notNull()


    var tabAdapter : MainTabAdapter by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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

//        this.contactTab = ContactFragment.newInstance(group)
//        this.roomListTab = RoomListFragment.newInstance(group)
//        this.homeTab = HomeFragment.newInstance(group)
//
//        main_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//
//                if (tab!!.position == Utils.TAB_HOME) {
//                    group = homeTab.group
//                }
//
//            }
//
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//
//
//                val fragment: HasGroupFragment? = when (tab!!.position) {
//                    Utils.TAB_HOME -> homeTab
//                    Utils.TAB_ROOM -> roomListTab
//                    Utils.TAB_CONTACT -> contactTab
//                    else -> null
//                }
//
//
//                val f = fragment as Fragment
//                val args = Bundle()
//                args.putParcelable(ARG_GROUP, group)
//                fragment.arguments = args
//
//
//                val fragmentManager = supportFragmentManager
//                val fragmentTransaction = fragmentManager.beginTransaction()
//                fragmentTransaction.replace(R.id.fragment_content, fragment as Fragment)
//                fragmentTransaction.commit()
//
//            }
//
//
//        })
//        main_tab.getTabAt(Utils.TAB_HOME)?.select()
    }

    fun changeGroup(g: Team){
        group=g
        tabAdapter.changeGroup(g)
    }

}
