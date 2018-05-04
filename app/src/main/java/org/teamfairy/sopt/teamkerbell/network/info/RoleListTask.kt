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
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell.model.realm.RoleR
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-06.
 */
class RoleListTask(context: Context, var handler: Handler, token : String?): NetworkTask(context,token) {

    var message: String = "No Message"
    var msgCode = MSG_FAIL


    fun extractFeatureFromJson(jsonResponse: String){

        message = "No Message"

        val realm = getRealmDefault(context)
        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has("message")) {
                message = baseJsonResponse.getString("message")
                if(message.contains("Success")){

                    realm.beginTransaction()
                    realm.where(RoleR::class.java).findAll().deleteAllFromRealm()

                    val dataArray : JSONArray = baseJsonResponse.getJSONArray("data")
                    for(i in 0 until dataArray.length()){
                        if(dataArray.getJSONObject(i)!=null) {
                            val obj = dataArray.getJSONObject(i)
                            val roleR = RoleR()
                            roleR.role_idx = obj.getInt("role_idx")
                            roleR.g_idx = obj.getInt("g_idx")
                            roleR.title = obj.getString("title")
                            roleR.master_idx = obj.getInt("master_idx")
                            roleR.write_time = obj.getString("write_time")

                            realm.copyToRealmOrUpdate(roleR)

                        }
                    }
                    realm.commitTransaction()
                    msgCode=MSG_SUCCESS

                }
                else{
                    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context,jsonResponse.toString(),Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }finally {
            if(realm.isInTransaction) realm.commitTransaction()
        }

    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        val obj =extractFeatureFromJson(result!!)

        val msg = handler.obtainMessage()
        msg.what = msgCode
        Log.d(NetworkTask::class.java.simpleName,"get Message "+if(msgCode== Utils.MSG_SUCCESS) "Success" else " failed")
        msg.obj = obj
        val data = Bundle()
        data.putString("message",message)
        msg.data=data
        handler.sendMessage(msg)
    }
}