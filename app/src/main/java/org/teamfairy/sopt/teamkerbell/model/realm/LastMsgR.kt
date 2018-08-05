package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class LastMsgR : RealmObject(){

    var g_idx : Int?= -1
    @PrimaryKey
    var room_idx : Int?= -1


    var chat_idx : Int=-1

    var type:Int=0
    var u_idx:Int=-1
    var content: String=""
    var date : String=""

    var cnt : Int = 0


    companion object {
        const val MSG = "msg"
        const val CNT = "cnt"
    }
}