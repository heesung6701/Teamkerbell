package org.teamfairy.sopt.teamkerbell.model.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.model.interfaces.UserInfoInterface
import org.teamfairy.sopt.teamkerbell.model.realm.UserR


/**
 * Created by lumiere on 2018-03-13.
 */
class TaskResponse(
        var u_idx: Int,
        var task_idx: Int,
        var response_idx: Int,
        var content: String,
        var write_time: String,

        var fileArray: ArrayList<String>,

        var count: Int
) : UserInfoInterface(),Parcelable {

    fun setPhotoInfo(context: Context) {
        super.setPhotoInfo(context, u_idx)
    }

    fun setPhotoInfo(realm: Realm) {
        super.setPhotoInfo(realm, u_idx)
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.createStringArrayList(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(u_idx)
        writeInt(task_idx)
        writeInt(response_idx)
        writeString(content)
        writeString(write_time)
        writeStringList(fileArray)
        writeInt(count)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<TaskResponse> = object : Parcelable.Creator<TaskResponse> {
            override fun createFromParcel(source: Parcel): TaskResponse = TaskResponse(source)
            override fun newArray(size: Int): Array<TaskResponse?> = arrayOfNulls(size)
        }
    }
}