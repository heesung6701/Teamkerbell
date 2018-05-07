package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.TimeUtils
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.utils.Utils


/**
 * Created by lumiere on 2018-03-13.
 */
data class Role(
        var role_idx: Int,
        var g_idx: Int,
        var title: String?,
        var master_idx: Int?,
        var write_time: String?
) : UserInfoInterface, Parcelable {
    override var name: String = ""

    override var photo: String = ""

    fun getTime(): String {
        return Utils.getMonthDayTime(write_time!!) ?: ""
    }

    override fun setPhotoInfo(context: Context) {
        val realm = DatabaseHelpUtils.getRealmDefault(context)
        setPhotoInfo(realm)
    }

    override fun setPhotoInfo(realm: Realm) {
        val userR = realm.where(UserR::class.java).equalTo("u_idx", master_idx).findFirst()
                ?: UserR()
        name = userR.name
        photo = userR.photo
    }



    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(role_idx)
        writeInt(g_idx)
        writeString(title)
        writeValue(master_idx)
        writeString(write_time)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Role> = object : Parcelable.Creator<Role> {
            override fun createFromParcel(source: Parcel): Role = Role(source)
            override fun newArray(size: Int): Array<Role?> = arrayOfNulls(size)
        }
    }
}