package org.teamfairy.sopt.teamkerbell.network

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_TOKEN
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_EXIST
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_EXPIRED
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_INVALID
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_NO_INTERNET
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_NO_INTERNET_STR
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-05.
 */

// 아이디 중복확인
// 회원가입
open class RefreshTokenTask(context: Context, var handler: Handler?, token: String?) : NetworkTask(context, token) {

    constructor(context: Context, handler: Handler) : this(context, handler, null)

    var message: String = "No Message"
    var msgCode = MSG_FAIL

    open fun extractFeatureFromJson(jsonResponse: String): Any? {
        val data: String?
        try {

            val baseJsonResponse = JSONObject(jsonResponse)
            if (baseJsonResponse.has(USGS_REQUEST_URL.JSON_MESSAGE)) {
                message = baseJsonResponse.getString(USGS_REQUEST_URL.JSON_MESSAGE)
                if (message.contains("Success") || message.contains("success"))
                    msgCode = MSG_SUCCESS
                else if (message.contains(MSG_NO_INTERNET_STR)) {
                    msgCode = MSG_NO_INTERNET
                } else if (message.contains("Exist") || message.contains("exist")) {
                    msgCode = MSG_EXIST
                } else if (message.contains("Expired") || message.contains("expired")) {
                    msgCode = MSG_EXPIRED
                } else if (message.contains("Invalid") || message.contains("invalid")) {
                    msgCode = MSG_INVALID
                } else if (message.contains("Failed") || message.contains("failed")) {
                    msgCode = MSG_FAIL
                }
            }
            if (baseJsonResponse.has(JSON_TOKEN)) {
                data = baseJsonResponse.getString(JSON_TOKEN)
                return data as Any
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        val obj = extractFeatureFromJson(result!!)

        if (handler != null) {
            val msg = handler!!.obtainMessage()
            msg.what = msgCode
            Log.d(NetworkTask::class.java.simpleName, "get Message " + if (msgCode == MSG_SUCCESS) "Success" else " failed")

            val bundle = Bundle()
            bundle.putString("message", message)
            msg.data = bundle

            if (obj != null)
                msg.obj = obj

            handler!!.sendMessage(msg)
        }
    }
}