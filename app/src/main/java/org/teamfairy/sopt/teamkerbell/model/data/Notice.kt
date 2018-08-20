package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell.model.interfaces.RoomInfoInterface
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.utils.Utils

/**
 * Created by lumiere on 2018-01-01.
 */
data class Notice(
        override var u_idx: Int,
        var write_time: String,
        var content: String?,
        override var room_idx: Int,
        var notice_idx: Int,
        var status : Int
) : RoomInfoInterface(), ListDataInterface, Parcelable {

    constructor(u_idx: Int,write_time: String,content: String?,room_idx: Int,notice_idx: Int) : this(u_idx,write_time,content,room_idx,notice_idx, ARG_STATUS_NOT_READ)
    constructor() : this(u_idx = 0,  write_time = "", content = "", room_idx = 0, notice_idx = 0,status = ARG_STATUS_READ)

    override fun getMainTitle(): String = content!!

    override fun getSubTitle(): String = Utils.getMonthDayTime(write_time)

    override fun getTime(): String = Utils.getMonthDayTime(write_time)

    fun setGroupInfo(context: Context) = super.setGroupInfo(context, room_idx)

    fun setGroupInfo(realm: Realm) = super.setGroupInfo(realm, room_idx)

    fun setPhotoInfo(context: Context) = super.setPhotoInfo(context, u_idx)

    fun setPhotoInfo(realm: Realm) = super.setPhotoInfo(realm, u_idx)

    override fun getRoomTitle(): String {
        return roomName
    }
    override fun getGroupTitle(): String {
        return groupName
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readInt(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(u_idx)
        writeString(write_time)
        writeString(content)
        writeInt(room_idx)
        writeInt(notice_idx)
        writeInt(status)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Notice> = object : Parcelable.Creator<Notice> {
            override fun createFromParcel(source: Parcel): Notice = Notice(source)
            override fun newArray(size: Int): Array<Notice?> = arrayOfNulls(size)
        }


        var ARG_NOTICE_IDX = "notice_idx"
        var ARG_STATUS = "status"
        var ARG_STATUS_READ = 1
        var ARG_STATUS_NOT_READ = 0

    }
}