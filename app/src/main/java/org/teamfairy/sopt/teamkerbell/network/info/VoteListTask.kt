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
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DATA
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_FINISHED
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_NOT_FINISHED
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_TITLE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_VOTE_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_WRITE_TIME
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-06.
 */
class VoteListTask(context: Context, var handler: Handler, token: String?) : NetworkTask(context, token) {

    constructor(context: Context, handler: Handler) : this(context, handler, null)

    var message: String = "No Message"
    var msgCode = MSG_FAIL


    fun extractFeatureFromJson(jsonResponse: String): ArrayList<Vote>? {

        message = "No Message"


        val datas = ArrayList<Vote>()

//        val realm = getRealmDefault(context)
        try {
            val baseJsonResponse = JSONObject(jsonResponse)
            if (baseJsonResponse.has(JSON_MESSAGE)) {
                message = baseJsonResponse.getString(JSON_MESSAGE)
                if (message.contains("Success")) {
//                    realm.beginTransaction()
                    val voteData: JSONObject = baseJsonResponse.getJSONObject(JSON_DATA)
                    val voteNotFinished: JSONArray = voteData.getJSONArray(JSON_NOT_FINISHED)

                    for (j in 0 until voteNotFinished.length()) {

                        val obj: JSONObject = voteNotFinished.getJSONObject(j)

                        val vote = Vote(obj.getInt(JSON_VOTE_IDX),
                                obj.getInt(JSON_U_IDX),
                                obj.getString(JSON_WRITE_TIME),
                                obj.getString(JSON_CONTENT),
                                obj.getInt(JSON_ROOM_IDX),
                                obj.getString(JSON_TITLE),
                                obj.getInt(JSON_STATUS))

                        datas.add(vote)


                    }

                    val voteFinished: JSONArray = voteData.getJSONArray(JSON_FINISHED)

                    for (j in 0 until voteFinished.length()) {

                        val obj: JSONObject = voteFinished.getJSONObject(j)

                        val vote = Vote(obj.getInt(JSON_VOTE_IDX),
                                obj.getInt(JSON_U_IDX),
                                obj.getString(JSON_WRITE_TIME),
                                obj.getString(JSON_CONTENT),
                                obj.getInt(JSON_ROOM_IDX),
                                obj.getString(JSON_TITLE),
                                obj.getInt(JSON_STATUS))

                        datas.add(vote)

//                        val vote: JSONObject = voteFinished.getJSONObject(j)
//
//                        val obj = VoteR()
//                        obj.vote_idx = vote.getInt(JSON_VOTE_IDX)
//                        obj.u_idx = vote.getInt(JSON_U_IDX)
//                        obj.write_time = vote.getString(JSON_WRITE_TIME)
//                        obj.content = vote.getString(JSON_CONTENT)
//                        obj.room_idx = vote.getInt(JSON_ROOM_IDX)
//                        obj.title = vote.getString(JSON_TITLE)
//                        obj.status = vote.getInt(JSON_STATUS)
//
//                        realm.copyToRealmOrUpdate(obj)

                    }
//                    realm.commitTransaction()
                    msgCode = MSG_SUCCESS

                    return datas
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, jsonResponse.toString(), Toast.LENGTH_SHORT).show()
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
//            if (realm.isInTransaction) realm.commitTransaction()
        }
        return null

    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        val obj = extractFeatureFromJson(result!!)

        val msg = handler.obtainMessage()
        msg.what = msgCode
        msg.obj = obj
        Log.d(NetworkTask::class.java.simpleName, "get Message " + if (msgCode == Utils.MSG_SUCCESS) "Success" else " failed")

        val data = Bundle()
        data.putString("message", message)
        msg.data = data
        handler.sendMessage(msg)
    }
}