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
import org.teamfairy.sopt.teamkerbell.model.data.Role
import org.teamfairy.sopt.teamkerbell.model.realm.RoleR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CHAT_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MASTER_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROLE_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_TITLE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_WRITE_TIME
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS

/**
 * Created by lumiere on 2018-01-06.
 */
class RoleListTask(context: Context, var handler: Handler, token : String?): NetworkTask(context,token) {

    var message: String = "No Message"
    var msgCode = MSG_FAIL


    fun extractFeatureFromJson(jsonResponse: String) : ArrayList<Role>?{

        val datas : ArrayList<Role> = ArrayList<Role>()

        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has("message")) {
                message = baseJsonResponse.getString("message")
                if(message.contains("Success")){


                    val dataArray : JSONArray = baseJsonResponse.getJSONArray("data")
                    for(i in 0 until dataArray.length()){
                        if(dataArray.getJSONObject(i)!=null) {
                            val obj = dataArray.getJSONObject(i)
                            val role = Role(obj.getInt(JSON_ROLE_IDX),
                                    obj.getInt(JSON_ROOM_IDX),
                                    obj.getString(JSON_TITLE),
                                    obj.getInt(JSON_MASTER_IDX),
                                    obj.getString(JSON_WRITE_TIME))
                            datas.add(role)

                        }
                    }
                    msgCode=MSG_SUCCESS
                    return datas
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
        }
        return null

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