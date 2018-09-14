package org.teamfairy.sopt.teamkerbell.activities.items.role.task

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.app_bar_more.*
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.content_task_response.*
import kotlinx.android.synthetic.main.li_task_response.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.activities.items.role.adapter.FeedbackListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.Role
import org.teamfairy.sopt.teamkerbell.model.data.RoleFeedback
import org.teamfairy.sopt.teamkerbell.model.data.TaskResponse
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.info.FeedBackTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROLE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_TASK_RESPONSE
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import java.net.URISyntaxException
import kotlin.properties.Delegates
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.support.v4.widget.SwipeRefreshLayout
import org.teamfairy.sopt.teamkerbell.activities.items.filter.MenuFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.MenuActionInterface
import org.teamfairy.sopt.teamkerbell.model.assist.TaskResponseWithFeedback
import org.teamfairy.sopt.teamkerbell.model.data.RoleTask
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_DELETE
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_GET
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_PUT
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_TASK


class TaskResponseActivity : AppCompatActivity(), MenuActionInterface , SwipeRefreshLayout.OnRefreshListener{
    override fun menuEdit() {
         attemptEdit()
    }

    override fun menuDelete() {
        attemptDelete()
    }


    private var mSwipeRefreshLayout: SwipeRefreshLayout by Delegates.notNull()
    override fun onRefresh() {
        connectFeedBackList()
        mSwipeRefreshLayout.isRefreshing = false
    }


    var adapter: FeedbackListAdapter by Delegates.notNull()
    val dataList = ArrayList<RoleFeedback>()

    var role: Role by Delegates.notNull()
    var task: RoleTask by Delegates.notNull()
    var taskResponse: TaskResponse by Delegates.notNull()



    var ivProfile: ImageView by Delegates.notNull()
    var tvName: TextView by Delegates.notNull()
    var tvDetail: TextView by Delegates.notNull()
    var ivFile: ImageView by Delegates.notNull()
    var tvContent: TextView by Delegates.notNull()
    var tvCommentC: TextView by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_response)
        setSupportActionBar(toolbar)


        taskResponse = intent.getParcelableExtra(INTENT_TASK_RESPONSE)

        taskResponse.setPhotoInfo(this)

        if (taskResponse.fileArray.isNotEmpty()) {
            li_iv_file.visibility = View.VISIBLE
            li_iv_file.setOnClickListener {
                try {
//                    requestAppPermissions()
                    Toast.makeText(applicationContext,taskResponse.fileArray.first().substringAfterLast('/')+"를 다운로드 시작합니다",Toast.LENGTH_SHORT).show()
                    val r = DownloadManager.Request(Uri.parse(taskResponse.fileArray.first()))

// This put the download in the same Download dir the browser uses
                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, taskResponse.fileArray.first().substringAfterLast('/'))

// When downloading music and videos they will be listed in the player
// (Seems to be available since Honeycomb only)
                    r.allowScanningByMediaScanner()

// Notify user when download is completed
// (Seems to be available since Honeycomb only)
                    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

// Start download
                    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.enqueue(r)

                } catch (e: URISyntaxException) {
                    Toast.makeText(applicationContext, "올바르지 않은 파일형식입니다.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
//                    Toast.makeText(applicationContext,"기한 만료된 파일입니다.",Toast.LENGTH_SHORT).show()
                }
            }
        }



        role = intent.getParcelableExtra(INTENT_ROLE)
        task = intent.getParcelableExtra(INTENT_TASK)

        supportActionBar!!.title = role.title


        ivProfile= findViewById(R.id.li_iv_profile)
        tvName = findViewById(R.id.li_tv_name)
        tvDetail = findViewById(R.id.li_tv_detail)
        ivFile = findViewById(R.id.li_iv_file)
        tvContent= findViewById(R.id.li_tv_content)
        tvCommentC = findViewById(R.id.li_tv_comment_cnt)

        setResponseData(taskResponse)

        recyclerView.layoutManager = LinearLayoutManager(this)


        adapter = FeedbackListAdapter(applicationContext, dataList)
        recyclerView.adapter = adapter


        if(taskResponse.u_idx==LoginToken.getUserIdx(applicationContext))
            MenuFunc(this,MenuFunc.MENU_OPT.SHOW_ALL)

        btn_commit.setOnClickListener {
            val txt = edt_commit.text.toString()
            val task = FeedBackTask(applicationContext, HandlerGetFeedback(this), LoginToken.getToken(applicationContext))
            val jsonParam = JSONObject()
            jsonParam.put(USGS_REQUEST_URL.URL_ROLE_FEEDBACK_PARAM_ROLE_RESPONSE_IDX, taskResponse.response_idx)
            jsonParam.put(USGS_REQUEST_URL.URL_ROLE_FEEDBACK_PARAM_CONTENT, txt)
            task.execute(USGS_REQUEST_URL.URL_ROLE_FEEDBACK,METHOD_POST, jsonParam.toString())

            edt_commit.setText("")
        }

        btn_back.setOnClickListener {
            onBackPressed()
        }

        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)


    }



    override fun onResume() {
        super.onResume()
        connectFeedBackList()
    }

    private fun attemptEdit(){
        val intent = Intent(this,MakeTaskResponseActivity::class.java)
        intent.putExtra(INTENT_TASK,task)
        intent.putExtra(INTENT_TASK_RESPONSE,taskResponse)
        startActivity(intent)
    }
    private fun attemptDelete(){
        val task = FeedBackTask(applicationContext, HandlerDelete(this), LoginToken.getToken(applicationContext))

        task.execute(USGS_REQUEST_URL.URL_ROLE_RESPONSE + "/" + taskResponse.response_idx, METHOD_DELETE)
    }


    private fun setResponseData(taskResponse : TaskResponse){
        this.taskResponse=taskResponse
        tvName.text = taskResponse.name
        tvDetail.text = taskResponse.write_time

        tvContent.text = taskResponse.content
        tvCommentC.text = if (taskResponse.count != 0) taskResponse.count.toString() else ""

        if (NetworkUtils.getBitmapList(taskResponse.photo, ivProfile, this, "user${ taskResponse.u_idx}"))
            ivProfile.setImageResource(R.drawable.icon_profile_default)

    }
    private fun connectFeedBackList() {
        val task = FeedBackTask(applicationContext, HandlerGetFeedback(this), LoginToken.getToken(applicationContext))

        task.execute(USGS_REQUEST_URL.URL_ROLE_FEEDBACK + "/" + taskResponse.response_idx, METHOD_GET)
    }


    private class HandlerDelete(activity: TaskResponseActivity) : Handler() {
        private val mActivity: WeakReference<TaskResponseActivity> = WeakReference<TaskResponseActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                                    //TODO SOMETHING

                    }
                    else -> {
                        val message = msg.data.getString("message");
                        Toast.makeText(activity.applicationContext, message, Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }
    }

    private class HandlerEdit(activity: TaskResponseActivity) : Handler() {
        private val mActivity: WeakReference<TaskResponseActivity> = WeakReference<TaskResponseActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        //TODO SOMETHING

                    }
                    else -> {
                        val message = msg.data.getString("message");
                        Toast.makeText(activity.applicationContext, message, Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }
    }

    private class HandlerGetFeedback(activity: TaskResponseActivity) : Handler() {
        private val mActivity: WeakReference<TaskResponseActivity> = WeakReference<TaskResponseActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        val taskResponseWithFeedback = msg.obj as TaskResponseWithFeedback
                        val response = taskResponseWithFeedback.taskResponse
                        response!!.setPhotoInfo(activity.applicationContext)
                        activity.setResponseData(response)

                        val datas: ArrayList<RoleFeedback> = taskResponseWithFeedback.feedbacks!!
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

                        activity.recyclerView.scrollToPosition(dataList.lastIndex)
                        activity.adapter.notifyDataSetChanged()
                    }
                    else -> {
                        val message = msg.data.getString(USGS_REQUEST_URL.JSON_MESSAGE);
                        Toast.makeText(activity.applicationContext, message, Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }
    }

}
