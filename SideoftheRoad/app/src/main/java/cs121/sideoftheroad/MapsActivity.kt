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
import android.graphics.Point
import android.util.Log
import android.view.Display
import kotlinx.android.synthetic.main.app_bar_main2.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * The callback for location permissions
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // Permission granted, turn on location. This is only required for the first run
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                // This should never happen.
            }

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
}
