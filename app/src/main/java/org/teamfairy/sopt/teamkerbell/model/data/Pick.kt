package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.interfaces.UserInfoInterface
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
        override var room_idx: Int
) : UserInfoInterface(), ListDataInterface {


    fun setPhotoInfo(context: Context) {
      super.setPhotoInfo(context,u_idx)
    }


    fun setPhotoInfo(realm: Realm) {
        super.setPhotoInfo(realm,u_idx)
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