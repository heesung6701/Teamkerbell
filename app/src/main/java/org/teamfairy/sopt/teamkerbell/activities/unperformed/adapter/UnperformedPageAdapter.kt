package org.teamfairy.sopt.teamkerbell.activities.unperformed.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.teamfairy.sopt.teamkerbell.activities.unperformed.fragment.UnperformedFragment
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.model.data.Vote
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.utils.Utils
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-05-01.
 */
class UnperformedPageAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    var tabCount: Int = 3
    var noticeTab: UnperformedFragment by Delegates.notNull()
    var signalTab: UnperformedFragment by Delegates.notNull()
    var voteTab: UnperformedFragment by Delegates.notNull()


    init {
        this.noticeTab = UnperformedFragment()
        noticeTab.type= Utils.TAB_UNPERFORMED_NOTICE


        this.signalTab = UnperformedFragment()
        signalTab.type= Utils.TAB_UNPERFORMED_SIGNAL

        this.voteTab = UnperformedFragment()
        voteTab.type=Utils.TAB_UNPERFORMED_VOTE
    }


    override fun getItem(position: Int): Fragment? {
        return when (position) {
            Utils.TAB_UNPERFORMED_NOTICE -> noticeTab
            Utils.TAB_UNPERFORMED_SIGNAL -> signalTab
            Utils.TAB_UNPERFORMED_VOTE -> voteTab
            else -> null
        }

    }

    override fun getCount(): Int = tabCount

    fun updateList(result : HashMap<String,ArrayList<*>>){
        if(result.containsKey(USGS_REQUEST_URL.JSON_NOTICE)) {
            val noticeList = result[USGS_REQUEST_URL.JSON_NOTICE] as ArrayList<Notice>
            noticeTab.updateDataList(noticeList as ArrayList<*>)
        }
        if(result.containsKey(USGS_REQUEST_URL.JSON_VOTE)) {
            val noticeList= result[USGS_REQUEST_URL.JSON_VOTE] as ArrayList<Vote>
            voteTab.updateDataList(noticeList as ArrayList<*>)
        }

        if(result.containsKey(USGS_REQUEST_URL.JSON_SIGNAL)) {
            val signalList  = result[USGS_REQUEST_URL.JSON_SIGNAL] as ArrayList<Signal>
            signalTab.updateDataList(signalList as ArrayList<*>)
        }

    }
}
