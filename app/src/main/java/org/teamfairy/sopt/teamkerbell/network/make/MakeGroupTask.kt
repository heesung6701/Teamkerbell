package org.teamfairy.sopt.teamkerbell.network.make

import android.content.Context
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.json.JSONException
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_CTRL_NAME
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DATA
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_DEFAULT_ROOM_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_G_IDX
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_MESSAGE
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL.JSON_REAL_NAME
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_FAIL
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_NO_INTERNET
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_NO_INTERNET_STR
import org.teamfairy.sopt.teamkerbell.utils.Utils.Companion.MSG_SUCCESS
import kotlin.properties.Delegates

/**
 * Created by lumiere on 2018-01-05.
 */


//아이디 중복확인
//회원가입
class MakeGroupTask(context: Context, handler: Handler, token : String?): GetMessageTask(context,handler,token) {

    override  fun extractFeatureFromJson(jsonResponse: String) : Any? {
        var group : Team by Delegates.notNull()
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

                group = Team(data.getInt(JSON_G_IDX),
                        data.getString(JSON_REAL_NAME),
                        data.getString(JSON_CTRL_NAME),
                        data.getString(JSON_DEFAULT_ROOM_IDX))
                if (data.has(Team.ARG_PHOTO))
                    group.photo = data.getString(Team.ARG_PHOTO)

                return group
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }


}