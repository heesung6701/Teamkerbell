package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.model.data.Signal

/**
 * Created by lumiere on 2018-01-01.
 */
open class SignalR(): RealmObject() {

    @PrimaryKey
    var light_idx: Int? = null

    var u_idx: Int? = null
    var chat_idx: Int? = null
    var write_time: String? = null
    var open_status: Int? = null
    var g_idx: Int? = null
    var content: String? = null
    var entire_status: Int? = null
    var color: String? = null


    fun toSignal(realm: Realm) : Signal {
        val userR = realm.where(UserR::class.java).equalTo("u_idx",u_idx).findFirst() ?: UserR()
        val lights = Signal(light_idx!!, u_idx!!, chat_idx, write_time!!, open_status, g_idx!!, content, entire_status!!, color)
        lights.name = userR.name
        lights.photo = userR.photo

        return lights
    }
}