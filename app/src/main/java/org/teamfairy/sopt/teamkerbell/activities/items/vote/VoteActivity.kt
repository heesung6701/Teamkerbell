package org.teamfairy.sopt.teamkerbell.activities.items.vote

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_more.*
import kotlinx.android.synthetic.main.content_vote.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R.id.btn_complete
import org.teamfairy.sopt.teamkerbell.R.id.tv_count
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.activities.items.vote.adapter.ChoiceListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.*
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_VOTE_PARAM_VALUE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_VOTE_PARAM_VOTEID
import org.teamfairy.sopt.teamkerbell.network.info.VoteResponseTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_VOTE
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class VoteActivity : AppCompatActivity() ,View.OnClickListener{


    private var fromList = false

    var group: Team by Delegates.notNull()
    var vote : Vote by Delegates.notNull()
    var voteResponse : VoteResponse by Delegates.notNull()




    private var dataListChoice: ArrayList<HashMap<String,String>> = arrayListOf<HashMap<String,String>>()
    private var recyclerChoice: RecyclerView by Delegates.notNull()
    private var adapterChoice: ChoiceListAdapter by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)
        vote = intent.getParcelableExtra<Vote>(INTENT_VOTE)

        vote.setPhotoInfo(applicationContext)

        tv_title.text=vote.title
        tv_content.text=vote.content
        if (NetworkUtils.getBitmapList(vote.photo, iv_profile, applicationContext,"user"+vote.u_idx))
            iv_profile.setImageResource(R.drawable.icon_profile_default_png)
        tv_name.text=vote.name
        tv_time.text=vote.getTime()


            layout_response.visibility= View.VISIBLE

            recyclerChoice = findViewById(R.id.recyclerView_choice)
            recyclerChoice.layoutManager = LinearLayoutManager(this)
            adapterChoice= ChoiceListAdapter(dataListChoice,applicationContext)
            adapterChoice.setOnItemClick(this)
            recyclerChoice.adapter=adapterChoice

            if(adapterChoice.selectedId==-1){
                btn_complete.isEnabled=false
            }

            btn_complete.setOnClickListener {
                updateVoteResponse()
            }

        btn_back.setOnClickListener {
            finish()
        }


        connectVoteResponse(vote.vote_idx)
    }
    override fun onClick(p0: View?) {
        val pos = recyclerChoice.getChildAdapterPosition(p0)
        if(!vote.isFinished()) {
            adapterChoice.selectedId = pos
            adapterChoice.notifyDataSetChanged()

            btn_complete.isEnabled=true
        }
    }

    fun updateExampleList(){

        val userChoiceIdx = if(isVoted()) voteResponse.responses[LoginToken.getUserIdx(applicationContext)]!! else -1

        if(!vote.isFinished()) {

            dataListChoice.clear()

            var totalCnt =0
            voteResponse.examples.iterator().forEach {
                val h = HashMap<String, String>()
                h.put("content", it.value)
                var cnt = 0
                val choiceId = it.key
                voteResponse.responses.iterator().forEach {
                    if (it.value == choiceId)
                        cnt++
                }
                h["count"] = cnt.toString() + " 명"
                totalCnt+=cnt
                h["choice_idx"] = it.key.toString()


                dataListChoice.add(h)

                if(it.key==userChoiceIdx)
                    adapterChoice.selectedId=dataListChoice.lastIndex
            }

            tv_count.text= ("""${totalCnt.toString()}/${voteResponse.responses.size.toString()}명 참여중""")
            adapterChoice.notifyDataSetChanged()
        }
    }


    private fun updateVoteResponse(){
        val selectIdx =adapterChoice.selectedId
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

                task.execute(USGS_REQUEST_URL.URL_RESPONSE_VOTE, jsonParam.toString())
            }

        } else {
            Toast.makeText(applicationContext, "항목을 선택해주세요", Toast.LENGTH_SHORT).show()
        }
    }
    private fun isVoted() : Boolean = voteResponse.responses.containsKey(LoginToken.getUserIdx(applicationContext))

    fun updateSuccess(){

        if (fromList) finish()

        voteResponse.responses[LoginToken.getUserIdx(applicationContext)] = dataListChoice[adapterChoice.selectedId]["choice_idx"]!!.toInt()
        updateExampleList()

    }
    private fun connectVoteResponse(vote_idx: Int) {
        val task = VoteResponseTask(applicationContext, HandlerGetVoteResponse(this), LoginToken.getToken(applicationContext))
        task.execute(USGS_REQUEST_URL.URL_DETAIL_VOTE_RESPONSE + "/" + group.g_idx + "/" + vote_idx)
    }

    private class HandlerGetVoteResponse(activity: VoteActivity) : Handler() {
        private val mActivity: WeakReference<VoteActivity> = WeakReference<VoteActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null){
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        activity.voteResponse = msg.obj as VoteResponse
                        activity.updateExampleList()
                    }
                    Utils.MSG_FAIL -> {

                    }
                }
            }
        }
    }


    private class HandlerUpdateVoteResponse(activity: VoteActivity) : Handler() {
        private val mActivity: WeakReference<VoteActivity> = WeakReference<VoteActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null){
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

}
