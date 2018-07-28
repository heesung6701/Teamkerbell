package org.teamfairy.sopt.teamkerbell.activities.items.role.task

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import org.teamfairy.sopt.teamkerbell.R

import kotlinx.android.synthetic.main.app_bar_commit.*
import kotlinx.android.synthetic.main.content_make_task_response.*
import org.json.JSONObject
import org.teamfairy.sopt.teamkerbell._utils.FileUtils.Companion.getRealPathFromURI
import org.teamfairy.sopt.teamkerbell._utils.FileUtils.Companion.updatePhoto
import org.teamfairy.sopt.teamkerbell.activities.items.role.adapter.FileListAdapter
import org.teamfairy.sopt.teamkerbell.model.data.RoleTask
import org.teamfairy.sopt.teamkerbell.network.GetMessageTask
import org.teamfairy.sopt.teamkerbell.network.USGS_REQUEST_URL
import org.teamfairy.sopt.teamkerbell.utils.IntentTag
import org.teamfairy.sopt.teamkerbell.utils.LoginToken
import org.teamfairy.sopt.teamkerbell.utils.Utils
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import kotlin.properties.Delegates
import android.support.v4.app.ActivityCompat
import android.os.Build
import android.view.View
import kotlinx.android.synthetic.main.activity_make_task_response.*
import org.teamfairy.sopt.teamkerbell.network.NetworkTask


class MakeTaskResponseActivity : AppCompatActivity() {


    private val SELECT_FILE = 11
    private val SELECT_IMAGE = 10

    private var roleTask: RoleTask by Delegates.notNull()
    var fileArray = ArrayList<File>()

    var adapter: FileListAdapter by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_task_response)
        setSupportActionBar(toolbar)

        roleTask = intent.getParcelableExtra(IntentTag.INTENT_TASK)
        tv_task_name.text = roleTask.content

        btn_back.setOnClickListener {
            finish()
        }

//        btn_add_photo.setOnClickListener {
//
//
//            if(checkPermissionREAD_EXTERNAL_STORAGE(this,MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_IMAGE)){
//                intentImage()
//            }
//
//
//        }
        btn_add_file.setOnClickListener {

            if(checkPermissionREAD_EXTERNAL_STORAGE(this,MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_FILE)){
                intentFile()
            }
        }

        btn_commit.setOnClickListener {
            if(edt_content.text.isEmpty() && edt_content.text.isBlank()){
                Toast.makeText(applicationContext,"내용을 입력해주세요",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isUploading()

            val task = GetMessageTask(applicationContext, HandlerUpload(this), LoginToken.getToken(applicationContext))

            val jsonParam = JSONObject()
            try {
                jsonParam.put(USGS_REQUEST_URL.URL_ROLE_RESPONSE_PARAM_ROLE_IDX, roleTask.role_idx)
                jsonParam.put(USGS_REQUEST_URL.URL_ROLE_RESPONSE_PARAM_ROLE_TASK_IDX, roleTask.task_idx)
                jsonParam.put(USGS_REQUEST_URL.URL_ROLE_RESPONSE_PARAM_RESPONSE_CONTENT, edt_content.text.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            fileArray.forEach {
                task.files.add(it)
            }
            Log.d("NetworkTask", jsonParam.toString())

            task.execute(USGS_REQUEST_URL.URL_ROLE_RESPONSE,NetworkTask.METHOD_POST, jsonParam.toString())
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FileListAdapter(fileArray)
        recyclerView.adapter = adapter


    }

    private fun isUploading(){
        upload_progress.visibility= View.VISIBLE
        btn_add_file.isEnabled=false
        btn_commit.isEnabled=false
        edt_content.isEnabled=false
    }
    private fun isFailed(){
        upload_progress.visibility= View.GONE
        btn_add_file.isEnabled=true
        btn_commit.isEnabled=true
        edt_content.isEnabled=true

    }

    private fun intentFile() {

        val intent = Intent(Intent.ACTION_GET_CONTENT)

//       구글 드라이브 등에서 가져오는기 막음
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.type = "*/*"
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivityForResult(intent, SELECT_FILE);

    }

    private fun intentImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, SELECT_IMAGE)

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
    }

    private class HandlerUpload(activity: MakeTaskResponseActivity) : Handler() {
        private val mActivity: WeakReference<MakeTaskResponseActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    Utils.MSG_SUCCESS -> {
                        activity.fileArray.forEach {
                            it.delete()
                        }
                        Toast.makeText(activity.applicationContext, "업로드 되었습니다.", Toast.LENGTH_SHORT).show()
                        activity.finish()
                    }
                    else -> {
                        activity.isFailed()
                    }
                }

            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val url = getPath(applicationContext, data!!.data)
                    val file = File(url)
                    fileArray.add(file)
                    adapter.notifyDataSetChanged()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

        }

        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val file = updatePhoto(getRealPathFromURI(data!!.data, contentResolver), null)
                    fileArray.add(file)
                    adapter.notifyDataSetChanged()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_IMAGE = 123
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_FILE = 124

    private fun checkPermissionREAD_EXTERNAL_STORAGE(
            context: Context,request: Int): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                                context as Activity,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            request)

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    context,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    request)
                }
                return false
            } else {
                return true
            }

        } else {
            return true
        }
    }

    fun showDialog(msg: String, context: Context,
                   permission: String,request : Int) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage("$msg permission is necessary")
        alertBuilder.setPositiveButton(android.R.string.yes) { _, _ ->
            ActivityCompat.requestPermissions(context as Activity,
                    arrayOf(permission),
                    request)

        }
        val alert = alertBuilder.create()
        alert.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_IMAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intentImage()
            } else {
                Toast.makeText(applicationContext, "GET_ACCOUNTS Denied",
                        Toast.LENGTH_SHORT).show()

            }
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_FILE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intentFile()
            } else {
                Toast.makeText(applicationContext, "GET_ACCOUNTS Denied",
                        Toast.LENGTH_SHORT).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions,
                    grantResults)
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }


            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }


    fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                      selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null)
                cursor.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Docs
     */
    fun isGoogleDocssUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority
    }
}
