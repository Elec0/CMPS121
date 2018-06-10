package cs121.sideoftheroad

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import cs121.sideoftheroad.dbmapper.tables.tblItem
import cs121.sideoftheroad.dbmapper.tables.tblUser

import kotlinx.android.synthetic.main.activity_detail2.*
import java.io.InputStream
import java.net.URL
import android.content.DialogInterface
import android.content.SharedPreferences
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.content_detail.*


class DetailActivity : AppCompatActivity() {


    private var dynamoDBMapper: DynamoDBMapper? = null
    private var client: AmazonDynamoDBClient? = null
    private var itemId: String = ""
    private var user: tblUser? = null
    private var item: tblItem? = null

    private var prefs: SharedPreferences? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail2)


        prefs = this.getSharedPreferences(Main2Activity.PREFS_FILE, 0)
        val username = prefs!!.getString(Main2Activity.PREF_USERNAME, "") // Should never be null

        itemId = intent.getStringExtra("itemId")
        Log.d(Main2Activity.TAG,username + " " + itemId)
        dbConnect()
        GrabUserItemTask(itemId).execute()
        Thread.sleep(2_000)
        Log.d(Main2Activity.TAG,item.toString())

        if(item?.pics != null && !item?.pics.equals("null"))
            DownloadImageTask(detailImg).execute(item?.pics)

        detailTitle.text = (item?.title)
        detailPrice.text = ("$" + item?.price)
        if(detailPrice.text.toString() == "\$Free")
            detailPrice.text = ("Free")
        detailDesc.text = (item?.description)

        fab.setOnClickListener { view ->
            AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Confirm pickup?")
                    .setMessage("Have you picked up the item?")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                        val intent = Intent(this@DetailActivity, Main2Activity::class.java)
                        DeleteItemTask(itemId).execute()
                        startActivity(intent)
                    })
                    .setNegativeButton("No", null)
                    .show()
        }

        fab2.setOnClickListener { view ->
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.type = "*/*"
            intent.data = Uri.parse("mailto:")
            val sendTo = arrayOf<String>(user!!.email)
            intent.putExtra(Intent.EXTRA_EMAIL, sendTo)
            intent.putExtra(Intent.EXTRA_SUBJECT, "[Side of the Road]" + item!!.title)
            intent.putExtra(Intent.EXTRA_TEXT, "Hi. I'd like to ask about your listing")
            startActivity(Intent.createChooser(intent, "Send Email"))
        }


    }

    /**
     * Connect to the database and save the client and mapper objects?
     */
    fun dbConnect() {
        client = AmazonDynamoDBClient(AWSMobileClient.getInstance().credentialsProvider)
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(client)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .build()
    }

    private fun getUser(userId: String): tblUser? {
        var user: tblUser? = null

        user = dynamoDBMapper?.load(tblUser::class.java,
                userId)

        return user
    }

    private fun getItem(itemId: String?): tblItem? {
        var item: tblItem? = null

        item = dynamoDBMapper?.load(tblItem::class.java, itemId)

        return item
    }


    inner class GrabUserItemTask(itemId: String) : AsyncTask<String, Void, Boolean>() {
        var itemId = itemId

        override fun doInBackground(vararg params: String?): Boolean? {
            item = getItem(itemId)
            user = getUser(item?.userId!!)
            if(user != null && item != null)
                return true
            return false
        }

        override fun onPostExecute(success: Boolean?) {
            if (success!!) {
                Log.i(Main2Activity.TAG, "Item/User loaded for detailactivity")
            } else
                Log.i(Main2Activity.TAG, "Item/User not loaded for detailactivity")
        }
    }

    inner class DeleteItemTask(itemId: String): AsyncTask<String, Void, Boolean>(){
        var itemId = itemId

        override fun doInBackground(vararg params: String?): Boolean? {
            val item = tblItem()
            item.itemId = itemId

            dynamoDBMapper?.delete(item)

            return true
        }
    }

    inner class DownloadImageTask(img: ImageView) : AsyncTask<String, Void, Bitmap>() {
        var locImg: ImageView = img

        override fun doInBackground(vararg param: String?): Bitmap? {
            var urldisplay = param[0];
            var mIcon11: Bitmap? = null;

            if (!urldisplay!!.startsWith("http://"))
                urldisplay = "http://" + urldisplay
            Log.i(Main2Activity.TAG, "Download " + urldisplay)

            try {
                var inS: InputStream = URL(urldisplay).openStream()
                mIcon11 = BitmapFactory.decodeStream(inS)
                return mIcon11

            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(success: Bitmap?) {
            if (success != null) {
                Log.i(Main2Activity.TAG, "Finished downloading, set bitmap to image.")
                locImg.setImageBitmap(success)
            } else {
                Log.i(Main2Activity.TAG, "Image was not downlaoded successfully")
            }
        }
    }
}
