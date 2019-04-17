package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.model.data.Room

/**
 * Created by lumiere on 2018-01-01.
 */
open class RoomR() : RealmObject() {
    var g_idx: Int = Room.ARG_NULL_IDX
    @PrimaryKey
    var room_idx: Int = Room.ARG_NULL_IDX
    var real_name: String = "알수없음"
    var ctrl_name: String = "알수없음"
    var photo: String = ""

    fun toChatRoom(): Room = Room(g_idx, room_idx, real_name, ctrl_name, photo)
}
