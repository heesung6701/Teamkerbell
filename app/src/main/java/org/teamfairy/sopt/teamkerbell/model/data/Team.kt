package org.teamfairy.sopt.teamkerbell.model.data

import android.os.Parcel
import android.os.Parcelable
import org.teamfairy.sopt.teamkerbell.model.interfaces.GroupInterface
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR

/**
 * Created by lumiere on 2018-01-01.
 */
data class Team(
        var g_idx: Int, //Primary Key
        override var real_name: String,
        var ctrl_name: String,
        var photo: String?,
        var default_room_idx: Int=-1
) : GroupInterface,Parcelable{

    constructor(g_idx: Int,real_name: String,ctrl_name: String,default_room_idx: Int):this(g_idx,real_name,ctrl_name,null,default_room_idx)

    override fun toString(): String =("$g_idx/$real_name/$ctrl_name/$photo")

    override fun getIdx(): Int =g_idx

    fun toGroupR(): GroupR {
        val groupR= GroupR()
        groupR.g_idx=g_idx
        groupR.real_name=real_name
        groupR.ctrl_name=ctrl_name
        groupR.photo=photo?:""
        groupR.default_room_idx = default_room_idx
        return groupR
    }


    constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(g_idx)
        writeString(real_name)
        writeString(ctrl_name)
        writeString(photo)
        writeInt(default_room_idx)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Team> = object : Parcelable.Creator<Team> {
            override fun createFromParcel(source: Parcel): Team = Team(source)
            override fun newArray(size: Int): Array<Team?> = arrayOfNulls(size)
        }


        var max_length  = 12

        var ARG_G_IDX = "g_idx"
        var ARG_REAL_NAME = "real_name"
        var ARG_CTRL_NAME = "ctrl_name"
        var ARG_PHOTO = "photo"
        var ARG_DEFAULT_ROOM_IDX = "default_room_idx"
    }
}
