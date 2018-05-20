package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.interfaces.UserInfoInterface
import org.teamfairy.sopt.teamkerbell.utils.Utils

/**
 * Created by lumiere on 2018-01-01.
 */
data class Signal(
        var light_idx: Int, //Primary Key
        override var u_idx: Int,
        var chat_idx: Int?,
        var write_time: String,
        var open_status: Int?,
        override var room_idx: Int, //Foreign Key
        var content: String?,
        var entire_status: Int,
        var color: String?
) : UserInfoInterface(), ListDataInterface, Parcelable {

    fun setPhotoInfo(context: Context) {
        super.setPhotoInfo(context, u_idx)
    }

    fun setPhotoInfo(realm: Realm) {
        super.setPhotoInfo(realm, u_idx)
    }

    override fun getMainTitle(): String {
        return content!!
    }

    override fun getTime(): String {
        return Utils.getMonthDayTime(write_time)
    }


    override fun getSubTitle(): String {
        return Utils.getMonthDayTime(write_time)
    }

    constructor(light_idx: Int, u_idx: Int, chat_idx: Int?, write_time: String, open_status: Int?, room_idx: Int, content: String?, entire_status: Int) : this(light_idx, u_idx, chat_idx, write_time, open_status, room_idx, content, entire_status, null)


    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readString(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readInt(),
            source.readString(),
            source.readInt(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(light_idx)
        writeInt(u_idx)
        writeValue(chat_idx)
        writeString(write_time)
        writeValue(open_status)
        writeInt(room_idx)
        writeString(content)
        writeInt(entire_status)
        writeString(color)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Signal> = object : Parcelable.Creator<Signal> {
            override fun createFromParcel(source: Parcel): Signal = Signal(source)
            override fun newArray(size: Int): Array<Signal?> = arrayOfNulls(size)
        }
    }
}