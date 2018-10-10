package org.teamfairy.sopt.teamkerbell.model.data

import android.os.Parcel
import android.os.Parcelable
import org.teamfairy.sopt.teamkerbell.model.interfaces.GroupInterface
import org.teamfairy.sopt.teamkerbell.model.realm.RoomR
import org.teamfairy.sopt.teamkerbell.utils.Utils

/**
 * Created by lumiere on 2018-01-01.
 */
class Room(
        var g_idx: Int, //Foreign Key
        var room_idx: Int, //Primary Key
        override var real_name: String,
        var ctrl_name: String,
        var photo: String?
) : GroupInterface, Parcelable{
    constructor() : this(ARG_NULL_IDX,Room.ARG_ALL_IDX, ARG_ALL,"")
    constructor(g_idx: Int,room_idx: Int, real_name: String, ctrl_name: String):this(g_idx,room_idx,real_name,ctrl_name,null)

    override fun getIdx() =room_idx

    var lastMsgStr = ""
    var lastMsgTime = ""
    var newMsgCnt = 0

    fun getTime() : String = Utils.getMonthDayTime(lastMsgTime)

    fun setLastMsg(lastMsgStr : String, lastMsgTime : String, newMsgCnt : Int){
        this.lastMsgStr=lastMsgStr
        this.lastMsgTime=lastMsgTime
        this.newMsgCnt=newMsgCnt
    }


    fun toChatRoomR(): RoomR {
        val chatRoomR= RoomR()
        chatRoomR.g_idx=g_idx
        chatRoomR.room_idx=room_idx
        chatRoomR.real_name=real_name
        chatRoomR.ctrl_name=ctrl_name
        chatRoomR.photo=photo?:""
        return chatRoomR
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
        writeInt(g_idx)
        writeInt(room_idx)
        writeString(real_name)
        writeString(ctrl_name)
        writeString(photo)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Room> = object : Parcelable.Creator<Room> {
            override fun createFromParcel(source: Parcel): Room = Room(source)
            override fun newArray(size: Int): Array<Room?> = arrayOfNulls(size)
        }

        var max_length = 12


        var ARG_ROOM_IDX = "room_idx"
        var ARG_REAL_NAME = "real_name"
        var ARG_CTRL_NAME = "ctrl_name"
        var ARG_PHOTO = "photo"
        var ARG_NULL_IDX = -2
        var ARG_ALL_IDX = -1
        var ARG_ALL = "All"
    }
}
