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
        var light_idx: Int,
        override var u_idx: Int,
        var color: String?,
        var content: String?,
        var write_time: String?
) : UserInfoInterface(), ListDataInterface,Parcelable {
    override fun getRoomTitle(): String {
        return ""
    }


    override var room_idx: Int = 0

    fun setPhotoInfo(context: Context) =super.setPhotoInfo(context,u_idx)


    fun setPhotoInfo(realm: Realm) = super.setPhotoInfo(realm,u_idx)

    override fun getMainTitle(): String {
        if (content.isNullOrEmpty()) content = ""
        return content!!
    }

    override fun getSubTitle(): String {
        if (write_time.isNullOrEmpty()) write_time = "아직 답변하지 않았습니다."
        return write_time!!
    }
    override fun getTime(): String {
        return Utils.getMonthDayTime(write_time!!)
    }



    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(light_idx)
        writeInt(u_idx)
        writeString(color)
        writeString(content)
        writeString(write_time)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SignalResponse> = object : Parcelable.Creator<SignalResponse> {
            override fun createFromParcel(source: Parcel): SignalResponse = SignalResponse(source)
            override fun newArray(size: Int): Array<SignalResponse?> = arrayOfNulls(size)
        }
    }
}