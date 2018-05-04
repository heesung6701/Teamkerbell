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
import org.teamfairy.sopt.teamkerbell.model.realm.SignalR
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-06.
 */
class SignalListTask(context: Context, var handler: Handler, token : String?): NetworkTask(context,token) {

    constructor(context: Context, handler : Handler) : this(context,handler,null)
    var message: String = "No Message"
    var msgCode = MSG_FAIL

    fun extractFeatureFromJson(jsonResponse: String){

        message = "No Message"
        val realm  = getRealmDefault(context)
        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has("message")) {
                message = baseJsonResponse.getString("message")
                if(message.contains("Success")){

                    val dataArray : JSONArray = baseJsonResponse.getJSONArray("data")

                    realm.beginTransaction()
                    for( i in 0 until dataArray.length()){
                        val data : JSONObject = dataArray.getJSONObject(i)

                        val obj = SignalR()
                        obj.light_idx=data.getInt("light_idx")
                        obj.u_idx=data.getInt("u_idx")
                        obj.chat_idx=data.getInt("chat_idx")
                        obj.write_time=data.getString("write_time")
                        obj.open_status=data.getInt("open_status")
                        obj.g_idx=data.getInt("g_idx")
                        obj.content=data.getString("content")
                        obj.entire_status=data.getInt("entire_status")
                        if(data.has("color"))
                            obj.color=data.getString("color")

                        realm.copyToRealmOrUpdate(obj)

                    }
                    realm.commitTransaction()
                    msgCode = MSG_SUCCESS

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
            if(realm.isInTransaction)
                realm.commitTransaction()
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