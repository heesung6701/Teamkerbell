package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.model.data.RoleFeedback
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

class FeedBackTask(context: Context, var handler: Handler, token: String?) : NetworkTask(context, token) {

    val dataList = ArrayList<RoleFeedback>()

    var msg_code: Int = MSG_FAIL
    var message: String = "No Message"

    fun extractFeatureFromJson(jsonResponse: String) {

        message = "No Message"

        val realm = getRealmDefault(context)
        try {
            val baseJsonResponse = JSONObject(jsonResponse)
            if (baseJsonResponse.has("message")) {
                message = baseJsonResponse.getString("message")
                if (message.contains("Success")) {
                    val datas = baseJsonResponse.getJSONArray("data")
                    for (i in 0 until datas.length()) {
                        val obj = datas.getJSONObject(i)


                        val responseIdx = obj.getInt("role_response_idx")
                        val uIdx = obj.getInt("u_idx")
                        val content: String = obj.getString("content")
                        dataList.add(RoleFeedback(responseIdx, uIdx, content))
                    }

                    msg_code = MSG_SUCCESS

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

    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        extractFeatureFromJson(result!!)

        val msg = handler.obtainMessage()
        msg.what = msg_code
        msg.obj=dataList
        val data = Bundle()
        data.putString("message", message)
        msg.data = data
        handler.sendMessage(msg)
    }
}