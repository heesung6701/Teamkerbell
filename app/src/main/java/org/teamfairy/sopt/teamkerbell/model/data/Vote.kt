package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell.model.interfaces.ListDataInterface
import org.teamfairy.sopt.teamkerbell.model.interfaces.RoomInfoInterface
import org.teamfairy.sopt.teamkerbell.model.interfaces.UserInfoInterface
import org.teamfairy.sopt.teamkerbell.model.realm.VoteR
import org.teamfairy.sopt.teamkerbell.utils.Utils

/**
 * Created by lumiere on 2018-01-01.
 */
data class Vote(
        var vote_idx: Int,  // Primary Key
        override var u_idx: Int,
        var write_time: String,
        var content: String?,
        override var room_idx: Int,
        var title: String?,
        var status: Int?
) : RoomInfoInterface(), ListDataInterface, Parcelable {


    override fun getRoomTitle(): String {
        return roomName
    }

    override fun getGroupTitle(): String {
        return groupName
    }

    fun setGroupInfo(context: Context){
        super.setGroupInfo(context,room_idx)
    }

    fun setGroupInfo(realm: Realm){
        super.setGroupInfo(realm,room_idx)
    }
    fun setPhotoInfo(context: Context) {
        super.setPhotoInfo(context, u_idx)
    }

    fun setPhotoInfo(realm: Realm) {
        super.setPhotoInfo(realm, u_idx)
    }
    fun toVoteR(): VoteR {
        val voteR = VoteR()
        voteR.vote_idx=vote_idx
        voteR.u_idx=u_idx
        voteR.write_time=write_time
        voteR.content=content
        voteR.room_idx=room_idx
        voteR.title=title
        voteR.status=status
        return voteR
    }

    fun isFinished() : Boolean =  (status == 0)

    override fun getMainTitle(): String {
        return title!!
    }

    override fun getSubTitle(): String {
        return content!!

    }

    override fun getTime(): String{
        return Utils.getMonthDayTime(write_time)
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readValue(Int::class.java.classLoader) as Int?
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(vote_idx)
        writeInt(u_idx)
        writeString(write_time)
        writeString(content)
        writeInt(room_idx)
        writeString(title)
        writeValue(status)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Vote> = object : Parcelable.Creator<Vote> {
            override fun createFromParcel(source: Parcel): Vote = Vote(source)
            override fun newArray(size: Int): Array<Vote?> = arrayOfNulls(size)
        }


        val ARG_WRITETIME = "write_time"
    }
}