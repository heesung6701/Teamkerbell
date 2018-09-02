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
import org.teamfairy.sopt.teamkerbell.model.data.Vote
import org.teamfairy.sopt.teamkerbell.model.data.VoteResponse
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CHOICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_TITLE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_VALUE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_VOTE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_VOTE_CONTENT_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_VOTE_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_WRITE_TIME
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
            if (baseJsonResponse.has(JSON_MESSAGE)) {
                message = baseJsonResponse.getString(JSON_MESSAGE)
                if (message.contains("Success")) {

                    if (baseJsonResponse.has(JSON_VOTE)) {
                        val data: JSONObject = baseJsonResponse.getJSONObject(JSON_VOTE)
                        vote = Vote(
                                data.getInt(JSON_VOTE_IDX),
                                data.getInt(JSON_U_IDX),
                                data.getString(JSON_WRITE_TIME),
                                data.getString(JSON_CONTENT),
                                data.getInt(JSON_ROOM_IDX),
                                data.getString(JSON_TITLE), data.getInt(JSON_STATUS)
                        )
                    }


                    if (baseJsonResponse.has(JSON_CHOICE)) {
                        val examplesArray: JSONArray = baseJsonResponse.getJSONArray(JSON_CHOICE)
                        for (i in 0 until examplesArray.length()) {
                            val example = examplesArray.getJSONObject(i)
                            val idx = example.getInt(JSON_VOTE_CONTENT_IDX)
                            val choice = example.getString(JSON_CONTENT)

                            choices[idx] = choice
                        }
                    }

                    val responseArray: JSONArray = baseJsonResponse.getJSONArray(JSON_RESPONSE)

                    for (i in 0 until responseArray.length()) {
                        val data: JSONObject = responseArray.getJSONObject(i)


                        val uIdx = data.getInt(JSON_U_IDX)
                        var value = -1
                        if (data.has(JSON_VALUE) && data.get(JSON_VALUE) is Int)
                            value = data.getInt(JSON_VALUE)
                        responses[uIdx] = value
                    }

                    msgCode= MSG_SUCCESS
                    return VoteResponse(vote, choices, responses)
                } else {
//                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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