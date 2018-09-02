package org.teamfairy.sopt.teamkerbell.utils

/**
 * Created by lumiere on 2018-02-03.
 */
class StatusCode {
    companion object {
        const val userChange = 101
        const val groupChange = 102
        const val joinedGroupChange =103
        const val roomChange = 104
        const val joinedRoomChange = 105
        const val userWithJoinedGroupChange = 106
        const val roomWithJoinedRoomChange = 107
        const val joinedGroupWithJoinedRoom = 108
        const val allChange = 109

        const val votePush = 301
        const val makeNotice = 302
        const val makeSignal = 303
        const val makeVote = 304
        const val makeRole = 305


        const val chatMessage =909
    }
}