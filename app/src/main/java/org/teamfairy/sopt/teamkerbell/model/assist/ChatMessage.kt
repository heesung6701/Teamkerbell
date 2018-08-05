package org.teamfairy.sopt.teamkerbell.model.list

import android.content.Context
import org.teamfairy.sopt.teamkerbell.model.realm.ChatMessageR
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.model.realm.UserR


/**
 * Created by lumiere on 2018-01-01.
 */
data class ChatMessage(
        var chat_idx: Int,
        var type: Int,
        var u_idx: Int?,
        var content: String?,
        var date: String?
) {

    var count: Int? = 0
    var name: String? = null
    var photo: String? = null

    fun getItemContent(): String {
        val delmi = content!!.indexOf('/')
        return content!!.substring(delmi + 1)
    }
    fun update(
                type: Int,
                u_idx: Int?,
                content: String?,
                date: String?){
        this.type=type
        this.u_idx=u_idx
        this.content=content
        this.date=date

    }

    fun toChatMessageR(): ChatMessageR {
        val chatMessageR = ChatMessageR()
        chatMessageR.chat_idx = chat_idx
        chatMessageR.type = type
        chatMessageR.u_idx = u_idx?:-1
        chatMessageR.content = content?:""
        chatMessageR.date = date?:""
        return chatMessageR
    }

    fun toChatMessageF(): ChatMessageF = ChatMessageF(chat_idx, type, u_idx, content, date)

    fun isSender(context: Context): Boolean {
        return u_idx == LoginToken.getUserIdx(context)
    }

    fun setPhotoInfo(context: Context) {
        val realm = DatabaseHelpUtils.getRealmDefault(context)
        setPhotoInfo(realm)
    }

    fun setPhotoInfo(realm: Realm) {
        val userR = realm.where(UserR::class.java).equalTo("u_idx", u_idx).findFirst() ?: UserR()

        name = userR.name
        photo = userR.photo

    }

}
