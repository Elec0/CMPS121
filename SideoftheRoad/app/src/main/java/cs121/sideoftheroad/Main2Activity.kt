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
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.app_bar_main2.*
import android.Manifest
import android.app.ActionBar
import android.app.Fragment
import android.app.FragmentManager
import android.content.ClipData
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.CardView
import cs121.sideoftheroad.R.id.*
import android.util.TypedValue
import android.widget.TextView
import android.graphics.Color.parseColor
import android.media.Image
import android.os.AsyncTask
import android.support.v4.content.ContextCompat.startActivity
import kotlinx.android.synthetic.main.content_main2.*
import android.widget.LinearLayout
import android.util.DisplayMetrics
import android.util.LruCache
import android.view.*
import android.widget.ImageView
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.mobileconnectors.s3.transfermanager.Download
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import cs121.sideoftheroad.dbmapper.tables.tblItem
import java.io.InputStream
import java.net.URL
import kotlin.concurrent.thread


class Main2Activity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var username = ""
    private val ITEM_TABLE_NAME = "sideoftheroad-mobilehub-1016005302-item"

    private var dynamoDBMapper: DynamoDBMapper? = null
    private var client: AmazonDynamoDBClient? = null
    private var prefs: SharedPreferences? = null
    private var imgCache: LruCache<String, Bitmap>? = null

    companion object {
        val TAG = "SOTR"
        val PREFS_FILE = "cs121.sideoftheroad.prefs"
        var PREF_USERNAME = "Username"
    }

    private val CAMERA_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        prefs = this.getSharedPreferences(PREFS_FILE, 0)
        username = prefs!!.getString(PREF_USERNAME, "") // Should never be null

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        // Set up the image cache (https://developer.android.com/topic/performance/graphics/cache-bitmap)
        val maxMemory = Runtime.getRuntime().maxMemory() / 1024
        // Use 1/8th of the memory for the cache
        val cacheSize = maxMemory / 8

        val retainFragment : RetainFragment = RetainFragment.findOrCreateRetainFragment(fragmentManager)
        imgCache = retainFragment.mRetainedCache
        if(imgCache == null) {
            imgCache = object : LruCache<String, Bitmap>(cacheSize.toInt()) {
                override fun sizeOf(url: String, bitmap: Bitmap): Int {
                    return bitmap.byteCount / 1024
                }
            }
        }


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
        val innerParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val imageParams =  LinearLayout.LayoutParams(
                width/4,
                width/4
        )
        imageParams.gravity = Gravity.CENTER

        // Get the value of the margins from the dimen.xml
        val margins: IntArray = intArrayOf(resources.getDimension(R.dimen.card_margin_start).toInt(), resources.getDimension(R.dimen.card_margin_top).toInt(),
                resources.getDimension(R.dimen.card_margin_end).toInt(), resources.getDimension(R.dimen.card_margin_bottom).toInt())
        // Then convert from dp to pixels
        for(i in margins.indices) {
            margins[i] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, margins[i].toFloat(), resources.getDisplayMetrics()).toInt()
        }
        // Set the margins, but only have the end margin if the card is on the right side of the screen
        cardParams.setMargins(margins[0], margins[1], if(loc%2==1) margins[2] else 0, margins[3])


        // Set the rest of the card params
        cardParams.gravity = if(loc == 0) Gravity.START else Gravity.END
        cardParams.weight = 1f
        card.layoutParams = cardParams
        // Set CardView corner radius
        card.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.getDisplayMetrics())

        // Done setting the card specific stuff

        // This is the linear layout where we will be putting our items
        val content = LinearLayout(this)
        content.layoutParams = innerParams
        content.orientation = LinearLayout.VERTICAL

        // Initialize a new ImageView and stick it in the CardView
        val iv = ImageView(this)
        iv.layoutParams = imageParams

        // Set the default image for an item
        iv.setImageResource(R.drawable.ic_launcher_round)
        if(item.pics != null && !item.pics.equals("null"))
            DownloadImageTask(iv).execute(item.pics)
        content.addView(iv)

        // Initialize a new TextView to put in CardView
        val tv = TextView(this)
        tv.layoutParams = innerParams
        tv.text = item.title + "\n" + item.description
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)

        content.addView(tv)


        // Do whatever we're going to do when the user clicks on an item
        card.setOnClickListener(View.OnClickListener {
            Log.i(TAG, "The card was clicked on! " + item.itemId)
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("itemId", item.itemId)
            startActivity(intent)
        })

        card.addView(content)
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


    // ******* Cache functions *******
    fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            imgCache!!.put(key, bitmap)
        }
    }

    fun getBitmapFromMemCache(key: String) : Bitmap? {
        return imgCache!!.get(key)
    }


    // ******** Async Tasks ********

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

    inner class DownloadImageTask(img: ImageView) : AsyncTask<String, Void, Bitmap>() {
        var locImg:ImageView = img

        override fun doInBackground(vararg param: String?): Bitmap? {
            var urldisplay = param[0];
            var bitmap = getBitmapFromMemCache(urldisplay!!)

            Log.i(TAG, bitmap.toString())

            // If the cache had the image, just return it immediately
            if(bitmap != null) {
                return bitmap
            }

            if(!urldisplay!!.startsWith("http://"))
                urldisplay = "http://" + urldisplay

            try {
                var inS: InputStream = URL(urldisplay).openStream()
                bitmap = BitmapFactory.decodeStream(inS)
                Log.i(TAG, "Add bitmap to cache")
                Log.i(TAG, bitmap.toString())
                addBitmapToMemoryCache(urldisplay, bitmap)
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
                Log.i(TAG, "Image was not downloaded successfully")
            }
        }
    }


    class RetainFragment : Fragment() {
        var mRetainedCache: LruCache<String, Bitmap>? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setRetainInstance(true)
        }

        companion object {
            private val TAG = "RetainFragment"

            fun findOrCreateRetainFragment(fm: FragmentManager): RetainFragment {
                var fragment: RetainFragment? = fm.findFragmentByTag(TAG) as? RetainFragment
                if (fragment == null) {
                    fragment = RetainFragment()
                    fm.beginTransaction().add(fragment, TAG).commit()
                }
                return fragment
            }
        }
    }
}
