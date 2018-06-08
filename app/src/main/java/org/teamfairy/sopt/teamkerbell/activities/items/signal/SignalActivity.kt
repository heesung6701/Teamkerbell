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
import android.widget.Toast
import com.google.android.gms.internal.se
import kotlinx.android.synthetic.main.app_bar_response.*
import kotlinx.android.synthetic.main.content_recyclerview.*
import kotlinx.android.synthetic.main.content_signal.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.listview.adapter.ListDataAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.SignalResponse
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_DETAIL_LIGHTS_RESPONSE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_LIGHTS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_LIGHTS_PARAM_COLOR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_LIGHTS_PARAM_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_LIGHTS_PARAM_SIGNAL_IDX
import org.teamfairy.sopt.teamkerbell.network.info.SignalResponseListTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_RESPONDED
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_SIGNAL
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


class SignalActivity : AppCompatActivity(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectSignalResponseList()
        mSwipeRefreshLayout.isRefreshing = false
    }

    var group: Team by Delegates.notNull()
    var room: Room by Delegates.notNull()
    var signal: Signal by Delegates.notNull()
    private var responded: Boolean = false

    private var adapter: ListDataAdapter by Delegates.notNull()
    private var dataList: ArrayList<ListDataInterface> = arrayListOf<ListDataInterface>()

    companion object {
        const val RED = 1
        const val YELLOW = 2
        const val GREEN = 4
        const val DEFAULT = 8
    }

    private var selectColor = DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signal)
        setSupportActionBar(toolbar)



        signal = intent.getParcelableExtra(INTENT_SIGNAL)
        signal.setPhotoInfo(applicationContext)
        signal.setGroupInfo(applicationContext)

        room = intent.getParcelableExtra(INTENT_ROOM) ?: DatabaseHelpUtils.getRoom(applicationContext, signal.room_idx)
        group = intent.getParcelableExtra(INTENT_GROUP)?:DatabaseHelpUtils.getGroup(applicationContext,room.g_idx)
        if (room.room_idx == Room.ARG_ALL_IDX || room.room_idx == Room.ARG_NULL_IDX)
            room = DatabaseHelpUtils.getRoom(applicationContext, signal.room_idx)


        updateColor(GREEN)

        tv_name.text = signal.name
        tv_time.text = Utils.getYearMonthDay(signal.write_time)
        tv_content.text = signal.content

        updateUI()

        btn_red.setOnClickListener {
            updateColor(RED)
        }
        btn_yellow.setOnClickListener {
            updateColor(YELLOW)
        }
        btn_green.setOnClickListener {
            updateColor(GREEN)
        }


        btn_back.setOnClickListener {
            finish()
        }

        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

    }

    private fun updateColor(c: Int) {
        if (selectColor != DEFAULT && selectColor == c) return
        when (selectColor) {
            GREEN -> iv_focus_green.visibility = View.INVISIBLE
            YELLOW -> iv_focus_yellow.visibility = View.INVISIBLE
            RED -> iv_focus_red.visibility = View.INVISIBLE
        }

        selectColor = c

        when (selectColor) {
            GREEN -> iv_focus_green.visibility = View.VISIBLE
            YELLOW -> iv_focus_yellow.visibility = View.VISIBLE
            RED -> iv_focus_red.visibility = View.VISIBLE
        }

        if (responded)
            connectSignalResponseList()
    }

    private fun updateUI() {

        val btnCommit = findViewById<TextView>(R.id.btn_commit)
        val btnMore = findViewById<ImageButton>(R.id.btn_more)

        responded = intent.getBooleanExtra(INTENT_RESPONDED, false)
        if (responded) {
            recyclerView.layoutManager = LinearLayoutManager(this)

            adapter = ListDataAdapter(dataList, applicationContext)
            adapter.setOnItemClick(this)
            recyclerView.adapter = adapter


            connectSignalResponseList()

            btnCommit.visibility = View.GONE
            edt_response.visibility = View.GONE

            btnMore.visibility = View.VISIBLE
            layout_response_list.visibility = View.VISIBLE


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

        } else {
            when (signal.color) {
                "r" -> updateColor(RED)
                "g" -> updateColor(GREEN)
                "y" -> updateColor(YELLOW)
                "a" -> updateColor(RED)
            }


            btnCommit.visibility = View.VISIBLE
            edt_response.visibility = View.VISIBLE

            btnMore.visibility = View.GONE
            layout_response_list.visibility = View.GONE


        }
        btn_commit.setOnClickListener {
            attemptCommit()
        }
    }

    private fun attemptCommit() {
        val task = GetMessageTask(applicationContext, HandlerResponse(this), LoginToken.getToken(applicationContext))

        val content = edt_response.text!!.toString()
        if (content.isNotEmpty()) {
            val jsonParam = JSONObject()

            val color = when (selectColor) {
                RED -> "r"
                GREEN -> "g"
                YELLOW -> "y"
                else -> "a"
            }

            try {
                jsonParam.put(URL_RESPONSE_LIGHTS_PARAM_SIGNAL_IDX, signal.signal_idx)
                jsonParam.put(URL_RESPONSE_LIGHTS_PARAM_COLOR, color)
                jsonParam.put(URL_RESPONSE_LIGHTS_PARAM_CONTENT, content)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            task.execute(URL_RESPONSE_LIGHTS, jsonParam.toString())
        } else {
            Toast.makeText(applicationContext, "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onClick(p0: View?) {
        val pos = recyclerView.getChildAdapterPosition(p0)
    }


    private fun connectSignalResponseList() {
        dataList.clear()
        adapter.notifyDataSetChanged()

        val color = when (selectColor) {
            RED -> "r"
            GREEN -> "g"
            YELLOW -> "y"
            else -> "a"
        }
        val task = SignalResponseListTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
        task.execute(URL_DETAIL_LIGHTS_RESPONSE + "/" + color + "/" + room.room_idx + "/" + signal.signal_idx)
    }

    fun updateDataList(signalResponseList: ArrayList<SignalResponse>) {

        dataList.clear()

        signalResponseList.iterator().forEach {
            it.setPhotoInfo(applicationContext)
            dataList.add(it)
        }
        val strNot = if (selectColor == RED) "미" else ""
        tv_count.text = ("${dataList.size} 명 ${strNot}응답")

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
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        val list = msg.obj as ArrayList<SignalResponse>
                        activity.updateDataList(list)
                    }
                    else -> {

                    }
                }
            }
        }
    }


    private class HandlerResponse(activity: SignalActivity) : Handler() {
        private val mActivity: WeakReference<SignalActivity> = WeakReference<SignalActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        Toast.makeText(activity.applicationContext, "신호등을 보냈습니다.", Toast.LENGTH_SHORT).show()
                        activity.finish()
                    }
                    else -> {
                        val result = msg.data.getString("message")
                        Toast.makeText(activity.applicationContext, result, Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }
    }
}
