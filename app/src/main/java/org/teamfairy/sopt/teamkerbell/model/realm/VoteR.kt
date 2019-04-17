package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.model.data.User
import org.teamfairy.sopt.teamkerbell.model.data.Vote

/**
 * Created by lumiere on 2018-01-01.
 */
open class VoteR() : RealmObject() {
    @PrimaryKey
    var vote_idx: Int? = null

    var u_idx: Int? = null
    var write_time: String? = null
    var content: String? = null
    var room_idx: Int? = null
    var title: String? = null
    var status: Int? = null
    var g_idx: Int? = null

    fun toVote(realm: Realm): Vote {
        val userR = realm.where(UserR::class.java).equalTo(User.ARG_U_IDX, u_idx).findFirst() ?: UserR()
        val vote = Vote(vote_idx!!, u_idx!!, write_time!!, content, room_idx!!, title, status)
        vote.name = userR.name
        vote.photo = userR.photo
        return vote
    }
}