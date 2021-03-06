package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.Realm
import io.realm.RealmObject
import org.teamfairy.sopt.teamkerbell.model.data.Pick

/**
 * Created by lumiere on 2018-01-01.
 */
open class PickR : RealmObject(){

    var u_idx: Int?=null

    var chat_idx: Int?=null
    var write_time: String?=null
    var content: String?=null
    var g_idx: Int?=null
    var room_idx: Int?=null

    fun toPick(realm : Realm) : Pick {
        val pick= Pick(u_idx!!, chat_idx!!, write_time!!, content,g_idx!!,room_idx!!)
        pick.setGroupInfo(realm,pick.room_idx)
        pick.setPhotoInfo(realm,pick.u_idx)
        return pick
    }


}