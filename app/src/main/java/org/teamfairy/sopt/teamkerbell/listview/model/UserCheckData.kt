package org.teamfairy.sopt.teamkerbell.model.list

import org.teamfairy.sopt.teamkerbell.R.id.name
import org.teamfairy.sopt.teamkerbell.R.id.phone
import org.teamfairy.sopt.teamkerbell.model.data.User


/**
 * Created by lumiere on 2017-12-30.
 */
class UserCheckData(u_idx : Int,name : String,phone : String,bio :String,photo : String,id : String) : User(u_idx, name, phone, bio, photo, id){
    var isChecked: Boolean = false
}