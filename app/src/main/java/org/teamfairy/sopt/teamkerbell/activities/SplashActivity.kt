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
import org.teamfairy.sopt.teamkerbell.activities.unperformed.UnperformedActivity
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.LoginToken.Companion.getUserIdx
import java.lang.ref.WeakReference

class SplashActivity : AppCompatActivity() {


    private fun login() {

        Log.d("FirebaseToken", FirebaseInstanceId.getInstance().token.toString())

        val intent = Intent(applicationContext, UnperformedActivity::class.java)

        DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_GROUP,true)
        DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_JOINED_GROUP,true)
        DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_USER,true)
        DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_ROOM,true)
        DatabaseHelpUtils.setPref_isUpdate(applicationContext, DatabaseHelpUtils.PREF_ISUPDATE_JOINED_ROOM,true)

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

        if(intent.hasExtra(IntentTag.EXIT) && intent.getBooleanExtra(IntentTag.EXIT,false)) {
            Handler().postDelayed({
                finish()
                return@postDelayed
            }, 1000)
        }else {

            LoginToken.getPref(applicationContext)
            if (LoginToken.isValid()) {
                login()
            } else {
                Handler().postDelayed({
                    val i = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(i)
                }, 3000)
            }
        }
    }
}
