package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell._utils.StatusCode
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedR
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-06.
 */
class JoinedListTask(context: Context, var handler: Handler?, token: String?) : NetworkTask(context, token) {

    var msgCode: Int? = MSG_FAIL


    fun extractFeatureFromJson(jsonResponse: String) {

        var realm: Realm?=null

        msgCode = MSG_FAIL

        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has("message")) {
                val message = baseJsonResponse.getString("message")
                if (message.contains("Success")) {

                    realm = getRealmDefault(context)
                    realm.beginTransaction()

                    realm.where(JoinedR::class.java).findAll().deleteAllFromRealm()

                    val dataArray: JSONArray = baseJsonResponse.getJSONArray("data")

                    for (i in 0 until dataArray.length()) {

                        val data: JSONObject = dataArray.getJSONObject(i)


                        val uIdx = data.getInt("u_idx")
                        val gIdx = data.getInt("g_idx")

                        val joinedR = realm.createObject(JoinedR::class.java)
                        joinedR.g_idx = gIdx
                        joinedR.u_idx = uIdx


                    }
                    val isUpdateR = IsUpdateR()
                    isUpdateR.what= StatusCode.joinedChange
                    isUpdateR.isUpdate=true
                    realm.copyToRealmOrUpdate(isUpdateR)

                    realm.commitTransaction()
                    msgCode = MSG_SUCCESS
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, jsonResponse.toString(), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }finally {
            if (realm!=null && realm.isInTransaction) {
                realm.commitTransaction()
                Log.d("RealmTransaction", "commit " + this::class.java.simpleName)
            }
        }

    }



    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        extractFeatureFromJson(result!!)

        if (handler != null) {
            val msg = handler!!.obtainMessage()
            msg.what = msgCode!!
            Log.d(NetworkTask::class.java.simpleName,"get Message "+if(msgCode== Utils.MSG_SUCCESS) "Success" else " failed")
            handler!!.sendMessage(msg)
        }
    }
}