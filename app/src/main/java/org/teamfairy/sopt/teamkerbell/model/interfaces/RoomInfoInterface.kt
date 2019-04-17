package org.teamfairy.sopt.teamkerbell.model.interfaces

import android.content.Context
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.model.data.Team.Companion.ARG_G_IDX
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR
import org.teamfairy.sopt.teamkerbell.model.realm.JoinedRoomR
import org.teamfairy.sopt.teamkerbell.model.realm.RoomR

/**
 * Created by lumiere on 2018-02-04.
 */
open class RoomInfoInterface() : UserInfoInterface() {
    open var groupName: String = ""
    open var roomName: String = ""

    open fun setGroupInfo(context: Context, room_idx: Int) {
        val realm = DatabaseHelpUtils.getRealmDefault(context)
        setGroupInfo(realm, room_idx)
        realm.close()
    }

    open fun setGroupInfo(realm: Realm, room_idx: Int) {
        val roomR = realm.where(RoomR::class.java).equalTo(ARG_ROOM_IDX, room_idx).findFirst() ?: RoomR()
        val joinedRoomR = realm.where(JoinedRoomR::class.java).equalTo(ARG_ROOM_IDX, room_idx).findFirst() ?: JoinedRoomR()

        val groupR = realm.where(GroupR::class.java).equalTo(ARG_G_IDX, joinedRoomR.g_idx).findFirst() ?: GroupR()
        roomName = roomR.real_name
        groupName = groupR.real_name
    }
}