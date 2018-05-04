package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.utils.Utils

/**
 * Created by lumiere on 2018-01-01.
 */
data class Pick(
        override var u_idx: Int,
        var chat_idx: Int?,
        var write_time: String,
        var content: String?,
        var pick_idx: Int?,
        override var g_idx: Int
) : ListDataInterface {

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

    override fun getMainTitle(): String {
        return content!!
    }

    override fun getSubTitle(): String {
        return write_time
    }


    override fun getTime(): String {
        return Utils.getMonthDayTime(write_time)
    }

}