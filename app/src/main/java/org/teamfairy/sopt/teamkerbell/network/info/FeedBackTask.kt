package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.model.assist.TaskResponseWithFeedback
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.model.data.RoleFeedback
import org.teamfairy.sopt.teamkerbell.model.data.TaskResponse
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DATA
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_FEEDBACK
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_FEEDBACK_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_FILE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_FILES
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_TASK_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_WRITE_TIME
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

class FeedBackTask(context: Context, var handler: Handler, token: String?) : NetworkTask(context, token) {


    var msgCode: Int = MSG_FAIL
    var message: String = "No Message"

    fun extractFeatureFromJson(jsonResponse: String) : TaskResponseWithFeedback? {

        message = "No Message"

        val realm = getRealmDefault(context)
        try {
            val dataList = ArrayList<RoleFeedback>()
            val baseJsonResponse = JSONObject(jsonResponse)
            if (baseJsonResponse.has(JSON_MESSAGE)) {
                message = baseJsonResponse.getString(JSON_MESSAGE)
                if (message.contains("Success")) {
                    val data = baseJsonResponse.getJSONObject(JSON_DATA)

                    val responseJ = data.getJSONArray(JSON_RESPONSE).getJSONObject(0)
                    val resp: TaskResponse = TaskResponse(
                            responseJ.getInt(JSON_U_IDX),
                            responseJ.getInt(JSON_TASK_IDX),
                            responseJ.getInt(JSON_RESPONSE_IDX),
                            responseJ.getString(JSON_CONTENT),
                            responseJ.getString(JSON_WRITE_TIME))
                    val fileArray = data.getJSONArray(JSON_FILES)
                    for (i in 0 until fileArray.length()) {
                        resp.fileArray.add(fileArray.getJSONObject(i).getString(JSON_FILE))
                    }
                    val feedbacks = data.getJSONArray(JSON_FEEDBACK)
                    for (i in 0 until feedbacks.length()) {
                        val obj = feedbacks.getJSONObject(i)


                        val responseIdx = obj.getInt(JSON_RESPONSE_IDX)
                        val feedbackIdx = obj.getInt(JSON_FEEDBACK_IDX)
                        val uIdx = obj.getInt(JSON_U_IDX)
                        val content: String = obj.getString(JSON_CONTENT)
                        val writeTime: String = obj.getString(JSON_WRITE_TIME)
                        dataList.add(RoleFeedback(feedbackIdx,responseIdx, uIdx, content,writeTime))
                    }

                    msgCode = MSG_SUCCESS
                    val taskResponseWithFeedback = TaskResponseWithFeedback()
                    taskResponseWithFeedback.taskResponse = resp
                    taskResponseWithFeedback.feedbacks = dataList
                    return taskResponseWithFeedback

                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
            if (realm.isInTransaction) realm.commitTransaction()
        }
        return null
    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        val obj = extractFeatureFromJson(result!!)

        val msg = handler.obtainMessage()
        msg.what = msgCode
        Log.d(NetworkTask::class.java.simpleName,"get Message "+if(msgCode== MSG_SUCCESS) "Success" else " failed")
        msg.obj= extractFeatureFromJson(result)
        val data = Bundle()
        data.putString("message", message)
        msg.data = data
        handler.sendMessage(msg)
    }
}