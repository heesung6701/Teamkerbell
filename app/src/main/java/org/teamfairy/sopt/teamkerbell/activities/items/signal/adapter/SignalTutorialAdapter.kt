package org.teamfairy.sopt.teamkerbell.activities.items.signal.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.teamfairy.sopt.teamkerbell.activities.items.signal.tutorial.*

/**
 * Created by lumiere on 2018-05-01.
 */
class SignalTutorialAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    public var tutorial: ArrayList<Fragment> = ArrayList<Fragment>()

    init {
        tutorial.clear()
        tutorial.add(SignalTutorialFragment1())
        tutorial.add(SignalTutorialFragment2())
        tutorial.add(SignalTutorialFragment3())
        tutorial.add(SignalTutorialFragment4())
        tutorial.add(SignalTutorialFragment5())
        tutorial.add(SignalTutorialFragment6())
    }

    override fun getItem(position: Int): Fragment? {
        return tutorial[position]
    }

    override fun getCount(): Int = tutorial.size
}
