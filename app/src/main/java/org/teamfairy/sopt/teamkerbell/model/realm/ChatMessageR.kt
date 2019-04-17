package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by lumiere on 2018-01-01.
 */
open class ChatMessageR : RealmObject() {
    @PrimaryKey
    var chat_idx: Int = -1

    var type: Int = 0
    var u_idx: Int = -1
    var content: String = ""
    var date: String = ""
}
