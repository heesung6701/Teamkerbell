package org.teamfairy.sopt.teamkerbell.network

import org.teamfairy.sopt.teamkerbell.activities.chat.socket.Constants.Companion.JSON_CHAT_IDX
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.Constants.Companion.JSON_CONTENT
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.Constants.Companion.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.activities.chat.socket.Constants.Companion.JSON_U_IDX

/**
 * Created by lumiere on 2018-01-04.
 */
object USGS_REQUEST_URL {
    // REQUEST PARAMATER

    const val JSON_DATA = "data"
    const val JSON_RESULT = "result"

    const val JSON_MESSAGE = "message"

//    User
    const val JSON_U_IDX = "u_idx"

    const val JSON_NAME = "name"
    const val JSON_PHONE = "phone"
    const val JSON_BIO = "bio"
    const val JSON_ID = "id"
    const val JSON_PWD = "pwd"
    const val JSON_CLIENT_TOKEN = "client_token"
    const val JSON_TOKEN = "token"

//    Group & ChatRoom
    const val JSON_G_IDX = "g_idx"
    const val JSON_ROOM_IDX = "chatroom_idx"

    const val JSON_PHOTO = "photo"
    const val JSON_REAL_NAME = "real_name"
    const val JSON_CTRL_NAME = "ctrl_name"
    const val JSON_DEFAULT_ROOM_IDX = "default_chatroom_idx"

//    Chat
    const val JSON_CHAT_IDX = "chat_idx"

//    item:Notice
    const val JSON_NOTICE_IDX = "notice_idx"
    const val JSON_NOTICE = "notice"
    const val JSON_NOTICES = "notices"

//    item:Signal
    const val JSON_SIGNAL_IDX = "light_idx"
    const val JSON_SIGNALS = "lights"
    const val JSON_SIGNAL = "light"
    const val JSON_COLOR = "color"
    const val JSON_OPEN_STATUS = "open_status"
    const val JSON_ENTIRE_STATUS = "entire_status"
    const val JSON_RESPONSE_COLOR = "response_color"
    const val JSON_RESPONSE_CONTENT = "response_content"

//    item:Vote
    const val JSON_VOTE_IDX = "vote_idx"
    const val JSON_VOTES = "votes"
    const val JSON_VOTE = "vote"
    const val JSON_RESPONSE = "response"
    const val JSON_CHOICE = "choice"
    const val JSON_VALUE = "value"
    const val JSON_VOTE_CONTENT_IDX = "vote_content_idx"
    const val JSON_ENDTIME = "endtime"
    const val JSON_FINISHED = "Finished"
    const val JSON_NOT_FINISHED = "NotFinished"

//    Item:Role
    const val JSON_ROLE_IDX = "role_idx"
    const val JSON_MASTER_IDX = "master_idx"
    const val JSON_PLUS_ARRAY = "plusArray"
    const val JSON_MINUS_ARRAY = "minusArray"
    const val JSON_TASK_IDX = "role_task_idx"
    const val JSON_TASK_ARRAY = "taskArray"
    const val JSON_RESPONSE_IDX = "role_response_idx"
    const val JSON_FEEDBACK = "feedback"
    const val JSON_FEEDBACK_IDX = "role_feedback_idx"

    // common
    const val JSON_TITLE = "title"
    const val JSON_BODY = "body"
    const val JSON_INDEX = "index"
    const val JSON_CONTENT = "content"
    const val JSON_WRITE_TIME = "write_time"
    const val JSON_STATUS = "status"
    const val JSON_RESPONSE_STATUS = "response_status"
    const val JSON_USER_ARRAY = "userArray"
    const val JSON_FILE = "file"
    const val JSON_FILES = "files"

    const val URL_ROOT = "http://13.124.143.2:3003"
    const val URL_STORAGE = "http://13.124.143.2:3004"
    const val URL_SOCKET = "http://13.124.143.2:3030"
//    private val URL_ROOT = "https://teamkerbell.ml"

    private const val URL_AUTH = "$URL_ROOT/auth"

    // 회원가입
    const val URL_REGIST = "$URL_AUTH/register" // POST METHOD
    const val URL_REGIST_PARAM_ID = JSON_ID
    const val URL_REGIST_PARAM_PWD = JSON_PWD
    const val URL_REGIST_PARAM_NAME = JSON_NAME
    const val URL_REGIST_PARAM_PHONE = JSON_PHONE

    private val URL_INFO = "$URL_ROOT/info"

    // ID중복체크
    val URL_REGIST_CHECK = "$URL_REGIST/check" // GET METHOD //id=starbucks
    const val URL_REGIST_CHECK_PARAM_ID = "id"

    // 미수행작업
    val URL_UNPERFORMED = "$URL_INFO/unperformed"

    val URL_USER = "$URL_INFO/user" // 유저 정보

    private val URL_JOINED = "$URL_INFO/joined"
    val URL_JOINED_GROUP = "$URL_JOINED/group" // joined group 정보
    val URL_JOINED_ROOM = "$URL_JOINED/chatroom" // joined joined 정보

    private val URL_GROUP = "$URL_INFO/group" // 그룹정보

    val URL_GROUPLIST = URL_GROUP // 그룹 리스트
    val URL_ROOMLIST = "$URL_INFO/chatroom" // 채팅방 리스트

    private val URL_REMOVE = "$URL_INFO/remove" // 유저 정보

    val URL_REMOVE_NOTICE = "$URL_REMOVE/notice"

    val URL_REMOVE_SIGNAL = "$URL_REMOVE/lights"
    val URL_REMOVE_SIGNAL_PARAMS_SIGNAL_IDX = JSON_SIGNAL_IDX

    val URL_REMOVE_VOTE = "$URL_REMOVE/vote"
    val URL_REMOVE_VOTE_PARAMS_VOTE_IDX = JSON_VOTE_IDX

    // 홈화면공지
    val URL_GROUP_NOTICE = "$URL_GROUP/notice"

    // 홈화면 신호등
    private val URL_GROUP_LIGHT = "$URL_GROUP/lights"
    val URL_GROUP_LIGHT_RECEIVER = "$URL_GROUP_LIGHT/receiver"
    val URL_GROUP_LIGHT_SENDER = "$URL_GROUP_LIGHT/sender"

    // 홈화면 픽
    private val URL_GROUP_VOTE = "$URL_GROUP/vote"
    val URL_GROUP_VOTE_RECEIVER = "$URL_GROUP_VOTE/receiver"
    val URL_GROUP_VOTE_SENDER = "$URL_GROUP_VOTE/sender"

    private val URL_DETAIL = "$URL_INFO/detail"
    private val URL_SINGLE = "$URL_DETAIL/single"

    // 신호등 하나 보여주기
    val URL_DETAIL_SINGLE_LIGHT = "$URL_SINGLE/lights"

    // 신호등 하나 보여주기
    val URL_DETAIL_SINGLE_NOTICE = "$URL_SINGLE/notice"

    // 채팅방 공지
    val URL_DETAIL_NOTICE = "$URL_DETAIL/notice"

    // 채팅방 신호등
    val URL_DETAIL_LIGHTS = "$URL_DETAIL/lights"

    val URL_DETAIL_LIGHTS_RESPONSE = "$URL_DETAIL_LIGHTS/each"

    // 채팅방 투표
    val URL_DETAIL_VOTE = "$URL_DETAIL/vote"

    // 투표상세정보
    val URL_DETAIL_VOTE_RESPONSE = "$URL_SINGLE/vote"

    const val URL_ROLE = "$URL_ROOT/role"
    const val URL_ROLE_GET = URL_ROLE

    const val URL_ROLE_PARAM_ROLE_IDX = JSON_ROLE_IDX

    // 로그인
    val URL_LOGIN = "$URL_AUTH/login"
    const val URL_LOGIN_PARAM_ID = JSON_ID
    const val URL_LOGIN_PARAM_PWD = JSON_PWD
    const val URL_LOGIN_PARAM_CLIENTTOKEN = JSON_CLIENT_TOKEN

    // 프로필 수정
    val URL_PROFILE = "$URL_AUTH/profile"
    const val URL_PROFILE_PARAM_NAME = JSON_NAME
    const val URL_PROFILE_PARAM_BIO = JSON_BIO
    const val URL_PROFILE_PARAM_PHONE = JSON_PHONE

    const val URL_REFRESH_TOKEN = "$URL_AUTH/token"
    const val URL_REFRESH_TOKEN_PARAM_ID = JSON_ID
    const val URL_REFRESH_TOKEN_PARAM_U_IDX = JSON_U_IDX

    // 팀원 초대
    private val URL_INVITE = "$URL_AUTH/invite"
    val URL_INVITE_GROUP = "$URL_INVITE/group"
    const val URL_INVITE_GROUP_PARAM_GID = JSON_G_IDX
    const val URL_INVITE_GROUP_PARAM_NAME = JSON_NAME
    const val URL_INVITE_GROUP_PARAM_PHONE = JSON_PHONE

    val URL_INVITE_ROOM = "$URL_INVITE/chatroom"
    const val URL_INVITE_ROOM_PARAM_G_IDX = JSON_G_IDX
    const val URL_INVITE_ROOM_PARAM_ROOM_IDX = JSON_ROOM_IDX
    const val URL_INVITE_ROOM_PARAM_USER_ARRAY = JSON_USER_ARRAY

    private val URL_MAKE = "$URL_INFO/make"

    // 공지 생성
    val URL_MAKE_NOTICE = "$URL_MAKE/notice"
    const val URL_MAKE_NOTICE_PARAM_UID = JSON_U_IDX
    const val URL_MAKE_NOTICE_PARAM_CHATID = JSON_CHAT_IDX
    const val URL_MAKE_NOTICE_PARAM_ROOM_IDX = JSON_ROOM_IDX
    const val URL_MAKE_NOTICE_PARAM_CONTENT = JSON_CONTENT

    // 신호등 생성
    val URL_MAKE_SIGNAL = "$URL_MAKE/lights"
    const val URL_MAKE_SIGNAL_PARAM_UID = JSON_U_IDX
    const val URL_MAKE_SIGNAL_PARAM_CHATID = JSON_CHAT_IDX
    const val URL_MAKE_SIGNAL_PARAM_ROOM_IDX = JSON_ROOM_IDX
    const val URL_MAKE_SIGNAL_PARAM_CONTENT = JSON_CONTENT
    const val URL_MAKE_SIGNAL_PARAM_OPENSTATUS = JSON_OPEN_STATUS
    const val URL_MAKE_SIGNAL_PARAM_ENTIRESTATUS = JSON_ENTIRE_STATUS
    const val URL_MAKE_SIGNAL_PARAM_USERARRAY = JSON_USER_ARRAY

    // 투표 생성
    val URL_MAKE_VOTE = "$URL_MAKE/vote"
    const val URL_MAKE_VOTE_PARAM_GID = JSON_G_IDX
    const val URL_MAKE_VOTE_PARAM_ROOM_IDX = JSON_ROOM_IDX
    const val URL_MAKE_VOTE_PARAM_ENDTIME = JSON_ENDTIME
    const val URL_MAKE_VOTE_PARAM_TITLE = JSON_TITLE
    const val URL_MAKE_VOTE_PARAM_CONTENT = JSON_CONTENT
    const val URL_MAKE_VOTE_PARAM_CHOICE = JSON_CHOICE

    val URL_MAKE_GROUP = "$URL_MAKE/group" // 그룹 생성
    const val URL_MAKE_GROUP_PARAM_NAME = JSON_NAME

    val URL_MAKE_ROOM = "$URL_MAKE/chatroom" // 채팅방 생성
    const val URL_MAKE_ROOM_PARAM_G_IDX = JSON_G_IDX
    const val URL_MAKE_ROOM_PARAM_NAME = JSON_NAME
    const val URL_MAKE_ROOM_PARAM_USERARRAY = JSON_USER_ARRAY

    private val URL_RESPONSE = "$URL_INFO/response"

    // 신호등 응답하기
    val URL_RESPONSE_LIGHTS = "$URL_RESPONSE/lights"
    const val URL_RESPONSE_LIGHTS_PARAM_SIGNAL_IDX = JSON_SIGNAL_IDX
    const val URL_RESPONSE_LIGHTS_PARAM_COLOR = JSON_COLOR
    const val URL_RESPONSE_LIGHTS_PARAM_CONTENT = JSON_CONTENT

    // 투표 응답하기
    val URL_RESPONSE_VOTE = "$URL_RESPONSE/vote"
    const val URL_RESPONSE_VOTE_PARAM_VOTEID = JSON_VOTE_IDX
    const val URL_RESPONSE_VOTE_PARAM_VALUE = JSON_VALUE

    val URL_RESPONSE_PRESS = "$URL_RESPONSE/press"
    const val URL_RESPONSE_PRESS_GID = JSON_G_IDX
    const val URL_RESPONSE_PRESS_VOTEID = JSON_VOTE_IDX

    // 공지응답하기
    val URL_RESPONSE_NOTICE = "$URL_RESPONSE/notice"
    const val URL_RESPONSE_NOTICE_PARAM_NOTICEID = JSON_NOTICE_IDX

    const val URL_ROLE_POST = URL_ROLE
    const val URL_ROLE_REGISTER_PARAM_ROOM_IDX = JSON_ROOM_IDX
    const val URL_ROLE_REGISTER_PARAM_TITLE = JSON_TITLE
    const val URL_ROLE_REGISTER_PARAM_TASK_ARRAY = JSON_TASK_ARRAY

    const val URL_ROLE_RESPONSE = "$URL_ROLE/response"

    const val URL_ROLE_RESPONSE_PARAM_ROLE_IDX = JSON_ROLE_IDX
    const val URL_ROLE_RESPONSE_PARAM_ROLE_TASK_IDX = JSON_TASK_IDX
    const val URL_ROLE_RESPONSE_PARAM_RESPONSE_CONTENT = JSON_RESPONSE_CONTENT
    const val URL_ROLE_RESPONSE_PARAM_ROLE_RESPONSE_IDX = JSON_RESPONSE_IDX
    const val URL_ROLE_RESPONSE_PARAM_MINUS_ARRAY = JSON_MINUS_ARRAY

    const val URL_ROLE_FEEDBACK = "$URL_ROLE/feedback"

    const val URL_ROLE_FEEDBACK_PARAM_ROLE_RESPONSE_IDX = JSON_RESPONSE_IDX
    const val URL_ROLE_FEEDBACK_PARAM_ROLE_FEEDBACK_IDX = JSON_FEEDBACK_IDX
    const val URL_ROLE_FEEDBACK_PARAM_CONTENT = JSON_CONTENT

    const val URL_ROLE_TASK = "$URL_ROLE/task"

    const val URL_ROLE_TASK_PARAM_ROLE_IDX = JSON_ROLE_IDX
    const val URL_ROLE_TASK_PARAM_MINUS_ARRAY = JSON_MINUS_ARRAY
    const val URL_ROLE_TASK_PARAM_PLUS_ARRAY = JSON_PLUS_ARRAY

    const val URL_ROLE_USER = "$URL_ROLE/user"

    const val URL_ROLE_USER_PARAM_ROLE_IDX = JSON_ROLE_IDX
    const val URL_ROLE_USER_PARAM_TASK_IDX = JSON_TASK_IDX
    const val URL_ROLE_USER_PARAM_MINUS_ARRAY = JSON_MINUS_ARRAY
    const val URL_ROLE_USER_PARAM_PLUS_ARRAY = JSON_PLUS_ARRAY
    const val URL_ROLE_USER_PARAM_ROLE_STATUS = JSON_STATUS

    // 대화방나가기
    private const val URL_LEAVE = "$URL_AUTH/leave" // DELTE METHOD
    const val URL_LEAVE_GROUP = "$URL_LEAVE/group" // DELTE METHOD
    const val URL_LEAVE_GROUP_PARAM_GID = JSON_G_IDX

    const val URL_LEAVE_ROOM = "$URL_LEAVE/chatroom" // DELTE METHOD
    const val URL_LEAVE_ROOM_PARAM_ROOM_IDX = JSON_ROOM_IDX

    const val URL_PHOTO_SINGLE = "$URL_STORAGE/photo/single"
    const val URL_PHOTO_SINGLE_PARAM_ROOM_IDX = JSON_ROOM_IDX
    const val URL_PHOTO_SINGLE_PARAM_U_IDX = JSON_U_IDX

    const val URL_FILE = "$URL_STORAGE/file"
    const val URL_FILE_PARAM_ROOM_IDX = JSON_ROOM_IDX
    const val URL_FILE_PARAM_U_IDX = JSON_U_IDX
}
