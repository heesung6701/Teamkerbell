package org.teamfairy.sopt.teamkerbell.network

import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import android.util.Log
import android.webkit.URLUtil
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS
import java.io.InputStream
import java.net.URL

/**
 * Created by lumiere on 2018-01-06.
 */
class BitmapTask(var handler: Handler) : AsyncTask<String, Void, ByteArray>() {

    val LOG_TAG = this::class.java.simpleName
    var msgCode = MSG_FAIL

    override fun doInBackground(vararg params: String?): ByteArray? {
        var inputStream: InputStream? = null
        if (!URLUtil.isValidUrl(params[0])) {
            msgCode = MSG_FAIL
            return null
        }
        val url = URL(params[0])

        Log.d(LOG_TAG.plus("_URL"), url.toString())

        for (i in 1..3) {
            try {
                inputStream = url.openStream() as InputStream
                msgCode = MSG_SUCCESS
            } catch (e: Exception) {
                Log.d(LOG_TAG.plus("_ERROR"), e.toString())
                e.printStackTrace()
            }
        }

        return inputStream?.readBytes()
    }

    override fun onPostExecute(result: ByteArray?) {
        super.onPostExecute(result)

        val msg = Message()
        msg.obj = result
        msg.what = msgCode
        Log.d(NetworkTask::class.java.simpleName, "get Bitmap Message" + if (msgCode == MSG_SUCCESS) "Success" else " failed")

        handler.sendMessage(msg)
    }
}
