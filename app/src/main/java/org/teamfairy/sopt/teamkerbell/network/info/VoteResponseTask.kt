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
import org.teamfairy.sopt.teamkerbell._utils.TagUtils
import org.teamfairy.sopt.teamkerbell.model.data.Vote
import org.teamfairy.sopt.teamkerbell.model.data.VoteResponse
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-01-06.
 */
class VoteResponseTask(context: Context, var handler: Handler, token: String?) : NetworkTask(context, token) {

    constructor(context: Context, handler: Handler) : this(context, handler, null)

    var message: String = "No Message"
    var msgCode = MSG_FAIL

    fun extractFeatureFromJson(jsonResponse: String): VoteResponse? {

        var vote: Vote by Delegates.notNull()
        val choices = HashMap<Int, String>()
        val responses = HashMap<Int, Int>()

        message = "No Message"

        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has("message")) {
                message = baseJsonResponse.getString("message")
                if (message.contains("Success")) {

                    if (baseJsonResponse.has("vote")) {
                        val data: JSONObject = baseJsonResponse.getJSONObject("vote")
                        vote = Vote(
                                data.getInt("vote_idx"),
                                data.getInt("u_idx"),
                                data.getString("write_time"),
                                data.getString("content"),
                                data.getInt("g_idx"),
                                data.getString("title"), data.getInt("status")
                        )
                    }


                    if (baseJsonResponse.has("choice")) {
                        val examplesArray: JSONArray = baseJsonResponse.getJSONArray("choice")
                        for (i in 0 until examplesArray.length()) {
                            val example = examplesArray.getJSONObject(i)
                            val idx = example.getInt("vote_content_idx")
                            val choice = example.getString("content")

                            choices[idx] = choice
                        }
                    }

                    val responseArray: JSONArray = baseJsonResponse.getJSONArray("response")

                    for (i in 0 until responseArray.length()) {
                        val data: JSONObject = responseArray.getJSONObject(i)


                        val uIdx = data.getInt("u_idx")
                        var value = -1
                        if (data.has("value") && data.get("value") is Int)
                            value = data.getInt("value")
                        responses[uIdx] = value
                    }

                    msgCode= MSG_SUCCESS
                    return VoteResponse(vote, choices, responses)
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

        Log.d(NetworkTask::class.java.simpleName,"get Message "+if(msgCode== MSG_SUCCESS) "Success" else " failed")
        msg.obj = obj

        val data = Bundle()
        data.putString("message", message)

        msg.data = data
        handler.sendMessage(msg)
    }
}