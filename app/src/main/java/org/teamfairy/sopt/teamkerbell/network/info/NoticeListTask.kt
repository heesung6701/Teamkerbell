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
class NoticeListTask(context: Context, var handler: Handler, token: String?) : NetworkTask(context, token) {

    var message: String = "No Message"
    var msgCode = MSG_FAIL

    fun extractFeatureFromJson(jsonResponse: String) : ArrayList<Notice>?{

        message = "No Message"
        val datas = ArrayList<Notice>()

        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has("message")) {
                message = baseJsonResponse.getString("message")
                if (message.contains("Success")) {

                    val dataArray: JSONArray = baseJsonResponse.getJSONArray("data")

                    for (i in 0 until dataArray.length()) {
                        val data: JSONObject = dataArray.getJSONObject(i)


                        val obj = Notice()

                        obj.u_idx = data.getInt(JSON_U_IDX)
                        obj.chat_idx = data.getInt(JSON_CHAT_IDX)
                        obj.write_time = data.getString(JSON_WRITE_TIME)
                        obj.content = data.getString(JSON_CONTENT)
                        obj.room_idx = data.getInt(JSON_ROOM_IDX)
                        obj.notice_idx = data.getInt(JSON_NOTICE_IDX)
                        obj.status=data.getInt(JSON_RESPONSE_STATUS)

                        datas.add(obj)
                    }
                    msgCode= MSG_SUCCESS
                    return datas
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, jsonResponse.toString(), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }finally {
        }
        return null
    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        val obj = extractFeatureFromJson(result!!)


        val msg = handler.obtainMessage()
        msg.what = msgCode
        Log.d(NetworkTask::class.java.simpleName,"get Message "+if(msgCode== Utils.MSG_SUCCESS) "Success" else " failed")
        msg.obj = obj

        val data = Bundle()
        data.putString("message", message)
        msg.data = data
        handler.sendMessage(msg)
    }
}