package org.teamfairy.sopt.teamkerbell.fcm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject
import android.os.Build
import android.app.NotificationChannel
import android.graphics.Color
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.SplashActivity
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.realm.BadgeCnt
import org.teamfairy.sopt.teamkerbell.model.realm.RoomR
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_BODY
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CHAT_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DATA
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_G_IDX
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_GROUP
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_JOINED_GROUP
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_JOINED_ROOM
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_ROOM
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_USER
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.setPref_isUpdate
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.StatusCode
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_INDEX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_STATUS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_TITLE
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by lumiere on 2018-01-13.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage!!.from!!)

        // Check if message contains a data payload.

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Title: " + remoteMessage.notification?.title)
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification?.body?:"null")
            sendNotification(remoteMessage.notification?.title ?: "팀커벨", remoteMessage.notification?.body ?: "메세지가 도착했습니다", "팀커벨",createID())
        } else {
            if (remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "Message data payload: " + remoteMessage.data)

                if (/* Check if data needs to be processed by long running job */ false) {
                    // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                    scheduleJob()
                } else {
                    // Handle message within 10 seconds
                    handleNow(remoteMessage)
                }

            }

        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
//        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
//        val myJob = dispatcher.newJobBuilder()
//                .setService(MyJobService::class.java)
//                .setTag("my-job-tag")
//                .build()
//        dispatcher.schedule(myJob)
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow(remoteMessage: RemoteMessage) {
        val params = remoteMessage.data

        var title: String = "팀커벨"
        var body: String?=null
        var idx : Int? =null
        var gIdx: Int = -1
        var roomIdx: Int = -1
        var chatIdx: Int = -1

        var status = 0
        try {
            val jsonObject = JSONObject(params)
            if (jsonObject.has(JSON_TITLE))
                title = jsonObject.getString(JSON_TITLE)
            if (jsonObject.has(JSON_BODY))
                body = jsonObject.getString(JSON_BODY)
            if (jsonObject.has(JSON_INDEX))
                idx = jsonObject.getInt(JSON_INDEX)
            if (jsonObject.has(JSON_ROOM_IDX))
                roomIdx = jsonObject.getInt(JSON_ROOM_IDX)
            if (jsonObject.has(JSON_CHAT_IDX))
                chatIdx = jsonObject.getInt(JSON_CHAT_IDX)
            if (jsonObject.has(JSON_STATUS))
                status = jsonObject.getInt(JSON_STATUS)
            if (jsonObject.has(JSON_DATA))
                status = jsonObject.getInt(JSON_DATA)
            if (jsonObject.has(JSON_G_IDX))
                gIdx = jsonObject.getInt(JSON_G_IDX)
            else{
                val realm = getRealmDefault(applicationContext)
                gIdx = realm.where(RoomR::class.java).findFirst()?.g_idx ?: -1
                realm.close()
            }


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        when (status) {
            StatusCode.groupChange -> {
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_GROUP, true)
            }
            StatusCode.joinedGroupChange ->
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_GROUP, true)
            StatusCode.userChange ->
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_USER, true)
            StatusCode.roomChange ->
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_ROOM, true)
            StatusCode.joinedRoomChange ->
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_ROOM, true)

            StatusCode.roomWithJoinedRoomChange-> {
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_ROOM, true)
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_ROOM, true)
            }

            StatusCode.joinedGroupWithJoinedRoom-> {
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_GROUP, true)
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_ROOM, true)
            }

            StatusCode.userWithJoinedGroupChange-> {
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_USER, true)
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_GROUP, true)
            }
            StatusCode.allChange-> {
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_GROUP, true)
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_ROOM, true)
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_USER, true)
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_GROUP, true)
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_ROOM, true)
            }
            StatusCode.votePush -> {
                sendNotification(title, body?:"공지가 만들어졌습니다.", DatabaseHelpUtils.getRoom(applicationContext,roomIdx).real_name,chatIdx)
                BadgeCnt.increase(applicationContext,BadgeCnt.WHAT_VOTE,gIdx)
            }
            StatusCode.makeSignal ->{
                sendNotification(title, body?:"신호등이 만들어졌습니다.", DatabaseHelpUtils.getRoom(applicationContext,roomIdx).real_name,chatIdx)
                BadgeCnt.increase(applicationContext,BadgeCnt.WHAT_SIGNAL,gIdx)

            }
            StatusCode.makeNotice ->{

                sendNotification(title, body?:"공지가 만들어 졌습니다", DatabaseHelpUtils.getRoom(applicationContext,roomIdx).real_name,chatIdx)
                BadgeCnt.increase(applicationContext,BadgeCnt.WHAT_NOTICE,gIdx)
            }
            StatusCode.makeVote ->{

                sendNotification(title, body?:"투표가 등록되었습니다", DatabaseHelpUtils.getRoom(applicationContext,roomIdx).real_name,chatIdx)
                BadgeCnt.increase(applicationContext,BadgeCnt.WHAT_VOTE,gIdx)
            }
            StatusCode.makeRole ->{
                sendNotification(title, body?:"역할분담이 등록되었습니다.", DatabaseHelpUtils.getRoom(applicationContext,roomIdx).real_name,chatIdx)
                BadgeCnt.increase(applicationContext,BadgeCnt.WHAT_ROLE,gIdx)
            }

            StatusCode.chatMessage ->{

                sendNotification(title, body?:"메세지가 도착했습니다.", DatabaseHelpUtils.getRoom(applicationContext,roomIdx).real_name,chatIdx,gIdx)
            }

        }

        if (LoginToken.isValid()) {

            Log.d(TAG, "LoginToken is Valid ")
            NetworkUtils.connectUserList(applicationContext,null)
            NetworkUtils.connectGroupList(applicationContext,null)
            NetworkUtils.connectRoomList(applicationContext,null)
            NetworkUtils.connectJoinedGroupList(applicationContext,null)
            NetworkUtils.connectJoinedRoomList(applicationContext,null)
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     */
    private fun sendNotification(title: String, content: String, CHANNEL_ID: String,nId : Int,g_idx : Int) {
        if(!DatabaseHelpUtils.getSettingPush(applicationContext)) return
        if(!DatabaseHelpUtils.getSettingPush(applicationContext,g_idx)) return
        sendNotification(title,content,CHANNEL_ID,nId)
    }
    private fun sendNotification(title: String, content: String, CHANNEL_ID: String,nId : Int) {

        val intent = Intent(this, SplashActivity::class.java)
        LoginToken.getPref(applicationContext)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)


        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)


        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name);// The user-visible name of the channel.
            val importance = NotificationManager.IMPORTANCE_HIGH

            val mChannel: NotificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description=CHANNEL_ID
            mChannel.enableLights(true)
            mChannel.lightColor= Color.GREEN
            mChannel.enableVibration(true)
            mChannel.vibrationPattern=longArrayOf(100, 200, 100, 200)
            mChannel.lockscreenVisibility=Notification.VISIBILITY_PUBLIC
            mNotificationManager.createNotificationChannel(mChannel)

            notificationBuilder.setChannelId(CHANNEL_ID)

        }


        mNotificationManager.notify(nId, notificationBuilder.build())
        Log.d(TAG, "nId : $nId")

    }

    private fun createID(): Int {
        val now = Date()
        return Integer.parseInt(SimpleDateFormat("ddHHmmss", Locale.US).format(now))
    }
    companion object {
        private val TAG = "FCM"
    }
}