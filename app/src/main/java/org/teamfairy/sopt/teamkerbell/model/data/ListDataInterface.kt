package org.teamfairy.sopt.teamkerbell.model.data


/**
 * Created by lumiere on 2018-02-04.
 */
interface ListDataInterface : UserInfoInterface {
    var u_idx: Int
    var g_idx : Int

    fun getMainTitle() : String
    fun getSubTitle() : String
    fun getTime(): String
}