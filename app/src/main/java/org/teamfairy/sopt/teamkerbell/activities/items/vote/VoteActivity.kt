package org.teamfairy.sopt.teamkerbell.activities.items.vote

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_more.*
import kotlinx.android.synthetic.main.content_vote.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.activities.items.filter.MenuFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.MenuActionInterface
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter.ChoiceListAdapter
import org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter.ResultByChoiceListAdapter
import org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter.ResultByMemberListAdapter
import org.teamfairy.sopt.teamkerbell.dialog.ConfirmDeleteDialog
import org.teamfairy.sopt.teamkerbell.model.data.*
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_DELETE
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_GET
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_PUT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_VOTE_PARAM_VALUE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_VOTE_PARAM_VOTEID
import org.teamfairy.sopt.teamkerbell.network.info.VoteResponseTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_USER
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_VOTE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_VOTE_IDX
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class VoteActivity : AppCompatActivity(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener , MenuActionInterface{
    override fun menuEdit() {
        attemptEdit()
    }

    override fun menuDelete() {
        showDeleteDialog()
    }

    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectVoteResponse(vote?.vote_idx ?: 1)
        mSwipeRefreshLayout.isRefreshing = false
    }


    private var fromList = false

    var group: Team by Delegates.notNull()
    var room: Room by Delegates.notNull()

    var vote: Vote?=null
    private var voteIdx : Int by Delegates.notNull()

    var voteResponse: VoteResponse by Delegates.notNull()


    private var dataListChoice: ArrayList<HashMap<String, String>> = arrayListOf<HashMap<String, String>>()

    private var recyclerChoice: RecyclerView by Delegates.notNull()
    private var adapterChoice: ChoiceListAdapter by Delegates.notNull()


    private var dataListResult: ArrayList<HashMap<String, String>> = arrayListOf<HashMap<String, String>>()
    private var recyclerResult: RecyclerView by Delegates.notNull()

    private var adapterResultC: ResultByChoiceListAdapter by Delegates.notNull()
    private var adapterResultM: ResultByMemberListAdapter by Delegates.notNull()
    private var adapterResultN: ResultByChoiceListAdapter by Delegates.notNull()

    private var isShowResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote)
        setSupportActionBar(toolbar)



        recyclerChoice = findViewById(R.id.recyclerView_choice)
        recyclerChoice.layoutManager = LinearLayoutManager(this)
        adapterChoice = ChoiceListAdapter(dataListChoice, applicationContext)
        adapterChoice.setOnItemClick(this)
        recyclerChoice.adapter = adapterChoice

        recyclerResult = findViewById(R.id.recyclerView)
        recyclerResult.layoutManager = LinearLayoutManager(this)
        adapterResultC = ResultByChoiceListAdapter(dataListResult, applicationContext)
        adapterResultM = ResultByMemberListAdapter(dataListResult, applicationContext)
        adapterResultN = ResultByChoiceListAdapter(dataListResult, applicationContext)
        recyclerResult.adapter = adapterResultC


        recentTap = btn_by_choice


        when {
            intent.hasExtra(INTENT_VOTE) -> {
                vote = intent.getParcelableExtra<Vote>(INTENT_VOTE)
                vote!!.setPhotoInfo(applicationContext)
                setVoteInfo()

            }
            intent.hasExtra(INTENT_VOTE_IDX) -> voteIdx=intent.getIntExtra(INTENT_VOTE_IDX,0)
            else -> finish()
        }



        btn_by_choice.setOnClickListener(resultTabOnClickListener)
        btn_by_member.setOnClickListener(resultTabOnClickListener)
        btn_by_not_voted.setOnClickListener(resultTabOnClickListener)


        mSwipeRefreshLayout = findViewById(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)




        btn_back.setOnClickListener {
            finish()
        }

        tv_count.setOnClickListener {
            showResult()
            updateResultList()
        }
        tv_back_choice.setOnClickListener {
            showChoices()
            updateChoiceList()
        }




        layout_send_noti.setOnClickListener {
            val task = GetMessageTask(applicationContext, HandlerPress(this), LoginToken.getToken(applicationContext))
            val jsonParam = JSONObject()
            jsonParam.put(USGS_REQUEST_URL.URL_RESPONSE_PRESS_VOTEID, voteResponse.vote.vote_idx)
            jsonParam.put(USGS_REQUEST_URL.URL_RESPONSE_PRESS_GID, group.g_idx)
            task.execute(USGS_REQUEST_URL.URL_RESPONSE_PRESS, METHOD_POST, jsonParam.toString())
        }

        connectVoteResponse(vote?.vote_idx ?: voteIdx)


    }

    override fun onResume() {
        super.onResume()
        if(vote !=null)
            setVoteInfo()
    }

    private fun attemptEdit(){

    }

    private fun showDeleteDialog() {

        val dialog = ConfirmDeleteDialog(this)
        dialog.show()

        dialog.setOnClickListenerYes(View.OnClickListener {
            vote?.let { attemptDelete(it) }
        })
    }
    private fun attemptDelete(vote : Vote){
        val task = GetMessageTask(applicationContext, HandlerDelete(this), LoginToken.getToken(applicationContext))

        val jsonParam = JSONObject()
        jsonParam.put(USGS_REQUEST_URL.URL_REMOVE_VOTE_PARAMS_VOTE_IDX, vote.vote_idx)

        task.execute(USGS_REQUEST_URL.URL_REMOVE_VOTE, METHOD_DELETE,jsonParam.toString())

    }

    private fun setVoteInfo() {
        val vote = this.vote!!

        if(vote.u_idx==LoginToken.getUserIdx(applicationContext))
            MenuFunc(this, MenuFunc.MENU_OPT.DELETE_ONLY)

        room = intent.getParcelableExtra(INTENT_ROOM) ?: DatabaseHelpUtils.getRoom(applicationContext, vote.room_idx)
        group = intent.getParcelableExtra(INTENT_GROUP) ?: DatabaseHelpUtils.getGroup(applicationContext, room.g_idx)

        supportActionBar!!.title = vote.title
        tv_chat_name.text = group.real_name

        tv_content.text = vote.content
        if (NetworkUtils.getBitmapList(vote.photo, iv_profile, applicationContext, "user${vote.u_idx}"))
            iv_profile.setImageResource(R.drawable.icon_profile_default)
        tv_name.text = vote.name
        tv_time.text = vote.getTime()

        if (vote.isFinished()) {
            showResult()
            btn_complete.visibility = View.GONE
        } else {
            showChoices()
            enableCompleteButton()
            btn_complete.setOnClickListener {
                updateVoteResponse()
            }
        }

    }

    private var recentTap: TextView by Delegates.notNull()
    private var resultTabOnClickListener = View.OnClickListener {
        changeResultTap(it.id)
        updateResultList()
    }

    private fun changeResultTap(it: Int) {

        dataListResult.clear()
        when (it) {
            R.id.btn_by_choice -> {
                layout_send_noti.visibility = View.GONE
                recentTap.setTextColor(ContextCompat.getColor(applicationContext, R.color.gray))
                btn_by_choice.setTextColor(ContextCompat.getColor(applicationContext, R.color.mainColor))
                recentTap = btn_by_choice
                recyclerResult.adapter = adapterResultC
            }
            R.id.btn_by_member -> {
                layout_send_noti.visibility = View.GONE
                recentTap.setTextColor(ContextCompat.getColor(applicationContext, R.color.gray))
                btn_by_member.setTextColor(ContextCompat.getColor(applicationContext, R.color.mainColor))
                recentTap = btn_by_member

                recyclerResult.adapter = adapterResultM
            }
            R.id.btn_by_not_voted -> {
                layout_send_noti.visibility = View.VISIBLE
                recentTap.setTextColor(ContextCompat.getColor(applicationContext, R.color.gray))
                btn_by_not_voted.setTextColor(ContextCompat.getColor(applicationContext, R.color.mainColor))
                recentTap = btn_by_not_voted

                recyclerResult.adapter = adapterResultN
            }
        }

    }

    private fun updateResultList() {

        dataListResult.clear()
        when (recentTap.id) {
            R.id.btn_by_choice -> {

                tv_back_choice.visibility = View.VISIBLE

                voteResponse.examples.iterator().forEach {
                    val choiceIdx = it.key
                    var count = 0

                    val hashChoice = HashMap<String, String>()
                    hashChoice["type"] = "header"
                    hashChoice["count"] = count.toString()
                    hashChoice["content"] = it.value
                    dataListResult.add(hashChoice)

                    voteResponse.responses.iterator().forEach {
                        if (it.value == choiceIdx) {

                            val hashResponse = HashMap<String, String>()
                            hashResponse["type"] = "item"
                            val user = DatabaseHelpUtils.getUser(applicationContext, it.key)
                            hashResponse["name"] = user.name.toString()
                            hashResponse["u_idx"] = user.u_idx.toString()
                            hashResponse["photo"] = user.photo.toString()

                            dataListResult.add(hashResponse)
                            count++
                        }
                    }

                    hashChoice["count"] = count.toString()
                }


                recyclerResult.adapter.notifyDataSetChanged()
            }
            R.id.btn_by_member -> {


                tv_back_choice.visibility = View.GONE
                voteResponse.responses.iterator().forEach {
                    val h = HashMap<String, String>()

                    val user = DatabaseHelpUtils.getUser(applicationContext, it.key)
                    val choice = voteResponse.examples[it.value]

                    h["u_idx"] = user.u_idx.toString()
                    h["photo"] = user.photo.toString()
                    h["name"] = user.name.toString()
                    h["content"] = choice ?: "투표내용"
                    h["time"] = "투표한 시간도 보내주나??" //상형한테 물어봐야제


                    dataListResult.add(h)

                }

                recyclerResult.adapter.notifyDataSetChanged()
            }
            R.id.btn_by_not_voted -> {

                tv_back_choice.visibility = View.GONE

                voteResponse.responses.iterator().forEach {

                    val choice = voteResponse.examples[it.value]

                    if (choice == null) {
                        val hashResponse = HashMap<String, String>()

                        val u = DatabaseHelpUtils.getUser(applicationContext, it.key)
                        hashResponse["name"] = u.name.toString()
                        hashResponse["u_idx"] = u.u_idx.toString()
                        hashResponse["photo"] = u.photo.toString()

                        dataListResult.add(hashResponse)
                    }

                }

                recyclerResult.adapter.notifyDataSetChanged()
            }
        }

    }


    private fun enableCompleteButton() {
        if (unableVote()) {
            btn_complete.isEnabled = false
            btn_complete.background = ContextCompat.getDrawable(applicationContext, R.drawable.shape_round_btn_gray_light)
        } else {
            btn_complete.isEnabled = true
            btn_complete.background = ContextCompat.getDrawable(applicationContext, R.drawable.shape_round_btn)
        }
    }

    private fun unableVote(): Boolean = (vote!!.isFinished() || adapterChoice.selectedId != -1 && (dataListChoice[adapterChoice.selectedId].containsKey("choice_idx")
            && dataListChoice[adapterChoice.selectedId]["choice_idx"]!!.toInt() == voteResponse.responses[LoginToken.getUserIdx(applicationContext)]))

    private fun showResult() {
        isShowResult = true

        changeResultTap(R.id.btn_by_choice)

        layout_choices.visibility = View.GONE
        layout_result.visibility = View.VISIBLE
    }

    private fun showChoices() {

        isShowResult = false

        layout_choices.visibility = View.VISIBLE
        layout_result.visibility = View.GONE


    }

    override fun onClick(p0: View?) {
        val pos = recyclerChoice.getChildAdapterPosition(p0)
        if (vote!=null && !vote!!.isFinished()) {
            adapterChoice.selectedId = pos
            adapterChoice.notifyDataSetChanged()

            enableCompleteButton()
        }
    }

    fun updateChoiceList() {

        val userChoiceIdx = if (isVoted()) voteResponse.responses[LoginToken.getUserIdx(applicationContext)]!! else -1



        dataListChoice.clear()

        var totalCnt = 0
        voteResponse.examples.iterator().forEach {
            val h = HashMap<String, String>()
            h["content"] = it.value
            var cnt = 0
            val choiceId = it.key
            voteResponse.responses.iterator().forEach {
                if (it.value == choiceId)
                    cnt++
            }
            h["count"] = cnt.toString() + " 명"
            totalCnt += cnt
            h["choice_idx"] = it.key.toString()


            dataListChoice.add(h)

            if (it.key == userChoiceIdx)
                adapterChoice.selectedId = dataListChoice.lastIndex
        }

        tv_count.text = ("""${totalCnt.toString()} 명 참여중""")
        adapterChoice.notifyDataSetChanged()


        enableCompleteButton()
    }


    private fun updateVoteResponse() {
        val selectIdx = adapterChoice.selectedId
        if (selectIdx != -1) {
            if (dataListChoice[selectIdx].containsKey("choice_idx")
                    && dataListChoice[selectIdx]["choice_idx"]!!.toInt() == voteResponse.responses[LoginToken.getUserIdx(applicationContext)]) {
                Toast.makeText(applicationContext, "변경사항이 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val task = GetMessageTask(applicationContext, HandlerUpdateVoteResponse(this), LoginToken.getToken(applicationContext))

                val jsonParam = JSONObject()

                try {
                    jsonParam.put(URL_RESPONSE_VOTE_PARAM_VALUE, dataListChoice[selectIdx]["choice_idx"])
                    jsonParam.put(URL_RESPONSE_VOTE_PARAM_VOTEID, voteResponse.vote.vote_idx)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                task.execute(USGS_REQUEST_URL.URL_RESPONSE_VOTE, METHOD_PUT, jsonParam.toString())
            }

        } else {
            Toast.makeText(applicationContext, "항목을 선택해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isVoted(): Boolean = voteResponse.responses.containsKey(LoginToken.getUserIdx(applicationContext))

    fun updateSuccess() {
        if (fromList) finish()

        voteResponse.responses[LoginToken.getUserIdx(applicationContext)] = dataListChoice[adapterChoice.selectedId]["choice_idx"]!!.toInt()
        showResult()
        updateResultList()

    }

    private fun connectVoteResponse(vote_idx: Int) {
        val task = VoteResponseTask(applicationContext, HandlerGetVoteResponse(this), LoginToken.getToken(applicationContext))
        task.execute(USGS_REQUEST_URL.URL_DETAIL_VOTE_RESPONSE + "/" + vote_idx,METHOD_GET)
    }

    private class HandlerGetVoteResponse(activity: VoteActivity) : Handler() {
        private val mActivity: WeakReference<VoteActivity> = WeakReference<VoteActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        activity.voteResponse = msg.obj as VoteResponse
                        if (activity.vote == null) {
                            activity.vote = activity.voteResponse.vote
                            activity.vote!!.setPhotoInfo(activity.applicationContext)
                            activity.setVoteInfo()
                        }
                        if (activity.isShowResult) activity.updateResultList()
                        else activity.updateChoiceList()
                    }
                    Utils.MSG_FAIL -> {
                        val message = msg.data.getString(JSON_MESSAGE)
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


    private class HandlerUpdateVoteResponse(activity: VoteActivity) : Handler() {
        private val mActivity: WeakReference<VoteActivity> = WeakReference<VoteActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        activity.updateSuccess()
                    }
                    Utils.MSG_FAIL -> {

                    }
                }
            }
        }
    }

    private class HandlerPress(activity: VoteActivity) : Handler() {
        private val mActivity: WeakReference<VoteActivity> = WeakReference<VoteActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                val message = msg.data.getString("message")
                if (message.toString().contains("Success"))
                    Toast.makeText(activity.applicationContext, "요청하였습니다", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(activity.applicationContext, "실패하였습니다", Toast.LENGTH_SHORT).show()

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
    private class HandlerDelete(activity: VoteActivity) : Handler() {
        private val mActivity: WeakReference<VoteActivity> = WeakReference<VoteActivity>(activity)

        override fun handleMessage(msg: Message) {
            mActivity.get()?.deleteResult(msg)
        }
    }
}
