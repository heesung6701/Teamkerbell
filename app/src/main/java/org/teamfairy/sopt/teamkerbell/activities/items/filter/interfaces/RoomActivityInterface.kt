package org.teamfairy.sopt.teamkerbell.activities.items.filter.interfaces

import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-05-20.
 */
interface RoomActivityInterface {
    var group : Team
    var room : Room?
    fun changeRoom(room : Room)
}