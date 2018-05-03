package org.teamfairy.sopt.teamkerbell.network.auth

import android.content.Context
import android.os.Bundle
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import android.os.Handler
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.model.data.User

/**
 * Created by lumiere on 2018-01-04.
 */
class LoginTask(context: Context, var handler: Handler): NetworkTask(context) {

    var message: String = "No Message"

    fun extractFeatureFromJson(jsonResponse: String){

        message = "No Message"
        try {
            val baseJsonResponse = JSONObject(jsonResponse)
            if (baseJsonResponse.has("message")) {
                message = baseJsonResponse.getString("message")

                if(message.contains("Success")){

                    val user= User(
                            baseJsonResponse.getInt("u_idx"),
                            baseJsonResponse.getString("name")
                    )
                    if(baseJsonResponse.has("phone"))
                        user.phone=baseJsonResponse.getString("phone")
                    if(baseJsonResponse.has("bio"))
                        user.bio=baseJsonResponse.getString("bio")
                    if(baseJsonResponse.has("photo"))
                        user.photo=baseJsonResponse.getString("photo")
                    if(baseJsonResponse.has("id"))
                        user.id=baseJsonResponse.getString("id")

                    LoginToken.setPref(context,user,
                            baseJsonResponse.getString("token"))
                }
                else{
                    Log.d("NetworkTask:Error",message)
                }
            }else{
                Log.d("NetworkTask:Error",jsonResponse)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)


        extractFeatureFromJson(result!!)

        val msg = handler.obtainMessage()
        val data = Bundle()
        data.putString("message",message)
        msg.data=data
        handler.sendMessage(msg)
    }
}