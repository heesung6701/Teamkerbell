package org.teamfairy.sopt.teamkerbell.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN_PARAM_CLIENTTOKEN
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN_PARAM_ID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN_PARAM_PWD
import org.teamfairy.sopt.teamkerbell.network.auth.LoginTask
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.LoginToken.Companion.getUserIdx
import java.lang.ref.WeakReference

class SplashActivity : AppCompatActivity() {

    private val _tag = this::class.java.simpleName

    var isSuccess: Boolean = false
    private var _txtId: String = ""
    private var _txtPwd: String = ""
    private val mHandlerLogin: Handler = HandlerLogin(this)

    private class HandlerLogin(activity: SplashActivity) : Handler() {
        private val mActivity: WeakReference<SplashActivity> = WeakReference<SplashActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null){
                val result = msg.data.getString("message")
                when {
                    result.contains("Success") -> {
                        activity.isSuccess = true
                        activity.login()
                    }
                    result.contains("Failed") -> {

                    }
                    else -> {

                    }
                }

            }
        }
    }

    fun login() {

        Log.d("FirebaseToken", FirebaseInstanceId.getInstance().token.toString())

        val dataBase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val dataBaseReference: DatabaseReference = dataBase.reference
        val dataBaseFireToken: DatabaseReference? = dataBaseReference.child("firebase_tokens").ref
        dataBaseFireToken!!.child(getUserIdx(applicationContext).toString()).setValue( FirebaseInstanceId.getInstance().token!!.toString())

//        val intent = Intent(applicationContext, UnPerformendActivity::class.java)
        val intent = Intent(applicationContext, LoginActivity::class.java)


        DatabaseHelpUtils.setPref_isUpdate(applicationContext,DatabaseHelpUtils.PREF_ISUPDATE_GROUP,true)
        DatabaseHelpUtils.setPref_isUpdate(applicationContext,DatabaseHelpUtils.PREF_ISUPDATE_JOINED_GROUP,true)
        DatabaseHelpUtils.setPref_isUpdate(applicationContext,DatabaseHelpUtils.PREF_ISUPDATE_USER,true)

        NetworkUtils.connectUserList(applicationContext, null)
        NetworkUtils.connectGroupList(applicationContext, null)
        NetworkUtils.connectJoinedGroupList(applicationContext, null)

        startActivity(intent)


        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val pref = applicationContext.getSharedPreferences("pref_login_info", MODE_PRIVATE)
        _txtId = pref.getString("id", "")
        _txtPwd = pref.getString("pwd", "")
        if (!_txtId.isEmpty() && !_txtPwd.isEmpty()) {
            Log.d("$_tag/try", "login ID is $_txtId")
            attemptLogin(_txtId, _txtPwd)
        }

        Handler().postDelayed({
            if (!isSuccess) {
                    val i = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(i)
                   finish()
            }
        }, 3000)
    }


    private fun attemptLogin(txtId: String, txtPwd: String) {
        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_LOGIN_PARAM_ID, txtId)
            jsonParam.put(URL_LOGIN_PARAM_PWD, txtPwd)
            jsonParam.put(URL_LOGIN_PARAM_CLIENTTOKEN, FirebaseInstanceId.getInstance().token!!.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val loginTask = LoginTask(applicationContext, mHandlerLogin)
        loginTask.execute(URL_LOGIN, jsonParam.toString())
    }


}
