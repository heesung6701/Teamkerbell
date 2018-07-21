package org.teamfairy.sopt.teamkerbell.network

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LOGIN
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_GROUP
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_VOTE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_PROFILE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_REGIST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_LIGHTS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_PRESS
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_RESPONSE_VOTE
import java.io.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_BIO
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_FILE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_G_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_NAME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_PHONE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_PHOTO
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_RESPONSE_CONTENT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROLE_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_TASK_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_USER_ARRAY
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_GROUP_NOTICE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_INVITE_GROUP
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_INVITE_ROOM
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LEAVE_GROUP
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_LEAVE_ROOM
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_ROOM
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_MAKE_SIGNAL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_FEEDBACK_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_RESPONSE_POST
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_RESPONSE_PUT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_TASK_PUT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.URL_ROLE_USER_PUT
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
    var photo : File?=null
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
            return "{\"message\":\"$MSG_NO_INTERNET_STR\"}"
        }
        try {
            if (params[0] == URL_REGIST ||
                    params[0] == URL_LOGIN ||
                    params[0] == URL_MAKE_NOTICE ||
                    params[0] == URL_MAKE_SIGNAL ||
                    params[0] == URL_MAKE_VOTE ||
                    params[0] == URL_INVITE_GROUP ||
                    params[0] == URL_INVITE_ROOM ||
                    params[0] == URL_RESPONSE_LIGHTS ||
                    params[0] == URL_RESPONSE_PRESS ||
                    params[0] == URL_RESPONSE_NOTICE ||
                    params[0]==URL_ROLE_POST ||
                    params[0]== URL_ROLE_FEEDBACK_POST)
             {
                 method="POST"
                jsonResponse = makeHttpRequestPost(*params)
            }else if( params[0] == URL_RESPONSE_VOTE ||
                params[0]== URL_ROLE_TASK_PUT ||
                    params[0]== URL_ROLE_USER_PUT){
                method="PUT"
                jsonResponse = makeHttpRequestPost(*params)
            }else if (params[0] == URL_LEAVE_GROUP ||
                    params[0] == URL_LEAVE_ROOM
                    ) {
                jsonResponse = makeHttpRequestDelete(*params)

            } else if (params[0] == URL_PROFILE ||
                    params[0]== URL_ROLE_RESPONSE_PUT) {
                method="PUT"
                jsonResponse = makeHttpRequestFormData(*params)
            } else if (params[0] == URL_MAKE_GROUP ||
                    params[0] == URL_MAKE_ROOM ||
                    params[0]== URL_ROLE_RESPONSE_POST) {
                method="POST"
                jsonResponse = makeHttpRequestFormData(*params)
            } else {//get method
                method="GET"
                jsonResponse = makeHttpRequestGet(*params)
//                URL_GROUP_NOTICE
//                URL_GROUPLIST
//                URL_ROOMLIST
//                URL_JOINED_GROUP
//                URL_JOINED_ROOM
//                URL_REGIST_CHECK
//                URL_DETAIL_LIGHTS_RESPONSE
//                URL_UNPERFORMED
//                URL_DETAIL_VOTE_RESPONSE
//                URL_USER
//                URL_DETAIL_SINGLE_LIGHT
//                URL_ROLE_GET
//                URL_ROLE_GET
//                URL_ROLE_TASK_GET
//                URL_ROLE_RESPONSE_GET
//                URL_ROLE_FEEDBACK_GET

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


            inputStream = try {

                if (urlConnection.responseCode / 100 == 2) {
                    urlConnection.inputStream
                } else {
                    urlConnection.errorStream
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(LOG_TAG + "/HTTP_ERROR", urlConnection.responseCode.toString() + "")
                urlConnection.errorStream
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

            inputStream = try {

                if (urlConnection.responseCode / 100 == 2) {
                    urlConnection.inputStream
                } else {
                    urlConnection.errorStream
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("$LOG_TAG/HTTP_ERROR", "")
                urlConnection.errorStream
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



            if(jsonObj.has(JSON_G_IDX)){
                addFormField(request, JSON_G_IDX,jsonObj.getString(JSON_G_IDX))
                Log.d(LOG_TAG,"$JSON_G_IDX/"+jsonObj.getString(JSON_G_IDX))
            }

            if(jsonObj.has(JSON_USER_ARRAY)){
                addFormField(request, JSON_USER_ARRAY,jsonObj.getString(JSON_USER_ARRAY))
                Log.d(LOG_TAG,"$JSON_USER_ARRAY/"+jsonObj.getString(JSON_USER_ARRAY))
            }



            //프로필, 그룹 추가
            if(jsonObj.has(JSON_NAME)){
                addFormField(request, JSON_NAME,jsonObj.getString(JSON_NAME))
                Log.d(LOG_TAG,"$JSON_NAME/"+jsonObj.getString(JSON_NAME))
            }
            if(jsonObj.has(JSON_BIO)){
                addFormField(request, JSON_BIO,jsonObj.getString(JSON_BIO))
                Log.d(LOG_TAG,"$JSON_BIO/"+jsonObj.getString(JSON_BIO))
            }
            if(jsonObj.has(JSON_PHONE)){
                addFormField(request, JSON_PHONE,jsonObj.getString(JSON_PHONE))
                Log.d(LOG_TAG,"$JSON_PHONE/"+jsonObj.getString(JSON_PHONE))
            }

            if(photo!=null){
                addFilePart(request,os,JSON_PHOTO,photo!!)
                Log.d(LOG_TAG,"$JSON_PHOTO/"+photo.toString())
            }
            if(file!=null){
                addFilePart(request,os,JSON_FILE,file!!)
                Log.d(LOG_TAG,"$JSON_FILE/"+file.toString())
            }

            //역할추가
            if (jsonObj.has(JSON_ROLE_IDX)){
                addFormField(request, JSON_ROLE_IDX, jsonObj.getString(JSON_ROLE_IDX))
                Log.d(LOG_TAG, "$JSON_ROLE_IDX/" + jsonObj.getString(JSON_ROLE_IDX))
            }
            if(jsonObj.has(JSON_TASK_IDX)){
                addFormField(request, JSON_TASK_IDX, jsonObj.getString(JSON_TASK_IDX))
                Log.d(LOG_TAG, "$JSON_TASK_IDX/" + jsonObj.getString(JSON_TASK_IDX))
            }
            if (jsonObj.has(JSON_RESPONSE_CONTENT)){
                addFormField(request, JSON_RESPONSE_CONTENT, jsonObj.getString(JSON_RESPONSE_CONTENT))
                Log.d(LOG_TAG, "$JSON_RESPONSE_CONTENT/" + jsonObj.getString(JSON_RESPONSE_CONTENT))
            }
            if(files.size>0){
                files.forEach {
                    Log.d(LOG_TAG, "$JSON_FILE/" + it.toString())
                    addFilePart(request, os, JSON_FILE, it)
                }

            }

            //content wrapper 종료
            addFormFinish(request)


            urlConnection.connect()
            inputStream = try {
                if (urlConnection.responseCode / 100 == 2) {
                    urlConnection.inputStream
                } else {
                    urlConnection.errorStream
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("$LOG_TAG/HTTP_ERROR", "")
                urlConnection.errorStream
            }

            jsonResponse = readFromStream(inputStream)
            Log.d("$LOG_TAG/RESPONSE", jsonResponse)

            os?.close()
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
