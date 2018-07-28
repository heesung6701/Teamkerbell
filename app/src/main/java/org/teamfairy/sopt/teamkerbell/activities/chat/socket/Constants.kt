package org.teamfairy.sopt.teamkerbell.activities.chat.socket

import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL

class Constants{
    companion object {
        const val UPDATE_CHAT = "updatechat"
        const val SEND_MESSAGE = "sendmessage"


        const val ENTER_ROOM = "enterroom"
        const val LEAVE_ROOM = "leaveroom"
        const val SEND_CHAT  = "sendchat"

        const val ENTER_RESULT = "enterresult"
        const val LEAVE_RESULT = "leaveresult"



        const val JSON_CHAT_IDX = USGS_REQUEST_URL.JSON_CHAT_IDX
        const val JSON_U_IDX = USGS_REQUEST_URL.JSON_U_IDX
        const val JSON_ROOM_IDX = USGS_REQUEST_URL.JSON_ROOM_IDX
        const val JSON_CONTENT = USGS_REQUEST_URL.JSON_CONTENT
        const val JSON_WRITE_TIME = USGS_REQUEST_URL.JSON_WRITE_TIME
        const val JSON_TYPE = "type"
        const val JSON_COUNT = "count"

    }
}