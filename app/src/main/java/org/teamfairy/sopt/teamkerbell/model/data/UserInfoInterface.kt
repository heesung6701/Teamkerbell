package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import io.realm.Realm

/**
 * Created by lumiere on 2018-02-04.
 */
interface UserInfoInterface {
    var name : String
    var photo : String

    fun setPhotoInfo(context : Context)
    fun setPhotoInfo(realm : Realm)
}