package org.teamfairy.sopt.teamkerbell.model.list

import org.teamfairy.sopt.teamkerbell.model.data.Team

/**
 * Created by lumiere on 2017-12-30.
 */
data class ChatListData(
    var group: Team,
    var content: String?,
    var time: String?,
    var count_new: Int
)