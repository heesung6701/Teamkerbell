package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.realm.UserR
import org.teamfairy.sopt.teamkerbell.model.realm.VoteR

/**
 * Created by lumiere on 2018-01-01.
 */
data class Vote(
        var vote_idx: Int,  // Primary Key
        override var u_idx: Int,
        var write_time: String,
        var content: String?,
        override var g_idx: Int,
        var title: String?,
        var status: Int?
) : ListDataInterface, Parcelable {
    override var name: String = ""

    override var photo: String = ""

    override fun setPhotoInfo(context: Context) {
        val realm = DatabaseHelpUtils.getRealmDefault(context)
        setPhotoInfo(realm)
    }
    fun toVoteR(): VoteR {
        val voteR = VoteR()
        voteR.vote_idx=vote_idx
        voteR.u_idx=u_idx
        voteR.write_time=write_time
        voteR.content=content
        voteR.g_idx=g_idx
        voteR.title=title
        voteR.status=status
        return voteR
    }

    override fun setPhotoInfo(realm: Realm) {
        val userR = realm.where(UserR::class.java).equalTo("u_idx", u_idx).findFirst() ?: UserR()
            name = userR.name
            photo = userR.photo
    }
    override fun getMainTitle(): String {
        return title!!
    }

    override fun getSubTitle(): String {
        return content!!
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
        writeInt(g_idx)
        writeString(title)
        writeValue(status)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Vote> = object : Parcelable.Creator<Vote> {
            override fun createFromParcel(source: Parcel): Vote = Vote(source)
            override fun newArray(size: Int): Array<Vote?> = arrayOfNulls(size)
        }
    }
}