package org.teamfairy.sopt.teamkerbell.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import org.teamfairy.sopt.teamkerbell.model.data.Team

/**
 * Created by lumiere on 2018-05-10.
 */
class CurrentGroup {
    companion object {
        private const val PREF_GROUP = "pref_group"

        private var team: Team? = null

        fun setGroup(group: Team){
            this.team= group
        }
        fun getGroup(context: Context): Team {
            if (team == null) getPref(context)
            return team!!
        }

        fun getPref(context: Context) {

            val pref = context.getSharedPreferences(PREF_GROUP, MODE_PRIVATE)

            val g = Team(pref.getInt(Team.ARG_G_IDX, 0),
                    pref.getString(Team.ARG_REAL_NAME, "그룹0"),
                    pref.getString(Team.ARG_CTRL_NAME, null),
                    pref.getString(Team.ARG_PHOTO, null),
                    pref.getInt(Team.ARG_DEFAULT_ROOM_IDX,-1))

            team = g

        }

        fun setPref(context: Context, group: Team) {
            Companion.team = group
            setPref(context)

        }

        private fun setPref(context: Context) {

            if (team == null) return

            val pref = context.getSharedPreferences(PREF_GROUP, MODE_PRIVATE).edit()
            val g = team!!

            Log.d("CurrentGroup/", g.toString())
            pref.putInt(Team.ARG_G_IDX, g.g_idx)
            pref.putString(Team.ARG_REAL_NAME, g.real_name)
            pref.putString(Team.ARG_CTRL_NAME, g.ctrl_name)
            pref.putString(Team.ARG_PHOTO, g.photo)

            pref.apply()

        }
    }
}