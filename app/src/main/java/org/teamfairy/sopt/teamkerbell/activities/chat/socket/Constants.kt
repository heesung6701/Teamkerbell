package org.teamfairy.sopt.teamkerbell.activities.chat.socket

import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL

class Constants{
    companion object {
        const val updatechat = "updatechat"
        const val sendchat = "sendchat"


        const val u_idx = USGS_REQUEST_URL.JSON_U_IDX
        const val chatroom_idx = USGS_REQUEST_URL.JSON_CHAT_ROOM_IDX
        const val content = USGS_REQUEST_URL.JSON_CONTENT
        const val JSON_TYPE = "type"

    }
}