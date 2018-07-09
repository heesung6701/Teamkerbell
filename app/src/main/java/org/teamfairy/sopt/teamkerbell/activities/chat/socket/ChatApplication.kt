package org.teamfairy.sopt.teamkerbell.activities.chat.socket

import android.app.Application
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.teamfairy.sopt.teamkerbell.activities.chat.ChatActivity
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import java.net.URISyntaxException

class ChatApplication {


    companion object {

        fun getSocket(): Socket? {

            var mSocket: Socket? = null
            try {
                val opts: IO.Options = IO.Options()
                val port: Int = USGS_REQUEST_URL.URL_SOCKET.substringAfterLast(':').toInt()
                opts.port = port;

                Log.d(ChatActivity::class.java.simpleName+"/Socket", "포트 번호 : $port")
                Log.d(ChatActivity::class.java.simpleName+"/Socket", "URL : ${USGS_REQUEST_URL.URL_SOCKET}")
                mSocket = IO.socket(USGS_REQUEST_URL.URL_SOCKET, opts)
            } catch (e: URISyntaxException) {
                throw e;
            }
            Log.d(ChatActivity::class.java.simpleName+"/Socket", "연결 성공")
            return mSocket
        }
    }
}