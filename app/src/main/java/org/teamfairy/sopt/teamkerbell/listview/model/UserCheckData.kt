package org.teamfairy.sopt.teamkerbell.model.list

import org.teamfairy.sopt.teamkerbell.model.data.User


/**
 * Created by lumiere on 2017-12-30.
 */
data class UserCheckData(
        var user : User,
        var isChecked: Boolean?
)