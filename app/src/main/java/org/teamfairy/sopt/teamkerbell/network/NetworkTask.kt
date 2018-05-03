package org.teamfairy.sopt.teamkerbell.network

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_INVITE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LEAVE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_GROUP
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_LIGHT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_VOTE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MODIFY_ROLE_USER
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MODIFY_TASK
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_PROFILE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REGIST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_LIGHTS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_PRESS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_VOTE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_REGISTER
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_REGISTER_FEEDBACK
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_RESULT_REGISTER
import java.io.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.TagUtils
import org.teamfairy.sopt.teamkerbell.utils.Utils
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL_STR
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_NO_INTERNET_STR
import java.net.*
import java.nio.charset.Charset


/**
 * Created by lumiere on 2018-01-04.
 */
open class NetworkTask : AsyncTask<String, Void, String> {

    val LOG_TAG = NetworkTask::class.java.simpleName


    var files : ArrayList<File> = ArrayList<File>()
    var file : File?=null
    internal var token: String? = null
    internal var context: Context

    var method : String ? =null

    constructor(context: Context, token: String?) {
        this.token = token
        this.context = context
    }

    constructor(context: Context) : this(context, null)

    override fun doInBackground(vararg params: String): String? {
        var jsonResponse: String? = null
        if (NetworkState.getWhatKindOfNetwork(context) === NetworkState.NONE_STATE) {
            return "{\"message\":\"${MSG_NO_INTERNET_STR}\"}"
        }
        try {
            if (params[0] == URL_REGIST ||
                    params[0] == URL_LOGIN ||
                    params[0] == URL_MAKE_NOTICE ||
                    params[0] == URL_MAKE_LIGHT ||
                    params[0] == URL_MAKE_VOTE ||
                    params[0] == URL_INVITE ||
                    params[0] == URL_RESPONSE_LIGHTS ||
                    params[0] == URL_RESPONSE_PRESS ||
                    params[0] == URL_RESPONSE_NOTICE ||
                    params[0]==URL_ROLE_REGISTER ||
                    params[0]== URL_ROLE_REGISTER_FEEDBACK)
             {
                 method="POST"
                jsonResponse = makeHttpRequestPost(*params)
            }else if( params[0] == URL_RESPONSE_VOTE ||
                params[0]== URL_MODIFY_TASK ||
                    params[0]== URL_MODIFY_ROLE_USER ){
                method="PUT"
                jsonResponse = makeHttpRequestPost(*params)
            }else if (params[0] == URL_LEAVE) {
                jsonResponse = makeHttpRequestDelete(*params)

            } else if (params[0] == URL_PROFILE) {
                method="PUT"
                jsonResponse = makeHttpRequestFormData(*params)
            } else if (params[0] == URL_MAKE_GROUP ||
                    params[0]==URL_ROLE_RESULT_REGISTER ) {
                method="POST"
                jsonResponse = makeHttpRequestFormData(*params)
            } else {//get method
                method="GET"
                jsonResponse = makeHttpRequestGet(*params)
                //URL_REGIST_CHECK
                //URL_SHOW_GROUPLIST
                //URL_USERLIST_LIGHT
                //URL_GROUP_PICK
                //URL_DETAIL_LIGHTS_RESPONSE
                //URL_UNPERFORMED
                //URL_DETAIL_VOTE_RESPONSE
                // URL_JOINED
                // URL_USER
                //URL_ROLE_SHOW_TASK
                //URL_ROLE_SHOW_RESPONSE
                //URL_ROLE_SHOW_USER
                //URL_NEWINFO
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (jsonResponse == null) jsonResponse = "{\"message\" : \"${MSG_FAIL_STR}\"}"
        return jsonResponse
    }


    private fun createUrl(vararg params: String): URL? {
        val url: URL
        try {
            url = URL(params[0])
        } catch (e: MalformedURLException) {
            Log.e(LOG_TAG, "Error with creating URL", e)
            return null
        }

        return url
    }

    @Throws(IOException::class)
    private fun makeHttpRequestPost(vararg params: String): String? {
        var jsonResponse: String? = null
        var inputStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {

            Log.d("$LOG_TAG/REQUEST_URL", params[0])
            Log.d("$LOG_TAG/REQUEST_JSON", params[1])
            val url = createUrl(*params)
            urlConnection = url!!.openConnection() as HttpURLConnection
            urlConnection.requestMethod = method
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setRequestProperty("Accept", "application/json")
            urlConnection.connectTimeout = 3000
            urlConnection.readTimeout = 3000
            if (token != null)
                urlConnection.setRequestProperty("token", token!!)
            urlConnection.doOutput = true


            urlConnection.connect()

            val os = urlConnection.outputStream
            val osw = OutputStreamWriter(os, "UTF-8")
            osw.write(params[1])
            osw.flush()


            try {

                if (urlConnection.responseCode / 100 == 2) {
                    inputStream = urlConnection.inputStream
                } else {
                    inputStream = urlConnection.errorStream
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("$LOG_TAG/HTTP_ERROR", "")
                inputStream = urlConnection.errorStream
            }

            jsonResponse = readFromStream(inputStream)
            Log.d("$LOG_TAG/RESPONSE", jsonResponse)
            osw.close()


            Log.d("$LOG_TAG/STATUS", urlConnection.responseCode.toString())
            Log.d("$LOG_TAG/MSG", urlConnection.responseMessage)

            if (jsonResponse.contains("UnauthorizedError: invalid token") || jsonResponse.contains("UnauthorizedError: invalid signature")) {
                //   LoginToken.logout(context, LoginToken.STATE_TOKEN_OVER)
            }


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
        if (jsonResponse != null) {
            if (jsonResponse.contains("jwt expired")) {
                //     LoginToken.logout(context, LoginToken.STATE_TOKEN_OVER)
            }
        }

        return jsonResponse
    }

    @Throws(IOException::class)
    private fun makeHttpRequestGet(vararg params: String): String? {
        var jsonResponse: String? = null
        var inputStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = createUrl(*params)
            urlConnection = url!!.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 3000
            urlConnection.readTimeout = 3000

            if (token != null)
                urlConnection.setRequestProperty("token", token!!)

            Log.d("$LOG_TAG/REQUEST_URL", params[0])
            urlConnection.connect()


            try {

                if (urlConnection.responseCode / 100 == 2) {
                    inputStream = urlConnection.inputStream
                } else {
                    inputStream = urlConnection.errorStream
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(LOG_TAG + "/HTTP_ERROR", urlConnection.responseCode.toString() + "")
                inputStream = urlConnection.errorStream
            }

            jsonResponse = readFromStream(inputStream)
            Log.d("$LOG_TAG/RESPONSE", jsonResponse)

            Log.d("$LOG_TAG/STATUS", urlConnection.responseCode.toString())
            Log.d("$LOG_TAG/MSG", urlConnection.responseMessage)
            if (jsonResponse.contains("UnauthorizedError: invalid token") || jsonResponse.contains("UnauthorizedError: invalid signature"))


                urlConnection.disconnect()
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
        if (jsonResponse != null) {
            if (jsonResponse.contains("jwt expired")) {
            }
        }
        return jsonResponse
    }


    @Throws(IOException::class)
    private fun makeHttpRequestDelete(vararg params: String): String? {
        var jsonResponse: String? = null
        var inputStream: InputStream? = null

        var urlConnection: HttpURLConnection? = null
        try {
            val url = createUrl(*params)
            urlConnection = url!!.openConnection() as HttpURLConnection

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                urlConnection.requestMethod = "DELETE"
            }else{
                urlConnection.setRequestProperty("X-HTTP-Method-Override", "DELETE");
                urlConnection.requestMethod = "POST"
            }
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setRequestProperty("Accept", "application/json")
            urlConnection.connectTimeout = 3000
            urlConnection.readTimeout = 3000
            if (token != null)
                urlConnection.setRequestProperty("token", token!!)

            urlConnection.doOutput = true


            Log.d("$LOG_TAG/REQUEST_URL", params[0])
            Log.d("$LOG_TAG/REQUEST_JSON", params[1])
            urlConnection.connect()

            val os = urlConnection.outputStream
            val osw = OutputStreamWriter(os, "UTF-8")
            osw.write(params[1])
            osw.flush()

            try {

                if (urlConnection.responseCode / 100 == 2) {
                    inputStream = urlConnection.inputStream
                } else {
                    inputStream = urlConnection.errorStream
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("$LOG_TAG/HTTP_ERROR", "")
                inputStream = urlConnection.errorStream
            }

            jsonResponse = readFromStream(inputStream)
            Log.d("$LOG_TAG/RESPONSE", jsonResponse)
            osw.close()


            Log.d("$LOG_TAG/STATUS", urlConnection.responseCode.toString())
            Log.d("$LOG_TAG/MSG", urlConnection.responseMessage)

            if (jsonResponse.contains("UnauthorizedError: invalid token") || jsonResponse.contains("UnauthorizedError: invalid signature")) {
                //   LoginToken.logout(context, LoginToken.STATE_TOKEN_OVER)
            }


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
        if (jsonResponse != null) {
            if (jsonResponse.contains("jwt expired")) {
                //     LoginToken.logout(context, LoginToken.STATE_TOKEN_OVER)
            }
        }

        return jsonResponse
    }




    private val crlf = "\r\n"
    private val twoHyphens = "--"
    private val boundary: String = "*****"
    private var charset : Charset = Charsets.UTF_8
    @Throws(IOException::class)
    private fun makeHttpRequestFormData(vararg params: String): String? {
        var jsonResponse: String? = null
        var inputStream: InputStream? = null

        var urlConnection: HttpURLConnection? = null

        val os: OutputStream
        val request: DataOutputStream
        try {

            val url = createUrl(*params)
            Log.d("$LOG_TAG/REQUEST_URL", params[0])

            urlConnection = url!!.openConnection() as HttpURLConnection

            urlConnection.doOutput = true
            urlConnection.requestMethod = method

            urlConnection.doInput = true

            urlConnection.useCaches = false
            urlConnection.setRequestProperty("cache-control", "no-cache")
            urlConnection.setRequestProperty("accept","*/*")
            urlConnection.setRequestProperty("accept-encoding","gzip, deflate")
            urlConnection.setRequestProperty(
                    "content-type", "multipart/form-data;boundary=" + this.boundary);
            urlConnection.setRequestProperty("connection", "keep-alive")



            try {
                urlConnection.requestProperties.iterator().forEach {
                    Log.d(LOG_TAG, it.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }



            if (token != null)
                urlConnection.setRequestProperty("token", token!!)

            os = urlConnection.outputStream
            request = DataOutputStream(urlConnection.outputStream)

            val jsonObj =JSONObject(params[1])
            Log.d(LOG_TAG,params[1])
            if(jsonObj.has("name")){
                addFormField(request,"name",jsonObj.getString("name"))
                Log.d(LOG_TAG,"name/"+jsonObj.getString("name"))
            }
            if(jsonObj.has("bio")){
                addFormField(request,"bio",jsonObj.getString("bio"))
                Log.d(LOG_TAG,"bio/"+jsonObj.getString("bio"))
            }
            if(jsonObj.has("phone")){
                addFormField(request,"phone",jsonObj.getString("phone"))
                Log.d(LOG_TAG,"phone/"+jsonObj.getString("phone"))
            }
            if(file!=null){
                addFilePart(request,os,"photo",file!!)
                Log.d(LOG_TAG,"file/"+file.toString())
            }
            if (jsonObj.has("role_idx")){
                addFormField(request, "role_idx", jsonObj.getString("role_idx"))
                Log.d(LOG_TAG, "role_idx/" + jsonObj.getString("role_idx"))
            }
            if(jsonObj.has("role_task_idx")){
                addFormField(request, "role_task_idx", jsonObj.getString("role_task_idx"))
                Log.d(LOG_TAG, "role_task_idx/" + jsonObj.getString("role_task_idx"))
            }
            if (jsonObj.has("response_content")){
                addFormField(request, "response_content", jsonObj.getString("response_content"))
                Log.d(LOG_TAG, "content/" + jsonObj.getString("response_content"))
            }
            if(files.size>0){
                files.forEach {
                    Log.d(LOG_TAG, "file/" + it.toString())
                    addFilePart(request, os, "file", it)
                }

            }

            //content wrapper 종료
            addFormFinish(request)


            urlConnection.connect()
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
            Log.d("$LOG_TAG/RESPONSE", jsonResponse)

            if (os != null) os.close()
            request.close()


            Log.d("$LOG_TAG/STATUS", urlConnection.responseCode.toString())
            Log.d("$LOG_TAG/MSG", urlConnection.responseMessage)

            urlConnection.disconnect()

            if(urlConnection.responseCode/100==2) {
                if (file != null) {
                    file!!.delete()
                }
                files.forEach {
                    it.delete()
                }
            }

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

    private fun addFormFinish(request: DataOutputStream){
        request.writeBytes(this.twoHyphens + this.boundary +
                this.twoHyphens + this.crlf)
        request.flush()
    }


    private fun addFormField(request : DataOutputStream, name: String, value: String) {

        request.writeBytes(this.twoHyphens + this.boundary + this.crlf)

        request.writeBytes("Content-Disposition: form-data; name=\"" +
                name + "\";" + this.crlf)
        request.writeBytes("Content-Type: text/plain; charset=" + charset.toString()+this.crlf+this.crlf);
        request.write(value.toByteArray(charset))
        request.writeBytes(this.crlf)
    }

     private fun addFilePart(request:DataOutputStream, os : OutputStream, fieldName: String, uploadFile: File) {

         val fileName = uploadFile.name
         request.writeBytes(this.twoHyphens + this.boundary + this.crlf)
         request.writeBytes("Content-Disposition: form-data; name=\"" +
                 fieldName + "\";filename=\"" +
                 fileName + "\"" + this.crlf)
         Log.d("$LOG_TAG/content-type",URLConnection.guessContentTypeFromName(fileName))
         request.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)+this.crlf)
         request.writeBytes("Content-Transfer-Encoding: binary"+this.crlf)

         request.writeBytes(this.crlf)



         Log.d("$LOG_TAG/file",uploadFile.readText(Charset.defaultCharset()))
         val buffer = ByteArray(4096)
         var bytesRead :Int
         var inputStream : FileInputStream? = null
         try {
             inputStream = FileInputStream(uploadFile)
             while (true) {
                 bytesRead = inputStream.read(buffer)
                 Log.d("$LOG_TAG/file",bytesRead.toString())
                 if(bytesRead==-1) break
                 os.write(buffer, 0, bytesRead)
             }

             os.flush()
         }catch ( e : Exception){
             e.printStackTrace()
         }
         if(inputStream!=null) inputStream.close()
         request.writeBytes(this.crlf)
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


}
