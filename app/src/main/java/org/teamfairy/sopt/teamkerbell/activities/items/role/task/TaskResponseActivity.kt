package org.teamfairy.sopt.teamkerbell.activities.items.role.task

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
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
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.*
import org.teamfairy.sopt.teamkerbell.activities.items.filter.MenuFunc
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.MenuActionInterface
import org.teamfairy.sopt.teamkerbell.model.assist.TaskResponseWithFeedback
import org.teamfairy.sopt.teamkerbell.model.data.RoleTask
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_DELETE
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_GET
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_TASK


class TaskResponseActivity : AppCompatActivity(), MenuActionInterface, SwipeRefreshLayout.OnRefreshListener {
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
    private var tvDetail: TextView by Delegates.notNull()
    private var layoutFile: LinearLayout by Delegates.notNull()
    var tvFileName: TextView by Delegates.notNull()
    var tvContent: TextView by Delegates.notNull()
    private var tvCommentC: TextView by Delegates.notNull()

    private var btnBefore: ImageButton by Delegates.notNull()
    private var btnNext: ImageButton by Delegates.notNull()

    var selectedFile: Int = -1


    var dialogDelete: BottomSheetDialog by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_response)
        setSupportActionBar(toolbar)




        role = intent.getParcelableExtra(INTENT_ROLE)
        task = intent.getParcelableExtra(INTENT_TASK)

        supportActionBar!!.title = role.title


        ivProfile = findViewById(R.id.li_iv_profile)
        tvName = findViewById(R.id.li_tv_name)
        tvDetail = findViewById(R.id.li_tv_detail)
        layoutFile = findViewById(R.id.li_layout_file)
        tvFileName = findViewById(R.id.li_tv_file_name)
        tvContent = findViewById(R.id.li_tv_content)
        tvCommentC = findViewById(R.id.li_tv_comment_cnt)

        btnBefore = findViewById(R.id.li_btn_file_before)
        btnNext = findViewById(R.id.li_btn_file_next)


        taskResponse = intent.getParcelableExtra(INTENT_TASK_RESPONSE)
        taskResponse.setPhotoInfo(this)

        setResponseData(taskResponse)

        recyclerView.layoutManager = LinearLayoutManager(this)


        adapter = FeedbackListAdapter(applicationContext, dataList)
        adapter.setOnLongClickHandler(HandlerDeleteFeedBack(this))
        recyclerView.adapter = adapter


        if (taskResponse.u_idx == LoginToken.getUserIdx(applicationContext))
            MenuFunc(this, MenuFunc.MENU_OPT.SHOW_ALL)

        btn_commit.setOnClickListener {
            val txt = edt_commit.text.toString()
            val task = FeedBackTask(applicationContext, HandlerGetFeedback(this), LoginToken.getToken(applicationContext))
            val jsonParam = JSONObject()
            jsonParam.put(USGS_REQUEST_URL.URL_ROLE_FEEDBACK_PARAM_ROLE_RESPONSE_IDX, taskResponse.response_idx)
            jsonParam.put(USGS_REQUEST_URL.URL_ROLE_FEEDBACK_PARAM_CONTENT, txt)
            task.execute(USGS_REQUEST_URL.URL_ROLE_FEEDBACK, METHOD_POST, jsonParam.toString())

            edt_commit.setText("")
        }

        btn_back.setOnClickListener {
            onBackPressed()
        }

        btnNext.setOnClickListener {
            selectedFile++
            if (selectedFile == taskResponse.fileArray.lastIndex) it.visibility = View.INVISIBLE
            tvFileName.text = ("${selectedFile + 1}.${taskResponse.fileArray[selectedFile].substringAfterLast('/')}")

            btnBefore.visibility = View.VISIBLE

        }
        btnBefore.setOnClickListener {
            selectedFile--
            if (selectedFile == -1) {
                it.visibility = View.INVISIBLE
                tvFileName.text = getString(R.string.txt_download_all)
//                tvFileName.text=("All.${taskResponse.fileArray[0]}" + if(taskResponse.fileArray.size>1) "+${taskResponse.fileArray.size-1}" else "")
            } else {
                tvFileName.text = ("${selectedFile + 1}.${taskResponse.fileArray[selectedFile].substringAfterLast('/')}")
            }
            btnNext.visibility = View.VISIBLE
        }

        mSwipeRefreshLayout = findViewById(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)


    }


    override fun onResume() {
        super.onResume()
        connectFeedBackList()
    }

    private fun attemptEdit() {
        val intent = Intent(this, MakeTaskResponseActivity::class.java)
        intent.putExtra(INTENT_TASK, task)
        intent.putExtra(INTENT_TASK_RESPONSE, taskResponse)
        startActivity(intent)
    }

    private fun attemptDelete() {
        val task = GetMessageTask(applicationContext, HandlerDelete(this), LoginToken.getToken(applicationContext))

        task.execute(USGS_REQUEST_URL.URL_ROLE_RESPONSE + "/" + taskResponse.response_idx, METHOD_DELETE)
    }

    private fun attemptDeleteFeedback(feedback: RoleFeedback) {
        val task = GetMessageTask(applicationContext, HandlerDeleteFeedBackSuccess(this), LoginToken.getToken(applicationContext))

        val jsonParam = JSONObject()
        jsonParam.put(USGS_REQUEST_URL.URL_ROLE_FEEDBACK_PARAM_ROLE_RESPONSE_IDX, taskResponse.response_idx)
        jsonParam.put(USGS_REQUEST_URL.URL_ROLE_FEEDBACK_PARAM_ROLE_FEEDBACK_IDX, feedback.feedback_idx.toString())

        task.execute(USGS_REQUEST_URL.URL_ROLE_FEEDBACK, METHOD_DELETE,jsonParam.toString())
    }


    private fun setResponseData(taskResponse: TaskResponse) {


        this.taskResponse = taskResponse
        tvName.text = taskResponse.name
        tvDetail.text = taskResponse.write_time

        tvContent.text = taskResponse.content
        tvCommentC.text = if (taskResponse.count != 0) taskResponse.count.toString() else ""

        if (NetworkUtils.getBitmapList(taskResponse.photo, ivProfile, this, "user${taskResponse.u_idx}"))
            ivProfile.setImageResource(R.drawable.icon_profile_default)

        if (taskResponse.fileArray.isNotEmpty()) {
            tvFileName.text = getString(R.string.txt_download_all)
//            tvFileName.text = (taskResponse.fileArray[0] + if (taskResponse.fileArray.size > 1) "+${taskResponse.fileArray.size - 1}" else "")
            layoutFile.visibility = View.VISIBLE

            layoutFile.setOnClickListener {
                if (selectedFile == -1) {
                    Toast.makeText(applicationContext, taskResponse.fileArray.first().substringAfterLast('/') +
                            (if(taskResponse.fileArray.size>1)  "(+${taskResponse.fileArray.size - 1})" else "" )
                            +"를 다운로드 시작합니다", Toast.LENGTH_SHORT).show()
//                    requestAppPermissions()
                    taskResponse.fileArray.forEach {
                        checkPermission(it)
                    }
                } else {
                    Toast.makeText(applicationContext, "${taskResponse.fileArray[selectedFile].substringAfterLast('/')}를 다운로드 시작합니다", Toast.LENGTH_SHORT).show()
                    checkPermission(taskResponse.fileArray[selectedFile])
                }
            }
            if (taskResponse.fileArray.size > 1)
                btnNext.visibility = View.VISIBLE

        } else {
            tvFileName.text = getString(R.string.txt_no_file)
//            layoutFile.visibility = View.GONE
        }


    }

    private fun downLoadFile(url: String) {
        try {
            val r = DownloadManager.Request(Uri.parse(url))

            // This put the download in the same Download dir the browser uses
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substringAfterLast('/'))

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
            e.printStackTrace()
            Toast.makeText(applicationContext, getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
            //Toast.makeText(applicationContext,"기한 만료된 파일입니다.",Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission(url: String) {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                } else {
                    downLoadFile(url)
                }
            } else {
                downLoadFile(url)
            }
        } else {
            downLoadFile(url)
        }
    }

    fun openDeleteDialog(id: Int) {
        val view = layoutInflater.inflate(R.layout.sheet_delete, null)
        dialogDelete = BottomSheetDialog(this)
        dialogDelete.setContentView(view)
        val btnDelete = view.findViewById(R.id.sheet_delete) as LinearLayout
        btnDelete.setOnClickListener {
            attemptDeleteFeedback(dataList[id])
        }
//        dialogDelete.setOnCancelListener(DialogInterface.OnCancelListener {  })
        dialogDelete.show()
    }

    private fun deleteFeedback(msg: Message) {
        when (msg.what) {
            Utils.MSG_SUCCESS -> {
                Toast.makeText(applicationContext, getString(R.string.txt_delete_success), Toast.LENGTH_SHORT).show()
                connectFeedBackList()
                dialogDelete.dismiss()
            }
            else -> {
                Toast.makeText(applicationContext, getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
            }

        }

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
                        activity.finish()

                    }
                    else -> {
                        Toast.makeText(activity.applicationContext, activity.getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(activity.applicationContext, activity.getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }
    }

    private class HandlerDeleteFeedBack(activity: TaskResponseActivity) : Handler() {
        private val mActivity: WeakReference<TaskResponseActivity> = WeakReference<TaskResponseActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get() ?: return
            activity.openDeleteDialog(msg.what)
        }
    }

    private class HandlerDeleteFeedBackSuccess(activity: TaskResponseActivity) : Handler() {
        private val mActivity: WeakReference<TaskResponseActivity> = WeakReference<TaskResponseActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            activity?.deleteFeedback(msg)
        }
    }

}
