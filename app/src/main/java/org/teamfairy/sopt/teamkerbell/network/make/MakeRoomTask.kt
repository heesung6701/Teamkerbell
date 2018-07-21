package org.teamfairy.sopt.teamkerbell.network.make

import android.content.Context
import android.os.Handler
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.TagUtils
import org.teamfairy.sopt.teamkerbell.model.data.Room
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CTRL_NAME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DATA
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_G_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_PHOTO
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_REAL_NAME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_NO_INTERNET
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_NO_INTERNET_STR
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-01-05.
 */


class MakeRoomTask(context: Context, handler: Handler, token : String?,var g_idx : Int): GetMessageTask(context,handler,token) {

    override  fun extractFeatureFromJson(jsonResponse: String) : Room? {
        try {

            val baseJsonResponse = JSONObject(jsonResponse)


            if (baseJsonResponse.has(JSON_MESSAGE)) {
                message = baseJsonResponse.getString(JSON_MESSAGE)
                if (message.contains("Success") || message.contains("success"))
                    msgCode= MSG_SUCCESS
                else if (message.contains(MSG_NO_INTERNET_STR)) {
                    msgCode = MSG_NO_INTERNET
                    Toast.makeText(context,"인터넷 연결상태를 확인해주세요", Toast.LENGTH_SHORT).show()
                }
                else if (message.contains("Failed") || message.contains("failed")) {
                    msgCode = MSG_FAIL
                    Toast.makeText(context,"잠시후 다시 도전 해주세요", Toast.LENGTH_SHORT).show()
                }
            }


            if(baseJsonResponse.has(JSON_DATA)) {
                val data = baseJsonResponse.getJSONObject(JSON_DATA)

                val room = Room(g_idx,
                        data.getInt(JSON_ROOM_IDX),
                        data.getString(JSON_REAL_NAME),
                        data.getString(JSON_CTRL_NAME))
                if (data.has(JSON_PHOTO))
                    room.photo = data.getString(JSON_PHOTO)

                return room
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }


}