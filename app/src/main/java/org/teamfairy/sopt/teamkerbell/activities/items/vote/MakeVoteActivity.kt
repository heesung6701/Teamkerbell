package org.teamfairy.sopt.teamkerbell.activities.items.vote

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_commit.*
import kotlinx.android.synthetic.main.content_make_vote.*
import org.json.JSONArray
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.activities.items.filter.SelectRoomFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_VOTE_PARAM_CHOICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_VOTE_PARAM_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_VOTE_PARAM_ENDTIME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_VOTE_PARAM_GID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_VOTE_PARAM_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_VOTE_PARAM_TITLE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_FROM_CHAT
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_VOTE_IDX
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import java.util.*
import kotlin.properties.Delegates

class MakeVoteActivity : AppCompatActivity(), RoomActivityInterface{
    override fun changeRoom(room: Room) {
        this.room = room
    }

    val LOG_TAG = this::class.java.name

    var isConnecting: Boolean = false

    var dataList: ArrayList<Team> = arrayListOf()

    var endDate = "2018-06-05"
    var endTime = "00:00:00"

    override var group: Team by Delegates.notNull()
    override var room: Room? = null


    var cnt_vote_examples = 2
    var voteExamples = arrayListOf<EditText>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_vote)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra<Team>(INTENT_GROUP) as Team
        room = intent.getParcelableExtra(INTENT_ROOM) ?: null

        SelectRoomFunc(this)

        voteExamples.add(edt_vote_example1)
        voteExamples.add(edt_vote_example2)
        btn_vote_example_add.setOnClickListener {

            cnt_vote_examples++
            val edtView = layoutInflater.inflate(R.layout.item_example, null, false)
            voteExamples.add(edtView.findViewById(R.id.edt_vote_example))
            layout_vote_examples.addView(edtView)
        }

        chk_end_time.setOnCheckedChangeListener { _, b ->
            if (b) {
                layout_end_time.visibility = View.VISIBLE
            } else {
                layout_end_time.visibility = View.GONE
            }
        }

        btn_commit.setOnClickListener {

            if (!isConnecting) {
                val title: String
                val content: String

                val voteExampleList = ArrayList<String>()

                voteExamples.iterator().forEach {
                    val str = it.text.toString()
                    if (str.isNotBlank())
                        voteExampleList.add(str.trim())

                }
                if (voteExampleList.size < 2) {
                    Toast.makeText(applicationContext, "최소 답변은 2개 이상입니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                if (room != null) {

                    title = edt_title.text.toString()
                    if (title.isNotEmpty()) {
                        content = edt_content.text.toString()
                        if (content.isNotEmpty()) {

                            val jsonParam = JSONObject()
                            try {
                                jsonParam.put(URL_MAKE_VOTE_PARAM_GID, group.g_idx)
                                jsonParam.put(URL_MAKE_VOTE_PARAM_ROOM_IDX, room!!.room_idx)
                                jsonParam.put(URL_MAKE_VOTE_PARAM_TITLE, title)
                                jsonParam.put(URL_MAKE_VOTE_PARAM_CONTENT, content)
                                if(chk_end_time.isChecked)
                                    jsonParam.put(URL_MAKE_VOTE_PARAM_ENDTIME, "$endDate $endTime")

                                val jsonArray = JSONArray()
                                voteExampleList.iterator().forEach {
                                    jsonArray.put(it)
                                }
                                jsonParam.put(URL_MAKE_VOTE_PARAM_CHOICE, jsonArray)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            val task = GetMessageTask(applicationContext, HandlerMake(this), LoginToken.getToken(applicationContext))
                            isConnecting = true

                            task.execute(USGS_REQUEST_URL.URL_MAKE_VOTE,METHOD_POST, jsonParam.toString())
                        } else
                            Toast.makeText(applicationContext, "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else
                        Toast.makeText(applicationContext, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                } else
                    Toast.makeText(applicationContext, "그룹을 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        }


        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        var day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)


        if (hour > 20)
            day += 1
        btn_end_time_date.text = (year.toString() + "년 " + (month + 1).toString() + "월 " + day.toString() + "일")
        btn_end_time_time.text = ("오후 10:00")

        btn_end_time_date.setOnClickListener {

            var context: Context = ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                context = this
            val datePickerDialog = DatePickerDialog(context, object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                    endDate = getDateFormatStr(year, monthOfYear + 1, dayOfMonth)
                    btn_end_time_date.text = (year.toString() + "년 " + (monthOfYear + 1).toString() + "월 " + dayOfMonth.toString() + "일")
                }
            }, year, month, day)

            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()

        }
        btn_end_time_time.setOnClickListener {
            val dialog = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(timePicker: TimePicker?, hourOfDay: Int, minute: Int) {
                    endTime = getTimeFormatStr(hourOfDay, minute)
                    if (hourOfDay > 12)
                        btn_end_time_time.text = ("오후 " + (hourOfDay - 12).toString() + ":" + if (minute < 10) "0" + minute.toString() else minute.toString())
                    else
                        btn_end_time_time.text = ("오전 " + hourOfDay.toString() + ":" + if (minute < 10) "0" + minute.toString() else minute.toString())

                }
            }, 22, 0, false);

            dialog.show();
        }





        btn_back.setOnClickListener {
            finish()
        }
    }

    fun getTimeFormatStr(hour: Int, minute: Int): String {
        val str = StringBuffer()
        if (hour < 10) str.append("0")
        str.append(hour)
        str.append(":")
        if (minute < 10) str.append("0")
        str.append(minute)
        str.append(":00")
        return str.toString()
    }

    fun getDateFormatStr(year: Int, month: Int, day: Int): String {
        val str = StringBuffer()
        str.append(year)
        str.append("-")
        if (month < 10) str.append("0")
        str.append(month)
        str.append("-")
        if (day < 10) str.append("0")
        str.append(day)

        return str.toString()
    }

    override fun finish() {

        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
    }


    private class HandlerMake(activity: MakeVoteActivity) : Handler() {
        private val mActivity: WeakReference<MakeVoteActivity> = WeakReference<MakeVoteActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        Toast.makeText(activity.applicationContext, "투표가 만들어졌습니다.", Toast.LENGTH_SHORT).show()

                        val obj = msg.obj as String
                        val idx = obj.toInt()

                        Handler().postDelayed(Runnable {
                            if(!activity.intent.getBooleanExtra(INTENT_FROM_CHAT,false)){
                                val intent = Intent(activity.applicationContext, VoteActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                intent.putExtra(INTENT_GROUP, activity.group)
                                intent.putExtra(INTENT_VOTE_IDX, idx.toInt())
                                activity.startActivity(intent)
                            }
                            activity.finish()

                        }, 500)
                    }
                    else -> {
                        val result = msg.data.getString("message")
                        activity.isConnecting = false
                        Toast.makeText(activity.applicationContext, result, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}
