package org.teamfairy.sopt.teamkerbell.model.realm

import android.content.Context
import android.util.Log
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils.Companion.getRealmDefault

/**
 * Created by lumiere on 2018-01-01.
 */
open class BadgeCnt() : RealmObject(){
    var what: Int=-1
    var g_idx : Int = -1
    var cnt : Int = 0

    companion object {
        const  val ARG_WHAT = "what"
        const  val ARG_G_IDX = "g_idx"

        const val WHAT_NOTICE =0
        const val WHAT_SIGNAL  = 1
        const val WHAT_VOTE =2
        const val WHAT_ROLE  = 3

        fun increase(applicationContext : Context, key : Int, g_idx: Int){

            val realm = getRealmDefault(applicationContext)

            var badgeCnt : BadgeCnt? = realm.where(BadgeCnt::class.java).equalTo(BadgeCnt.ARG_WHAT,key).equalTo(ARG_G_IDX,g_idx).findFirst()

            realm.beginTransaction()
            if(badgeCnt==null) {
                Log.d("BadgeCnt", "[key:$key,g_idx: $g_idx] create")
                badgeCnt= realm.createObject(BadgeCnt::class.java)
                badgeCnt!!.what=key
                badgeCnt!!.g_idx=g_idx
            }
            badgeCnt.cnt+=1
            realm.commitTransaction()
            Log.d("BadgeCnt", "[key:$key,g_idx: $g_idx] increase - > ${badgeCnt.cnt}")
            realm.close()


        }
        fun clear(applicationContext : Context, key : Int, g_idx: Int){
            val realm = getRealmDefault(applicationContext)

            var badgeCnt : BadgeCnt? = realm.where(BadgeCnt::class.java).equalTo(BadgeCnt.ARG_WHAT,key).equalTo(ARG_G_IDX,g_idx).findFirst()

            realm.beginTransaction()
            if(badgeCnt==null) {
                Log.d("BadgeCnt", "[key:$key,g_idx: $g_idx] create")
                badgeCnt= realm.createObject(BadgeCnt::class.java)
                badgeCnt!!.what=key
                badgeCnt!!.g_idx=g_idx
            }
            badgeCnt.cnt=0
            realm.commitTransaction()
            Log.d("BadgeCnt", "[key:$key,g_idx: $g_idx] clear")
            realm.close()
        }
        fun getCount(applicationContext: Context, key : Int, g_idx: Int) : Int{
            val realm = getRealmDefault(applicationContext)


            var badgeCnt : BadgeCnt? = realm.where(BadgeCnt::class.java).equalTo(BadgeCnt.ARG_WHAT,key).equalTo(ARG_G_IDX,g_idx).findFirst()

            if(badgeCnt==null) {
                Log.d("BadgeCnt", "[key:$key,g_idx: $g_idx] create")
                realm.beginTransaction()
                badgeCnt= realm.createObject(BadgeCnt::class.java)
                badgeCnt!!.what=key
                badgeCnt!!.g_idx=g_idx
                realm.commitTransaction()
            }

            Log.d("BadgeCnt", "[key:$key,g_idx: $g_idx] ${badgeCnt.cnt}")
            val c = badgeCnt.cnt ?: 0
            realm.close()
            return c
        }
    }
}
