package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell.model.interfaces.UserInfoInterface


/**
 * Created by lumiere on 2018-03-13.
 */
class RoleFeedback(
        var feedback_idx: Int,
        var response_idx: Int,
        var u_idx: Int,
        var content: String,
        var write_time: String?
) : UserInfoInterface() {

    fun setPhotoInfo(context: Context) {
        super.setPhotoInfo(context, u_idx)
    }

    fun setPhotoInfo(realm: Realm) {
        super.setPhotoInfo(realm, u_idx)
    }
}