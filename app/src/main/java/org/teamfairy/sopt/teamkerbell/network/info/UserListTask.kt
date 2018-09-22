package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Handler
import android.util.Log
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_BIO
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ID
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_PHONE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_PHOTO
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-06.
 */
class UserListTask(context: Context, var handler: Handler?, token: String?) : NetworkTask(context, token) {


    private var msgCode: Int = MSG_FAIL


    fun extractFeatureFromJson(jsonResponse: String) {


        val realm: Realm = getRealmDefault(context)
        msgCode = MSG_FAIL

        try {
            val baseJsonResponse = JSONObject(jsonResponse)
            if (baseJsonResponse.has(JSON_MESSAGE)) {
                val message = baseJsonResponse.getString(JSON_MESSAGE)
                if (message.contains("Success")) {


                    realm.beginTransaction()
                    realm.where(UserR::class.java).notEqualTo(UserR.ARG_U_IDX, LoginToken.getUserIdx(context)).findAll().deleteAllFromRealm()

                    val dataArray: JSONArray = baseJsonResponse.getJSONArray("data")

                    for (i in 0 until dataArray.length()) {
                        val data: JSONObject = dataArray.getJSONObject(i)

                        val user = User(
                                data.getInt(USGS_REQUEST_URL.JSON_U_IDX),
                                data.getString(USGS_REQUEST_URL.JSON_NAME)
                        )
                        user.phone = if (data.has(USGS_REQUEST_URL.JSON_PHONE)) data.getString(JSON_PHONE) else ""
                        user.bio = if (data.has(JSON_BIO)) data.getString(JSON_BIO) else ""
                        user.photo = if (data.has(JSON_PHOTO)) data.getString(JSON_PHOTO) else ""
                        user.id = if (data.has(JSON_ID)) data.getString(JSON_ID) else ""


                        val userR = user.toUserR()
                        realm.copyToRealmOrUpdate(userR)
                    }


                    val isUpdateR: IsUpdateR = realm.where(IsUpdateR::class.java).equalTo(IsUpdateR.ARG_WHAT, IsUpdateR.WHAT_USER).findFirst()
                            ?: realm.createObject(IsUpdateR::class.java, IsUpdateR.WHAT_USER)
                    if (!isUpdateR.isUpdate) {
                        isUpdateR.isUpdate = true
                        Log.d(LOG_TAG, "User Info is updated")
                    }

                    msgCode = MSG_SUCCESS

                    realm.commitTransaction()
                } else {
                    Log.d(LOG_TAG, message)
                }
            } else {
                Log.d(LOG_TAG, jsonResponse)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
            if (realm.isInTransaction)
                realm.commitTransaction()
        }

    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        extractFeatureFromJson(result!!)

        if (handler != null) {
            val msg = handler!!.obtainMessage()
            msg.what = msgCode
            Log.d(NetworkTask::class.java.simpleName, "get Message " + if (msgCode == Utils.MSG_SUCCESS) "Success" else " failed")
            handler!!.sendMessage(msg)
        }
    }
}