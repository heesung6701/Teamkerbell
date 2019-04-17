package org.teamfairy.sopt.teamkerbell.model.data

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by lumiere on 2018-01-01.
 */
data class VoteResponse(
    var vote: Vote,
    var examples: HashMap<Int, String>, // Example_id , Example_content
    var responses: HashMap<Int, Int> // User_id, Example_id
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readParcelable<Vote>(Vote::class.java.classLoader),
            source.readSerializable() as HashMap<Int, String>,
            source.readSerializable() as HashMap<Int, Int>
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(vote, 0)
        writeSerializable(examples)
        writeSerializable(responses)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VoteResponse> = object : Parcelable.Creator<VoteResponse> {
            override fun createFromParcel(source: Parcel): VoteResponse = VoteResponse(source)
            override fun newArray(size: Int): Array<VoteResponse?> = arrayOfNulls(size)
        }
    }
}