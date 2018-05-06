package org.teamfairy.sopt.teamkerbell.activities.items.signal

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import kotlinx.android.synthetic.main.app_bar_commit.*
import kotlinx.android.synthetic.main.content_recyclerview.*
import kotlinx.android.synthetic.main.content_signal.*
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.listview.adapter.ListDataAdapter
import org.teamfairy.sopt.teamkerbell.model.data.SignalResponse
import org.teamfairy.sopt.teamkerbell.model.data.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_DETAIL_LIGHTS_RESPONSE
import org.teamfairy.sopt.teamkerbell.network.info.SignalResponseListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_RESPONDED
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_SIGNAL
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


class SignalActivity : AppCompatActivity(), View.OnClickListener ,SwipeRefreshLayout.OnRefreshListener{
    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectSignalResponseList()
        mSwipeRefreshLayout.isRefreshing=false

    }


    var group: Team by Delegates.notNull()
    var signal : Signal by Delegates.notNull()
    private var responded : Boolean = false


    private var adapter: ListDataAdapter by Delegates.notNull()
    private var dataList: ArrayList<ListDataInterface> = arrayListOf<ListDataInterface>()


    companion object {
        const val RED = 1
        const val YELLOW = 2
        const val GREEN = 4
    }

    private var selectColor = GREEN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signal)
        setSupportActionBar(toolbar)


        group = intent.getParcelableExtra(INTENT_GROUP)
        signal = intent.getParcelableExtra(INTENT_SIGNAL)
        signal.setPhotoInfo(applicationContext)

        tv_name.text = signal.name
        tv_time.text=Utils.getYearMonthDay(signal.write_time)
        tv_content.text= signal.content

        updateUI()

        btn_red.setOnClickListener {
            when(selectColor){
                GREEN -> iv_focus_green.visibility=View.INVISIBLE
                YELLOW ->iv_focus_yellow.visibility=View.INVISIBLE
            }
            selectColor=RED
            iv_focus_red.visibility=View.VISIBLE
            updateColor()
        }
        btn_yellow.setOnClickListener {
            when(selectColor){
                RED-> iv_focus_red.visibility=View.INVISIBLE
                GREEN -> iv_focus_green.visibility=View.INVISIBLE
            }
            selectColor= YELLOW
            iv_focus_yellow.visibility=View.VISIBLE
            updateColor()
        }
        btn_green.setOnClickListener {
            when(selectColor){
                RED-> iv_focus_red.visibility=View.INVISIBLE
                YELLOW -> iv_focus_yellow.visibility=View.INVISIBLE
            }
            selectColor= GREEN
            iv_focus_green.visibility=View.VISIBLE
            updateColor()
        }


        btn_back.setOnClickListener{
            finish()
        }

        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

    }
    private fun updateColor(){

        if(responded)
            connectSignalResponseList()
    }
    private fun updateUI(){

        val btnCommit = findViewById<TextView>(R.id.btn_commit)
        val btnMore = findViewById<ImageButton>(R.id.btn_more)

        responded = intent.getBooleanExtra(INTENT_RESPONDED,false)
        if(responded){
            recyclerView.layoutManager = LinearLayoutManager(this)

            adapter = ListDataAdapter(dataList, applicationContext)
            adapter.setOnItemClick(this)
            recyclerView.adapter = adapter


            connectSignalResponseList()

            btnCommit.visibility= View.GONE
            edt_response.visibility=View.GONE

            btnMore.visibility= View.VISIBLE
            layout_response_list.visibility=View.VISIBLE
            when (selectColor) {
                RED ->
                    iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.red))
                GREEN ->
                    iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.green))
                YELLOW ->
                    iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.yellow))
                else ->
                    iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
            }

        }else{
            btnCommit.visibility= View.VISIBLE
            edt_response.visibility=View.VISIBLE

            btnMore.visibility= View.GONE
            layout_response_list.visibility=View.GONE
        }

    }

    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
    }


    private fun connectSignalResponseList() {
        dataList.clear()
        adapter.notifyDataSetChanged()

        val color = when(selectColor){
            RED-> "r"
            GREEN -> "g"
            YELLOW -> "y"
            else -> "a"
        }
        val task = SignalResponseListTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
        task.execute(URL_DETAIL_LIGHTS_RESPONSE + "/" + color + "/" + group.g_idx + "/" + signal.light_idx)
    }

    fun updateDataList(signalResponseList : ArrayList<SignalResponse>){

        dataList.clear()

        signalResponseList.iterator().forEach {
            it.setPhotoInfo(applicationContext)
            dataList.add(it)
        }
        val strNot = if(selectColor== RED) "미" else ""
        tv_count.text="${dataList.size} 명 ${strNot}응답"

        when (selectColor) {
            RED ->
                iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.red))
            GREEN ->
                iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.green))
            YELLOW ->
                iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.yellow))
            else ->
                iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
        }

        adapter.notifyDataSetChanged()
    }

    private class HandlerGet(activity: SignalActivity) : Handler() {
        private val mActivity: WeakReference<SignalActivity> = WeakReference<SignalActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()

            if (activity != null) {
                val list = msg.obj as ArrayList<SignalResponse>
                activity.updateDataList(list)
            }
        }
    }

}
