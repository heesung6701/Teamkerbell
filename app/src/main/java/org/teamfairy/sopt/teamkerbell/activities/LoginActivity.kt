package org.teamfairy.sopt.teamkerbell.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.provider.ContactsContract
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView

import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.google.firebase.iid.FirebaseInstanceId

import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.unperformed.UnperformedActivity
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN_PARAM_CLIENTTOKEN
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN_PARAM_ID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN_PARAM_PWD
import org.teamfairy.sopt.teamkerbell.network.auth.LoginTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.EXIT
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.FROMSIGNUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ID
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_PWD
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.lang.ref.WeakReference

class LoginActivity : AppCompatActivity() {

    private val mHandlerLogin: Handler = HandlerLogin(this)

    private class HandlerLogin(activity: LoginActivity) : Handler() {
        private val mActivity: WeakReference<LoginActivity> = WeakReference<LoginActivity>(activity)

        override fun handleMessage(msg: Message) {

            val activity = mActivity.get()

            if (activity != null) {

                activity.mAuthTask = null
                activity.showProgress(false)

                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        activity.login()
                    }
                    else -> {

                        val result = msg.data.getString(JSON_MESSAGE)

                        if (result.contains("Failed"))
                            Toast.makeText(activity.applicationContext, "아이디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(activity.applicationContext, result, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun login() {

        // 로그인할때 받아와야되나??
        NetworkUtils.connectGroupList(applicationContext, null, true)
        NetworkUtils.connectUserList(applicationContext, null, true)
        NetworkUtils.connectJoinedGroupList(applicationContext, null, true)
        NetworkUtils.connectJoinedRoomList(applicationContext, null, true)
        NetworkUtils.connectRoomList(applicationContext, null, true)

//        DatabaseHelpUtils.setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_ROOM, true)
//        DatabaseHelpUtils.setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_GROUP, true)
//        DatabaseHelpUtils.setPref_isUpdate(applicationContext, PREF_ISUPDATE_ROOM, true)

        val intent = Intent(applicationContext, UnperformedActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

//    val PREF_LOGIN_DATA = "pref_login_info"
//    fun setPref(id: String, pwd: String) {
//        val pref = applicationContext.getSharedPreferences(PREF_LOGIN_DATA, MODE_PRIVATE).edit()
//        pref.putString("id", id)
//        pref.putString("pwd", pwd)
//        pref.apply()
//    }

    override fun onBackPressed() {
        finish()
    }

    private var mAuthTask: LoginTask? = null

    var ttt = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        populateAutoComplete()

//        easter_egg.setOnClickListener {
//            if(ttt){
//                email.setText("heesung6701@naver.com")
//                password.setText("asdfghjk")
//
//            }else {
//                email.setText("dd@teamkerbell.tk")
//                password.setText("12341234")
//            }
//            ttt=!ttt;
//        }

        if (intent.getBooleanExtra(EXIT, false)) {
            finish()
        }

        if (intent.getBooleanExtra(FROMSIGNUP, false)) {
            email.setText(intent.getStringExtra(INTENT_ID) ?: "")
            password.setText(intent.getStringExtra(INTENT_PWD) ?: "")
            attemptLogin()
        }

        email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (password.text.isNotEmpty() && email.text.isNotEmpty()) {
                    btn_sign_in.setTextColor(Color.WHITE)
                    btn_sign_in.background = ContextCompat.getDrawable(applicationContext, R.drawable.shape_round_btn)
                } else {
                    btn_sign_in.setTextColor(Color.BLACK)
                    btn_sign_in.background = ContextCompat.getDrawable(applicationContext, R.drawable.shape_round_btn_gray)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (password.text.isNotEmpty() && email.text.isNotEmpty()) {
                    btn_sign_in.setTextColor(Color.WHITE)
                    btn_sign_in.background = ContextCompat.getDrawable(applicationContext, R.drawable.shape_round_btn)
                } else {
                    btn_sign_in.setTextColor(Color.BLACK)
                    btn_sign_in.background = ContextCompat.getDrawable(applicationContext, R.drawable.shape_round_btn_gray)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        btn_sign_in.setOnClickListener { attemptLogin() }

        btn_sign_up.setOnClickListener {

            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }
    }

    private fun mayRequestContacts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok
                    ) { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) }
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }

    private var emailStr = ""
    private var passwordStr = ""
    private fun attemptLogin() {

        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        emailStr = email.text.toString()
        passwordStr = password.text.toString()
        if (emailStr.isBlank()) {
            email.error = getString(R.string.error_enter)
            email.requestFocus()
            return
        }
        if (!emailStr.contains("@")) {
            email.error = getString(R.string.error_invalid_email)
            email.requestFocus()
            return
        }
        if (passwordStr.isBlank()) {
            password.error = getString(R.string.error_enter)
            password.requestFocus()
            return
        }

        showProgress(true)

        val jsonParam = JSONObject()
        try {
            jsonParam.put(URL_LOGIN_PARAM_ID, emailStr)
            jsonParam.put(URL_LOGIN_PARAM_PWD, passwordStr)
            Log.d("FirebaseToken", FirebaseInstanceId.getInstance().token.toString())
            jsonParam.put(URL_LOGIN_PARAM_CLIENTTOKEN, FirebaseInstanceId.getInstance().token.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mAuthTask = LoginTask(applicationContext, mHandlerLogin)
        mAuthTask!!.execute(URL_LOGIN, METHOD_POST, jsonParam.toString())
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        // Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        email.setAdapter(adapter)
    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private val REQUEST_READ_CONTACTS = 0
    }
}
