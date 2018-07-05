package org.teamfairy.sopt.teamkerbell.activities.items.role.task

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.system.Os.read
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.app_bar_back.*
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.content_task_response.*
import kotlinx.android.synthetic.main.li_task_response.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R.id.*
import org.teamfairy.sopt.teamkerbell.activities.items.role.adapter.FeedbackListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Role
import org.teamfairy.sopt.teamkerbell.model.data.RoleFeedback
import org.teamfairy.sopt.teamkerbell.model.data.TaskResponse
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.info.FeedBackTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROLE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_TASK_RESPONSE
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.io.*
import java.lang.ref.WeakReference
import java.net.URISyntaxException
import java.net.URL
import kotlin.properties.Delegates

class TaskResponseActivity : AppCompatActivity() {



    var adapter: FeedbackListAdapter by Delegates.notNull()
    val dataList = ArrayList<RoleFeedback>()

    var role : Role by Delegates.notNull()
    var taskResponse: TaskResponse by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_response)
        setSupportActionBar(toolbar)

        taskResponse = intent.getParcelableExtra(INTENT_TASK_RESPONSE)
        taskResponse.setPhotoInfo(this)
        if(taskResponse.fileArray.isNotEmpty()){
            li_iv_file.visibility= View.VISIBLE
            li_iv_file.setOnClickListener {
                try {

//                    파일 다운로드를 mainThread를 안해
                    val DownloadURL = taskResponse.fileArray.first()
                    val FileName = "다운로드 파일이름 어떻게 정하냐"
                    val inputStream = URL(DownloadURL).openStream()

                    val file = File(FileName)
                    val out = FileOutputStream(file)
                    writeFile(inputStream, out)
                    out.close()

//                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(taskResponse.fileArray.first()))
//                    startActivity(intent)
                }catch (e : URISyntaxException){
                    Toast.makeText(applicationContext,"올바르지 않은 파일형식입니다.",Toast.LENGTH_SHORT).show()
                }catch (e : Exception){
                    Toast.makeText(applicationContext,"잠시 후 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
//                    Toast.makeText(applicationContext,"기한 만료된 파일입니다.",Toast.LENGTH_SHORT).show()
                }
            }
        }



        role=intent.getParcelableExtra(INTENT_ROLE)

        supportActionBar!!.title = role.title


        val ivProfile: ImageView = findViewById(R.id.li_iv_profile)
        val tvName: TextView = findViewById(R.id.li_tv_name)
        val tvDetail: TextView = findViewById(R.id.li_tv_detail)
        val ivFile: ImageView = findViewById(R.id.li_iv_file)
        val tvContent: TextView = findViewById(R.id.li_tv_content)
        val tvCommentC: TextView = findViewById(R.id.li_tv_comment_cnt)

        tvName.text = taskResponse.name
        tvDetail.text = taskResponse.write_time

        tvContent.text = taskResponse.content
        tvCommentC.text = if (taskResponse.count != 0) taskResponse.count.toString() else ""

        if (NetworkUtils.getBitmapList(taskResponse.photo, ivProfile, this, "user" + taskResponse.u_idx))
            ivProfile.setImageResource(R.drawable.icon_profile_default_png)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager


        adapter = FeedbackListAdapter(applicationContext, dataList)
        recyclerView.adapter = adapter


        btn_commit.setOnClickListener {
            val txt = edt_commit.text.toString()
            val task = FeedBackTask(applicationContext, HandlerGetFeedback(this), LoginToken.getToken(applicationContext))
            val jsonParam = JSONObject()
            jsonParam.put(USGS_REQUEST_URL.URL_ROLE_REGISTER_FEEDBACK_PARAM_ROLE_RESPONSE_IDX, taskResponse.response_idx)
            jsonParam.put(USGS_REQUEST_URL.URL_ROLE_REGISTER_FEEDBACK_PARAM_CONTENT, txt)
            task.execute(USGS_REQUEST_URL.URL_ROLE_REGISTER_FEEDBACK, jsonParam.toString())

            edt_commit.setText("")
        }

        btn_back.setOnClickListener {
            onBackPressed()
        }
    }

    @Throws(IOException::class)
    private fun writeFile(inputstream:  InputStream, os: OutputStream) {
        var c = 0
        while (true) {
            c = inputstream.read()
            if(c==-1) break
            os.write(c)
        }
        os.flush()
    }

    override fun onResume() {
        super.onResume()
        connectFeedBackList()
    }

    private fun connectFeedBackList() {
        val task = FeedBackTask(applicationContext, HandlerGetFeedback(this), LoginToken.getToken(applicationContext))

        task.execute(USGS_REQUEST_URL.URL_ROLE_SHOW_FEEDBACK + "/" + taskResponse.response_idx)
    }


    private class HandlerGetFeedback(activity: TaskResponseActivity) : Handler() {
        private val mActivity: WeakReference<TaskResponseActivity> = WeakReference<TaskResponseActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        val datas: ArrayList<RoleFeedback> = msg.obj as ArrayList<RoleFeedback>
                        val dataList = activity.dataList
                        dataList.clear()

                        datas.forEach {
                            it.setPhotoInfo(activity.applicationContext)
                            dataList.add(it)
                        }

                        if (dataList.size > 0)
                            activity.li_tv_comment_cnt.text = dataList.size.toString()
                        else
                            activity.li_tv_comment_cnt.text = ""

                        activity.recyclerView.scrollToPosition(dataList.size - 1)
                        activity.adapter.notifyDataSetChanged()
                    }
                    else -> {
                        val message = msg.data.getString("message");
                        Toast.makeText(activity.applicationContext, message, Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }
    }
}