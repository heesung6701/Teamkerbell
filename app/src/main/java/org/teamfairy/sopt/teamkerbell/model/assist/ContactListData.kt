package org.teamfairy.sopt.teamkerbell.model.list

import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.data.User

/**
 * Created by lumiere on 2017-12-30.
 */
data class ContactListData(
    var group: Team?,
    var user: User?,
    val isGroupName: Boolean
)