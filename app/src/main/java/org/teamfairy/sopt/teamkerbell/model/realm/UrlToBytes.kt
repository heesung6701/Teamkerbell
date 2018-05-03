package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


/**
 * Created by lumiere on 2018-02-13.
 */
open class UrlToBytes()  :RealmObject(){
    @PrimaryKey
    var key :String?=null

    var url : String?=null

    var byteArray : ByteArray?=null
}