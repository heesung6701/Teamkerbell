package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.model.list.ContactListData
import org.teamfairy.sopt.teamkerbell.model.data.User

/**
 * Created by lumiere on 2018-01-01.
 */
open class UserR : RealmObject() {
    @PrimaryKey
    var u_idx: Int=-2

    var name: String="이름없음"
    var phone: String="010-2081-3818"
    var bio: String=""
    var photo: String=""

    var id: String=""

    fun toContactListData(): ContactListData = ContactListData(null, User(u_idx, name, phone, bio, photo, id),false)
    fun toUser() : User = User(u_idx, name, phone, bio, photo, id)
}