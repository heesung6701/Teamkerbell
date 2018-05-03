package org.teamfairy.sopt.teamkerbell.network.info

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.model.data.SignalResponse
import org.teamfairy.sopt.teamkerbell.utils.Utils

/**
 * Created by lumiere on 2018-01-06.
 */
class SignalResponseListTask(context: Context, var handler: Handler, token : String?): NetworkTask(context,token) {

    var message: String = "No Message"

    var msgCode = Utils.MSG_FAIL

    fun extractFeatureFromJson(jsonResponse: String): ArrayList<SignalResponse> {
        val result = arrayListOf<SignalResponse>()

        message = "No Message"
        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has("message")) {
                message = baseJsonResponse.getString("message")
                if(message.contains("Success")){

                    val dataArray : JSONArray = baseJsonResponse.getJSONArray("data")

                    for( i in 0 until dataArray.length()){
                        val data : JSONObject = dataArray.getJSONObject(i)
                        val obj = SignalResponse(data.getInt("light_idx"),
                                data.getInt("u_idx"),
                                data.getString("color"),
                                data.getString("content"),
                                data.getString("write_time")
                        )
                        if(obj.content.equals("null")) obj.content=null
                        if(obj.write_time.equals("null")) obj.write_time=null
                        result.add(obj)

                    }
                    msgCode=Utils.MSG_SUCCESS
                }
                else{
                    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context,jsonResponse.toString(),Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return result
    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        val obj =extractFeatureFromJson(result!!)

        val msg = handler.obtainMessage()
        msg.what = msgCode

        msg.obj = obj
        val data = Bundle()
        data.putString("message",message)
        msg.data=data
        handler.sendMessage(msg)
    }
}