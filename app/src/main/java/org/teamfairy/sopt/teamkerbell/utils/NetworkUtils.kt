package org.teamfairy.sopt.teamkerbell._utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.os.Handler
import android.os.Message
import android.webkit.URLUtil
import android.widget.ImageView
import org.teamfairy.sopt.teamkerbell.network.BitmapTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_GROUPLIST
import org.teamfairy.sopt.teamkerbell.network.info.GroupRListTask
import org.teamfairy.sopt.teamkerbell.network.info.JoinedListTask
import org.teamfairy.sopt.teamkerbell.network.info.UserRListTask
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_GROUP
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_JOINED
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.PREF_ISUPDATE_USER
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getPref_isUpdate
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.getRealmDefault
import org.teamfairy.sopt.teamkerbell._utils.DatabaseHelpUtils.Companion.setPref_isUpdate
import org.teamfairy.sopt.teamkerbell.model.realm.UrlToBytes
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS
import java.net.*


/**
 * Created by lumiere on 2018-01-04.
 */
open class NetworkUtils {


    companion object {

        const val GROUP = 1
        const val JOINED = 2
        const val USER = 4


        private val LOG_TAG = NetworkUtils::class.java.simpleName

        fun connectGroupList(applicationContext: Context, handler: Handler?) {
            if (getPref_isUpdate(applicationContext, PREF_ISUPDATE_GROUP)) {
                Log.d("$LOG_TAG/connect", "update Group List")
                val task = GroupRListTask(applicationContext, object : Handler() {
                    override fun handleMessage(msg: Message?) {
                        if (msg!!.what == MSG_SUCCESS) {
                            if (handler != null)
                                handler.sendEmptyMessage(MSG_SUCCESS)
                            setPref_isUpdate(applicationContext, PREF_ISUPDATE_GROUP, false)
                        }
                    }
                }, LoginToken.getToken(applicationContext))
                task.execute(URL_GROUPLIST)
            } else {
                if (handler != null)
                    handler.sendEmptyMessage(MSG_SUCCESS)
            }
        }

        fun connectUserList(applicationContext: Context, handler: Handler?) {
            if (getPref_isUpdate(applicationContext, PREF_ISUPDATE_USER)) {
                Log.d("$LOG_TAG/connect", "update User List")
                val task = UserRListTask(applicationContext, object : Handler() {
                    override fun handleMessage(msg: Message?) {
                        if (msg!!.what == MSG_SUCCESS) {
                            if (handler != null)
                                handler.sendEmptyMessage(MSG_SUCCESS)
                            setPref_isUpdate(applicationContext, PREF_ISUPDATE_USER, false)
                        }
                    }
                }, LoginToken.getToken(applicationContext))
                task.execute(USGS_REQUEST_URL.URL_USER)
            } else {
                if (handler != null)
                    handler.sendEmptyMessage(MSG_SUCCESS)
            }

        }


        fun connectJoinedList(applicationContext: Context, handler: Handler?) {
            if (getPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED)) {
                Log.d("$LOG_TAG/connect", "update Joined List")
                val task = JoinedListTask(applicationContext, object : Handler() {
                    override fun handleMessage(msg: Message?) {
                        if (msg!!.what == MSG_SUCCESS) {
                            if (handler != null)
                                handler.sendEmptyMessage(MSG_SUCCESS)
                            setPref_isUpdate(applicationContext, PREF_ISUPDATE_JOINED, false)
                        }
                    }
                }, LoginToken.getToken(applicationContext))
                task.execute(USGS_REQUEST_URL.URL_JOINED)
            } else {
                handler?.sendEmptyMessage(MSG_SUCCESS)
            }

        }

        fun getBitmapList(str: String?, imageView: ImageView, context: Context, key: String): Boolean {

            Log.d("$LOG_TAG/key", key)
            if (!URLUtil.isValidUrl(str) || str == null || str == " ")
                return true
            val realm = DatabaseHelpUtils.getRealmDefault(context)
            try {
                var urlToBytes = realm.where(UrlToBytes::class.java).equalTo("key", key).findFirst()

                Log.d("RealmTransaction", "begin $LOG_TAG")
                realm.beginTransaction()
                if (urlToBytes == null) {
                    Log.d("$LOG_TAG/key", "create $key")
                    urlToBytes = realm.createObject(UrlToBytes::class.java, key)
                }
                if (!urlToBytes!!.url.equals(str)) {
                    Log.d("$LOG_TAG/key", "clear byte array because url is changed ")
                    urlToBytes.byteArray = null
                }

                Log.d("RealmTransaction", "commit $LOG_TAG")
                realm.commitTransaction()

                if (urlToBytes.byteArray == null || !urlToBytes.url.equals(str)) {
                    Log.d("$LOG_TAG/key", "try to update data:$str")
                    realm.beginTransaction()
                    urlToBytes.url = str
                    realm.commitTransaction()
                    val url = URL(str)

                    val taskProfile = BitmapTask(HandlerUpdateProfile(context, imageView, urlToBytes))

                    taskProfile.execute(url.toString())
                } else {
                    val bytes = urlToBytes.byteArray
                    Log.d(LOG_TAG, "Already bitmap is saved")
                    val bitmap = BitmapFactory.decodeStream(bytes!!.inputStream())
                    setImageView(bitmap, imageView)
                }
            } catch (e: MalformedURLException) {
                Log.d("$LOG_TAG/error", "Photo url is not valid")

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (realm.isInTransaction) {
                    realm.commitTransaction()
                    Log.d("RealmTransaction", "commit $LOG_TAG")
                }
            }
            return false
        }

        fun setImageView(bitmap: Bitmap, imageView: ImageView) {
            imageView.setImageBitmap(bitmap)
//            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
//            imageView.background = ShapeDrawable(OvalShape())
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                imageView.clipToOutline = true
//            }
//            else{
//                imageView.setImageDrawable(RoundImage(bitmap))
//            }
        }

    }

    private class HandlerUpdateProfile(var context: Context?, var imageView: ImageView, var urlToBytes: UrlToBytes) : Handler() {

        override fun handleMessage(msg: Message) {
            if (context != null) {
                when (msg.what) {
                    MSG_SUCCESS -> {
                        if (msg.obj is ByteArray) {
                            val bytes = msg.obj as ByteArray
                            val bitmap = BitmapFactory.decodeStream(bytes.inputStream())
                            setImageView(bitmap, imageView)

                            val realm = getRealmDefault(context!!)

                            Log.d("RealmTransaction", "begin $LOG_TAG")
                            realm.beginTransaction()
                            urlToBytes.byteArray = bytes
                            realm.commitTransaction()
                            Log.d("RealmTransaction", "commit $LOG_TAG")

                        }
                    }
                    else -> {
                        Log.d("$LOG_TAG/Error", msg.data.getString("message"))
                    }
                }
            }
        }
    }

}