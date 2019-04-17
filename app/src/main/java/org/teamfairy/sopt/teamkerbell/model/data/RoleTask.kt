package org.teamfairy.sopt.teamkerbell.model.data

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by lumiere on 2018-03-13.
 */
data class RoleTask(
    var role_idx: Int?,
    var task_idx: Int?,
    var content: String,
    var userIdArray: IntArray
) : Parcelable {
  constructor(content: String) : this(null, null, content, IntArray(0))

  constructor(source: Parcel) : this(
          source.readValue(Int::class.java.classLoader) as Int?,
          source.readValue(Int::class.java.classLoader) as Int?,
          source.readString(),
          source.createIntArray()
  )

  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
    writeValue(role_idx)
    writeValue(task_idx)
    writeString(content)
    writeIntArray(userIdArray)
  }

  companion object {
    @JvmField
    val CREATOR: Parcelable.Creator<RoleTask> = object : Parcelable.Creator<RoleTask> {
      override fun createFromParcel(source: Parcel): RoleTask = RoleTask(source)
      override fun newArray(size: Int): Array<RoleTask?> = arrayOfNulls(size)
    }
  }
}