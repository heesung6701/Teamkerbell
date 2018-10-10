package org.teamfairy.sopt.teamkerbell.model.data

import org.teamfairy.sopt.teamkerbell.model.list.UserCheckData
import org.teamfairy.sopt.teamkerbell.model.realm.UserR


/**
 * Created by lumiere on 2018-01-01.
 */
open class User(
        var u_idx: Int, //Primary Key
        var name: String?,
        var phone: String?,
        var bio: String?,
        var photo: String?,
        var id: String?
){
    companion object {

        var name_max_length = 20


        var ARG_U_IDX = "u_idx"
        var ARG_NAME = "name"
        var ARG_PHONE = "phone"
        var ARG_BIO = "bio"
        var ARG_PHOTO = "photo"
        var ARG_ID = "id"

    }

    constructor(u_idx: Int, name: String) : this(u_idx, name, null, null, null, null)

    constructor(u_idx: Int, name: String, photo: String?, id: String?) : this(u_idx, name, null, null, photo, id)

    fun toUserCheckData(b : Boolean) : UserCheckData{
        val ucd : UserCheckData = UserCheckData(u_idx,name!!,phone!!,bio!!,photo!!,id!!)
        ucd.isChecked=b
        return ucd
    }

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

    override fun toString(): String {
        return "$ARG_U_IDX:"+u_idx+","+
                "$ARG_NAME:"+name+","+
                "$ARG_PHONE:"+phone+","+
                "$ARG_BIO:"+bio+","+
                "$ARG_PHOTO:"+photo+","+
                "$ARG_ID:"+id+","
    }

}
