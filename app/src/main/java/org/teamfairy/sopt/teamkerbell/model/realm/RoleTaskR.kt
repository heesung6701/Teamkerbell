package org.teamfairy.sopt.teamkerbell.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.model.data.RoleTask

/**
 * Created by lumiere on 2018-01-01.
 */
open class RoleTaskR : RealmObject() {
    var role_idx: Int? = null
    @PrimaryKey
    var task_idx: Int? = null
    var content: String? = null
    var userIdArrayStr: String? = null
    fun toRoleTask(): RoleTask {

        if (userIdArrayStr == "") {
            return RoleTask(role_idx!!, task_idx!!, content!!, IntArray(0))
        } else {
            val strs = userIdArrayStr!!.split("/")

            val userIdArray = IntArray(strs.size)
            for (i in 0 until strs.size) {
                userIdArray[i] = strs[i].toInt()
            }
            return RoleTask(role_idx!!, task_idx!!, content!!, userIdArray)
        }
    }
}