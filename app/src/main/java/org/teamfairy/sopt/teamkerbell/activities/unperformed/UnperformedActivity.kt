package org.teamfairy.sopt.teamkerbell.activities.unperformed

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View
import kotlinx.android.synthetic.main.activity_unperformed.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.main.MainActivity
import org.teamfairy.sopt.teamkerbell.activities.unperformed.adapter.UnperformedPageAdapter
import org.teamfairy.sopt.teamkerbell.activities.unperformed.fragment.UnperformedFragment
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.info.UnperformedTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_UNPERFORMED_NOTICE
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_UNPERFORMED_SIGNAL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.TAB_UNPERFORMED_VOTE
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class UnperformedActivity : AppCompatActivity(), UnperformedFragment.OnFragmentInteractionListener, SwipeRefreshLayout.OnRefreshListener {

    override fun onRefresh() {
        connectUnperformed()
        mSwipeRefreshLayout.isRefreshing = false
    }

    var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()


    var group : Team by Delegates.notNull()
    var adapter: UnperformedPageAdapter by Delegates.notNull()

    override fun onFragmentInteraction(msg: Message) {
        when (msg.what) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unperformed)

        group=intent.getParcelableExtra(INTENT_GROUP)


        iv_unperformed_1.setColorFilter(ContextCompat.getColor(applicationContext, R.color.black))
        iv_unperformed_2.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
        iv_unperformed_3.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))

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
                        iv_unperformed_2.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
                        iv_unperformed_3.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
                        iv_unperformed_1.setColorFilter(ContextCompat.getColor(applicationContext, R.color.black))
                    }
                    TAB_UNPERFORMED_SIGNAL -> {
                        iv_unperformed_1.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
                        iv_unperformed_3.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
                        iv_unperformed_2.setColorFilter(ContextCompat.getColor(applicationContext, R.color.black))
                    }
                    TAB_UNPERFORMED_VOTE -> {
                        iv_unperformed_1.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
                        iv_unperformed_2.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
                        iv_unperformed_3.setColorFilter(ContextCompat.getColor(applicationContext, R.color.black))
                    }
                }
            }

        })

        btn_start.setOnClickListener {

            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)

            startActivity(intent)
        }


        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)
    }


    private fun updateDataList(result: HashMap<String, ArrayList<*>>) {
        var cnt = 0
        result.iterator().forEach {
            when {
                it.key == USGS_REQUEST_URL.JSON_NOTICE -> cnt += it.value.size
                it.key == USGS_REQUEST_URL.JSON_SIGNAL -> cnt += it.value.size
                it.key == USGS_REQUEST_URL.JSON_VOTE -> cnt += it.value.size
            }
        }

        adapter.updateList(result)
        if (cnt == 0) {
            btn_start.visibility = View.VISIBLE
        } else
            btn_start.visibility = View.GONE

    }

    override fun onResume() {
        super.onResume()
        connectUnperformed()
    }

    private fun connectUnperformed() {
        val task = UnperformedTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
        task.execute(USGS_REQUEST_URL.URL_UNPERFORMED)
    }


    private class HandlerGet(activity: UnperformedActivity) : Handler() {
        private val mActivity: WeakReference<UnperformedActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()

            if (activity != null) {


                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        activity.updateDataList(msg.obj as HashMap<String, ArrayList<*>>)
                    }
                    else -> {

                    }
                }
            }
        }

    }

}
