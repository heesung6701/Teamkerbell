package org.teamfairy.sopt.teamkerbell.model.data

import org.teamfairy.sopt.teamkerbell.model.realm.UserR


/**
 * Created by lumiere on 2018-01-01.
 */
data class User(
        var u_idx: Int, //Primary Key
        var name: String?,
        var phone: String?,
        var bio: String?,
        var photo: String?,
        var id: String?
){
    fun toUserR() : UserR {
        val userR = UserR()
        userR.u_idx=u_idx
        userR.name=name!!
        userR.phone=phone!!
        userR.bio=bio ?: ""
        userR.photo=photo ?: ""
        userR.id=id!!
        return userR
    }

    constructor(u_idx: Int, name: String) : this(u_idx, name, null, null, null, null)

    constructor(u_idx: Int, name: String, photo: String?, id: String?) : this(u_idx, name, null, null, photo, id)


    override fun toString(): String {
        return "u_idx:"+u_idx+","+
                "name:"+name+","+
                "phone:"+phone+","+
                "bio:"+bio+","+
                "photo:"+photo+","+
                "id:"+id+","
    }

}
