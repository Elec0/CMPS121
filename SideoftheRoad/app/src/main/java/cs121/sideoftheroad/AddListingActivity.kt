package cs121.sideoftheroad

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.graphics.Bitmap
import android.R.attr.data
import android.support.v4.app.NotificationCompat.getExtras
import kotlinx.android.synthetic.main.activity_add_listing.*
import kotlinx.android.synthetic.main.nav_header_main2.*


class AddListingActivity : AppCompatActivity() {

    private var TAKE_PHOTO_REQUEST = 103

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)

        // This is the first time we're running this, which means we are adding a new entry
        // That means we need to open the camera to take a picture before we do anything else
        launchCamera()

    }


    // From https://medium.com/@bionicwan/android-how-to-take-a-photo-using-kotlin-6ce7f0dee9c8
    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        val fileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(intent.resolveActivity(packageManager) != null) {
            //mCurrentPhotoPath = fileUri.toString()
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        }
    }

    // Grab the result of launchCamera
    // This result is the data of the picture, which is not written to a file until we tell it to
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PHOTO_REQUEST) {
            if(data != null) {
                var extras = data.getExtras()
                var imageBitmap = extras.get("data") as Bitmap
                imageView2.setImageBitmap(imageBitmap)
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
