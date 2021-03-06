package org.teamfairy.sopt.teamkerbell.activities

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REGIST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REGIST_CHECK
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REGIST_CHECK_PARAM_ID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REGIST_PARAM_ID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REGIST_PARAM_NAME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REGIST_PARAM_PHONE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REGIST_PARAM_PWD
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.FROMSIGNUP
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_ID
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_PWD
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_EXIST
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS
import java.lang.ref.WeakReference
import java.net.URLEncoder
import javax.xml.transform.OutputKeys.METHOD

class SignUpActivity : AppCompatActivity() {




    private var mSignUpTask: GetMessageTask? = null
    private var isSamePwd: Boolean = false
    private var isExistID: Boolean = false

    var emailStr : String =""
    var passwordStr : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        phone.addTextChangedListener(object : TextWatcher {
            var beforeLength: Int = 0
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {


                if (editable.toString().length > beforeLength) {
                    if ((editable.toString().length == 3 || editable.toString().length == 8)) {
                        editable.append('-');
                    }
                } else {
                    if ((editable.toString().length == 3 || editable.toString().length == 8)) {
                        editable.delete(editable.length - 1, editable.length)
                    }
                }
                beforeLength = editable.toString().length
            }
        })

        password_chk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                if (password.text.toString().equals(password_chk.text.toString())) {
                    isSamePwd = true
                    password_chk.background.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColor), PorterDuff.Mode.SRC_ATOP)

                } else {
                    isSamePwd = false
                    password_chk.background.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.red), PorterDuff.Mode.SRC_ATOP)

                }


            }

            override fun afterTextChanged(editable: Editable) {

            }
        })


        email.setOnFocusChangeListener { v, hasFocus ->

            if(!hasFocus){
                attemptIdCheck(email.text.toString() .toString())

            }
        }
        email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(txt: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (txt!!.isNotEmpty()) {
//                    val encodeTxt: String = URLEncoder.encode(txt.toString(), "UTF-8")
//                    val str = String(encodeTxt.toByteArray(Charsets.UTF_8))
//                    attemptIdCheck(txt.toString())
                } else {
                    email.background.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColor), PorterDuff.Mode.SRC_ATOP)
                }
            }

        })

        btn_complete.setOnClickListener { attemptSignUp() }

        password_chk.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            //키보드 엔터눌러도 로그인 되는듯?
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptSignUp()
                return@OnEditorActionListener true
            }
            false
        })

    }


    private fun attemptIdCheck(str : String){
        val idCheckTask = GetMessageTask(applicationContext, HandlerIdCheck(this))

        val jsonParam = JSONObject()
        jsonParam.put(URL_REGIST_CHECK_PARAM_ID, str)

        idCheckTask.execute(URL_REGIST_CHECK, NetworkTask.METHOD_POST,jsonParam.toString())
    }
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {

        if (name.text.isNotEmpty() || email.text.isNotEmpty()) {
            if (doubleBackToExitPressedOnce) {
                finish()
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to back", Toast.LENGTH_SHORT).show()

            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
        } else
            finish()
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8
    }

    private fun attemptSignUp() {


        if (mSignUpTask != null) {
            return
        }

        // Reset errors.
        email.error = null
        password.error = null

        emailStr = email.text.toString()
        passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordStr)) {
            password.error = getString(R.string.error_field_required)
            focusView = password
            cancel = true
        } else if (!isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }
        if (password_chk.text.isNotEmpty() && !isSamePwd) {
            password_chk.error = getString(R.string.error_incorrect_password)
            focusView = password_chk
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }


        // Store values at the time of the login attempt.
        val txtName = name.text.toString()
        val txtPhone = phone.text.toString()


        if (TextUtils.isEmpty(txtName)) {
            name.error = getString(R.string.error_field_required)
            focusView = name
            cancel = true
        }else if(txtName.length>User.name_max_length){
            name.error = getString(R.string.error_invalid_length_20)
            focusView = name
            cancel = true
        }



        if (TextUtils.isEmpty(txtPhone)) {
            phone.error = getString(R.string.error_field_required)
            focusView = phone
            cancel = true
        }else if(txtPhone.length < "010-0000-0000".length){
            phone.error = getString(R.string.error_invalid_phone)
            focusView = phone
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {



            val jsonParam = JSONObject()

            try {
                jsonParam.put(URL_REGIST_PARAM_ID, emailStr)
                jsonParam.put(URL_REGIST_PARAM_PWD, passwordStr)
                jsonParam.put(URL_REGIST_PARAM_NAME, txtName)
                jsonParam.put(URL_REGIST_PARAM_PHONE, txtPhone)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            mSignUpTask = GetMessageTask(applicationContext, HandlerSignUp(this))
            mSignUpTask!!.execute(URL_REGIST, NetworkTask.METHOD_POST, jsonParam.toString())
        }
    }


    fun signUpSuccess(msg: Message) {
        mSignUpTask = null


        when (msg.what) {
            MSG_SUCCESS -> {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.putExtra(FROMSIGNUP, true)
                intent.putExtra(INTENT_ID, emailStr)
                intent.putExtra(INTENT_PWD, passwordStr)
                startActivity(intent)
                finish()
            }
            MSG_EXIST->{
                Toast.makeText(applicationContext, getString(R.string.error_field_existed), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(applicationContext, getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun updateIdCheck(msg : Message){
        val result = msg.data.getString("message")

        isExistID = result.contains("Exist")
        if (isExistID) {
            email.background.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.red), PorterDuff.Mode.SRC_ATOP)
            email.error = getString(R.string.error_field_existed)
        } else {
            email.background.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.mainColor), PorterDuff.Mode.SRC_ATOP)
            email.error = null
        }
    }
    private class HandlerSignUp(activity: SignUpActivity) : Handler() {
        private val mActivity: WeakReference<SignUpActivity> = WeakReference<SignUpActivity>(activity)

        override fun handleMessage(msg: Message) {

            mActivity.get()?.signUpSuccess(msg)
        }
    }


    private class HandlerIdCheck(activity: SignUpActivity) : Handler() {
        private val mActivity: WeakReference<SignUpActivity> = WeakReference<SignUpActivity>(activity)

        override fun handleMessage(msg: Message) {
            mActivity.get()?.updateIdCheck(msg)
        }
    }
}
