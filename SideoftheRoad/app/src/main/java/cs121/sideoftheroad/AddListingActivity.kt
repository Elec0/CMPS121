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
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.support.v4.app.NotificationCompat.getExtras
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer
import cs121.sideoftheroad.dbmapper.tables.tblUser
import kotlinx.android.synthetic.main.activity_add_listing.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.nav_header_main2.*
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.s3.AmazonS3Client
import cs121.sideoftheroad.dbmapper.tables.tblItem
import cs121.sideoftheroad.s3bucket.Constants
import java.io.File

class AddListingActivity : AppCompatActivity() {

    private var mCreateItemTask: createItemTask? = null

    private var titleStr: String = ""
    private var priceStr: String = ""
    private var descriptionStr: String = ""


    private var dynamoDBMapper: DynamoDBMapper? = null

    private var filePathStr: String = Environment.getExternalStorageState().toString()

    private var TAKE_PHOTO_REQUEST = 103

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)


        AWSMobileClient.getInstance().initialize(this).execute()
        val client = AmazonDynamoDBClient(AWSMobileClient.getInstance().credentialsProvider)
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(client)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .build()

        var username = intent.getStringExtra("username")

        // onclicklistener for submit button
        button.setOnClickListener {
            System.out.println("hasnt blown up yet 60")
            val completed = uploadToS3()
            System.out.println("62 not blown up yet")
            createItemTask(username, editText2.toString(), editText3.toString(), editText.toString(),completed)
        }
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
        val currentPathStr = fileUri.toString()

        if(intent.resolveActivity(packageManager) != null) {

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

    private fun uploadToS3(): Boolean {
        var completed: Boolean = false
        val transferUtility = TransferUtility.builder()
                .context(this@AddListingActivity.applicationContext)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(AmazonS3Client(AWSMobileClient.getInstance().credentialsProvider))
                        .build()
        System.out.println(filePathStr)
        val uploadObserver = transferUtility.upload(Constants.BUCKET_NAME, File(filePathStr))
        uploadObserver.setTransferListener(object : TransferListener {

            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED == state) {
                    Toast.makeText(applicationContext, "Upload Completed!", Toast.LENGTH_SHORT).show()
                    completed = true
                } else if (TransferState.FAILED == state) {
                    Toast.makeText(applicationContext,"Upload failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                val percentDone = percentDonef.toInt()
                Log.d("percent done", percentDone.toString())
            }

            override fun onError(id: Int, ex: Exception) {
                ex.printStackTrace()
            }
        })
            return completed
        }

    private fun createItem(item: tblItem){
        dynamoDBMapper?.save(item)
    }

    inner class createItemTask internal constructor(private val username: String, private val titleStr: String, private val priceStr: String, private val descriptionStr: String,private val completed: Boolean) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            if (completed) {
                val pics: MutableSet<String> = mutableSetOf(filePathStr)
                var newItem: tblItem = tblItem()
                newItem.title = titleStr
                newItem.description = descriptionStr
                newItem.itemId = username + "." + titleStr
                newItem.price = priceStr
                newItem.pics = pics
                createItem(newItem)

                return true
            }

            finish()
            return false
        }

        override fun onPostExecute(success: Boolean?) {
            if (success!!) {
                Log.d("STUFF","successful")
                val intent = Intent(this@AddListingActivity,Main2Activity::class.java)
                startActivity(intent)
                finish()
            }
            Log.d("STUFF","notsucesescesful")
        }

        override fun onCancelled() {

        }
    }
}
