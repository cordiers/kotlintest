package fr.strada.screens.splash

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import androidx.core.content.FileProvider
import fr.strada.R
import fr.strada.screens.auth.AuthentificationActivity
import fr.strada.screens.home.BaseActivity
import fr.strada.screens.home.MainActivity
import fr.strada.utils.SharedPreferencesUtils
import android.util.Log
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splach_screen.*
import java.text.SimpleDateFormat
import java.util.*


class SplachScreenActivity : BaseActivity() {

    val SPLASHSCREENACTIVITYTIME : Long = 1100

    companion object {
        var file : Uri? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splach_screen)

        var rotateAnimation= AnimationUtils.loadAnimation(this,R.anim.rotate)
        imgLogo.startAnimation(rotateAnimation)
        /////////////////////
        //testing if ther is a file shreed here or oppened using Strada
        onSharedIntent()
        Handler().postDelayed({
            // animation rotation
                if (SharedPreferencesUtils.isLoggedIn || !file?.path.isNullOrEmpty()){
                    var intent = Intent(this@SplachScreenActivity,MainActivity::class.java)
                    startActivity(intent).also {
                        finish()
                    }
                }else{
                    var intent = Intent(this@SplachScreenActivity,AuthentificationActivity::class.java)
                    startActivity(intent).also {
                        finish()
                    }
                }
        },SPLASHSCREENACTIVITYTIME)
    }


    // Test Opened or Shared File C1B/DDD/TGD
    fun onSharedIntent() {
        var receiverdIntent = intent
        var receivedAction = receiverdIntent.action
        var receivedType = receiverdIntent.type
        Log.i("receivedUri",receivedType.toString())
        if (receivedAction.equals(Intent.ACTION_SEND) || receivedAction.equals(Intent.ACTION_VIEW)) {
            if (receivedType == "application/octet-stream" || receivedType == "" || receivedType == null)
            {
                var receivedUri = receiverdIntent.data
                if (receivedUri != null)
                {   file = receivedUri
                    Log.i("receivedUri",receivedUri.toString())
                    Log.i("receivedUri",receivedUri.scheme)
                    Log.i("receivedUri",receivedUri.path)
                }else
                {
                    var receivedUri = receiverdIntent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri?
                    file = receivedUri
                    Log.i("receivedUri",receivedUri.toString())
                    Log.i("receivedUri",receivedUri!!.scheme)
                    Log.i("receivedUri",receivedUri!!.path)
                }
            }else if(receivedType.toString() == "application/unknown")
            {
                var receivedUri = receiverdIntent.data
                file = receivedUri
                Log.i("receivedUri",receivedUri.toString())
                Log.i("receivedUri",receivedUri!!.scheme)
                Log.i("receivedUri",receivedUri!!.path)

            }else
            {
                file = null
                var receivedUri = receiverdIntent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri?
                // Log.i("receivedUri",receivedUri.toString())
                // Log.i("receivedUri",receivedUri!!.scheme)
                // Log.i("receivedUri",receivedUri!!.path)
            }
        }else
        {
            file = null
        }
    }

}
