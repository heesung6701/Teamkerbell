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
import org.teamfairy.sopt.teamkerbell.model.data.Signal
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CHAT_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_COLOR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DATA
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ENTIRE_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_OPEN_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE_COLOR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_SIGNAL_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_U_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_WRITE_TIME
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

    fun extractFeatureFromJson(jsonResponse: String): ArrayList<Signal>?{

        message = "No Message"
        val datas = ArrayList<Signal>()

        val realm  = getRealmDefault(context)
        try {
            val baseJsonResponse = JSONObject(jsonResponse.toString())
            if (baseJsonResponse.has(JSON_MESSAGE)) {
                message = baseJsonResponse.getString(JSON_MESSAGE)
                if(message.contains("Success")){

                    val dataArray : JSONArray = baseJsonResponse.getJSONArray(JSON_DATA)

                    realm.beginTransaction()
                    for( i in 0 until dataArray.length()){
                        val data : JSONObject = dataArray.getJSONObject(i)

                        val obj = Signal(data.getInt(JSON_SIGNAL_IDX),
                                data.getInt(JSON_U_IDX),
                                data.getString(JSON_WRITE_TIME),
                                data.getInt(JSON_OPEN_STATUS),
                                data.getInt(JSON_ROOM_IDX),
                                data.getString(JSON_CONTENT),
                                data.getInt(JSON_ENTIRE_STATUS)
                        )
                        if(data.has(JSON_COLOR))
                            obj.responseColor=data.getString(JSON_COLOR)
                        if(data.has(JSON_RESPONSE_COLOR))
                            obj.responseColor=data.getString(JSON_RESPONSE_COLOR)
                        if(data.has(JSON_RESPONSE_CONTENT))
                            obj.responseColor=data.getString(JSON_RESPONSE_CONTENT)


                        if (obj.responseColor.equals("null")) obj.responseColor="a"
                        if (obj.responseContent.equals("null")) obj.responseContent = null
                        if (obj.responseColor.equals("null")) obj.responseColor = "a"

                        datas.add(obj)
                    }
                    msgCode = MSG_SUCCESS

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
            if(realm.isInTransaction)
                realm.commitTransaction()
        }

        return null
    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        val obj = extractFeatureFromJson(result!!)


        val msg = handler.obtainMessage()
        msg.what = msgCode
        msg.obj=obj
        Log.d(NetworkTask::class.java.simpleName,"get Message "+if(msgCode== Utils.MSG_SUCCESS) "Success" else " failed")

        val data = Bundle()
        data.putString("message",message)
        msg.data=data
        handler.sendMessage(msg)
    }
}