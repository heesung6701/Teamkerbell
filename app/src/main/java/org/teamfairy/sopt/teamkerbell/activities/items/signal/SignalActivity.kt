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
import kotlinx.android.synthetic.main.app_bar_response.*
import kotlinx.android.synthetic.main.content_recyclerview.*
import kotlinx.android.synthetic.main.content_signal.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.activities.items.filter.MenuFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.MenuActionInterface
import org.teamfairy.sopt.teamkerbell.dialog.ConfirmDeleteDialog
import org.teamfairy.sopt.teamkerbell.listview.adapter.ListDataAdapter
import org.teamfairy.sopt.teamkerbell.model.data.*
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_DELETE
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_GET
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_DETAIL_LIGHTS_RESPONSE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_DETAIL_SINGLE_LIGHT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_LIGHTS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_LIGHTS_PARAM_COLOR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_LIGHTS_PARAM_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_LIGHTS_PARAM_SIGNAL_IDX
import org.teamfairy.sopt.teamkerbell.network.info.SignalResponseListTask
import org.teamfairy.sopt.teamkerbell.network.info.SignalResponseTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_RESPONDED
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_SIGNAL
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_SIGNAL_IDX
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


class SignalActivity : AppCompatActivity(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, MenuActionInterface {
    override fun menuEdit() {
        attemptEdit()
    }

    override fun menuDelete() {
        showDeleteDialog()
    }

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectSignalResponseList()
        mSwipeRefreshLayout.isRefreshing = false
    }


    private var btnCommit : TextView by Delegates.notNull()
    private var btnMore : ImageButton by Delegates.notNull()


    var group: Team by Delegates.notNull()
    var room: Room by Delegates.notNull()
    var signal: Signal? = null
    private var signalIdx: Int by Delegates.notNull()

    private var responded: Boolean = false

    private var adapter: ListDataAdapter by Delegates.notNull()
    private var dataList: ArrayList<ListDataInterface> = arrayListOf<ListDataInterface>()

    private var isReadMode = false
    private var readSignalContent: String = ""

    companion object {
    }

    private var selectColor = Signal.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signal)
        setSupportActionBar(toolbar)


        btnCommit = findViewById(R.id.btn_commit)
        btnMore = findViewById(R.id.btn_more)

        setUI()

        if (intent.hasExtra(INTENT_SIGNAL)) {
            signal = intent.getParcelableExtra(INTENT_SIGNAL)
            signal!!.setPhotoInfo(applicationContext)
            signal!!.setGroupInfo(applicationContext)
            setSignalInfo()
            connectSignalResponse()
        } else if (intent.hasExtra(INTENT_SIGNAL_IDX)) {
            signalIdx = intent.getIntExtra(INTENT_SIGNAL_IDX, 0)
            connectSignalResponse(signalIdx)
        }

        updateUI()

        btn_red.setOnClickListener {
            if (responded)
                updateColor(Signal.RED)
        }
        btn_yellow.setOnClickListener {
            updateColor(Signal.YELLOW)
        }
        btn_green.setOnClickListener {
            updateColor(Signal.GREEN)
        }


        btn_back.setOnClickListener {
            if (isReadMode) {
                isReadMode = false
                readSignalContent = ""
                updateUI()
            } else finish()
        }



        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

    }

    private fun setSignalInfo() {
        val signal = this.signal!!
        room = intent.getParcelableExtra(INTENT_ROOM) ?: DatabaseHelpUtils.getRoom(applicationContext, signal.room_idx)
        group = intent.getParcelableExtra(INTENT_GROUP) ?: DatabaseHelpUtils.getGroup(applicationContext, room.g_idx)
        if (room.room_idx == Room.ARG_ALL_IDX || room.room_idx == Room.ARG_NULL_IDX)
            room = DatabaseHelpUtils.getRoom(applicationContext, signal.room_idx)

        updateColor(Signal.colorStrToByte(signal.responseColor))

        tv_name.text = signal.name
        tv_time.text = Utils.getYearMonthDay(signal.write_time)
        tv_content.text = signal.content

    }

    private fun updateColor(c: Byte) {
        if (selectColor != Signal.DEFAULT && selectColor == c) return
        when (selectColor) {
            Signal.GREEN -> iv_focus_green.visibility = View.INVISIBLE
            Signal.YELLOW -> iv_focus_yellow.visibility = View.INVISIBLE
            Signal.RED -> iv_focus_red.visibility = View.INVISIBLE
        }

        selectColor = c

        when (selectColor) {
            Signal.GREEN -> iv_focus_green.visibility = View.VISIBLE
            Signal.YELLOW -> iv_focus_yellow.visibility = View.VISIBLE
            Signal.RED -> iv_focus_red.visibility = View.VISIBLE
        }

        if (responded)
            updateUI()
        else
            if (selectColor == Signal.RED || selectColor == Signal.DEFAULT) {
                edt_response.setText(getString(R.string.txt_wait_response))
                edt_response.isEnabled = false
            } else {
                edt_response.setText("")
                edt_response.isEnabled = true
            }
    }


    private fun setUI() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ListDataAdapter(dataList, applicationContext)
        adapter.setOnItemClick(this)
        recyclerView.adapter = adapter


        btn_commit.setOnClickListener {
            if (signal != null)
                attemptCommit()
        }
    }

    private fun updateUI() {


        responded = intent.getBooleanExtra(INTENT_RESPONDED, responded)

        when {
            isReadMode -> {
                btnCommit.visibility = View.GONE
                edt_response.visibility = View.VISIBLE
                edt_response.isEnabled = false

                if(signal?.u_idx==LoginToken.getUserIdx(applicationContext))
                    MenuFunc(this,MenuFunc.MENU_OPT.DELETE_ONLY)
                else btnMore.visibility=View.INVISIBLE
                layout_response_list.visibility = View.GONE

                edt_response.setText(readSignalContent)

                enableSignButton(false)

            }
            responded -> {

                connectSignalResponseList()

                btnCommit.visibility = View.GONE
                edt_response.visibility = View.GONE

                if(signal?.u_idx==LoginToken.getUserIdx(applicationContext))
                    MenuFunc(this,MenuFunc.MENU_OPT.DELETE_ONLY)
                else btnMore.visibility=View.INVISIBLE
                layout_response_list.visibility = View.VISIBLE


                when (selectColor) {
                    Signal.RED ->
                        iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.red))
                    Signal.GREEN ->
                        iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.green))
                    Signal.YELLOW ->
                        iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.yellow))
                    else ->
                        iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
                }

                enableSignButton(true)

            }
            else -> {
                if (signal != null)
                    updateColor(Signal.colorStrToByte(signal!!.responseColor))

                edt_response.visibility = View.VISIBLE
                layout_response_list.visibility = View.GONE

                btnCommit.visibility=View.VISIBLE
                btnMore.visibility = View.GONE

                enableSignButton(true)

            }
        }
    }

    private fun enableSignButton(b: Boolean) {
        btn_red.isEnabled = b
        btn_yellow.isEnabled = b
        btn_green.isEnabled = b
    }


    private fun showDeleteDialog() {

        val dialog = ConfirmDeleteDialog(this)
        dialog.show()

        dialog.setOnClickListenerYes(View.OnClickListener {
            signal?.let { attemptDelete(it) }
        })
    }
    private fun attemptDelete(signal: Signal){
        val task = GetMessageTask(applicationContext, HandlerDelete(this), LoginToken.getToken(applicationContext))

        val jsonParam = JSONObject()
        jsonParam.put(USGS_REQUEST_URL.URL_REMOVE_SIGNAL_PARAMS_SIGNAL_IDX, signal.signal_idx)

        task.execute(USGS_REQUEST_URL.URL_REMOVE_SIGNAL, METHOD_DELETE,jsonParam.toString())

    }
    private  fun attemptEdit(){

    }

    private fun attemptCommit() {
        val task = GetMessageTask(applicationContext, HandlerResponse(this), LoginToken.getToken(applicationContext))

        val content = edt_response.text!!.toString()
        if (selectColor == Signal.RED || selectColor == Signal.DEFAULT) {
            Toast.makeText(applicationContext, "신호등을 초록 또는 노랑을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (content.isNotEmpty()) {
            val jsonParam = JSONObject()

            val color = Signal.colorByteToStr(selectColor)

            try {
                jsonParam.put(URL_RESPONSE_LIGHTS_PARAM_SIGNAL_IDX, signal!!.signal_idx)
                jsonParam.put(URL_RESPONSE_LIGHTS_PARAM_COLOR, color)
                jsonParam.put(URL_RESPONSE_LIGHTS_PARAM_CONTENT, content)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            task.execute(URL_RESPONSE_LIGHTS, METHOD_POST, jsonParam.toString())
        } else {
            Toast.makeText(applicationContext, "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onClick(p0: View?) {

        val pos = recyclerView.getChildAdapterPosition(p0)
        val signalResponse = dataList[pos] as SignalResponse

        if (selectColor == Signal.RED && signalResponse.content.isNullOrBlank()) return
        readSignalContent = signalResponse.content!!
        isReadMode = true
        updateUI()
    }


    private fun connectSignalResponseList() {
        dataList.clear()
        adapter.notifyDataSetChanged()

        val color = Signal.colorByteToStr(selectColor)
        val task = SignalResponseListTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))

        val url = URL_DETAIL_LIGHTS_RESPONSE + "/" + color + "/" + room.room_idx + "/" + (signal?.signal_idx
                ?: signalIdx)
        task.execute(url, METHOD_GET)
    }

    private fun connectSignalResponse(signal_idx: Int) {

        val task = SignalResponseTask(applicationContext, HandlerGetSingle(this), LoginToken.getToken(applicationContext))

        val url = "$URL_DETAIL_SINGLE_LIGHT/$signal_idx"
        task.execute(url, METHOD_GET)
    }

    private fun connectSignalResponse() = connectSignalResponse(signal?.signal_idx ?: signalIdx)

    fun updateDataList(signalResponseList: ArrayList<SignalResponse>) {

        dataList.clear()

        signalResponseList.iterator().forEach {
            it.setPhotoInfo(applicationContext)
            dataList.add(it)
        }
        val strNot = if (selectColor == Signal.RED) "미" else ""
        tv_count.text = ("${dataList.size} 명 ${strNot}응답")

        when (selectColor) {
            Signal.RED ->
                iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.red))
            Signal.GREEN ->
                iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.green))
            Signal.YELLOW ->
                iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.yellow))
            else ->
                iv_sign.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
        }

        adapter.notifyDataSetChanged()
    }

    fun updateSignal(signal: Signal) {

        if (this.signal == null) {
            this.signal = signal
            if (signal.responseColor.equals(Signal.STR_GREEN) || signal.u_idx == LoginToken.getUserIdx(applicationContext)) {
                responded = true

                this.signal!!.setPhotoInfo(applicationContext)
                setSignalInfo()
                updateUI()
                connectSignalResponseList()
            } else {
                setSignalInfo()
            }
        }
        if (Signal.colorStrToByte(signal.responseColor) != Signal.RED && Signal.colorStrToByte(signal.responseColor) != Signal.DEFAULT)
            edt_response.setText(signal.responseContent)
        updateColor(Signal.colorStrToByte(signal.responseColor))
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

    private class HandlerGetSingle(activity: SignalActivity) : Handler() {
        private val mActivity: WeakReference<SignalActivity> = WeakReference<SignalActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()

            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        val result = msg.obj as Signal
                        activity.updateSignal(result)
                    }
                    else -> {
                        val message = msg.data.getString(USGS_REQUEST_URL.JSON_MESSAGE)
                        if(message.contains("Success") || message.contains("Internal"))
                            Toast.makeText(activity.applicationContext, activity.getString(R.string.txt_deleted_item), Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(activity.applicationContext, activity.getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
                        activity.finish()
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
                        Toast.makeText(activity.applicationContext, activity.getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
                        activity.finish()
                    }

                }

            }
        }
    }

    fun deleteResult(msg: Message) {
        when (msg.what) {
            Utils.MSG_SUCCESS -> {
                Toast.makeText(applicationContext,getString(R.string.txt_delete_success),Toast.LENGTH_SHORT).show()
                finish()
            }
            else -> {
                Toast.makeText(applicationContext,getString(R.string.txt_message_fail),Toast.LENGTH_SHORT).show()
            }
        }

    }
    private class HandlerDelete(activity: SignalActivity) : Handler() {
        private val mActivity: WeakReference<SignalActivity> = WeakReference<SignalActivity>(activity)

        override fun handleMessage(msg: Message) {
            mActivity.get()?.deleteResult(msg)
        }
    }
}
