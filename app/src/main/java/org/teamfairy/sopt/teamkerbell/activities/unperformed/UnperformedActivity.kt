package org.teamfairy.sopt.teamkerbell.activities.unperformed

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_unperformed.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.GroupListActivity
import org.teamfairy.sopt.teamkerbell.activities.SplashActivity
import org.teamfairy.sopt.teamkerbell.activities.items.notice.NoticeActivity
import org.teamfairy.sopt.teamkerbell.activities.items.signal.SignalActivity
import org.teamfairy.sopt.teamkerbell.activities.items.vote.VoteActivity
import org.teamfairy.sopt.teamkerbell.activities.unperformed.adapter.UnperformedPageAdapter
import org.teamfairy.sopt.teamkerbell.activities.unperformed.fragment.UnperformedFragment
import org.teamfairy.sopt.teamkerbell.dialog.ConfirmDialog
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.Vote
import org.teamfairy.sopt.teamkerbell.model.realm.BadgeCnt
import org.teamfairy.sopt.teamkerbell.model.realm.BadgeCnt.Companion.WHAT_ROLE
import org.teamfairy.sopt.teamkerbell.model.realm.BadgeCnt.Companion.badgeClearAll
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_GET
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.info.UnperformedTask
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_NOTICE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_SIGNAL
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_VOTE
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_UNPERFORMED_NOTICE
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_UNPERFORMED_SIGNAL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_UNPERFORMED_VOTE
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class UnperformedActivity : AppCompatActivity(), UnperformedFragment.OnFragmentInteractionListener{



    var group : Team by Delegates.notNull()
    var adapter: UnperformedPageAdapter by Delegates.notNull()

    var isConnected= false


    override fun onFragmentInteraction(msg: Message) {
        when (msg.what) {
            TAB_UNPERFORMED_VOTE->{
                val i = Intent(applicationContext,VoteActivity::class.java)
                i.putExtra(INTENT_VOTE,msg.obj as Vote)
                startActivity(i)
            }
            TAB_UNPERFORMED_NOTICE->{
                val i = Intent(applicationContext,NoticeActivity::class.java)
                i.putExtra(INTENT_NOTICE,msg.obj as Notice)
                startActivity(i)

            }

            TAB_UNPERFORMED_SIGNAL->{

                val i = Intent(applicationContext, SignalActivity::class.java)
                i.putExtra(INTENT_SIGNAL,msg.obj as Signal)
                startActivity(i)

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unperformed)


//        Toast.makeText(applicationContext,"onCreate",Toast.LENGTH_SHORT).show()
        iv_unperformed_1.setColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColorP))
        iv_unperformed_2.setColorFilter(ContextCompat.getColor(applicationContext, R.color.grayLight))
        iv_unperformed_3.setColorFilter(ContextCompat.getColor(applicationContext, R.color.grayLight))

        adapter = UnperformedPageAdapter(supportFragmentManager)

        viewPager.adapter = adapter



        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    TAB_UNPERFORMED_NOTICE -> {
                        iv_unperformed_2.setColorFilter(ContextCompat.getColor(applicationContext, R.color.grayLight))
                        iv_unperformed_3.setColorFilter(ContextCompat.getColor(applicationContext, R.color.grayLight))
                        iv_unperformed_1.setColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColorP))

                        updateUI(false)
                    }
                    TAB_UNPERFORMED_SIGNAL -> {
                        iv_unperformed_1.setColorFilter(ContextCompat.getColor(applicationContext, R.color.grayLight))
                        iv_unperformed_3.setColorFilter(ContextCompat.getColor(applicationContext, R.color.grayLight))
                        iv_unperformed_2.setColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColorP))

                        updateUI(false)
                    }
                    TAB_UNPERFORMED_VOTE -> {
                        iv_unperformed_1.setColorFilter(ContextCompat.getColor(applicationContext, R.color.grayLight))
                        iv_unperformed_2.setColorFilter(ContextCompat.getColor(applicationContext, R.color.grayLight))
                        iv_unperformed_3.setColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColorP))

                        updateUI(true)
                    }
                }
            }

        })

        btn_start.setOnClickListener {

            if(it.tag==1){
                val intent = Intent(applicationContext, GroupListActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                showConfirmDialog()
            }

        }

    }
    private fun showConfirmDialog() {
        val message = if(!isConnected)
            getString(R.string.txt_check_internet)
        else
            getString(R.string.txt_unperformed_yet)

        val dialog = ConfirmDialog(this,message)
        dialog.show()
        if(!isConnected){
            dialog.setOnClickListenerYes(View.OnClickListener {
                connectUnperformed()
                dialog.dismiss()
            });
        }
    }


    private fun updateDataList(result: HashMap<String, ArrayList<*>>) {
        var cnt = 0
        result.iterator().forEach {
            when {
                it.key == USGS_REQUEST_URL.JSON_NOTICE -> cnt += it.value.size
                it.key == USGS_REQUEST_URL.JSON_SIGNALS -> cnt += it.value.size
                it.key == USGS_REQUEST_URL.JSON_VOTES -> cnt += it.value.size
            }
        }

        adapter.updateList(result)

        if(cnt == 0) {
            btn_start.background=ContextCompat.getDrawable(applicationContext,R.drawable.shape_round_btn)
            btn_start.tag=1
            btn_start.visibility= View.VISIBLE
        }else{
            btn_start.background=ContextCompat.getDrawable(applicationContext,R.drawable.shape_round_btn_gray)
            btn_start.tag=0
            updateUI(viewPager.currentItem==TAB_UNPERFORMED_VOTE)
        }

//        Toast.makeText(applicationContext,"badgeClear",Toast.LENGTH_SHORT).show()
        BadgeCnt.badgeClearAll(applicationContext)
    }

    private fun updateUI(isShowBtn : Boolean){

        if(btn_start.tag==1) {
            btn_start.visibility = View.VISIBLE
            layout_3dot.visibility=View.GONE
            return
        }

        if(isShowBtn){
            btn_start.visibility=View.VISIBLE
            layout_3dot.visibility=View.GONE
        }else{
            btn_start.visibility=View.GONE
            layout_3dot.visibility=View.VISIBLE
        }
    }
    override fun onResume() {
        super.onResume()
//        Toast.makeText(applicationContext,"onResume",Toast.LENGTH_SHORT).show()
        connectUnperformed()
    }


    private fun connectUnperformed() {
        isConnected=false
        progress.visibility=View.VISIBLE
        val task = UnperformedTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
        task.execute(USGS_REQUEST_URL.URL_UNPERFORMED,METHOD_GET)
    }


    private class HandlerGet(activity: UnperformedActivity) : Handler() {
        private val mActivity: WeakReference<UnperformedActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()



            when (msg.what) {
                Utils.MSG_SUCCESS -> {
                    activity?.isConnected=true
                    activity?.progress?.visibility=View.GONE
                    activity?.updateDataList(msg.obj as HashMap<String, ArrayList<*>>)
                }
                else -> {
                    activity?.isConnected=false
                    activity?.progress?.visibility=View.VISIBLE
                    activity?.showConfirmDialog()

                }
            }
        }

    }

    override fun onBackPressed() {
        val i = Intent(this, SplashActivity::class.java)
        i.putExtra(IntentTag.EXIT,true)
        i.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
        finish()

    }
}
