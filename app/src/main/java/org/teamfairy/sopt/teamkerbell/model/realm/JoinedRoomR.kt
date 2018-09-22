package org.teamfairy.sopt.teamkerbell.model.realm

import android.content.Context
import io.realm.RealmObject
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault

/**
 * Created by lumiere on 2018-01-01.
 */
open class JoinedRoomR() : RealmObject(){
    var room_idx: Int=-1
    var u_idx: Int=-1
    var g_idx : Int=-1

}
