package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell._utils.TagUtils
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-06.
 */
class UserRListTask(context: Context, var handler: Handler?, token: String?) : NetworkTask(context, token) {


    private var msgCode: Int = MSG_FAIL


    fun extractFeatureFromJson(jsonResponse: String) {


        val realm: Realm = getRealmDefault(context)
        msgCode = MSG_FAIL

        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has("message")) {
                val message = baseJsonResponse.getString("message")
                if (message.contains("Success")) {


                    realm.beginTransaction()
                    realm.where(UserR::class.java).findAll().deleteAllFromRealm()

                    val dataArray: JSONArray = baseJsonResponse.getJSONArray("data")

                    for (i in 0 until dataArray.length()) {
                        val data: JSONObject = dataArray.getJSONObject(i)

                        val user = UserR()

                        user.u_idx = data.getInt("u_idx")
                        user.name = data.getString("name")
                        user.photo = data.getString("photo")
                        user.id = data.getString("id")
                        if (data.has("bio"))
                            user.bio = data.getString("bio")
                        if (data.has("phone"))
                            user.phone = data.getString("phone")

                        realm.copyToRealmOrUpdate(user)
                    }

                    msgCode = MSG_SUCCESS
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, jsonResponse.toString(), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
            if (realm.isInTransaction)
                realm.commitTransaction()
        }

    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        extractFeatureFromJson(result!!)

        if (handler != null) {
            val msg = handler!!.obtainMessage()
            msg.what = msgCode
            Log.d(NetworkTask::class.java.simpleName,"get Message "+if(msgCode== Utils.MSG_SUCCESS) "Success" else " failed")
            handler!!.sendMessage(msg)
        }
    }
}