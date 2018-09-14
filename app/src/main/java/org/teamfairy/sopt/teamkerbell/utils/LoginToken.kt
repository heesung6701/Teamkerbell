package org.teamfairy.sopt.teamkerbell.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User

/**
 * Created by lumiere on 2018-01-05.
 */
class LoginToken(){

    companion object {

        private var user : User?=null
        private var token: String=""

        fun isValid() : Boolean=    (token.isNotEmpty() && user != null )

        fun getUserIdx(context: Context): Int{
            if(user ==null) getPref(context)
            return user!!.u_idx
        }
        fun getUser(context: Context) : User{
            if(user ==null) getPref(context)
            return user!!
        }

        fun getToken(context: Context) : String{
            if(token.isBlank()) getPref(context)
            return token
        }

        fun getPref(context: Context){

            val pref = context.getSharedPreferences("pref_login_token", MODE_PRIVATE)

            val u = User(pref.getInt("u_idx", 0),
                    pref.getString("name", "사용자0"),
                    pref.getString("phone", null),
                    pref.getString("bio", null),
                    pref.getString("photo", null),
                    pref.getString("id", null)
            )

            user =u
            token = pref.getString("token", "")

        }
        fun setPref(context: Context, user : User, token : String){

            Companion.user =user
            Companion.token =token

            setPref(context)

        }
        private fun setPref(context: Context){
            val pref = context.getSharedPreferences("pref_login_token", MODE_PRIVATE).edit()


            val u = user!!

            Log.d("LoginToken/", "${u.u_idx}/${u.name}/${token}")
            pref.putInt("u_idx",u.u_idx)
            pref.putString("name",u.name)
            pref.putString("phone",u.phone)
            pref.putString("bio",u.bio)
            pref.putString("photo",u.photo)
            pref.putString("id",u.id)
            pref.putString("token", token)
            pref.apply()

        }
        fun signOut(context : Context){
            val pref = context.getSharedPreferences("pref_login_token", MODE_PRIVATE).edit()
            pref.clear()
            pref.apply()
        }
    }
}
