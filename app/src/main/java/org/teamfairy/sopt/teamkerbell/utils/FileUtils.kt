package org.teamfairy.sopt.teamkerbell._utils


import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import java.io.*
import android.media.ExifInterface
import android.util.DisplayMetrics


/**
 * Created by lumiere on 2018-01-01.
 */
class FileUtils {
    companion object {
        private val LOG_TAG = this::class.java.simpleName


        private fun checkExinterface(imagePath: String, bitmap: Bitmap): Bitmap {
            // 이미지를 상황에 맞게 회전시킨다
            val exif = ExifInterface(imagePath)
            val exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val exifDegree = exifOrientationToDegrees(exifOrientation)

            return rotateBitmap(bitmap, exifDegree)
        }


        private fun exifOrientationToDegrees(exifOrientation: Int): Int {
            if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90
            } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180
            } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270
            }
            return 0
        }

        private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
            var bitmapRotated: Bitmap = bitmap
            if (degrees != 0) {
                val m: Matrix = Matrix();

                m.setRotate(degrees.toFloat(), bitmap.width.toFloat() * 0.5f,
                        bitmap.height.toFloat() * 0.5f);

                try {
                    val converted: Bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                            bitmap.width, bitmap.height, m, true);
                    if (bitmap != converted) {
                        bitmap.recycle();
                        bitmapRotated = converted;
                    }
                } catch (ex: OutOfMemoryError) {
                    bitmapRotated = bitmap
                }
            }
            return bitmapRotated
        }


        fun getRealPathFromURI(contentURI: Uri, contentResolver: ContentResolver): String {
            val result: String
            val cursor = contentResolver.query(contentURI, null, null, null, null)
            if (cursor == null) {
                result = contentURI.getPath()
            } else {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                result = cursor.getString(idx)
                cursor.close()
            }
            return result
        }

        private fun bitmapToByteArray(bitmap: Bitmap): ByteArrayOutputStream {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            return baos
        }

        private fun compressImage(bitmapOrig: Bitmap): ByteArrayOutputStream {

            val bitmap: Bitmap = bitmapOrig


            val baos = ByteArrayOutputStream()

            var options = if (baos.toByteArray().size <= 2000000) 100 else 0
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)
            Log.d(LOG_TAG + "/file_size_before", baos.toByteArray().size.toString())
            while (baos.toByteArray().size / 1024 > 100 && options >= 10) {
                baos.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)
                options -= 10
            }
            Log.d(LOG_TAG + "/file_options", options.toString())
            Log.d(LOG_TAG + "/file_size_after", baos.toByteArray().size.toString())

            return baos
        }

        fun updatePhoto(imagePath: String, imageView: ImageView?): File {

            val name = imagePath.substringAfterLast("/")
            val btt: Bitmap = BitmapFactory.decodeFile(imagePath)
            Log.d(LOG_TAG + "/file", btt.height.toString() + "," + btt.width.toString())
            val bitmap = resizeBitmap(imagePath)
            Log.d(LOG_TAG + "/file", bitmap.height.toString() + "," + bitmap.width.toString())

            val baos = compressImage(bitmap)

            val isBmCompressed = ByteArrayInputStream(baos.toByteArray())
            val bitmapCompressed = BitmapFactory.decodeStream(isBmCompressed, null, null)

            val bitmapRotated = checkExinterface(imagePath, bitmapCompressed)

            if(imageView!=null)
                setImageView(bitmapRotated, imageView)
            val isBm = ByteArrayInputStream(bitmapToByteArray(bitmapRotated).toByteArray())

            val folder = File(Environment.getExternalStorageDirectory(), "팀플의요정")
            if (!folder.exists() || !folder.isDirectory)
                folder.mkdir()

            var fileNew = File(folder.absolutePath + File.separator +  name)
            if (!fileNew.exists()) {
                fileNew = File(folder.absolutePath + File.separator +  name)
                fileNew.createNewFile()
            } else {
                fileNew.createNewFile()
            }
            Log.d(LOG_TAG + "/file", fileNew.path)

            val os = FileOutputStream(fileNew);

            var read: Int
            val bytes = ByteArray(1024)

            while (true) {
                read = isBm.read(bytes)
                if (read == -1) break
                os.write(bytes, 0, read);
            }
            os.flush()
            os.close()

            return fileNew
        }

        private fun setImageView(bitmap: Bitmap, imageView: ImageView) {

            imageView.setImageBitmap(bitmap)
            imageView.background = ShapeDrawable(OvalShape())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.clipToOutline = true
            }
        }


        private fun resizeBitmap(imagePath: String): Bitmap {

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true

            val bitmap = BitmapFactory.decodeFile(imagePath)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(bitmap, 100, 100)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false

            return BitmapFactory.decodeFile(imagePath, options)

        }

        private fun calculateInSampleSize(
                bitmap: Bitmap, reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val height = bitmap.height
            val width = bitmap.width
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                val halfHeight = height / 2;
                val halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }


        fun convertDpToPixel(dp: Float, context: Context): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }


        fun convertPixelsToDp(px: Float, context: Context): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }

    }

}