package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Handler
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-05.
 */
class GroupRListTask(context: Context, var handler: Handler?, token: String?) : NetworkTask(context, token) {

    var msg_code: Int = MSG_FAIL

    fun extractFeatureFromJson(jsonResponse: String) {

        val realm = getRealmDefault(context)
        try {


            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has("message")) {
                val message = baseJsonResponse.getString("message")
                if (message.contains("Success")) {


                    realm.beginTransaction()
                    realm.where(GroupR::class.java).findAll().deleteAllFromRealm()
                    val dataArray: JSONArray = baseJsonResponse.getJSONArray("data")

                    for (i in 0 until dataArray.length()) {
                        val data: JSONObject = dataArray.getJSONObject(i)
                        val g = GroupR()
                        g.g_idx = data.getInt("g_idx")
                        g.real_name = data.getString("real_name")
                        g.ctrl_name = data.getString("ctrl_name")
                        if (data.has("photo"))
                            g.photo = data.getString("photo")
                        realm.copyToRealmOrUpdate(g)
                    }
                    msg_code = MSG_SUCCESS
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
            if (realm.isInTransaction) {
                realm.commitTransaction()
            }
        }

    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        extractFeatureFromJson(result!!)

        if(handler!=null) {
            val msg = handler!!.obtainMessage()
            msg.what = MSG_SUCCESS
            handler!!.sendMessage(msg)
        }
    }
}