package org.teamfairy.sopt.teamkerbell._utils

import android.app.Activity
import android.os.Handler
import android.os.Message
import com.google.firebase.database.*
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessageF
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessageF.Companion.ARG_CHAT_IDX
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessageF.Companion.ARG_CONTENT
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.getNowForFirebase
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-02-24.
 */
class FirebaseMessageUtils {
    companion object {
        const val ARG_LAST_MESSAGE = "lastMessage"
        const val ARG_END_POINT = "endPoints"
        const val ARG_MESSAGES = "messages"
        const val ARG_FIREBASE_TOKENS = "firebase_tokens"
        const val ARG_GROUPS = "groups"


        var dataBase: FirebaseDatabase = FirebaseDatabase.getInstance()
        var dataBaseReference: DatabaseReference = dataBase.reference
        var dataBaseGroup: DatabaseReference by Delegates.notNull()
        var dataBaseMessages: DatabaseReference by Delegates.notNull()
        var dataBaseLastMessage: DatabaseReference? = null
        var dataBaseEndpoints: DatabaseReference by Delegates.notNull()
        var dataBaseFireToken: DatabaseReference by Delegates.notNull()

        fun setDatabaseGroup(group: Team, room: Room) {
            dataBaseGroup = dataBaseReference.child(ARG_GROUPS).child(group.ctrl_name).child(room.ctrl_name)
            dataBaseMessages = dataBaseGroup.child(ARG_MESSAGES).ref
            dataBaseLastMessage = dataBaseGroup.child(ARG_LAST_MESSAGE)
            dataBaseEndpoints = dataBaseGroup.child(ARG_END_POINT).ref
            dataBaseFireToken = dataBaseReference.child(ARG_FIREBASE_TOKENS).ref

        }

        fun sendMessage(type: Int, idx: Int, content: String, group: Team, room: Room, u_idx: Int, activity: Activity) {
            getLastChatIdx(group, room, HandlerSendMsg(type, idx, content, u_idx, activity))

        }

        fun getLastChatIdx(group: Team, room: Room, handler: Handler) {
            if (dataBaseLastMessage == null) setDatabaseGroup(group, room)
//            dataBaseLastMessage!!.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onCancelled(p0: DatabaseError?) {
//                }
//
//                override fun onDataChange(dataSnapshot: DataSnapshot?) {
//                    var lastChatIdx = 0
//                    if (dataSnapshot!!.hasChild(ARG_CHAT_IDX))
//                        lastChatIdx = dataSnapshot.child(ARG_CHAT_IDX).value.toString().toInt()
//
//                    handler.sendEmptyMessage(lastChatIdx)
//
//                }
//            })
        }
    }

    private class HandlerSendMsg(var type: Int, var idx: Int, var content: String, var u_idx: Int, activity: Activity) : Handler() {
        private val mActivity: WeakReference<Activity> = WeakReference<Activity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                val lastChatIdx = msg.what
                val chatMessage = ChatMessageF(lastChatIdx + 1, type, u_idx, idx.toString() + "/" + content, getNowForFirebase())
                dataBaseMessages.push().setValue(chatMessage)
                dataBaseLastMessage!!.child(ARG_CHAT_IDX).setValue(lastChatIdx + 1)
                val txt = when (type) {
                    ChatUtils.TYPE_SIGNAL -> "신호등"
                    ChatUtils.TYPE_NOTICE -> "공지"
                    ChatUtils.TYPE_VOTE -> "투포"
                    else -> "무언가"
                }
                dataBaseLastMessage!!.child(ARG_CONTENT).setValue(txt + "가 등록되었습니다.")

            }
        }
    }


}