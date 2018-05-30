package org.teamfairy.sopt.teamkerbell.activities.main.interfaces

import org.teamfairy.sopt.teamkerbell.model.data.Team

/**
 * Created by lumiere on 2018-05-10.
 */
interface HasGroupFragment {
    var group: Team
    fun changeGroup(g: Team)
}