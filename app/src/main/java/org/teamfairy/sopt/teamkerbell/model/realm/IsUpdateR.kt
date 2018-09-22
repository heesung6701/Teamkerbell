package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.utils.StatusCode

/**
 * Created by lumiere on 2018-01-01.
 */
open class IsUpdateR() : RealmObject(){
    @PrimaryKey
    var what: Int=-1
    var isUpdate : Boolean = false

    companion object {
        const  val ARG_WHAT = "what"

        const val WHAT_USER =StatusCode.userChange
        const val WHAT_GROUP =StatusCode.groupChange
        const val WHAT_ROOM  = StatusCode.roomChange
        const val WHAT_JOINED_GROUP  = StatusCode.joinedGroupChange
        const val WHAT_JOINED_ROOM  = StatusCode.joinedRoomChange

        fun create(realm : Realm,what : Int) : IsUpdateR{
            realm.beginTransaction()
            val isUpdateR = realm.createObject(IsUpdateR::class.java, what)
            realm.commitTransaction()
            return isUpdateR
        }
    }
}
