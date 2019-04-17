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
data class SignalResponse(
    var signal_idx: Int,
    override var u_idx: Int,
    var content: String?,
    var write_time: String?,
    var color: String?
) : UserInfoInterface(), ListDataInterface, Parcelable {
    override fun getRoomTitle(): String = ""

    override fun getGroupTitle(): String = ""

    override var room_idx: Int = 0

    fun setPhotoInfo(context: Context) = super.setPhotoInfo(context, u_idx)

    fun setPhotoInfo(realm: Realm) = super.setPhotoInfo(realm, u_idx)

    override fun getMainTitle(): String {
        if (!content.isNullOrBlank()) return content!!
        return if (Signal.colorStrToByte(color ?: "a") == Signal.RED) "아직 응답하지 않았습니다." else ""
    }

    override fun getSubTitle(): String = getTime()

    override fun getTime(): String = if (write_time.isNullOrBlank()) "" else Utils.getMonthDayTime(write_time!!)

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(signal_idx)
        writeInt(u_idx)
        writeString(content)
        writeString(write_time)
        writeString(color)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SignalResponse> = object : Parcelable.Creator<SignalResponse> {
            override fun createFromParcel(source: Parcel): SignalResponse = SignalResponse(source)
            override fun newArray(size: Int): Array<SignalResponse?> = arrayOfNulls(size)
        }
    }
}