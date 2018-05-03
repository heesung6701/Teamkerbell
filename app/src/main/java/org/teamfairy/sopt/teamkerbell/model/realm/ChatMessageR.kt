package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


/**
 * Created by lumiere on 2018-01-01.
 */
open class ChatMessageR : RealmObject(){

    @PrimaryKey
    var chat_idx : Int?=null

    var type:Int?=null
    var u_idx:Int?=null
    var content: String?=null
    var date : String?=null

}
