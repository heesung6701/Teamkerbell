package org.teamfairy.sopt.teamkerbell.activities.items.signal.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalListFragment
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.Utils
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-05-01.
 */
class SignalTabAdapter (fm : FragmentManager?) : FragmentStatePagerAdapter(fm) {

    var tabCount: Int = 0
    var receivedTab: SignalListFragment by Delegates.notNull()
    var requestedTab: SignalListFragment by Delegates.notNull()



    constructor(fm: FragmentManager?, tabCount: Int,group : Team) : this(fm) {
        this.tabCount = tabCount
        this.receivedTab = SignalListFragment()
        receivedTab.group=group
        receivedTab.state=Utils.SIGNAL_RECEIVER

        this.requestedTab = SignalListFragment()
        requestedTab.group=group
        requestedTab.state=Utils.SIGNAL_SENDER
    }

    override fun getItem(position: Int): Fragment? {
        when (position) {
            Utils.TAB_RECEIVE -> {
                return receivedTab
            }
            Utils.TAB_REQUEST -> {
                return requestedTab
            }
        }

        return null
    }

    fun changeRoom(room: Room){
        requestedTab.changeRoom(room)
        receivedTab.changeRoom(room)

    }
    override fun getCount(): Int = tabCount
}
