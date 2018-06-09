package cs121.sideoftheroad

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.app_bar_main2.*
import android.Manifest
import android.app.ActionBar
import android.graphics.Color
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.CardView
import cs121.sideoftheroad.R.id.*
import android.util.TypedValue
import android.widget.TextView
import android.graphics.Color.parseColor
import kotlinx.android.synthetic.main.content_main2.*
import android.view.ViewGroup
import android.widget.LinearLayout


class Main2Activity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        val TAG = "SOTR"
    }

    private val CAMERA_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val card = CreateCardView(0)
        layoutMain.addView(card)
    }

    /**
     * A general method to programatically create a new cardview to be placed inside the linearlayout
     * The loc variable determines if it is supposed to be on the left or right side of the screem
     * 0 = left
     * 1 = right
     */
    fun CreateCardView(loc: Int): CardView {
        // Initialize a new CardView
        val card = CardView(this)

        // Set the CardView layoutParams
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        card.layoutParams = params

        // Set CardView corner radius
        card.radius = 9f

        // Set cardView content padding
        card.setContentPadding(15, 15, 15, 15)

        // Set a background color for CardView
        card.setCardBackgroundColor(Color.parseColor("#FFC6D6C3"))

        // Set the CardView maximum elevation
        card.maxCardElevation = 15f

        // Set CardView elevation
        card.cardElevation = 9f

        // Initialize a new TextView to put in CardView
        val tv = TextView(this)
        tv.layoutParams = params
        tv.text = "CardView\nProgrammatically"
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30f)
        tv.setTextColor(Color.RED)

        // Put the TextView in CardView
        card.addView(tv)

        return card
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            // Add listing
            R.id.nav_camera -> {
                // Handle the camera action, then pass that picture to the AddListingActivity
                runCameraAdd()
            }
            // Settings
            R.id.nav_settings -> {

            }
            // Map
            R.id.nav_map -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun runCameraAdd() {
        // Get permission to use the camera first
        if(getCameraPermissions()) {
            val intent = Intent(this, AddListingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getCameraPermissions() : Boolean {
        val camPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (camPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Required permissions denied")
            makeCameraRequest()
            return false
        }
        else
            return true
    }

    private fun makeCameraRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_REQUEST_CODE)
    }

    // Permissions code from https://www.techotopia.com/index.php/Kotlin_-_Making_Runtime_Permission_Requests_in_Android
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                    // Run the camera intent, since we didn't actually run the intent the first time around
                    runCameraAdd()
                }
            }
        }
    }
}
