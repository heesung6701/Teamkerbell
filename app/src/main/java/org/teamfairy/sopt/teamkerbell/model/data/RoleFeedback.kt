package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.realm.UserR


/**
 * Created by lumiere on 2018-03-13.
 */
class RoleFeedback(
        var response_idx: Int,
        var u_idx: Int,
        var content: String
) : UserInfoInterface {

    override var name: String = ""

    override var photo: String = ""

    override fun setPhotoInfo(context: Context) {
        val realm = DatabaseHelpUtils.getRealmDefault(context)
        setPhotoInfo(realm)
    }

    override fun setPhotoInfo(realm: Realm) {
        val userR = realm.where(UserR::class.java).equalTo("u_idx", u_idx).findFirst() ?: UserR()
        name = userR.name
        photo = userR.photo
    }
}