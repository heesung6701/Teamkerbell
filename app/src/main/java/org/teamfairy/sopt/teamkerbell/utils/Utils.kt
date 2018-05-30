package org.teamfairy.sopt.teamkerbell.utils

import android.view.View
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lumiere on 2018-04-28.
 */
class Utils {
    companion object {
        const val TAB_CONTACT = 0
        const val TAB_ROOM = 1
        const val TAB_HOME = 2


        const val TAB_UNPERFORMED_NOTICE = 0
        const val TAB_UNPERFORMED_SIGNAL = 1
        const val TAB_UNPERFORMED_VOTE = 2


        const val TAB_RECEIVE = 0
        const val TAB_REQUEST = 1

        const val TAB_NOTICE = 0
        const val TAB_LIGHT = 1
        const val TAB_PICK = 2
        const val TAB_VOTE = 3


        const val TAB_GREEN = 0
        const val TAB_YELLOW = 1
        const val TAB_RED = 2


        const val TAB_BYEXAMPLE = 0
        const val TAB_BYMEMBER = 1
        const val TAB_NOTYET = 2

        const val SIGNAL_RECEIVER = 1
        const val SIGNAL_SENDER = 2
        const val SIGNAL_ALL = 3


        const val VOTE_RECEIVER = 1
        const val VOTE_SENDER = 2
        const val VOTE_ALL = 3


        const val MSG_SUCCESS: Int = 1
        const val MSG_NO_INTERNET: Int = 2
        const val MSG_FAIL: Int = 3

        const val MSG_NO_INTERNET_STR = "NO NETWORK"
        const val MSG_FAIL_STR = "CONNECT FAIL"


        const val RESPONSE_RECEIVE = 0
        const val RESPONSE_REQUEST = 1
        const val RESPONSE_MAKE = 2
        const val RESPONSE_READONLY = 3
        const val RESPONSE_FOR_CHECK = 4

        const val OPEN_STATUS_SECRET: Int = 0
        const val OPEN_STATUS_OPEN: Int = 1


        const val ENTIRE_STATUS_ENTIRE: Int = 1
        const val ENTIRE_STATUS_CHOSE: Int = 0


        fun getNow(): String {

            val now = System.currentTimeMillis()
            val date = Date(now)

            val sdfNow = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)

            return sdfNow.format(date)
        }

        fun getNowForFirebase(): String {

            val now = System.currentTimeMillis()
            val date = Date(now)


            val sdfNow = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)

            return sdfNow.format(date)
        }

        fun getNowToTime(txt: String): String {

            return txt.substring("yyyy-MM-dd ".length, "yyyy-MM-dd HH:mm".length)
        }


        fun getNowToDateTime(txt: String): String {

            return txt.substring("yyyy-".length, "yyyy-MM-dd HH:mm".length)
        }

        fun getYearMonthDay(txt: String): String {
            val y = txt.substring(0, "yyyy".length).toInt().toString()
            val m = txt.substring("yyyy-".length, "yyyy-MM".length).toInt().toString()
            val d = txt.substring("yyyy-MM-".length, "yyyy-MM-dd".length).toInt().toString()

            return "$y. $m. $d"
        }

        fun getMonthDayTime(txt: String): String {
            if(txt.isBlank() || txt.isEmpty()) return ""

            return txt.substring("yyyy-".length, "yyyy-MM".length).toInt().toString() + "월" +
                    txt.substring("yyyy-MM-".length, "yyyy-MM-dd".length).toInt().toString() + "일" +
                    txt.substring("yyyy-MM-dd".length, "yyyy-MM-dd HH:mm".length)
        }
    }

}