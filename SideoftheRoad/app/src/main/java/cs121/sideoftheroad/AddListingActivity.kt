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
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Environment
import android.support.v4.app.NotificationCompat.getExtras
import android.widget.ImageView
import cs121.sideoftheroad.R.id.imageView2
import kotlinx.android.synthetic.main.activity_add_listing.*
import kotlinx.android.synthetic.main.nav_header_main2.*
import java.nio.file.Files.exists
import android.os.Environment.getExternalStorageDirectory
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.io.FileOutputStream


class AddListingActivity : AppCompatActivity() {

    private val TAKE_PHOTO_REQUEST = 103

    private var curLoc: LatLng? = null
    private var locationManager : LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)

        // Create persistent LocationManager reference
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?;
        try {
            // Request location updates
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener);
        } catch(ex: SecurityException) {
            Log.d(Main2Activity.TAG, "Security Exception, no location available");
        }

        // This is the first time we're running this, which means we are adding a new entry
        // That means we need to open the camera to take a picture before we do anything else
        launchCamera()

        btnAdd.setOnClickListener { view ->
            Log.i(Main2Activity.TAG, "Add the listing.")
            val title: String = txtTitle.text.toString()
            val price: String = txtPrice.text.toString()
            val loc: LatLng? = curLoc

            // TODO: Upload to the database

            finish()
        }
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

                // Save the bitmap as a file for uploading
                val file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SideoftheRoad"
                val dir = File(file_path)
                if (!dir.exists())
                    dir.mkdirs()
                val file = File(dir, "curFile.png")
                val fOut = FileOutputStream(file)

                imageBitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
                fOut.flush()
                fOut.close()
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    // Define the location listener
    // From https://stackoverflow.com/questions/45958226/get-location-android-kotlin
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            curLoc = LatLng(location.latitude, location.longitude)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
}
