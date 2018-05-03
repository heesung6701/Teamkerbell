package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by lumiere on 2018-01-01.
 */
open class IsUpdateR() : RealmObject(){
    @PrimaryKey
    var what: Int=-1
    var isUpdate : Boolean = false
}
