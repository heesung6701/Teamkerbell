package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.interfaces.RoomInfoInterface
import org.teamfairy.sopt.teamkerbell.utils.Utils
import kotlin.experimental.or

/**
 * Created by lumiere on 2018-01-01.
 */
data class Signal(
    var signal_idx: Int, // Primary Key
    override var u_idx: Int,
    var write_time: String,
    var open_status: Int?,
    override var room_idx: Int, // Foreign Key
    var content: String?,
    var entire_status: Int,
    var responseColor: String?,
    var responseContent: String?
) : RoomInfoInterface(), ListDataInterface, Parcelable {

    constructor(signal_idx: Int, u_idx: Int, write_time: String, open_status: Int?, room_idx: Int, content: String?, entire_status: Int) :
            this(signal_idx, u_idx, write_time, open_status, room_idx, content, entire_status, "a", null)
    override fun getRoomTitle(): String {
        return roomName
    }

    fun setGroupInfo(context: Context) {
        super.setGroupInfo(context, room_idx)
    }
    fun setGroupInfo(realm: Realm) {
        super.setGroupInfo(realm, room_idx)
    }

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

    override fun getGroupTitle(): String {
        return groupName
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readInt(),
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(signal_idx)
        writeInt(u_idx)
        writeString(write_time)
        writeValue(open_status)
        writeInt(room_idx)
        writeString(content)
        writeInt(entire_status)
        writeString(responseColor)
        writeString(responseContent)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Signal> = object : Parcelable.Creator<Signal> {
            override fun createFromParcel(source: Parcel): Signal = Signal(source)
            override fun newArray(size: Int): Array<Signal?> = arrayOfNulls(size)
        }

        fun colorStrToInt(color: String?): Int {
            return when (color) {
                STR_RED -> RED.toInt()
                STR_GREEN -> GREEN.toInt()
                STR_YELLOW -> YELLOW.toInt()
                STR_DEFAULT -> RED.toInt()
                else -> RED.toInt()
            }
        }

        fun colorStrToByte(color: String?): Byte {
            return when (color) {
                STR_RED -> RED
                STR_GREEN -> GREEN
                STR_YELLOW -> YELLOW
                STR_DEFAULT -> RED
                else -> RED
            }
        }
        fun colorByteToStr(selectColor: Byte?): String {
            return when (selectColor) {
                RED -> STR_RED
                GREEN -> STR_GREEN
                YELLOW -> STR_YELLOW
                else -> STR_DEFAULT
            }
        }

        const val RED: Byte = 1
        const val YELLOW: Byte = 2
        const val GREEN: Byte = 4
        val ALL: Byte = RED or YELLOW or GREEN
        const val DEFAULT: Byte = 8

        const val STR_RED = "r"
        const val STR_GREEN = "g"
        const val STR_YELLOW = "y"
        const val STR_DEFAULT = "a"
    }
}