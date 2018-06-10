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
import android.content.ClipData
import android.graphics.Color
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.CardView
import cs121.sideoftheroad.R.id.*
import android.util.TypedValue
import android.widget.TextView
import android.graphics.Color.parseColor
import android.os.AsyncTask
import kotlinx.android.synthetic.main.content_main2.*
import android.view.ViewGroup
import android.widget.LinearLayout
import android.util.DisplayMetrics
import android.view.Gravity
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import cs121.sideoftheroad.dbmapper.tables.tblItem
import kotlin.concurrent.thread


class Main2Activity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var username = ""
    private val ITEM_TABLE_NAME = "sideoftheroad-mobilehub-1016005302-item"

    private var dynamoDBMapper: DynamoDBMapper? = null
    private var client: AmazonDynamoDBClient? = null

    companion object {
        val TAG = "SOTR"
    }

    private val CAMERA_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        username = intent.getStringExtra("username")
        setSupportActionBar(toolbar)

                val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // Connect to the database
        dbConnect()
        // Get all the stuff from said database
        // There's probably an easier way to get a callback from an asynctask than this but idk how
        val c: (List<tblItem>) -> Unit = { resultList -> dbCallback(resultList) }

        var scanTask = DatabaseFetchTask(c)
        scanTask!!.execute(null as Void?)
    }

    /**
     * We get all of the data back from the database here.
     * Populate the CardViews now
     */
    fun dbCallback(itemList: List<tblItem>) {
        // When creating these programatically we must put each group of 2 into a linearlayout so they
        // actually are next to each other
        val row1 = LinearLayout(this)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        row1.layoutParams = layoutParams


        var curRow = LinearLayout(this)
        for(i in itemList.indices) {
            curRow.addView(createCardView(i, itemList[i]))

            if(i % 2 == 1) { // If we've added the second in the row, make a new row
                layoutMain.addView(curRow)
                curRow = LinearLayout(this)
                row1.layoutParams = layoutParams
            }
        }
    }


    /**
     * A general method to programatically create a new cardview to be placed inside the linearlayout
     * The loc variable determines if it is supposed to be on the left or right side of the screem
     * 0 = left
     * 1 = right
     */
    fun createCardView(loc: Int, item: tblItem): CardView {
        // Initialize a new CardView
        val card = CardView(this)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        // Set the CardView layoutParams
        // Make the Card a square
        val cardParams = LinearLayout.LayoutParams(
                width/2,
                width/2
        )

        // Get the value of the margins from the dimen.xml
        val margins: IntArray = intArrayOf(resources.getDimension(R.dimen.card_margin_start).toInt(), resources.getDimension(R.dimen.card_margin_top).toInt(),
                resources.getDimension(R.dimen.card_margin_end).toInt(), resources.getDimension(R.dimen.card_margin_bottom).toInt())
        // Then convert from dp to pixels
        for(i in margins.indices) {
            margins[i] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, margins[i].toFloat(), resources.getDisplayMetrics()).toInt()
        }
        cardParams.setMargins(margins[0], margins[1], margins[2], margins[3])

        val innerParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Set the rest of the card params
        cardParams.gravity = if(loc == 0) Gravity.START else Gravity.END
        cardParams.weight = 1f

        card.layoutParams = cardParams

        // Set CardView corner radius
        card.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.getDisplayMetrics())

        // Set cardView content padding
        //card.setContentPadding(5, 5, 5, 5)

        // Set the CardView maximum elevation
        //card.maxCardElevation = 15f
        // Set CardView elevation
        //card.cardElevation = 9f

        // Initialize a new TextView to put in CardView
        val tv = TextView(this)
        tv.layoutParams = innerParams
        tv.text = item.title + "\n" + item.description
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)

        // Put the TextView in CardView
        card.addView(tv)

        return card
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
            intent.putExtra("username", username)
            startActivity(intent)
        }
    }

    private fun getCameraPermissions() : Boolean {
        val camPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val locCoarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val locFinePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if (camPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED ||
                locCoarsePermission != PackageManager.PERMISSION_GRANTED || locFinePermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Required permissions denied")
            makeCameraRequest()
            return false
        }
        else
            return true
    }

    private fun makeCameraRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), CAMERA_REQUEST_CODE)
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


    inner class DatabaseFetchTask internal constructor(val callback: (List<tblItem>) -> Unit) : AsyncTask<Void, Void, Boolean>() {
        var resultList = mutableListOf<tblItem>()

        override fun doInBackground(vararg p0: Void?): Boolean {


            var attributeValues = mutableMapOf<String, AttributeValue>()
            attributeValues.put(":minId", AttributeValue().withN("0"))

            var scanRequest = DynamoDBScanExpression()
                .withFilterExpression("size(itemId) > :minId")
                .withExpressionAttributeValues(attributeValues)

            var scanResult = dynamoDBMapper!!.scan(tblItem::class.java, scanRequest)

            Log.i(TAG, "Results: " + scanResult.count())
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
}
