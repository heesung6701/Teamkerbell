package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.model.data.Notice
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.model.data.Vote
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CHAT_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DATA
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ENTIRE_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_G_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_SIGNAL_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_NOTICES
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_NOTICE_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_OPEN_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_SIGNALS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_TITLE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_VOTES
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_VOTE_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_WRITE_TIME
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL

/**
 * Created by lumiere on 2018-05-30.
 */
class UnperformedTask(context: Context, var handler: Handler, token: String?) : NetworkTask(context, token) {

    var message: String = "No Message"

    var msgCode = MSG_FAIL

    fun extractFeatureFromJson(jsonResponse: String): HashMap<String, ArrayList<*>>? {

        val result = HashMap<String, ArrayList<*>>()
        message = "No Message"
        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has(JSON_MESSAGE)) {
                message = baseJsonResponse.getString(JSON_MESSAGE)
                if (message.contains("Success")) {
                    val dataJson: JSONObject = baseJsonResponse.getJSONObject(JSON_DATA)


                    val datasNotice: JSONArray = dataJson.getJSONArray(JSON_NOTICES)
                    val noticeList = ArrayList<Notice>()

                    for (j in 0 until datasNotice.length()) {
                        val data = datasNotice.getJSONObject(j)

                        val notice = Notice(
                                data.getInt(JSON_U_IDX),
                                data.getString(JSON_WRITE_TIME),
                                data.getString(JSON_CONTENT),
                                data.getInt(JSON_ROOM_IDX),
                                data.getInt(JSON_NOTICE_IDX))

                        if (data.has(JSON_STATUS))
                            notice.status = data.getInt(JSON_STATUS)

                        noticeList.add(notice)
                    }

                    val datasSignal: JSONArray = dataJson.getJSONArray(JSON_SIGNALS)
                    val signalList = ArrayList<Signal>()
                    for (j in 0 until datasSignal.length()) {
                        val data = datasSignal.getJSONObject(j)

                        val signal = Signal(
                                data.getInt(JSON_SIGNAL_IDX),
                                data.getInt(JSON_U_IDX),
                                data.getString(JSON_WRITE_TIME),
                                data.getInt(JSON_OPEN_STATUS),
                                data.getInt(JSON_ROOM_IDX),
                                data.getString(JSON_CONTENT),
                                data.getInt(JSON_ENTIRE_STATUS)
                        )
                        signalList.add(signal)
                    }


                    val datasVote: JSONArray = dataJson.getJSONArray(JSON_VOTES)
                    val voteList = ArrayList<Vote>()


                    for (j in 0 until datasVote.length()) {
                        val data = datasVote.getJSONObject(j)

                        val vote = Vote(
                                data.getInt(JSON_VOTE_IDX),
                                data.getInt(JSON_U_IDX),
                                data.getString(JSON_WRITE_TIME),
                                data.getString(JSON_CONTENT),
                                data.getInt(JSON_ROOM_IDX),
                                data.getString(JSON_TITLE),
                                data.getInt(JSON_STATUS)
                        )
                        voteList.add(vote)
                    }

                    result[JSON_NOTICE] = noticeList
                    result[JSON_SIGNALS] = signalList
                    result[JSON_VOTES] = voteList
                    msgCode = Utils.MSG_SUCCESS

                    return result
                } else {
                    Toast.makeText(context, context.getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, context.getString(R.string.txt_message_fail), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null
    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        extractFeatureFromJson(result!!)

        val obj = extractFeatureFromJson(result)

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