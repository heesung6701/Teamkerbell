package org.teamfairy.sopt.teamkerbell.utils

/**
 * Created by lumiere on 2018-04-27.
 */
class ChatUtils {
    companion object {
        const val TYPE_MESSAGE = 0

        const val TYPE_PHOTO:Int=1

        const val TYPE_PHOTOS:Int=2

        const val TYPE_FILE:Int=3

        const val TYPE_VIDEO:Int=4

        const val TYPE_NOTICE:Int=5

        const val TYPE_SIGNAL:Int=6
    //{"chat_idx":2,"content":"12\/신호등","write_time":"2018-08-03 19:25:11","count":0,"u_idx":27,"type":6}]

        const val TYPE_VOTE:Int=7

        const val TYPE_ROLE:Int=8


        const val TYPE_ENTER_GROUP:Int=9

        const val TYPE_INVITE:Int=10

        const val TYPE_GROUP_LEAVE:Int=11

        const val TYPE_LEAVE:Int=12

        const val TYPE_READLINE: Int = 50
    }
}