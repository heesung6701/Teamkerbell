package org.teamfairy.sopt.teamkerbell._utils

import android.content.Context
import android.support.v7.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmConfiguration
import android.util.Log
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.realm.RoomR
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedGroupR
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



        fun getUserListFromRealm(applicationContext: Context, dataListUser: ArrayList<User>, adapterUser: RecyclerView.Adapter<*>, group: Team) {

            val realm = getRealmDefault(applicationContext)

            dataListUser.clear()
            adapterUser.notifyDataSetChanged()
            val joinedRs = realm.where(JoinedGroupR::class.java).equalTo("g_idx", group.g_idx).findAll()
            joinedRs.iterator().forEach {
                val userR: UserR = realm.where(UserR::class.java).equalTo("u_idx", it.u_idx).findFirst()
                        ?: UserR()
                dataListUser.add(userR.toUser())
            }
            adapterUser.notifyDataSetChanged()
            realm.close()
        }
        fun getGroupListFromRealm(applicationContext: Context, dataListGroup: ArrayList<Team>, adapterGroup: RecyclerView.Adapter<*>, group: Team) {

            val realm = getRealmDefault(applicationContext)

            dataListGroup.clear()
            adapterGroup.notifyDataSetChanged()
            val groupR = realm.where(GroupR::class.java).findAll()
            groupR.iterator().forEach {
                dataListGroup.add(it.toGroup())
            }

            adapterGroup.notifyDataSetChanged()
            realm.close()
        }

        fun getRoomListFromRealm(applicationContext: Context, dataListRoom: ArrayList<Room>, adapterRoom: RecyclerView.Adapter<*>, group: Team) {

            val realm = getRealmDefault(applicationContext)

            dataListRoom.clear()
            adapterRoom.notifyDataSetChanged()
            val roomR = realm.where(RoomR::class.java).findAll()
            roomR.iterator().forEach {
                dataListRoom.add(it.toChatRoom())
            }

            adapterRoom.notifyDataSetChanged()
            realm.close()
        }

        const val PREF_ISUPDATE_USER = "user"
        const val PREF_ISUPDATE_GROUP = "group"
        const val PREF_ISUPDATE_ROOM = "room"
        const val PREF_ISUPDATE_JOINED_GROUP = "joined_group"
        const val PREF_ISUPDATE_JOINED_ROOM = "joined_room"
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