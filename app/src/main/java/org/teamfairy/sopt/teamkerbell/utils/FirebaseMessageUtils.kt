package org.teamfairy.sopt.teamkerbell._utils

import android.app.Activity
import android.os.Handler
import android.os.Message
import com.google.firebase.database.*
import org.teamfairy.sopt.teamkerbell.model.list.ChatMessageF
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.getNowForFirebase
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-02-24.
 */
class FirebaseMessageUtils {
    companion object {

        var dataBase: FirebaseDatabase = FirebaseDatabase.getInstance()
        var dataBaseReference: DatabaseReference = dataBase.reference
        var dataBaseGroup: DatabaseReference by Delegates.notNull()
        var dataBaseMessages: DatabaseReference by Delegates.notNull()
        var dataBaseLastMessage: DatabaseReference?=null
        var dataBaseEndpoints: DatabaseReference by Delegates.notNull()
        var dataBaseFireToken: DatabaseReference by Delegates.notNull()

        fun setDatabaseGroup(ctrl_name : String){
            dataBaseGroup = dataBaseReference.child("groups_test").child(ctrl_name)
            dataBaseMessages = dataBaseGroup.child("messages").ref
            dataBaseLastMessage= dataBaseGroup.child("LastMessage")
            dataBaseEndpoints = dataBaseGroup.child("endPoints").ref
            dataBaseFireToken = dataBaseReference.child("firebase_tokens").ref

        }

        fun sendMessage(type: Int, idx: Int, content:String, group : Team, u_idx : Int, activity : Activity) {
            getLastChatIdx(group ,HandlerSendMsg(type,idx,content,group ,u_idx,activity))

        }
        fun getLastChatIdx(group: Team, handler : Handler){
            if(dataBaseLastMessage==null) setDatabaseGroup(group.ctrl_name)
            dataBaseLastMessage!!.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    var lastChatIdx=0
                    if(dataSnapshot!!.hasChild("chat_idx"))
                        lastChatIdx = dataSnapshot.child("chat_idx").value.toString().toInt()

                    handler.sendEmptyMessage(lastChatIdx)

                }
            })
        }
    }
    private class HandlerSendMsg(var type: Int, var idx: Int, var content:String, var group : Team, var u_idx : Int, activity: Activity) : Handler() {
        private val mActivity: WeakReference<Activity> = WeakReference<Activity>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null){
                val lastChatIdx = msg.what
                val chatMessage = ChatMessageF(lastChatIdx+1 ,type,u_idx, idx.toString()+"/"+content, getNowForFirebase())
                dataBaseMessages.push().setValue(chatMessage)
                dataBaseLastMessage!!.child("chat_idx").setValue(lastChatIdx+1)
                val txt = when(type){
                    ChatUtils.TYPE_LIGHT-> "신호등"
                    ChatUtils.TYPE_NOTICE-> "공지"
                    ChatUtils.TYPE_VOTE-> "투포"
                    else ->  "무언가"
                }
                dataBaseLastMessage!!.child("content").setValue(txt+"가 등록되었습니다.")

            }
        }
    }


}