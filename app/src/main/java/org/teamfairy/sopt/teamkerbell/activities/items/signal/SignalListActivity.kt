package org.teamfairy.sopt.teamkerbell.activities.items.signal

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_signal_list.*
import kotlinx.android.synthetic.main.app_bar_filter_help.*
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.content_signal_list.*
import org.teamfairy.sopt.teamkerbell.activities.items.filter.FilterFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.activities.items.signal.adapter.SignalTabAdapter
import org.teamfairy.sopt.teamkerbell.activities.items.signal.adapter.SignalTutorialAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.Utils
import kotlin.properties.Delegates

class SignalListActivity : AppCompatActivity(),RoomActivityInterface {
    override var room: Room?=null

    private var tutorialPos = 1
    override fun changeRoom(room: Room) {
        this.room=room
        tabAdapter.changeRoom(room)
    }

    override var group : Team by Delegates.notNull()
    private var tabAdapter : SignalTabAdapter by Delegates.notNull()
    private var tutorialAdapter : SignalTutorialAdapter by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signal_list)
        setSupportActionBar(toolbar)
        tv_title.text = supportActionBar!!.title

        group = intent.getParcelableExtra(INTENT_GROUP)
        room = intent.getParcelableExtra(INTENT_ROOM) ?: Room()

        top_tab.addTab(top_tab.newTab().setText(getString(R.string.txt_receiver)))
        top_tab.addTab(top_tab.newTab().setText(getString(R.string.txt_sender)))

        tabAdapter = SignalTabAdapter(supportFragmentManager, top_tab.tabCount,group)

        viewPager.adapter = tabAdapter

        top_tab.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(top_tab))

        FilterFunc(this)

        btn_back.setOnClickListener {
            finish()
        }

        fab.setOnClickListener {
            val i = Intent(applicationContext, MakeSignalActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            i.putExtra(INTENT_GROUP,group)
            i.putExtra(INTENT_ROOM,room)
            viewPager.currentItem = Utils.TAB_REQUEST
            startActivity(i)
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }


        val isNeedTutorial = DatabaseHelpUtils.getPref_isUpdate(applicationContext,DatabaseHelpUtils.PREF_ISUPDATE_TUTORIAL_SIGNAL)
        setTutorial()
        if(isNeedTutorial)
            showTutorial()
        btn_help.setOnClickListener {
            DatabaseHelpUtils.setPref_isUpdate(applicationContext,DatabaseHelpUtils.PREF_ISUPDATE_TUTORIAL_SIGNAL,true)
            showTutorial()
        }
    }

    private var ivSigns : ArrayList<ImageView?> = ArrayList<ImageView?>()


    private fun showTutorial(){
        fab.visibility=View.GONE
        tutorialPos=1
        viewPager_tutorial.setCurrentItem(0,false)

        ivSigns[1]?.setImageDrawable(getDrawable(R.drawable.ic_sign_white))

        for(i in 2..ivSigns.lastIndex)
            ivSigns[i]?.setImageDrawable(getDrawable(R.drawable.ic_sign_gray))

        layout_tutorial.visibility=View.VISIBLE

    }
    private fun setTutorial(){

        ivSigns.clear()
        ivSigns.add(null)
        ivSigns.add(iv_sign_1_tutorial)
        ivSigns.add(iv_sign_2_tutorial)
        ivSigns.add(iv_sign_3_tutorial)
        ivSigns.add(iv_sign_4_tutorial)
        ivSigns.add(iv_sign_5_tutorial)
        ivSigns.add(iv_sign_6_tutorial)




        tutorialAdapter  = SignalTutorialAdapter(supportFragmentManager)
        viewPager_tutorial.adapter=tutorialAdapter

        viewPager_tutorial.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                setTutorial(position+1)
            }

        })


        btn_next_tutorial.setOnClickListener {
            if(tutorialPos>=ivSigns.lastIndex)
                closeTutorial()
            else
                setTutorial(tutorialPos+1)

        }

        iv_close_tutorial.setOnClickListener {
            closeTutorial()
        }

        for(i in 1..ivSigns.lastIndex){
            ivSigns[i]?.setOnClickListener {
                setTutorial(i)
            }
        }
    }
    private fun closeTutorial(){
        DatabaseHelpUtils.setPref_isUpdate(applicationContext,DatabaseHelpUtils.PREF_ISUPDATE_TUTORIAL_SIGNAL,false)
        layout_tutorial.visibility=View.GONE
        fab.visibility=View.VISIBLE
        tutorialPos=0;
    }
    private fun setTutorial(pos : Int){

        ivSigns[tutorialPos]?.setImageDrawable(getDrawable(R.drawable.ic_sign_gray))
        ivSigns[pos]?.setImageDrawable(getDrawable(R.drawable.ic_sign_white))

        viewPager_tutorial.setCurrentItem(pos-1,true)

        tutorialPos=pos

        if(tutorialPos==ivSigns.lastIndex)
            btn_next_tutorial.setText(R.string.action_start)
        else
            btn_next_tutorial.setText(R.string.action_next)
    }

}
