package org.teamfairy.sopt.teamkerbell._utils

import android.content.Context
import android.support.v7.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmConfiguration
import android.util.Log
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ALL
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ALL_IDX
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.Team.Companion.ARG_G_IDX
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.data.User.Companion.ARG_U_IDX
import org.teamfairy.sopt.teamkerbell.model.realm.*
import org.teamfairy.sopt.teamkerbell.utils.LoginToken

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
        fun getRoom(context: Context, room_idx:Int): Room {
            val realm = getRealmDefault(context)
            val roomR = realm.where(RoomR::class.java).equalTo(ARG_ROOM_IDX,room_idx).findFirst() ?: RoomR()
            realm.close()
            return roomR.toChatRoom()
        }
        fun getGroup(context: Context, g_idx:Int): Team {
            val realm = getRealmDefault(context)
            val groupR = realm.where(GroupR::class.java).equalTo(Team.ARG_G_IDX,g_idx).findFirst() ?: GroupR()
            realm.close()
            return groupR.toGroup()
        }
        fun getUser(context: Context, u_idx:Int): User {
            val realm = getRealmDefault(context)
            val userR = realm.where(UserR::class.java).equalTo("u_idx",u_idx).findFirst() ?: UserR()
            realm.close()
            return userR.toUser()
        }

        fun getUserListFromRealm(applicationContext: Context, dataListUser: ArrayList<User>, adapterUser: RecyclerView.Adapter<*>, group: Team,withoutUser : Boolean) {

            val realm = getRealmDefault(applicationContext)

            dataListUser.clear()
            adapterUser.notifyDataSetChanged()

            val joinedRs = if(withoutUser) realm.where(JoinedGroupR::class.java).equalTo(Team.ARG_G_IDX, group.g_idx).notEqualTo(User.ARG_U_IDX,LoginToken.getUserIdx(applicationContext)).findAll()
            else realm.where(JoinedGroupR::class.java).equalTo(Team.ARG_G_IDX, group.g_idx).findAll()
            joinedRs.iterator().forEach {
                val userR: UserR = realm.where(UserR::class.java).equalTo(User.ARG_U_IDX, it.u_idx).findFirst()
                        ?: UserR()

                dataListUser.add(userR.toUser())
            }
            adapterUser.notifyDataSetChanged()
            realm.close()
        }

        fun getUserListFromRealm(applicationContext: Context, dataListUser: ArrayList<User>, adapterUser: RecyclerView.Adapter<*>, group: Team) {
            getUserListFromRealm(applicationContext,dataListUser,adapterUser,group,false)
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

        fun getRoomUIdxListFromRealm(applicationContext: Context, roomMemberList: ArrayList<Int>, room: Room){

            val realm  = DatabaseHelpUtils.getRealmDefault(applicationContext)
            val joinedRs = realm.where(JoinedRoomR::class.java).equalTo(ARG_ROOM_IDX, room.room_idx).findAll()
            joinedRs.iterator().forEach {
                val user = realm.where(UserR::class.java).equalTo(ARG_U_IDX, it.u_idx).findFirst()
                        ?: UserR()
                roomMemberList.add(user.u_idx)
            }
        }
        fun getRoomUserListFromRealm(applicationContext: Context, roomMemberList: ArrayList<User>, room: Room){

            val realm  = DatabaseHelpUtils.getRealmDefault(applicationContext)
            val joinedRs = realm.where(JoinedRoomR::class.java).equalTo(ARG_ROOM_IDX, room.room_idx).findAll()
            joinedRs.iterator().forEach {
                val user = realm.where(UserR::class.java).equalTo(ARG_U_IDX, it.u_idx).findFirst()
                        ?: UserR()
                roomMemberList.add(user.toUser())
            }
        }

        fun getRoomListFromRealm(applicationContext: Context, dataListRoom: ArrayList<Room>, adapterRoom: RecyclerView.Adapter<*>, group: Team,containAll : Boolean) {

            val realm = getRealmDefault(applicationContext)

            dataListRoom.clear()

            if(containAll) dataListRoom.add(Room(ARG_ALL_IDX,ARG_ALL_IDX,ARG_ALL,ARG_ALL))
            val roomRs = realm.where(RoomR::class.java).equalTo(ARG_G_IDX, group.g_idx).findAll()
            roomRs.iterator().forEach {
                dataListRoom.add(it.toChatRoom())
            }

            adapterRoom.notifyDataSetChanged()
            realm.close()
        }
        fun getRoomListFromRealm(applicationContext: Context, dataListRoom: ArrayList<Room>, adapterRoom: RecyclerView.Adapter<*>, group: Team) {
            getRoomListFromRealm(applicationContext,dataListRoom,adapterRoom,group,false)
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

        fun setRecentChatIdx(applicationContext: Context, room_idx: Int, chat_idx:Int){
//            Log.d("set lastChatIdx, room$room_idx,", chat_idx.toString())
            val pref = applicationContext.getSharedPreferences("recentChatIdx", Context.MODE_PRIVATE).edit()
            pref.putInt("room$room_idx", chat_idx)
            pref.apply()
        }
        fun getRecentChatIdx(applicationContext: Context, room_idx: Int) : Int {
            val pref = applicationContext.getSharedPreferences("recentChatIdx", Context.MODE_PRIVATE)
            return pref.getInt("room$room_idx", -1)
        }
    }
}