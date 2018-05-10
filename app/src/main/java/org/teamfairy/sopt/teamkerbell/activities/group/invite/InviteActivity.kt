package org.teamfairy.sopt.teamkerbell.activities.group.invite

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.activity_invite.*
import kotlinx.android.synthetic.main.app_bar_back.*
import kotlinx.android.synthetic.main.content_invite.*
import net.glxn.qrgen.android.QRCode
import org.teamfairy.sopt.teamkerbell.model.data.Team
import org.teamfairy.sopt.teamkerbell.utils.IntentTag.Companion.INTENT_GROUP
import kotlin.properties.Delegates

class InviteActivity : AppCompatActivity() {


    var group  : Team by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite)
        setSupportActionBar(toolbar)

        group  = intent.getParcelableExtra<Team>(INTENT_GROUP)


        btn_copy.setOnClickListener {
            val clipboardManager = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(getString(R.string.app_name), tv_invite_url.text.toString())
            clipboardManager.primaryClip = clipData
            Toast.makeText(applicationContext, "초대 링크가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        tv_invite_mail.setOnClickListener {
            val i = Intent(applicationContext,InviteMailActivity::class.java)
            i.putExtra(INTENT_GROUP,group)
            startActivity(i)
        }

        tv_invite_phone.setOnClickListener {
            val i = Intent(applicationContext,InvitePhoneActivity::class.java)
            i.putExtra(INTENT_GROUP,group)
            startActivity(i)
        }

        tv_invite_qr.setOnClickListener{

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
                    } else {
                        val i = Intent(applicationContext,ScanActivity::class.java)
                        startActivity(i)
                    }
                } else {
                    val i = Intent(applicationContext,ScanActivity::class.java)
                    startActivity(i)
                }
            } else {
                val i = Intent(applicationContext,ScanActivity::class.java)
                startActivity(i)
            }
        }


        btn_back.setOnClickListener {
            onBackPressed()
        }
        tv_invite_url.text="기본 링크입니다"
        val stringForQRCode = tv_invite_url.text.toString()
        val bitmapQRcode : Bitmap= QRCode.from(stringForQRCode).bitmap()
        iv_qr_code.setImageBitmap(bitmapQRcode)
    }


    private fun requestExplorer() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, MY_CAMERA_REQUEST_CODE)
    }


    private val MY_CAMERA_REQUEST_CODE = 100

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();

            }

        }
    }
}
