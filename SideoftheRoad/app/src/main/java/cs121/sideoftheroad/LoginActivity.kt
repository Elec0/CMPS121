package cs121.sideoftheroad

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.AWSStartupResult
import com.amazonaws.mobile.client.AWSStartupHandler

import java.util.regex.Pattern

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import cs121.sideoftheroad.dbmapper.repos.UserRepo
import cs121.sideoftheroad.dbmapper.tables.tblUser
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private var mAuthTask: UserLoginTask? = null
    private var dynamoDBMapper: DynamoDBMapper? = null
    private var loginStr: String = ""
    private var passwordStr: String  = ""
    private var userFname: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        AWSMobileClient.getInstance().initialize(this) { Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!") }.execute()

        val client = AmazonDynamoDBClient(AWSMobileClient.getInstance().credentialsProvider)
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(client)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .build()

        login_button.setOnClickListener { attemptLogin() }
        register_button.setOnClickListener { sendRegisterActivity() }

    }
    private fun sendRegisterActivity(){
        val intent = Intent(this@LoginActivity,RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun attemptLogin(){
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        loginId.error = null
        password.error = null

        // Store values at the time of the login attempt.
        loginStr = loginId.text.toString()
        passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null


        // Check for a valid password, if the user entered one.
        // TODO: fix/find regex for password
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password.error = "Password is anything right now pls fix"
            focusView = password
            cancel = true
        }
        if (isEmailCheck(loginStr)) {
            // Check for a valid email address.
            if (TextUtils.isEmpty(loginStr)) {
                loginId.error = getString(R.string.error_loginid_required)
                focusView = loginId
                cancel = true
            } else if (!isEmailValid(loginStr)) {
                loginId.error = getString(R.string.error_invalid_email)
                focusView = loginId
                cancel = true
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = UserLoginTask(loginStr, passwordStr)
            mAuthTask!!.execute(null as Void?)
        }
    }

    private fun isEmailCheck(inputStr: String): Boolean {
        return inputStr.contains("@")
    }

    private fun isEmailValid(email: String): Boolean {
        val pattern = Pattern.compile("[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@ucsc.edu", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(loginStr)

        return matcher.find()
    }

    private fun isPasswordValid(password: String): Boolean{
        return true
    }

    // function that allows us to call the dynamodb to get the userobject
    // TODO: trying to figure out how to allow this to be called by creating an object
    private fun getUser(userId: String): tblUser? {
        var user: tblUser? = null

            user = dynamoDBMapper?.load(tblUser::class.java,
                    userId)

        return user
    }

    inner class UserLoginTask internal constructor(private val loginStr: String, private val passwordStr: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
           var user: tblUser? = getUser(loginStr)

            System.out.println("PLES PRINT" + user.toString())
            Log.d("THING",user.toString())
            if(user != null){
                userFname = user.fName
                return passwordStr == user.password
            }

            return false
            finish()
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTask = null

            if (success!!) {
                val intent = Intent(this@LoginActivity,Main2Activity::class.java)
                intent.putExtra("username", loginStr)
                intent.putExtra("userFname",userFname)
                startActivity(intent)
                finish()
            } else {
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
            }
        }

        override fun onCancelled() {
            mAuthTask = null
        }
    }

}
