package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
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
) : ListDataInterface,Parcelable {


    override var g_idx: Int = 0

    override var name: String = ""

    override var photo: String = ""


    override fun setPhotoInfo(context: Context) {
        val realm = DatabaseHelpUtils.getRealmDefault(context)
        setPhotoInfo(realm)
    }


    override fun setPhotoInfo(realm: Realm) {
        val userR = realm.where(UserR::class.java).equalTo("u_idx", u_idx).findFirst() ?: UserR()
        name = userR.name
        photo = userR.photo

    }

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