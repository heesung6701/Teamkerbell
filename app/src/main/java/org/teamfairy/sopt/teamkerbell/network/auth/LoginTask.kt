package org.teamfairy.sopt.teamkerbell.network.auth

import android.content.Context
import android.os.Bundle
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import android.os.Handler
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_BIO
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CLIENT_TOKEN
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_PHONE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_PHOTO
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_TOKEN
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-04.
 */
class LoginTask(context: Context, var handler: Handler) : NetworkTask(context) {

    var message: String = "No Message"
    var msgCode = Utils.MSG_FAIL

    fun extractFeatureFromJson(jsonResponse: String) {

        message = "No Message"

        val realm = DatabaseHelpUtils.getRealmDefault(context)
        try {
            val baseJsonResponse = JSONObject(jsonResponse)
            if (baseJsonResponse.has(JSON_MESSAGE)) {
                message = baseJsonResponse.getString(JSON_MESSAGE)

                if (message.contains("Success")) {

                    val user = User(
                            baseJsonResponse.getInt(USGS_REQUEST_URL.JSON_U_IDX),
                            baseJsonResponse.getString(USGS_REQUEST_URL.JSON_NAME)
                    )
                    user.phone = if (baseJsonResponse.has(USGS_REQUEST_URL.JSON_PHONE)) baseJsonResponse.getString(JSON_PHONE) else ""
                    user.bio = if (baseJsonResponse.has(JSON_BIO)) baseJsonResponse.getString(JSON_BIO) else ""
                    user.photo = if (baseJsonResponse.has(JSON_PHOTO)) baseJsonResponse.getString(JSON_PHOTO) else ""
                    user.id = if (baseJsonResponse.has(JSON_ID)) baseJsonResponse.getString(JSON_ID) else ""
                    val userR = user.toUserR()

                    realm.beginTransaction()
                    realm.copyToRealmOrUpdate(userR)
                    realm.commitTransaction()

                    LoginToken.setPref(context, user,
                            baseJsonResponse.getString(JSON_TOKEN))
                    msgCode = MSG_SUCCESS
                } else {
                    Log.d("NetworkTask:Error", message)
                }
            } else {
                Log.d("NetworkTask:Error", jsonResponse)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
            if (realm.isInTransaction) {
                realm.commitTransaction()
            }
            realm.close()
        }

    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        extractFeatureFromJson(result!!)

        val msg = handler.obtainMessage()
        msg.what = msgCode
        Log.d(NetworkTask::class.java.simpleName, "get Message " + if (msgCode == MSG_SUCCESS) "Success" else " failed")
        val data = Bundle()
        data.putString("message", message)
        msg.data = data
        handler.sendMessage(msg)
    }
}