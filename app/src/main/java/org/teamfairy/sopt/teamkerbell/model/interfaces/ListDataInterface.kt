package org.teamfairy.sopt.teamkerbell.model.interfaces


/**
 * Created by lumiere on 2018-02-04.
 */
interface ListDataInterface {
    var u_idx: Int
    var room_idx : Int

    var name : String
    var photo : String

    fun getMainTitle() : String
    fun getSubTitle() : String
    fun getTime(): String

}