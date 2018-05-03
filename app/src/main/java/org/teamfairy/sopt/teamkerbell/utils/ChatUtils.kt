package org.teamfairy.sopt.teamkerbell._utils

/**
 * Created by lumiere on 2018-04-27.
 */
class ChatUtils {
    companion object {
        const val TYPE_MESSAGE = 0
        //content가  String
        const val TYPE_LEAVE:Int=1
        //content가  idx/이름
        const val TYPE_PHOTO:Int=2
        //content가 URL
        const val TYPE_VIDEO:Int=3
        //content가 URL
        const val TYPE_FILE:Int=4
        //content가 URL
        const val TYPE_READLINE: Int = 5
        //content가  null
        const val TYPE_NOTICE:Int=6
        // idx값 / 내용
        const val TYPE_LIGHT:Int=7
        // idx값 / 내용
        const val TYPE_VOTE:Int=8
        // idx값 / 내용
        const val TYPE_ROLE:Int=9
        // idx값 / 내용
        const val TYPE_INVITE:Int=10
        //content가 이름
    }
}