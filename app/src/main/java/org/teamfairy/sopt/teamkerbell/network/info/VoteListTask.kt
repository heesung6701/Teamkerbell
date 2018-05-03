package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.model.realm.VoteR

/**
 * Created by lumiere on 2018-01-06.
 */
class VoteListTask(context: Context, var handler: Handler, token: String?) : NetworkTask(context, token) {

    constructor(context: Context, handler: Handler) : this(context, handler, null)

    var message: String = "No Message"

    fun extractFeatureFromJson(jsonResponse: String) {

        message = "No Message"
        val realm = getRealmDefault(context)
        try {
            val baseJsonResponse = JSONObject(jsonResponse)
            if (baseJsonResponse.has("message")) {
                message = baseJsonResponse.getString("message")
                if (message.contains("Success")) {
                    realm.beginTransaction()
                    val voteData: JSONObject = baseJsonResponse.getJSONObject("data")
                    val voteNotFinished: JSONArray = voteData.getJSONArray("NotFinished")

                    for (j in 0 until voteNotFinished.length()) {

                        val vote: JSONObject = voteNotFinished.getJSONObject(j)

                        val obj = VoteR()

                        obj.vote_idx = vote.getInt("vote_idx")
                        obj.u_idx = vote.getInt("u_idx")
                        obj.write_time = vote.getString("write_time")
                        obj.content = vote.getString("content")
                        obj.g_idx = vote.getInt("g_idx")
                        obj.title = vote.getString("title")
                        obj.status = vote.getInt("status")

                        realm.copyToRealmOrUpdate(obj)
                    }

                    val voteFinished: JSONArray = voteData.getJSONArray("Finished")

                    for (j in 0 until voteFinished.length()) {

                        val vote: JSONObject = voteFinished.getJSONObject(j)

                        val obj = VoteR()

                        obj.vote_idx = vote.getInt("vote_idx")
                        obj.u_idx = vote.getInt("u_idx")
                        obj.write_time = vote.getString("write_time")
                        obj.content = vote.getString("content")
                        obj.g_idx = vote.getInt("g_idx")
                        obj.title = vote.getString("title")
                        obj.status = vote.getInt("status")

                        realm.copyToRealmOrUpdate(obj)

                    }
                    realm.commitTransaction()

                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, jsonResponse.toString(), Toast.LENGTH_SHORT).show()
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
        msg.what = 0

        val data = Bundle()
        data.putString("message", message)
        msg.data = data
        handler.sendMessage(msg)
    }
}