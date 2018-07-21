package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.model.data.TaskResponse
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
            if (baseJsonResponse.has("message")) {
                message = baseJsonResponse.getString("message")
                if (message.contains("Success")) {
                    val datas = baseJsonResponse.getJSONArray("data")
                    for (i in 0 until datas.length()) {
                        val data = datas.getJSONObject(i)
                        val dataResponse = data.getJSONObject("response")
                        val dataFile = data.getJSONArray("file")

                        val files: ArrayList<String> = ArrayList<String>()
                        for (j in 0 until dataFile.length()) {
                            val dataFilsString  : JSONObject= dataFile.getJSONObject(j)
                            val fileUrl :String = dataFilsString.getString("file")
                            files.add(fileUrl)
                        }


                        var uIdx = dataResponse.getString("u_idx")
                        if(uIdx == "null") uIdx="-2"
                        val response = TaskResponse(
                                uIdx.toInt(),
                                dataResponse.getInt("role_task_idx"),
                                dataResponse.getInt("role_response_idx"),
                                dataResponse.getString("content"),
                                dataResponse.getString("write_time"),
                                files,
                                data.getInt("count"))

                        result.add(response)
                    }
                    msgCode=MSG_SUCCESS


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
        Log.d(NetworkTask::class.java.simpleName,"get Message "+if(msgCode== Utils.MSG_SUCCESS) "Success" else " failed")
        msg.obj = obj
        val data = Bundle()
        data.putString("message", message)
        msg.data = data
        handler.sendMessage(msg)
    }
}