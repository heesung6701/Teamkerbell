package org.teamfairy.sopt.teamkerbell.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat.finishAffinity
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.unperformed.UnperformedActivity
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.teamfairy.sopt.teamkerbell.network.RefreshTokenTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REFRESH_TOKEN_PARAM_ID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REFRESH_TOKEN_PARAM_U_IDX
import org.teamfairy.sopt.teamkerbell.utils.*
import org.teamfairy.sopt.teamkerbell.utils.LoginToken.Companion.signOut
import java.lang.ref.WeakReference

class SplashActivity : AppCompatActivity() {

    private fun login() {

        Log.d("FirebaseToken", FirebaseInstanceId.getInstance().token.toString())

        val intent = Intent(applicationContext, UnperformedActivity::class.java)

        DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_GROUP, true)
        DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_JOINED_GROUP, true)
        DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_USER, true)
        DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_ROOM, true)
        DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_JOINED_ROOM, true)

//        NetworkUtils.connectUserList(applicationContext, null,true)
//        NetworkUtils.connectGroupList(applicationContext, null,true)
//        NetworkUtils.connectRoomList(applicationContext, null,true)
//        NetworkUtils.connectJoinedGroupList(applicationContext, null,true)
//        NetworkUtils.connectJoinedRoomList(applicationContext, null,true)

        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()

        if (intent.hasExtra(IntentTag.EXIT) && intent.getBooleanExtra(IntentTag.EXIT, false)) {
            Handler().postDelayed({
                finish()
                return@postDelayed
            }, 1000)
        } else {

            LoginToken.getPref(applicationContext)
            if (LoginToken.isValid()) {
                checkValidToken()
            } else {
                Handler().postDelayed({
                    val i = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(i)
                }, 3000)
            }
        }
    }
    fun signOut() {
        LoginToken.signOut(applicationContext)

        val i = Intent(this, SplashActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        finishAffinity()
        startActivity(i)
    }
    fun checkValidToken() {
        val task = RefreshTokenTask(applicationContext, HandlerCheck(this), LoginToken.getToken(applicationContext))
        val jsonParam = JSONObject()
        val u = LoginToken.getUser(applicationContext)
        jsonParam.put(URL_REFRESH_TOKEN_PARAM_ID, u.id)
        jsonParam.put(URL_REFRESH_TOKEN_PARAM_U_IDX, u.u_idx)
        task.execute(USGS_REQUEST_URL.URL_REFRESH_TOKEN, NetworkTask.METHOD_POST, jsonParam.toString())
    }

    private class HandlerCheck(activity: SplashActivity) : Handler() {
        private val mActivity: WeakReference<SplashActivity> = WeakReference<SplashActivity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            when (msg.what) {
                Utils.MSG_SUCCESS -> {
                    activity?.login()
                }
                Utils.MSG_EXPIRED -> {
                    LoginToken.updateToken(activity?.applicationContext, msg.obj as String)
                    activity?.login()
                }
                Utils.MSG_INVALID -> {
                    activity?.signOut()
                }
                else -> {

                    // TODO 인터넷이 안좋아서 안될수도 있으니..
                    activity?.signOut()
                }
            }
        }
    }
}
