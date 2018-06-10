package cs121.sideoftheroad

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLngBounds
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.UiSettings
import android.R.attr.y
import android.R.attr.x
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.AsyncTask
import android.util.Log
import android.view.Display
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.google.android.gms.maps.model.Marker
import cs121.sideoftheroad.dbmapper.tables.tblItem
import kotlinx.android.synthetic.main.app_bar_main2.*
import kotlinx.android.synthetic.main.content_main2.*
import java.io.InputStream
import java.net.URL


class MapsActivity : AppCompatActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap

    private var dynamoDBMapper: DynamoDBMapper? = null
    private var client: AmazonDynamoDBClient? = null
    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getData()
    }

    /**
     * Get the data from the database again
     */
    fun getData() {
        dbConnect()
        val c: (List<tblItem>) -> Unit = { resultList -> dbCallback(resultList) }
        var scanTask = DatabaseFetchTask(c)
        scanTask!!.execute(null as Void?)
    }

    /**
     * We get all of the data back from the database here.
     */
    fun dbCallback(itemList: List<tblItem>) {
        // When creating these programatically we must put each group of 2 into a linearlayout so they
        // actually are next to each other
        val row1 = LinearLayout(this)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        row1.layoutParams = layoutParams


        var curRow = LinearLayout(this)
        for (i in itemList.indices) {
            var loc = itemList[i].location

            // We can remove this eventually
            if(loc.startsWith("lat/lng")) {
                loc = loc.split("(")[1]
                loc = loc.substring(0, loc.length - 1)
            }
            var loc2 = loc.split(",")
            var latLng = LatLng(loc2[0].toDouble(), loc2[1].toDouble())

            mMap.addMarker(MarkerOptions().position(latLng).title(itemList[i].title))


        }
    }

    /**
     * Connect to the database and save the client and mapper objects?
     */
    fun dbConnect() {
        AWSMobileClient.getInstance().initialize(this) { Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!") }.execute()

        client = AmazonDynamoDBClient(AWSMobileClient.getInstance().credentialsProvider)
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(client)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .build()
    }

    /**
     * The callback for location permissions
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // Permission granted, turn on location. This is only required for the first run
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {}
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y

        val uiSettings = mMap.uiSettings
        uiSettings.isZoomControlsEnabled = true

        mMap.setOnMarkerClickListener(this@MapsActivity)

        // We should probably handle people clicking never ask again for this.
        val permCoarse = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        val permFine = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)

        if (permCoarse != PackageManager.PERMISSION_GRANTED || permFine != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION), 13) // the 13 is arbitrary
        }

        try {
            mMap.isMyLocationEnabled = true // Turn showing device location on
        } catch (e: SecurityException) {
            Log.e(Main2Activity.TAG, "Location error security exception", e)
            // Do nothing because we can't see the device's location data, and that isn't critical for the app to work.
        }

        // This is UCSC's latLng
        val bounds = LatLngBounds(LatLng(36.976343, -122.072109), LatLng(37.004803, -122.041124))

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0))

    }

    override fun onMarkerClick(marker : Marker): Boolean {
        Log.i(Main2Activity.TAG, marker.toString() + ", " + marker.id.toString())
        return false
    }

    override fun onInfoWindowClick(marker : Marker) {
        Toast.makeText(this, "Click Info Window", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed(){

    }



    // ******************************************************************************
    inner class DatabaseFetchTask internal constructor(val callback: (List<tblItem>) -> Unit) : AsyncTask<Void, Void, Boolean>() {
        var resultList = mutableListOf<tblItem>()

        override fun doInBackground(vararg p0: Void?): Boolean {


            var attributeValues = mutableMapOf<String, AttributeValue>()
            attributeValues.put(":minId", AttributeValue().withN("0"))

            var scanRequest = DynamoDBScanExpression()
                    .withFilterExpression("size(itemId) > :minId")
                    .withExpressionAttributeValues(attributeValues)

            var scanResult = dynamoDBMapper!!.scan(tblItem::class.java, scanRequest)

            Log.i(Main2Activity.TAG, "Results: " + scanResult.count())
            for(result in scanResult) {
                resultList.add(result)
            }

            return true
        }


        override fun onPostExecute(success: Boolean?) {
            if(success!!) {
                callback(resultList)
            }
        }

    }

    inner class DownloadImageTask(img: ImageView) : AsyncTask<String, Void, Bitmap>() {
        var locImg: ImageView = img

        override fun doInBackground(vararg param: String?): Bitmap? {
            var urldisplay = param[0];
            var bitmap: Bitmap?  = null

            // If the cache had the image, just return it immediately
            if(bitmap != null) {
                return bitmap
            }

            if(!urldisplay!!.startsWith("http://"))
                urldisplay = "http://" + urldisplay

            try {
                var inS: InputStream = URL(urldisplay).openStream()
                bitmap = BitmapFactory.decodeStream(inS)
                return bitmap

            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(success: Bitmap?) {
            if(success != null) {
                locImg.setImageBitmap(success)
            }
            else {
                Log.i(Main2Activity.TAG, "Image was not downloaded successfully")
            }
        }
    }
}
