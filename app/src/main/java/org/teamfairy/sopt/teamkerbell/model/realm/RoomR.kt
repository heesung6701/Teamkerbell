package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.model.data.Room

/**
 * Created by lumiere on 2018-01-01.
 */
open class RoomR() : RealmObject(){
    @PrimaryKey
    var chatroom_idx: Int=-2
    var real_name: String="알수없음"
    var ctrl_name: String="알수없음"
    var photo: String=""

    fun toChatRoom() : Room = Room(chatroom_idx, real_name, ctrl_name, photo)
}
