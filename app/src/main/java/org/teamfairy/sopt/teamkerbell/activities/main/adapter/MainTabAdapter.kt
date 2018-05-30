package org.teamfairy.sopt.teamkerbell.activities.main.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.teamfairy.sopt.teamkerbell.activities.main.contact.ContactFragment
import org.teamfairy.sopt.teamkerbell.activities.main.home.HomeFragment
import org.teamfairy.sopt.teamkerbell.activities.main.room.RoomListFragment
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.Utils
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-05-01.
 */
class MainTabAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    var tabCount: Int = 0
    var homeTab: HomeFragment by Delegates.notNull()
    var roomTab: RoomListFragment by Delegates.notNull()
    var contactTab: ContactFragment by Delegates.notNull()


    constructor(fm: FragmentManager?, tabCount: Int, group: Team) : this(fm) {
        this.tabCount = tabCount

        this.homeTab = HomeFragment()
        this.homeTab.group=group
        this.roomTab = RoomListFragment()
        this.roomTab.group=group
        this.contactTab = ContactFragment()
        this.contactTab.group=group

    }

    fun changeGroup(g: Team) {
        homeTab.changeGroup(g)
        roomTab.changeGroup(g)
        contactTab.changeGroup(g)
    }

    override fun getItem(position: Int): Fragment? {
        return when (position) {
            Utils.TAB_HOME -> homeTab
            Utils.TAB_ROOM -> roomTab
            Utils.TAB_CONTACT -> contactTab
            else -> null
        }

    }

    override fun getCount(): Int = tabCount
}
