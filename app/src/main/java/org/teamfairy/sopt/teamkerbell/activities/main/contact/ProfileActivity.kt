package org.teamfairy.sopt.teamkerbell.activities.main.contact

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_back.*
import kotlinx.android.synthetic.main.content_profile.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell.utils.DatabaseHelpUtils
import org.teamfairy.sopt.teamkerbell.utils.FileUtils.Companion.getRealPathFromURI
import org.teamfairy.sopt.teamkerbell.utils.FileUtils.Companion.updatePhoto
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.NetworkTask.Companion.METHOD_PUT
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.NetworkUtils
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

class ProfileActivity : AppCompatActivity() {

    val LOG_TAG = this::class.java.name!!

    var filePhoto: File? = null
    private val SELECT_IMAGE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)

        val user = LoginToken.getUser(applicationContext)

        tv_name.text=user.name
        tv_email.text = user.id
        tv_phone.text  = user.phone

        val url = user.photo
        if (NetworkUtils.getBitmapList(url, img_profile,applicationContext,"user"+user.u_idx))
            img_profile.setImageResource(R.drawable.icon_profile_default)


        img_profile.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    } else {
                        requestExplorer()
                    }
                } else {
                    requestExplorer()
                }
            } else {
                requestExplorer()
            }
        }
        btn_back.setOnClickListener {
            onBackPressed()
        }
    }
    private fun requestExplorer() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, SELECT_IMAGE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    filePhoto = updatePhoto(getRealPathFromURI(data!!.data, contentResolver), img_profile)
                    uploadFIle()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun uploadFIle(){


        val jsonParam = JSONObject()
        try {
            jsonParam.put(USGS_REQUEST_URL.URL_PROFILE_PARAM_NAME, LoginToken.getUser(applicationContext).name)
            jsonParam.put(USGS_REQUEST_URL.URL_PROFILE_PARAM_BIO, LoginToken.getUser(applicationContext).bio)
            jsonParam.put(USGS_REQUEST_URL.URL_PROFILE_PARAM_PHONE, LoginToken.getUser(applicationContext).phone)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val task = GetMessageTask(applicationContext, HandlerGet(this), LoginToken.getToken(applicationContext))
        if(filePhoto!=null) task.photo = filePhoto!!
        task.execute(USGS_REQUEST_URL.URL_PROFILE, METHOD_PUT, jsonParam.toString())
    }


    fun getProfileUpdate(msg : Message){
        when (msg.what) {
            Utils.MSG_SUCCESS -> {
                if(msg.obj is String){

                    val user = LoginToken.getUser(applicationContext)
                    var photoUrl : String?= msg.obj as String
                    if (!URLUtil.isValidUrl(photoUrl)) photoUrl = null

                    user.photo=photoUrl

                    val realm = DatabaseHelpUtils.getRealmDefault(applicationContext)
                    realm.beginTransaction()
                    realm.copyToRealmOrUpdate(user.toUserR())
                    realm.commitTransaction()


                    Log.d("$LOG_TAG/LoginToken_user", user.toString())

                    Toast.makeText(applicationContext, "프로필 수정에 성공하셨습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else-> {
                val result = msg.data.getString("message")

                if (result.contains("Failed")) {
                    Toast.makeText(applicationContext, "아이디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
                }
            }

        }

    }


    private class HandlerGet(activity: ProfileActivity) : Handler() {
        private val mActivity: WeakReference<ProfileActivity> = WeakReference<ProfileActivity>(activity)

        override fun handleMessage(msg: Message) {
            mActivity.get()?.getProfileUpdate(msg)
        }
    }

}
