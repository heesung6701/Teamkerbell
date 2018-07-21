package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.interfaces.UserInfoInterface
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.utils.Utils


/**
 * Created by lumiere on 2018-03-13.
 */
data class Role(
        var role_idx: Int,
        var room_idx: Int,
        var title: String?,
        var master_idx: Int,
        var write_time: String?
) : UserInfoInterface(), Parcelable {

    fun setPhotoInfo(context: Context) = super.setPhotoInfo(context, master_idx)

    fun setPhotoInfo(realm: Realm) = super.setPhotoInfo(realm, master_idx)

    fun getTime(): String {
        return Utils.getMonthDayTime(write_time!!)
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readInt(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(role_idx)
        writeInt(room_idx)
        writeString(title)
        writeInt(master_idx)
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