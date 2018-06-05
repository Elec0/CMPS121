package cs121.sideoftheroad

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import cs121.sideoftheroad.dbmapper.tables.tblUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.regex.Pattern
import kotlin.concurrent.thread
import kotlin.math.ln

class RegisterActivity : AppCompatActivity() {

    private var dynamoDBMapper: DynamoDBMapper? = null
    private var loginStr: String = ""
    private var emailStr: String = ""
    private var fNameStr: String = ""
    private var lNameStr: String = ""
    private var passwordStr: String  = ""
    private var mAuthTask: UserRegisterTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val client = AmazonDynamoDBClient(AWSMobileClient.getInstance().credentialsProvider)
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(client)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .build()

        register_button2.setOnClickListener { attemptRegister() }
    }

    private fun attemptRegister(){
        userName.error = null
        email2.error = null
        var focusView: View? = null
        var cancel = false

        loginStr = userName.text.toString()
        emailStr = email2.text.toString()
        fNameStr = fName.text.toString()
        lNameStr = lName.text.toString()
        passwordStr = password2.text.toString()

        if (loginStr.isEmpty() || emailStr.isEmpty() || fNameStr.isEmpty() || lNameStr.isEmpty() || passwordStr.isEmpty()){
            Toast.makeText(applicationContext,"There is an empty field.",Toast.LENGTH_SHORT).show()
        }else if(!emailStr.contains("@ucsc.edu")){
            email2.error = "Please enter a UCSC email"
            email2.requestFocus()
        }else{
            Log.d("Result of register user", "userId: " + loginStr + " password: " + passwordStr + " email: " +
                emailStr + " FirstN: " + fNameStr + " LastN: " + lNameStr)

            var newUser: tblUser = tblUser()
            newUser.userId = loginStr
            newUser.email = emailStr
            newUser.fName = fNameStr
            newUser.lName = lNameStr
            newUser.password = passwordStr

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView?.requestFocus()
            } else {
                mAuthTask = UserRegisterTask(true, newUser)
                mAuthTask!!.execute(null as Void?)
            }
        }
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

    private fun createUser(user: tblUser){

            dynamoDBMapper?.save(user)

    }

    inner class UserRegisterTask internal constructor(private val isAllowed: Boolean, private val user: tblUser) : AsyncTask<Void, Void, Boolean>() {
        var emailValid: Boolean = true
        var userValid: Boolean = true

        override fun doInBackground(vararg params: Void): Boolean? {
            var existUser1: tblUser? = getUser(loginStr)
            Log.d("existuser", existUser1.toString())
                if(existUser1?.email == user.email){
                    emailValid = false
                }
                if(existUser1?.userId == user.userId){
                    userValid = false
                }
            if(!userValid || !emailValid){
                return false
            }
            createUser(user)
            finish()

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTask = null

            if (success!!) {
                val intent = Intent(this@RegisterActivity,Main2Activity::class.java)
                startActivity(intent)
                finish()
            }else{
                if(!emailValid){
                    email2.error = getString(R.string.error_email_exist)
                    email2.requestFocus()
                }
                if(!userValid){
                    userName.error = getString(R.string.error_username_exist)
                    userName.requestFocus()
                }
            }
        }

        override fun onCancelled() {
            mAuthTask = null
        }
    }
}
