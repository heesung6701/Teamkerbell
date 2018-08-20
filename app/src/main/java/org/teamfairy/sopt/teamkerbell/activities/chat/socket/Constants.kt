package org.teamfairy.sopt.teamkerbell.activities.chat.socket

import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL

class Constants{
    companion object {

//      채팅 리스트 들어가기
        const val ENTER_ROOM_LIST = "enterchatlist"
        const val ENTER_ROOM_LIST_RESULT = "listresult"

//       채팅 리스트 나가기
        const val LEAVE_ROOM_LIST = "leaveroom"

//        채팅방 들어가기
        const val ENTER_ROOM = "enterroom"
        const val ENTER_ROOM_RESULT = "roomresult"

//        채팅방 나가기
        const val LEAVE_ROOM = "leaveroom"
        const val LEAVE_ROOM_RESULT = "leaveresult"


//        채팅방 메시지보내기
        const val SEND_CHAT  = "sendchat"
//        채팅방 메세지 받기/업데이트
        const val UPDATE_CHAT = "updatechat"


        const val JSON_CHAT_IDX = USGS_REQUEST_URL.JSON_CHAT_IDX
        const val JSON_U_IDX = USGS_REQUEST_URL.JSON_U_IDX
        const val JSON_ROOM_IDX = USGS_REQUEST_URL.JSON_ROOM_IDX
        const val JSON_CONTENT = USGS_REQUEST_URL.JSON_CONTENT
        const val JSON_WRITE_TIME = USGS_REQUEST_URL.JSON_WRITE_TIME
        const val JSON_TYPE = "type"
        const val JSON_COUNT = "count"
        const val JSON_UN_READ_COUNT = "unreadcount"

    }
}