package fr.strada.screens.auth

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.models.User
import fr.strada.screens.home.MainActivity
import fr.strada.utils.LocaleHelper
import fr.strada.utils.RealmManager
import fr.strada.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
import android.widget.Toast
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.result.Credentials
import com.auth0.android.Auth0Exception
import com.auth0.android.provider.VoidCallback
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.PasswordlessType
import com.auth0.android.callback.BaseCallback
import com.auth0.android.lock.AuthenticationCallback
import com.auth0.android.lock.Lock
import com.auth0.android.lock.LockCallback
import com.auth0.android.lock.utils.LockException
import com.auth0.android.management.ManagementException
import com.auth0.android.management.UsersAPIClient
import com.auth0.android.result.Authentication
import com.auth0.android.result.UserProfile


class SignInActivity : AppCompatActivity() , DatePickerDialog.OnDateSetListener {

    private var usersClient: UsersAPIClient?= null
    private var authenticationAPIClient: AuthenticationAPIClient? = null
    private var lock: Lock? = null
    var file : String? = null

    private var auth0: Auth0? = null

    val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
    val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"

    override fun onStart() {
        super.onStart()

        if (!SharedPreferencesUtils.isLoggedIn){
            RealmManager.open()
            RealmManager.clear()
            SharedPreferencesUtils.clear()
        }

        if (intent.hasExtra("file") && intent.getStringExtra("file")!=null)
        {
            file = intent.getStringExtra("file")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btnBack.setOnClickListener {
            onBackPressed().also {
                finish()
            }
        }
        inputDate.setOnClickListener {
                val c = Calendar.getInstance()
                val dpd = DatePickerDialog(
                    this,
                    this,
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
                )
            dpd.datePicker.maxDate = System.currentTimeMillis() - 568025136000L
            dpd.show()
        }

        auth0 =  Auth0(getString(R.string.com_auth0_client_id),getString(R.string.com_auth0_domain))
        auth0?.let {
            it.isOIDCConformant = true
        it.isLoggingEnabled = true

        }

        btnSignIn.setOnClickListener {
            // logout()
            login()


            var email = validateEmail()
            var phone = validateInput(inputPhone)
            if (email!=null && phone!=null && !TextUtils.isEmpty(inputDate.text)){

                if (StradaApp.instance!!.getUser().email == email && StradaApp.instance!!.getUser().phone == phone && StradaApp.instance!!.getUser().date == inputDate.text.toString()){

                    var i =  Intent(this@SignInActivity, MainActivity::class.java)
                    SharedPreferencesUtils.isLoggedIn =  true
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    i.putExtra("file",file)
                    startActivity(i).also {
                        finish()
                    }

                }else{


                    RealmManager.open()
                    RealmManager.clear()
                    SharedPreferencesUtils.clear()

                    if (checkbox.isChecked){
                        SharedPreferencesUtils.isLoggedIn =  true
                        StradaApp.instance!!.saveUser(User(email,phone,inputDate.text.toString()))
                    }

                    var i =  Intent(this@SignInActivity, MainActivity::class.java)
                    i.putExtra("file",file)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i).also { finish() }
                }

            }
        }
    }

    private fun logout() {
        WebAuthProvider.logout(auth0!!)
            .withScheme("strada")
            .start(this, object : VoidCallback {
                override fun onSuccess(payload: Void) {
                    login()
                }

                override fun onFailure(error: Auth0Exception) {
                    // Show error to user
                }
            })
    }


    private fun login() {




        lock = Lock.newBuilder(auth0!!, object : AuthenticationCallback() {
           override fun onAuthentication(credentials: Credentials?) {

               runOnUiThread {
                   Toast.makeText(
                       this@SignInActivity,
                       "onAuthentication: " + credentials?.accessToken,
                       Toast.LENGTH_SHORT
                   ).show()


                   usersClient =  UsersAPIClient(auth0, credentials?.accessToken);
                   authenticationAPIClient =  AuthenticationAPIClient(auth0!!);

                   getProfile(credentials?.accessToken!!);

               }
           }

           override fun onCanceled() {
               runOnUiThread {
                   Toast.makeText(
                       this@SignInActivity,
                       "onCanceled: ",
                       Toast.LENGTH_SHORT
                   ).show()
               }
           }

           override fun onError(error: LockException?) {
               runOnUiThread {
                   Toast.makeText(
                       this@SignInActivity,
                       "accessToken: " + error!!.message,
                       Toast.LENGTH_SHORT
                   ).show()
               }
           }


       })
            .withScheme("app")
            .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
            .allowSignUp(false)
            .hideMainScreenTitle(true)
            .allowForgotPassword(false)
            // Ad parameters to the Lock Builder
            .build(this)





        startActivity(lock?.newIntent(this))





    /*    WebAuthProvider.login(auth0!!)
            .withScheme("strada")
            .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
            .start(this, object : AuthCallback {
                override fun onFailure(dialog: Dialog) {
                    runOnUiThread { dialog.show() }
                }

                override fun onFailure(exception: AuthenticationException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@SignInActivity,
                            "Error: " + exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onSuccess(credentials: Credentials) {
                    runOnUiThread {
                      //  val intent = Intent(this@LoginActivity, MainActivity::class.java)
                      //  intent.putExtra(EXTRA_ACCESS_TOKEN, credentials.getAccessToken())
                       // startActivity(intent)
                       // finish()

                        Toast.makeText(
                            this@SignInActivity,
                            "Success: " + credentials.accessToken,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })*/
    }

    override fun onDateSet(datePicker: DatePicker, i: Int, i1: Int, i2: Int) {
        val datepicked =  i2.toString() + "/" + (i1 + 1).toString() + "/" + i
        inputDate.text = datepicked

    }
    private fun validateEmail(): String? {
        val emailText = inputEmail.text.toString()

        if (TextUtils.isEmpty(emailText)) {
            inputEmail.error = getString(R.string.input_error_required)
            return null
        } else if (!emailText.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
            inputEmail.error = getString(R.string.input_error_email)
            return null
        }

        return emailText
    }
    private fun validateInput(input : EditText): String? {

        if (TextUtils.isEmpty(input.text)) {
            input.error = getString(R.string.input_error_required)
            return null
        } else if (input.length()<5) {
            input.error = getString(R.string.input_error)
            return null
        }

        return input.text.toString()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onDestroy() {
        super.onDestroy()
        lock?.onDestroy(this)
        lock = null
    }



    private fun getProfile(accessToken : String) {
    authenticationAPIClient?.userInfo(accessToken)!!.start(object : BaseCallback<UserProfile, AuthenticationException> {
            override fun onSuccess(payload: UserProfile?) {

                usersClient?.getProfile(payload!!.id)!!.start(object :
                    BaseCallback<UserProfile, ManagementException> {
                    override fun onSuccess(payload: UserProfile?) {
                       // SharedPreferencesUtils.isLoggedIn =  true
                       // StradaApp.instance!!.saveUser(User(payload!!.email,payload.,))


                        runOnUiThread {
                            Toast.makeText(
                                this@SignInActivity,
                                "Info: " + payload!!.name + payload!!.email + payload!!.pictureURL,
                                Toast.LENGTH_SHORT
                            ).show()
                        }



                    }

                    override fun onFailure(error: ManagementException?) {

                    }
                })

            }

            override fun onFailure(error: AuthenticationException?) {

            }
        })

}



}
