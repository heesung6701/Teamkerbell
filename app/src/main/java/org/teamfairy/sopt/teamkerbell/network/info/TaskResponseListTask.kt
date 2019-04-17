package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.Constants
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.model.data.TaskResponse
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DATA
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_FILE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

class TaskResponseListTask(context: Context, var handler: Handler, token: String?) : NetworkTask(context, token) {

    var message: String = "No Message"

    var msgCode = MSG_FAIL
    fun extractFeatureFromJson(jsonResponse: String): ArrayList<TaskResponse> {

        message = "No Message"

        val result = ArrayList<TaskResponse>()
        val realm = getRealmDefault(context)
        try {
            val baseJsonResponse = JSONObject(jsonResponse)
            if (baseJsonResponse.has(JSON_MESSAGE)) {
                message = baseJsonResponse.getString(JSON_MESSAGE)
                if (message.contains("Success")) {
                    val datas = baseJsonResponse.getJSONArray(JSON_DATA)
                    for (i in 0 until datas.length()) {
                        val data = datas.getJSONObject(i)
                        val dataResponse = data.getJSONObject(JSON_RESPONSE)
                        val dataFile = data.getJSONArray(JSON_FILE)

                        val files: ArrayList<String> = ArrayList<String>()
                        for (j in 0 until dataFile.length()) {
                            val dataFilsString: JSONObject = dataFile.getJSONObject(j)
                            val fileUrl: String = dataFilsString.getString(JSON_FILE)
                            files.add(fileUrl)
                        }

                        var uIdx = dataResponse.getString(USGS_REQUEST_URL.JSON_U_IDX)
                        if (uIdx == "null") uIdx = "-2"
                        val response = TaskResponse(
                                uIdx.toInt(),
                                dataResponse.getInt(USGS_REQUEST_URL.JSON_TASK_IDX),
                                dataResponse.getInt(USGS_REQUEST_URL.JSON_RESPONSE_IDX),
                                dataResponse.getString(USGS_REQUEST_URL.JSON_CONTENT),
                                dataResponse.getString(USGS_REQUEST_URL.JSON_WRITE_TIME),
                                files,
                                data.getInt(Constants.JSON_COUNT))

                        result.add(response)
                    }
                    msgCode = MSG_SUCCESS
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, jsonResponse, Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
            if (realm.isInTransaction) realm.commitTransaction()
        }
        return result
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