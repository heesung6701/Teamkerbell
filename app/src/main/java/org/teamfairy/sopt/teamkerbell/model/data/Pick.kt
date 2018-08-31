package org.teamfairy.sopt.teamkerbell.model.data

import org.teamfairy.sopt.teamkerbell.model.interfaces.RoomInfoInterface
import org.teamfairy.sopt.teamkerbell.utils.Utils

/**
 * Created by lumiere on 2018-01-01.
 */
data class Pick(
        var u_idx: Int,
        var chat_idx: Int?,
        var write_time: String,
        var content: String?,
        var g_idx: Int,
        var room_idx: Int
) : RoomInfoInterface(){

    fun getMainTitle(): String {
        return content!!
    }
    fun getTime(): String {
        return Utils.getMonthDayTime(write_time)
    }

}