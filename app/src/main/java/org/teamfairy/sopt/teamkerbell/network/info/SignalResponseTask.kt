package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DATA
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ENTIRE_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_OPEN_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE_COLOR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_SIGNAL_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_WRITE_TIME
import org.teamfairy.sopt.teamkerbell.utils.Utils

/**
 * Created by lumiere on 2018-01-06.
 */
class SignalResponseTask(context: Context, var handler: Handler, token: String?) : NetworkTask(context, token) {

    var message: String = "No Message"

    var msgCode = Utils.MSG_FAIL

    fun extractFeatureFromJson(jsonResponse: String): Signal? {

        message = "No Message"
        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has(JSON_MESSAGE)) {
                message = baseJsonResponse.getString(JSON_MESSAGE)
                if (message.contains("Success")) {

                    val data: JSONObject = baseJsonResponse.getJSONObject(JSON_DATA)

                    val obj = Signal(data.getInt(JSON_SIGNAL_IDX),
                            data.getInt(JSON_U_IDX),
                            data.getString(JSON_WRITE_TIME),
                            data.getInt(JSON_OPEN_STATUS),
                            data.getInt(JSON_ROOM_IDX),
                            data.getString(JSON_CONTENT),
                            data.getInt(JSON_ENTIRE_STATUS)
                    )
                    if (data.has(JSON_RESPONSE_COLOR)) obj.responseColor = data.getString(JSON_RESPONSE_COLOR)
                    if (data.has(JSON_RESPONSE_CONTENT)) obj.responseContent = data.getString(JSON_RESPONSE_CONTENT)

                    if (obj.content.equals("null")) obj.content = null
                    if (obj.responseContent.equals("null")) obj.responseContent = null
                    if (obj.responseColor.equals("null")) obj.responseColor = "a"
                    if (obj.write_time.equals("null")) obj.write_time = ""

                    msgCode = Utils.MSG_SUCCESS
                    return obj
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, jsonResponse.toString(), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
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