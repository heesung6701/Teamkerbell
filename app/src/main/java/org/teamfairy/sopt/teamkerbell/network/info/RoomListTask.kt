package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.model.realm.IsUpdateR
import org.teamfairy.sopt.teamkerbell.model.realm.RoomR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CTRL_NAME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_G_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_PHOTO
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_REAL_NAME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-05.
 */
class RoomListTask(context: Context, var handler: Handler?, token: String?) : NetworkTask(context, token) {

    var msgCode: Int = MSG_FAIL

    fun extractFeatureFromJson(jsonResponse: String) {

        val realm = getRealmDefault(context)
        try {


            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has("message")) {
                val message = baseJsonResponse.getString("message")
                if (message.contains("Success")) {


                    realm.beginTransaction()
                    realm.where(RoomR::class.java).findAll().deleteAllFromRealm()
                    val dataArray: JSONArray = baseJsonResponse.getJSONArray("data")

                    for (i in 0 until dataArray.length()) {
                        val data: JSONObject = dataArray.getJSONObject(i)
                        val r = RoomR()
                        r.g_idx = data.getInt(JSON_G_IDX)
                        r.room_idx = data.getInt(JSON_ROOM_IDX)
                        r.real_name = data.getString(JSON_REAL_NAME)
                        r.ctrl_name = data.getString(JSON_CTRL_NAME)
                        if (data.has(JSON_PHOTO))
                            r.photo = data.getString(JSON_PHOTO)
                        realm.copyToRealmOrUpdate(r)
                    }
                    msgCode = MSG_SUCCESS


                    val isUpdateR : IsUpdateR = realm.where(IsUpdateR::class.java).equalTo(IsUpdateR.ARG_WHAT , IsUpdateR.WHAT_ROOM).findFirst()
                            ?: realm.createObject(IsUpdateR::class.java, IsUpdateR.WHAT_ROOM)
                    if(!isUpdateR.isUpdate) {
                        isUpdateR.isUpdate = true
                        Log.d(LOG_TAG,"Room Info is updated")
                    }


                    realm.commitTransaction()


                } else {
                    Log.d(LOG_TAG, message)
                }
            } else {
                Log.d(LOG_TAG, jsonResponse)
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
            msg.what = msgCode
            Log.d(NetworkTask::class.java.simpleName,"get Message "+if(msgCode== Utils.MSG_SUCCESS) "Success" else " failed")
            handler!!.sendMessage(msg)
        }
    }
}