package org.teamfairy.sopt.teamkerbell.fcm

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
import org.teamfairy.sopt.teamkerbell.R
import org.teamfairy.sopt.teamkerbell.activities.SplashActivity
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_GROUP
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_JOINED_GROUP
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_JOINED_ROOM
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_ROOM
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_USER
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.setPref_isUpdate
import org.teamfairy.sopt.teamkerbell._utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell._utils.StatusCode
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_G_IDX


/**
 * Created by lumiere on 2018-01-13.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {



    private var nId: Int = 0
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

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage!!.from!!)

        // Check if message contains a data payload.

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Title: " + remoteMessage.notification!!.title!!)
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)
            sendNotification(remoteMessage.notification!!.title!!, remoteMessage.notification!!.body!!, null)
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

        var title: String? = null
        var content: String? = null
        var g_idx: Int? = null

        var code = 0
        try {
            val jsonObject = JSONObject(params)
            if (jsonObject.has("title"))
                title = jsonObject.getString("title")
            if (jsonObject.has("body"))
                content = jsonObject.getString("body")
            if (jsonObject.has(JSON_G_IDX))
                g_idx = jsonObject.getInt(JSON_G_IDX)

            if (jsonObject.has("data")) {
                code = jsonObject.getInt("data")
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        when (code) {
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

            StatusCode.groupJoinedUserChange -> {
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_GROUP, true)
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_USER, true)
                setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED_GROUP, true)
            }
            StatusCode.votePush ->
                sendNotification(title!!, content!!, null)
            StatusCode.makeLights ->
                sendNotification(title!!, content!!, null)
            StatusCode.makeNotice ->
                sendNotification(title!!, content!!, null)
            StatusCode.chatMessage ->
                sendNotification(title!!, content!!, g_idx)

        }

        if (LoginToken.isValid()) {

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
    private fun sendNotification(title: String, content: String, notifyId: Int?) {

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = "my_channel_01"// The id of the channel.
            val name = getString(R.string.app_name);// The user-visible name of the channel.
            val importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel: NotificationChannel? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(CHANNEL_ID, name, importance)
            } else {
                null
            }
            notificationBuilder.setChannelId(CHANNEL_ID)
        }

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notifyId == null) {
            mNotificationManager.notify(nId++,/* ID of notification */ notificationBuilder.build())
            Log.d(TAG, "nId : $nId")
        } else {
            Log.d(TAG, "notifyId : $notifyId")
            mNotificationManager.notify(notifyId,/* ID of notification */ notificationBuilder.build())
        }


    }

    companion object {
        private val TAG = this::class.java.name
    }
}