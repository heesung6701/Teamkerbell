package org.teamfairy.sopt.teamkerbell.network

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by lumiere on 2018-01-04.
 */
object NetworkState {



    private const val WIFE_STATE = "WIFE"
    private const val MOBILE_STATE = "MOBILE"
    const val NONE_STATE = "NONE"


    fun getWhatKindOfNetwork(context: Context): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                return WIFE_STATE
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                return MOBILE_STATE
            }
        }
        return NONE_STATE
    }


}
