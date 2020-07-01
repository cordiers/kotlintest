package fr.strada.screens.home

import android.app.Dialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.usb.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.strada.R
import fr.strada.screens.auth.AuthentificationActivity
import fr.strada.screens.home.fragments.DrawerFragment
import fr.strada.screens.home.fragments.HomeFragment
import fr.strada.screens.home.fragments.LectureFragment
import fr.strada.screens.splash.SplachScreenActivity
import fr.strada.screens.splash.SplachScreenActivity.Companion.file
import fr.strada.utils.Constants
import fr.strada.utils.KeyBoardUtils

import fr.strada.utils.LocaleHelper
import fr.strada.utils.RealmManager
import kotlinx.android.synthetic.main.dialog_detectreader.*
import java.nio.ByteBuffer

class MainActivity : BaseActivity() {

    private var toolbar: Toolbar? = null
    private var drawerFragment: DrawerFragment? = null
    private lateinit var floatingActionButtonSettings : FloatingActionButton
    private val ACTION_USB_REQUEST_PERMISSION = "ACTION_USB_REQUEST_PERMISSION"

    var usbdevice: UsbDevice? = null
    var usbdeviceconnection: UsbDeviceConnection? = null
    var usbmanager: UsbManager? = null
    private var result: StringBuilder? = null

    lateinit var drawerLayout: DrawerLayout


    override fun onStart() {
        super.onStart()
        usbmanager = null
        usbdevice = null
        usbdeviceconnection = null
        val intentFilter2 = IntentFilter()
        intentFilter2.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED")
        intentFilter2.addAction("android.hardware.usb.action.USB_ACCESSORY_DETACHED")
        intentFilter2.addAction(ACTION_USB_REQUEST_PERMISSION)
        registerReceiver(this.usbAttachmentReceiver, intentFilter2)
        usbmanager = getSystemService(Context.USB_SERVICE) as UsbManager
        try
        {    if (!file!!.path.isNullOrEmpty())
               removeAllFragment(LectureFragment(),"",true)
        }catch (e:Exception)
        {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        floatingActionButtonSettings = findViewById(R.id.floatingActionButtonSettings)
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar?
        toolbar!!.title = ""

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar!!.setNavigationIcon(R.drawable.ic_propos)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        drawerFragment = supportFragmentManager.findFragmentById(R.id.fragment_navigation_drawer) as DrawerFragment
        drawerFragment!!.setUpDrawer(R.id.fragment_navigation_drawer, findViewById<View>(R.id.drawer_layout) as DrawerLayout, toolbar!!)

        drawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        ////
        var mDrawerToggle = object : ActionBarDrawerToggle(this,drawerLayout,toolbar, R.string.drawer_open, R.string.drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                KeyBoardUtils.hideKeyboard(this@MainActivity)
            }
        }
        drawerLayout.addDrawerListener(mDrawerToggle)
        //////
        val nMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        ///// si l'utilisateur click sur not show more notification
        if (intent.action == "Pause")
        {
            var type = intent.getIntExtra("type",-1)
            when(type){
                1 -> {
                    RealmManager.UpdateLastDownloadNotification(false)
                    nMgr.cancel(intent.getIntExtra("notificationId",0))
                }
                2 -> {
                    RealmManager.UpdateCardIdentificationNotification(false)
                    nMgr.cancel(intent.getIntExtra("notificationId",0))
                }
                else->{

                }
            }

        }
        /////

    }



    override fun onBackPressed()
    {   var lastFragment=supportFragmentManager.fragments.last()
        var isHomeFragmentPresent=  lastFragment is HomeFragment
        if(isHomeFragmentPresent)
        {   closeApp()

        }else
        {
            removeAllFragment(HomeFragment(), "Accueil") // back to home
        }
    }

    private fun closeApp()
    {
        var closeIntent = Intent(this@MainActivity, AuthentificationActivity::class.java)
        closeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        closeIntent.putExtra(Constants.EXIT, true)
        startActivity(closeIntent)
        finish()
    }

    fun removeAllFragment(f: Fragment, tag: String, cleanStack: Boolean = true) {
        val ft = supportFragmentManager.beginTransaction()
        if (cleanStack) {
            // clearBackStack();
            ft.remove(f)
        }
        ft.replace(R.id.container_body, f,tag)
        ft.addToBackStack(null)
        ft.commit()
    }

    private val usbAttachmentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null) {
                val extras = intent.extras
                var c: Char = 65535.toChar()
                val hashCode = action.hashCode()
                if (hashCode != -2114103349) {
                    if (hashCode != -1608292967) {
                        if (hashCode == -1555371901 && action == ACTION_USB_REQUEST_PERMISSION) {
                            c = 2.toChar()
                        }
                    } else if (action == "android.hardware.usb.action.USB_DEVICE_DETACHED") {
                        c = 1.toChar()
                    }
                } else if (action == "android.hardware.usb.action.USB_DEVICE_ATTACHED") {
                    c = 0.toChar()
                }
                val str = "device"
                if (c.toInt() != 0) {
                    if (c.toInt() != 1) {
                        if (c.toInt() == 2 && extras != null) {
                            val usbDevice = extras.get(str) as UsbDevice? ?: return
                            if (!usbmanager!!.hasPermission(usbDevice)) {
                                usbdevice = null
                                usbdeviceconnection = null
                            } else if (OPENUSBDEVICE(usbDevice)) {
                            } else {

                            }
                        }
                    } else if (extras != null) {
                        val usbDevice2 = extras.get(str) as UsbDevice?
                        if (usbDevice2 != null && usbDevice2 == usbdevice) {
                            usbdevice = null
                            usbdeviceconnection = null
                        }
                    }
                } else if (extras != null) {
                    val usbDevice3 = extras.get(str) as UsbDevice?
                    if (usbDevice3 != null) {
                        usbdevice = null
                        usbdeviceconnection = null

                        showDialog()

                    }
                }
            }
        }
    }
    internal var openDevice: UsbDeviceConnection? = null
    internal var usbEndpoint: UsbEndpoint? = null
    internal var usbEndpoint2: UsbEndpoint? = null
    fun OPENUSBDEVICE(usbDevice: UsbDevice): Boolean {
        val usbManager = usbmanager
        if (usbManager != null) {
            openDevice = usbManager.openDevice(usbDevice)
            if (openDevice != null) {

                var usbEndpoint3: UsbEndpoint? = null
                for (i in 0 until usbDevice.interfaceCount) {
                    val usbInterface = usbDevice.getInterface(i)
                    if ((usbInterface.interfaceClass == 11 || usbInterface.interfaceClass == 0) && (openDevice!!.claimInterface(
                            usbInterface,
                            false
                        ) || openDevice!!.claimInterface(usbInterface, true))
                    ) {
                        var usbEndpoint4 = usbEndpoint3
                        var usbEndpoint5 = usbEndpoint2
                        var usbEndpoint6 = usbEndpoint
                        for (i2 in 0 until usbInterface.endpointCount) {
                            val endpoint = usbInterface.getEndpoint(i2)
                            if (endpoint.type == 2 && endpoint.direction == 0) {
                                usbEndpoint6 = endpoint
                            }
                            if (endpoint.type == 2 && endpoint.direction == 128) {
                                usbEndpoint5 = endpoint
                            }
                            if (endpoint.type == 3 && endpoint.direction == 128) {
                                usbEndpoint4 = endpoint
                            }
                        }
                        usbEndpoint = usbEndpoint6
                        usbEndpoint2 = usbEndpoint5
                        usbEndpoint3 = usbEndpoint4
                    }
                }
                if (usbEndpoint == null || usbEndpoint2 == null || usbEndpoint3 == null) {
                    openDevice!!.close()
                    usbdevice = null
                    usbdeviceconnection = null
                    return false
                }

                val maxPacketSize = usbEndpoint3.maxPacketSize
                val allocate = ByteBuffer.allocate(maxPacketSize)
                val usbRequest = UsbRequest()
                usbRequest.initialize(openDevice, usbEndpoint3)
                usbRequest.clientData = allocate
                val usbDeviceConnection = openDevice
                usbdevice = usbDevice
                usbdeviceconnection = openDevice
                return true
            }
            usbdevice = null
            usbdeviceconnection = null
            return false
        }
        usbdevice = null
        usbdeviceconnection = null
        return false
    }
    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(this.usbAttachmentReceiver)
        }catch (e:Exception){

        }
    }

    fun showDialog(){
        val dialoAlertView = Dialog(this@MainActivity)
        dialoAlertView.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialoAlertView.setCancelable(false)
        dialoAlertView.setContentView(R.layout.dialog_detectreader)
        dialoAlertView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialoAlertView.btnScan.setOnClickListener {
            startActivity(Intent(this@MainActivity,ReaderActivityKotlin::class.java))
            dialoAlertView.dismiss()

        }

        dialoAlertView.btnExit.setOnClickListener {
            dialoAlertView.dismiss()
        }

        dialoAlertView.show()
    }




}
