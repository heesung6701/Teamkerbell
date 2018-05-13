package org.teamfairy.sopt.teamkerbell.model.list

import org.teamfairy.sopt.teamkerbell.model.data.User

/**
 * Created by lumiere on 2018-01-01.
 */
data class ChatMessageF(
        var chat_idx: Int?,
        var type: Int? ,
        var u_idx:Int?,
        var content: String?,
        var date : String?
    ){
    constructor() : this(null,null,null,null,null)
    constructor(chat_idx: Int?, type: Int?, user : User, date : String) : this(chat_idx,type,user.u_idx,user.name,date)

    fun toChatMessage(): ChatMessage =ChatMessage(chat_idx!!,type!!,u_idx,content,date)

    companion object {
        const  val ARG_CHAT_IDX = "chat_idx"
        const  val ARG_CONTENT = "content"
        const  val ARG_DATE = "date"
    }


}
