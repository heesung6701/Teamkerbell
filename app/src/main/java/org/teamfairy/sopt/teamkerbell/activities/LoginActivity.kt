package org.teamfairy.sopt.teamkerbell.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.ArrayList
import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN_PARAM_CLIENTTOKEN
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN_PARAM_ID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN_PARAM_PWD
import org.teamfairy.sopt.teamkerbell.network.auth.LoginTask
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import java.lang.ref.WeakReference


class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {

    private val mHandlerLogin: Handler = HandlerLogin(this)

    private class HandlerLogin(activity: LoginActivity) : Handler() {
        private val mActivity: WeakReference<LoginActivity> = WeakReference<LoginActivity>(activity)

        override fun handleMessage(msg: Message) {

            val activity = mActivity.get()

            if (activity != null) {

                activity.mAuthTask = null
                activity.showProgress(false)

                val result = msg.data.getString("message")

                when {
                    result.contains("Success") -> {
                        activity.setPref(activity.emailStr, activity.passwordStr)
                        activity.login()
                    }
                    result.contains("Failed") -> Toast.makeText(activity.applicationContext, "아이디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(activity.applicationContext, result, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    fun login() {



        //로그인할때 받아와야되나??
        NetworkUtils.connectGroupList(applicationContext,null,true)
        NetworkUtils.connectUserList(applicationContext,null,true)
        NetworkUtils.connectJoinedGroupList(applicationContext,null,true)
        NetworkUtils.connectJoinedRoomList(applicationContext,null,true)
        NetworkUtils.connectRoomList(applicationContext,null,true)


        val dataBase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val dataBaseReference: DatabaseReference = dataBase.reference
        val dataBaseFireToken: DatabaseReference? = dataBaseReference.child("firebase_tokens").ref
        dataBaseFireToken!!.child(LoginToken.getUserIdx(applicationContext).toString()).setValue(FirebaseInstanceId.getInstance().getToken()!!.toString())

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }


    fun setPref(id: String, pwd: String) {
        val pref = applicationContext.getSharedPreferences("pref_login_info", MODE_PRIVATE).edit()
        pref.putString("id", id)
        pref.putString("pwd", pwd)
        pref.apply()
    }

    override fun onBackPressed() {
        finish()
    }


    private var mAuthTask: LoginTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        populateAutoComplete()


        if (intent.getBooleanExtra(IntentTag.EXIT, false)) {
            finish()
        }

        if (intent.getBooleanExtra(IntentTag.FROMSIGNUP, false)) {
            email.setText(intent.getStringExtra("id") ?: "")
            password.setText(intent.getStringExtra("pwd") ?: "")
            attemptLogin()
        }


        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->  //키보드 엔터눌러도 로그인 되는듯?
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

        loaderManager.initLoader(0, null, this)
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
                    .setAction(android.R.string.ok,
                            { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) })
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
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
        mAuthTask!!.execute(URL_LOGIN, jsonParam.toString())
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

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE),

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
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
