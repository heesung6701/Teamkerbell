package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.model.data.Notice

/**
 * Created by lumiere on 2018-01-01.
 */
open class NoticeR() : RealmObject() {
    var u_idx: Int?=null
    var chat_idx: Int?=null
    var write_time: String?=null
    var content: String?=null
    var g_idx: Int?=null

    @PrimaryKey
    var notice_idx: Int?=null


    fun toNotice(realm: Realm) : Notice {
        val userR = realm.where(UserR::class.java).equalTo("u_idx",u_idx).findFirst() ?: UserR()
        val notice= Notice(u_idx!!, chat_idx, write_time!!, content, g_idx!!, notice_idx!!)
            notice.photo = userR.photo
            notice.name = userR.name
        return notice
    }
}