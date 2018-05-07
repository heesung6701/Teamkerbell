package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.model.data.Role

/**
 * Created by lumiere on 2018-01-01.
 */
open class RoleR : RealmObject(){
    @PrimaryKey
    var role_idx: Int?=null

    var g_idx : Int?=null
    var title: String?=null
    var master_idx : Int?=null
    var write_time : String?=null

    fun toRole(): Role = Role(role_idx!!, g_idx!!, title, master_idx, write_time)
}