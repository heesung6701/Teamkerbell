package org.teamfairy.sopt.teamkerbell.model.interfaces

import android.content.Context
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.data.User.Companion.ARG_U_IDX
import org.teamfairy.sopt.teamkerbell.model.realm.UserR

/**
 * Created by lumiere on 2018-02-04.
 */
open class UserInfoInterface {
    open var name: String = ""
    open var photo: String = ""

    open fun setPhotoInfo(context: Context, u_idx: Int) {
        val realm = DatabaseHelpUtils.getRealmDefault(context)
        setPhotoInfo(realm, u_idx)
        realm.close()
    }

    open fun setPhotoInfo(realm: Realm, u_idx: Int) {
        val userR = realm.where(UserR::class.java).equalTo(ARG_U_IDX, u_idx).findFirst() ?: UserR()
        name = userR.name
        photo = userR.photo
    }
}