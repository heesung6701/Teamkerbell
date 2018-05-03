package org.teamfairy.sopt.teamkerbell.model.list

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

    fun toChatMessage(): ChatMessage =ChatMessage(chat_idx!!,type!!,u_idx,content,date)


}
