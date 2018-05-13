package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.data.Room.Companion.ARG_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.model.data.User.Companion.ARG_U_IDX
import org.teamfairy.sopt.teamkerbell.model.interfaces.GroupInfoInterface
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.interfaces.UserInfoInterface
import org.teamfairy.sopt.teamkerbell.model.realm.GroupR
import org.teamfairy.sopt.teamkerbell.model.realm.RoomR
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.utils.Utils

/**
 * Created by lumiere on 2018-01-01.
 */
data class Notice(
        override var u_idx: Int,
        var chat_idx: Int?,
        var write_time: String,
        var content: String?,
        override var room_idx: Int,
        var notice_idx: Int
) : GroupInfoInterface(), ListDataInterface, Parcelable {


    constructor() : this(u_idx = 0, chat_idx = 0, write_time = "", content = "", room_idx = 0, notice_idx = 0)

    override fun getMainTitle(): String = content!!

    override fun getSubTitle(): String = Utils.getMonthDayTime(write_time)

    override fun getTime(): String = Utils.getMonthDayTime(write_time)

    fun setGroupInfo(context: Context) = super.setGroupInfo(context, room_idx)

    fun setGroupInfo(realm: Realm) = super.setGroupInfo(realm, room_idx)

    fun setPhotoInfo(context: Context) = super.setPhotoInfo(context, u_idx)

    fun setPhotoInfo(realm: Realm) = super.setPhotoInfo(realm, u_idx)

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(u_idx)
        writeValue(chat_idx)
        writeString(write_time)
        writeString(content)
        writeInt(room_idx)
        writeInt(notice_idx)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Notice> = object : Parcelable.Creator<Notice> {
            override fun createFromParcel(source: Parcel): Notice = Notice(source)
            override fun newArray(size: Int): Array<Notice?> = arrayOfNulls(size)
        }
    }
}