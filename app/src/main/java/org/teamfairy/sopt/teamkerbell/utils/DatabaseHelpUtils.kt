package org.teamfairy.sopt.teamkerbell._utils

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration
import android.util.Log
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR
import org.teamfairy.sopt.teamkerbell.model.realm.UserR

/**
 * Created by lumiere on 2018-04-27.
 */
class DatabaseHelpUtils {
    companion object {

        private val LOG_TAG = this::class.java.simpleName

        fun getRealmDefault(context : Context) : Realm{
            var realm : Realm
            val realmConfig: RealmConfiguration
            Realm.init(context)
            try {
                realm = Realm.getDefaultInstance()
            } catch (e: Exception) {
                realmConfig = RealmConfiguration
                        .Builder()
                        .deleteRealmIfMigrationNeeded()
                        .build();
                realm = Realm.getInstance(realmConfig)
            }
            return realm
        }
        fun getGroup(context: Context, g_idx:Int): Team {
            val realm = getRealmDefault(context)
            val groupR = realm.where(GroupR::class.java).equalTo("g_idx",g_idx).findFirst() ?: GroupR()
            realm.close()
            return groupR.toGroup()
        }
        fun getUser(context: Context, u_idx:Int): User {
            val realm = getRealmDefault(context)
            val userR = realm.where(UserR::class.java).equalTo("u_idx",u_idx).findFirst() ?: UserR()
            realm.close()
            return userR.toUser()
        }


        const val PREF_ISUPDATE_USER = "user"
        const val PREF_ISUPDATE_GROUP = "group"
        const val PREF_ISUPDATE_JOINED = "joined"
        const val PREF_ISUPDATE = "pref_msg_isUpdate"


        fun setPref_isUpdate(applicationContext : Context, key : String, tf : Boolean){

            val pref = applicationContext.getSharedPreferences(PREF_ISUPDATE, Context.MODE_PRIVATE).edit()
            pref.putBoolean(key,tf)
            pref.apply()

        }
        fun getPref_isUpdate(applicationContext: Context, key : String) : Boolean{
            val pref = applicationContext.getSharedPreferences(PREF_ISUPDATE, Context.MODE_PRIVATE)
            Log.d("$LOG_TAG/pref", pref.getBoolean(key, true).toString())
            return pref.getBoolean(key,true)
        }

        fun setRecentChatIdx(applicationContext: Context, g_idx: Int, chat_idx:Int){
            val pref = applicationContext.getSharedPreferences("recentChatIdx", Context.MODE_PRIVATE).edit()
            pref.putInt("group$g_idx", chat_idx)
            pref.apply()
        }
        fun getRecentChatIdx(applicationContext: Context, g_idx: Int) : Int{
            val pref = applicationContext.getSharedPreferences("recentChatIdx", Context.MODE_PRIVATE)
            return pref.getInt("group$g_idx", -1)
        }


    }
}