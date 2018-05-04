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
import org.teamfairy.sopt.teamkerbell.model.realm.RoleTaskR
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-06.
 */
class RoleTaskListTask(context: Context, var handler: Handler, token : String?): NetworkTask(context,token) {

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
                    realm.where(RoleTaskR::class.java).findAll().deleteAllFromRealm()

                    val dataArray : JSONArray = baseJsonResponse.getJSONArray("data")
                    for(i in 0 until dataArray.length()){
                        val obj = dataArray.getJSONObject(i)
                        val taskR = RoleTaskR()
                        taskR.role_idx=obj.getInt("role_idx")
                        taskR.task_idx=obj.getInt("role_task_idx")
                        taskR.content=obj.getString("content")
                        val users = obj.getJSONArray("userArray")
                        var uidArrayStr : String =if(users.length()>0)  users.getString(0) else ""
                        for( j in 1 until users.length()){
                            uidArrayStr += "/"+users.getString(j)
                        }
                        taskR.userIdArrayStr=uidArrayStr

                        realm.copyToRealmOrUpdate(taskR)

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


        extractFeatureFromJson(result!!)

        val msg = handler.obtainMessage()
        msg.what = msgCode
        Log.d(NetworkTask::class.java.simpleName,"get Message "+if(msgCode== Utils.MSG_SUCCESS) "Success" else " failed")
        val data = Bundle()
        data.putString("message",message)
        msg.data=data
        handler.sendMessage(msg)
    }
}