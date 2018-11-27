package org.teamfairy.sopt.teamkerbell.activities

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
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
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = this::class.java.simpleName

    private var verifiedPhone: Boolean = false

    private var mSignUpTask: GetMessageTask? = null
    private var isSamePwd: Boolean = false
    private var isExistID: Boolean = false

    var emailStr: String = ""
    var passwordStr: String = ""


    private lateinit var mAuth: FirebaseAuth

    private var mVerificationInProgress = false
    private var mVerificationId: String? = ""
    private lateinit var mResendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        btn_complete.setOnClickListener(this)
        btn_start_verification.setOnClickListener(this)
        btn_verify_phone.setOnClickListener(this)


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

            if (!hasFocus) {
                attemptIdCheck(email.text.toString().toString())

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


        password_chk.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->

            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptSignUp()
                return@OnEditorActionListener true
            }
            false
        })

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")

                mVerificationInProgress = false

                updateUI(STATE_VERIFY_SUCCESS, credential)

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                mVerificationInProgress = false

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request

                    Toast.makeText(applicationContext, "올바르지 않는 형식입니다.", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(applicationContext, "너무 많은 시도를 하였습니다. 잠시 후 다시 시도 해주세요.", Toast.LENGTH_SHORT).show()
                }

                // Show a message and update the UI
                // ...
            }

            override fun onCodeSent(verificationId: String?,
                                    token: PhoneAuthProvider.ForceResendingToken) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId!!)

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token


                //전화번호를 누르고 메세지가 보내졌다는 걸 의미한다.
                //UI업데이트 하는 작업 필요
                updateUI(STATE_CODE_SENT)
            }
        }

    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_start_verification -> {
                if (!validatePhoneNumber()) {
                    return
                }
                startPhoneNumberVerification("+82 ${phone.text.substring(1)}")
            }
            R.id.btn_complete -> {
                attemptSignUp()
            }
            R.id.btn_verify_phone -> {
                val code = verification_code.text.toString()
                if (TextUtils.isEmpty(code)) {
                    verification_code.error = "Cannot be empty."
                    return
                }

                verifyPhoneNumberWithCode(mVerificationId, code)
            }
        }

    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = phone.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            phone.error = "Invalid phone number."
            return false
        }

        return true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }


    private fun startPhoneNumberVerification(phoneNumber: String) {
        Log.d(TAG, "phoneNumber: $phoneNumber")
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,      // Phone number to verify
                60,               // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this,             // Activity (for callback binding)
                mCallbacks)       // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        Log.d(TAG, "signInWithCredential:success")


                        updateUI(STATE_VERIFY_SUCCESS)

//                        task.result?.let {
//                            val user = it.user
//
//                            updateUI(STATE_SIGNIN_SUCCESS, user)
//                        }

                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {

                            verification_code.error = "올바르지 않는 코드입니다."
                        }
                        updateUI(STATE_SIGNIN_FAILED)
                    }
                }
    }


    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    var onFinish: () -> Unit = {
        btn_start_verification.isClickable = true
        btn_start_verification.text = getString(R.string.action_resend)

        phone.isEnabled = true
    }

    var onTick: (Int) -> Unit = {
        btn_start_verification.text = it.toString()
    }

    private var countDown: CountDown = CountDown(onFinish, onTick)


    private fun updateUI(uiState: Int, user: FirebaseUser? = mAuth.currentUser, cred: PhoneAuthCredential? = null) {
        when (uiState) {
            STATE_INITIALIZED -> {
                // Initialized state, show only the phone number field and start button
//                enableViews(buttonStartVerification, fieldPhoneNumber)
//                disableViews(buttonVerifyPhone, buttonResend, fieldVerificationCode)
//                detail.text = null
            }
            STATE_CODE_SENT -> {
                layout_verification_phone.visibility = View.VISIBLE
                // Code sent state, show the verification field, the
                btn_start_verification.isClickable = false
                phone.isEnabled = false


                countDown.start()

            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, show all options
                countDown.cancel()

                btn_start_verification.text = getString(R.string.action_resend)
            }
            STATE_VERIFY_SUCCESS -> {
                phone.isEnabled = false

                countDown.cancel()

                btn_start_verification.text = getString(R.string.txt_verification_success)
                btn_start_verification.background = getDrawable(R.drawable.shape_round_mint)
                btn_start_verification.setTextColor(ContextCompat.getColor(this, R.color.white))

                layout_verification_phone.visibility = View.GONE
                verifiedPhone = true
            }
            STATE_SIGNIN_FAILED -> {
                // No-op, handled by sign-in check
//                detail.setText(R.string.status_sign_in_failed)
            }
            STATE_SIGNIN_SUCCESS -> {
                //
            }
        }// Np-op, handled by sign-in check

        if (user == null) {
//            // Signed out
//            phoneAuthFields.visibility = View.VISIBLE
//            signedInButtons.visibility = View.GONE
//
//            status.setText(R.string.signed_out)
        } else {
            // Signed in
//            phoneAuthFields.visibility = View.GONE
//            signedInButtons.visibility = View.VISIBLE
//
//            enableViews(fieldPhoneNumber, fieldVerificationCode)
//            fieldPhoneNumber.text = null
//            fieldVerificationCode.text = null
//
//            status.setText(R.string.signed_in)
//            detail.text = getString(R.string.firebase_status_fmt, user.uid)
        }
    }

    private fun attemptIdCheck(str: String) {
        val idCheckTask = GetMessageTask(applicationContext, HandlerIdCheck(this))

        val jsonParam = JSONObject()
        jsonParam.put(URL_REGIST_CHECK_PARAM_ID, str)

        idCheckTask.execute(URL_REGIST_CHECK, NetworkTask.METHOD_POST, jsonParam.toString())
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
        } else if (txtName.length > User.name_max_length) {
            name.error = getString(R.string.error_invalid_length_20)
            focusView = name
            cancel = true
        }



        if (TextUtils.isEmpty(txtPhone)) {
            phone.error = getString(R.string.error_field_required)
            focusView = phone
            cancel = true
        } else if (txtPhone.length < "010-0000-0000".length) {
            phone.error = getString(R.string.error_invalid_phone)
            focusView = phone
            cancel = true
        }


        if (!verifiedPhone) {
            phone.error = "핸드폰를 인증해주세요"
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
            MSG_EXIST -> {
                Toast.makeText(applicationContext, getString(R.string.error_field_existed), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(applicationContext, getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateIdCheck(msg: Message) {
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


    companion object {
        private val TAG = "PhoneAuthActivity"
        private val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private val STATE_INITIALIZED = 1
        private val STATE_VERIFY_FAILED = 3
        private val STATE_VERIFY_SUCCESS = 4
        private val STATE_CODE_SENT = 2
        private val STATE_SIGNIN_FAILED = 5
        private val STATE_SIGNIN_SUCCESS = 6
    }


}

class CountDown(var finishFunc: () -> Unit, var tickFunc: (Int) -> Unit) {
    private val RESET_COUNT: Long = 60

    var count: Int = RESET_COUNT.toInt()


    var countDownTimer = object : CountDownTimer(RESET_COUNT * 1000, 1000) {

        override fun onFinish() {
            finishFunc()
        }

        override fun onTick(p0: Long) {
            tickFunc(count)
            count--
        }
    }

    fun start() {
        count = RESET_COUNT.toInt()
        countDownTimer.start()
    }

    fun cancel() {
        countDownTimer.cancel()

    }

    fun resetCount() {
        countDownTimer.cancel()
        count = RESET_COUNT.toInt()
        countDownTimer = object : CountDownTimer(6000, 1000) {

            override fun onFinish() {
                finishFunc()
            }

            override fun onTick(p0: Long) {
                tickFunc(count)
                count--
            }
        }.start()

    }

}