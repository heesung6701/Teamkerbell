package org.teamfairy.sopt.teamkerbell.network.fcm

import android.os.AsyncTask
import android.util.Log
import org.teamfairy.sopt.teamkerbell.network.NetworkTask
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.StatusCode
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.NoRouteToHostException
import java.net.URL

/**
 * Created by lumiere on 2018-02-17.
 */
class FcmSendMessageTask : AsyncTask<String, Void, String>() {
    private val LOG_TAG = NetworkTask::class.java.simpleName
    var message: String = "No Message"


    private  val URL_FCM_SEND = "https://fcm.googleapis.com/fcm/send"
    private  val FCM_MESSAGE_KEY = "AAAAP98voPY:APA91bEkm0NL1S2rjr5YCZTGxtmMafddBfxcN1SXakkZCL-dT4c36jV6V37nfWGWQ8NpoiSMv7KkrasibcqplDoZepZuSDr0kqTr6iuJDwa_Y1IrhuXl0HUcJ_d-H1ygOQkbpFcTaHLd"

    override fun doInBackground(vararg p0: String): String? {
        return makeHttpRequest(*p0)
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

    }

    private fun createUrl(vararg params: String): URL? {
        val url: URL?
        try {
            url = URL(params[0])
        } catch (e: MalformedURLException) {
            Log.e(LOG_TAG, "Error with creating URL", e)
            return null
        }

        return url
    }
    @Throws(IOException::class)
    private fun makeHttpRequest(vararg params: String): String? {
        var jsonResponse: String?=null
        var inputStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {

            Log.d("$LOG_TAG/REQUEST_URL", URL_FCM_SEND)

            val url = createUrl(URL_FCM_SEND)
            urlConnection = url!!.openConnection() as HttpURLConnection

            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setRequestProperty("Accept", "application/json")
            urlConnection.setRequestProperty("Authorization", "key=$FCM_MESSAGE_KEY")
            urlConnection.connectTimeout = 3000
            urlConnection.readTimeout = 3000


            urlConnection.doOutput = true


            urlConnection.connect()

            val os = urlConnection.outputStream
            val osw = OutputStreamWriter(os, "UTF-8")
            osw.write(params[0])
            osw.flush()


            try {

                if (urlConnection.responseCode / 100 == 2) {
                    inputStream = urlConnection.inputStream
                } else {
                    inputStream = urlConnection.errorStream
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(LOG_TAG + "/HTTP_ERROR", "")
                inputStream = urlConnection.errorStream
            }

            jsonResponse = readFromStream(inputStream)
            Log.d(LOG_TAG + "/RESPONSE", jsonResponse)
            osw.close()

            Log.d("$LOG_TAG/STATUS", urlConnection.responseCode.toString())
            Log.d("$LOG_TAG/MSG", urlConnection.responseMessage)


            urlConnection.disconnect()
        } catch (e: NoRouteToHostException) {
            Log.d("$LOG_TAG/Error", e.toString())
            e.printStackTrace()
        } catch (e: Exception) {
            Log.d("$LOG_TAG/Error", e.toString())
            e.printStackTrace()
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect()
            }
            if (inputStream != null) {
                inputStream.close()
            }
        }

        return jsonResponse
    }

    @Throws(IOException::class)
    private fun readFromStream(inputStream: InputStream?): String {
        val output = StringBuilder()
        if (inputStream != null) {
            val inputStreamReader = InputStreamReader(inputStream)
            val reader = BufferedReader(inputStreamReader)
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line)
                line = reader.readLine()
            }
            reader.close()
        }
        return output.toString()
    }

    companion object {
        fun makeNotificationMessage(to : String,title : String, body : String,g_idx: Int,room_idx: Int) : String{
            val jsonParam = JSONObject()

            try {
                jsonParam.put("to", to)

                val data = JSONObject()
                data.put("data", StatusCode.chatMessage)
                data.put("body",body)
                data.put("title",title)
                data.put("g_idx",g_idx)
                data.put("room_idx",room_idx)

                jsonParam.put("data",data)

            } catch (e: Exception) {
                e.printStackTrace()
            }


            return jsonParam.toString()
        }
        fun makeDataMessage(to : String,title : String, body : String,g_idx: Int,room_idx: Int) : String{
            val jsonParam = JSONObject()

            try {
                jsonParam.put("to", to)

                val data = JSONObject()
                data.put("data",StatusCode.chatMessage)
                data.put("body",body)
                data.put("title",title)
                data.put("g_idx",g_idx)
                data.put("room_idx",g_idx)
                jsonParam.put("data", data)

            } catch (e: Exception) {
                e.printStackTrace()
            }


            return jsonParam.toString()
        }

    }
}