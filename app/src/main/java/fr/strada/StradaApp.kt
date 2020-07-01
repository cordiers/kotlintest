package fr.strada

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import fr.strada.models.User
import fr.strada.utils.LocaleHelper
import io.realm.Realm
import io.realm.RealmConfiguration
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class StradaApp : Application() , LifecycleObserver {
    var prefs: SharedPreferences? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        FirebaseAnalytics.getInstance(this)
        FirebaseApp.initializeApp(this)

        //Fabric.with(this, Crashlytics())

        // new code
        Fabric.with(Fabric.Builder(this).kits(Crashlytics()).appIdentifier(BuildConfig.APPLICATION_ID).build())

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        Realm.init(this)
        val configuration = RealmConfiguration.Builder()
            .schemaVersion(1)
            .name(resources.getString(R.string.app_name) + ".realm")
            .deleteRealmIfMigrationNeeded()
            .build()

        Realm.setDefaultConfiguration(configuration)
    }

    companion object {
        private val PREFS_NAME = "Strada_PREFS"
        var instance: StradaApp? = null
            private set
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "fr"))
    }

    fun saveUser(user: User) {
        val editor = prefs!!.edit()
        editor.putString("email", user.email)
        editor.putString("phone", user.phone)
        editor.putString("date", user.date)
        editor.apply()
    }

    fun saveUserName(name: String,cardNumber:String) {
        val editor = prefs!!.edit()
        editor.putString("name", name)
        editor.putString("cardNumber",cardNumber)
        editor.apply()
    }

    fun clearUser(){
        prefs!!.edit().clear().apply()
    }

    fun getUser() : User{
        return User(prefs!!.getString("name","").toString(),prefs!!.getString("email","").toString(),prefs!!.getString("phone","").toString(),prefs!!.getString("date","").toString(),prefs!!.getString("cardNumber","").toString())
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun OnAppDestory(){

    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d("Lifecycle","background")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        // App in foreground
        Log.d("Lifecycle","foreground")

    }



}