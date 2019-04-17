package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.model.data.Team

/**
 * Created by lumiere on 2018-01-01.
 */
open class GroupR() : RealmObject() {
    @PrimaryKey
    var g_idx: Int = -2
    var real_name: String = "알수없음"
    var ctrl_name: String = "알수없음"
    var photo: String = ""
    var default_room_idx: Int = - 1

    fun toGroup(): Team = Team(g_idx, real_name, ctrl_name, photo, default_room_idx)
}
