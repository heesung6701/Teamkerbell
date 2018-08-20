package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CHAT_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DATA
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_NOTICE_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_WRITE_TIME
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-06.
 */
class NoticeTask(context: Context, var handler: Handler, token: String?) : NetworkTask(context, token) {

    var message: String = "No Message"
    var msgCode = MSG_FAIL

    fun extractFeatureFromJson(jsonResponse: String): Notice? {

        message = "No Message"

        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has(JSON_MESSAGE)) {
                message = baseJsonResponse.getString(JSON_MESSAGE)
                if (message.contains("Success")) {

                    val dataArray: JSONArray = baseJsonResponse.getJSONArray(JSON_DATA)
                    val data: JSONObject = dataArray.getJSONObject(0)

                    val obj = Notice()

                    obj.u_idx = data.getInt(JSON_U_IDX)
                    obj.write_time = data.getString(JSON_WRITE_TIME)
                    obj.content = data.getString(JSON_CONTENT)
                    obj.room_idx = data.getInt(JSON_ROOM_IDX)
                    obj.notice_idx = data.getInt(JSON_NOTICE_IDX)
                    if(data.has(JSON_RESPONSE_STATUS))
                        obj.status = data.getInt(JSON_RESPONSE_STATUS)
                    else
                        obj.status = Notice.ARG_STATUS_READ


                    msgCode = MSG_SUCCESS
                    return obj
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, jsonResponse.toString(), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
        }
        return null
    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        val obj = extractFeatureFromJson(result!!)


        val msg = handler.obtainMessage()
        msg.what = msgCode
        Log.d(NetworkTask::class.java.simpleName, "get Message " + if (msgCode == Utils.MSG_SUCCESS) "Success" else " failed")
        msg.obj = obj

        val data = Bundle()
        data.putString("message", message)
        msg.data = data
        handler.sendMessage(msg)
    }
}