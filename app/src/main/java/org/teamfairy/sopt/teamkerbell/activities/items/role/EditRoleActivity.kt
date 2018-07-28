package org.teamfairy.sopt.teamkerbell.activities.items.role

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.android.gms.internal.tv
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_commit.*
import kotlinx.android.synthetic.main.content_edit_role.*
import org.json.JSONArray
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces.RoomActivityInterface
import org.teamfairy.sopt.teamkerbell.model.assist.ExampleEdit
import org.teamfairy.sopt.teamkerbell.model.data.Role
import org.teamfairy.sopt.teamkerbell.model.data.RoleTask
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_PUT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROLE
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ROOM
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_TASK
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class EditRoleActivity : AppCompatActivity() {

    var group: Team by Delegates.notNull()
    var room: Room by Delegates.notNull()
    var role: Role by Delegates.notNull()


    private var taskList: ArrayList<RoleTask> = ArrayList<RoleTask>()
    private val minusArray = JSONArray()


    private var edtViewList = HashMap<ImageButton, ExampleEdit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_role)
        setSupportActionBar(toolbar)

        group = intent.getParcelableExtra(INTENT_GROUP)
        room = intent.getParcelableExtra(INTENT_ROOM)
        role = intent.getParcelableExtra(INTENT_ROLE)
        taskList = intent.getParcelableArrayListExtra(INTENT_TASK)

        val ivDropDown : ImageView= findViewById(R.id.iv_drop_down)
        val tvRoomName :TextView= findViewById(R.id.tv_room_name)
        ivDropDown.visibility = View.GONE
        tvRoomName.text=room.real_name
        layout_select_room.isClickable = false

        tv_role_name.text=role.title


        taskList.forEach {
            val edtView = layoutInflater.inflate(R.layout.item_example_edt, null, false)
            val btn = edtView.findViewById<ImageButton>(R.id.btn_minus)
            val edt = edtView.findViewById<EditText>(R.id.edt_vote_example)
            edt.setText(it.content)
            edt.isEnabled =false
            btn.setOnClickListener {
                val thisEdit = edtViewList[it]!!
                layout_role_tasks.removeView(thisEdit.view)
                if(thisEdit.id!=-1)
                    minusArray.put(thisEdit.id)
                edtViewList.remove(it)
            }
            edtViewList[btn]= ExampleEdit(edtView,edt,it.task_idx)

            layout_role_tasks.addView(edtView)
        }
        layout_add_task.setOnClickListener {
            val edtView = layoutInflater.inflate(R.layout.item_example_edt, null, false)
            val btn = edtView.findViewById<ImageButton>(R.id.btn_minus)
            val edt = edtView.findViewById<EditText>(R.id.edt_vote_example)
            btn.setOnClickListener {
                val thisEdit = edtViewList[it]!!
                layout_role_tasks.removeView(thisEdit.view)
                edtViewList.remove(it)
            }
            edtViewList[btn]= ExampleEdit(edtView,edt)

            layout_role_tasks.addView(edtView)
        }

        btn_commit.setOnClickListener {
            val jsonParam = JSONObject()
            try {
                jsonParam.put(USGS_REQUEST_URL.URL_ROLE_TASK_PARAM_ROLE_IDX, role.role_idx)

                val plusArray = JSONArray()

                edtViewList.iterator().forEach {
                    val example = it.value
                    if(example.id==-1)
                        plusArray.put(example.edtText.text.toString())
                }

                jsonParam.put(USGS_REQUEST_URL.URL_ROLE_TASK_PARAM_PLUS_ARRAY,plusArray)
                jsonParam.put(USGS_REQUEST_URL.URL_ROLE_TASK_PARAM_MINUS_ARRAY,minusArray)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val task = GetMessageTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
            task.execute(USGS_REQUEST_URL.URL_ROLE_TASK, METHOD_PUT, jsonParam.toString())


        }

        btn_back.setOnClickListener {
            onBackPressed()
        }
    }



    override fun finish() {

        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
    }

    fun successCommit(){
//        val intent = Intent(applicationContext, RoleActivity::class.java)
//        intent.putExtra(INTENT_GROUP,group)
//        intent.putExtra(INTENT_ROOM,room)
//        intent.putExtra(INTENT_ROLE,role)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        startActivity(intent)
        finish()
    }


    private class HandlerGet(activity: EditRoleActivity) : Handler() {
        private val mActivity: WeakReference<EditRoleActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        activity.successCommit()
                    }
                    else -> {
                        Toast.makeText(activity.applicationContext, "잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


}
