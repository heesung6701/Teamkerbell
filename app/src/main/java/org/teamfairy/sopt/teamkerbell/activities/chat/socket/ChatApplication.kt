package org.teamfairy.sopt.teamkerbell.activities.chat.socket

import android.app.Application
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.teamfairy.sopt.teamkerbell.activities.chat.ChatActivity
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import java.net.URISyntaxException
import java.net.URL

class ChatApplication {


    companion object {

        fun getSocket(g_idx : Int): Socket? {

            val mSocket: Socket?
            try {
                val opts: IO.Options = IO.Options()
                val port: Int = USGS_REQUEST_URL.URL_SOCKET.substringAfterLast(':').toInt()

                opts.port = port


//                val url :String = "${USGS_REQUEST_URL.URL_SOCKET}?ns=$g_idx"
                val url :String = USGS_REQUEST_URL.URL_SOCKET

                Log.d(ChatActivity::class.java.simpleName+"/Socket", "포트 번호 : $port")
                Log.d(ChatActivity::class.java.simpleName+"/Socket", "URL : $url")
                mSocket = IO.socket(url, opts)


            } catch (e: URISyntaxException) {
                throw e;
            }
            Log.d(ChatActivity::class.java.simpleName+"/Socket", "연결 성공")
            return mSocket
        }
    }
}