package fr.strada.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.usb.*
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.ramijemli.percentagechartview.PercentageChartView
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.models.ApplicationIdentification
import fr.strada.models.DataCard
import fr.strada.models.Identification
import fr.strada.models.LectureHistory
import fr.strada.utils.*
import fr.strada.utils.Convert.HexStringToByteArray
import fr.strada.utils.Convert.PerformHashOfFileByte
import fr.strada.utils.Convert.ToByte
import fr.strada.utils.Convert.ToHexString
import fr.strada.utils.Convert.hexStringToByteArray
import fr.strada.utils.cardlib.BinaryReader
import fr.strada.utils.cardlib.DataParsing.*
import fr.strada.utils.cardlib.DataParsingG2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.codec.binary.Hex
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and


class ReaderActivityKotlin : AppCompatActivity() {

    internal var database = FirebaseDatabase.getInstance()
    internal var myRef = database.getReference("card")
    internal var identification: Identification? = null
    private var result: StringBuilder? = null
    private val buffer = ByteArray(300)
    internal var i = 0
    internal lateinit var binaryReader: BinaryReader

    internal var ScanMode = false

    private val stateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            if (action == STATE) {
                val extras = intent.extras
                if (extras != null) {
                    state =
                        extras.get(EXTRA_STATE) as State?
                    if (state != null) {
                        val sb = StringBuilder()
                        sb.append("Reader: stateChangeReceiver = ")
                        sb.append(state)
                        when (state!!.ordinal + 1) {
                            1 -> {
                                Log.d("Smart Card", "state_connect_card_reader")
                                this@ReaderActivityKotlin.textViewState!!.setText(R.string.state_connect_card_reader)
                                this@ReaderActivityKotlin.imageViewState!!.setImageDrawable(ContextCompat.getDrawable(this@ReaderActivityKotlin,R.drawable.ic_plug_usb))
                                return
                            }
                            2 -> {
                                Log.d("Smart Card", "state_grant_usb_device")
                                this@ReaderActivityKotlin.textViewState!!.setText(R.string.state_grant_usb_device)
                                return
                            }
                            3 -> {
                                Log.d("Smart Card", "state_grant_refused")
                                this@ReaderActivityKotlin.textViewState!!.setText(R.string.state_grant_refused)
                                this@ReaderActivityKotlin.imageViewState!!.setImageDrawable(
                                    ContextCompat.getDrawable(this@ReaderActivityKotlin,
                                        R.drawable.ic_plug_usb
                                    )
                                )
                                return
                            }
                            4 -> {
                                Log.d("Smart Card", "state_connect_card_reader")
                                this@ReaderActivityKotlin.textViewState!!.setText(R.string.state_connect_card_reader)
                                this@ReaderActivityKotlin.imageViewState!!.setImageDrawable(
                                    ContextCompat.getDrawable(this@ReaderActivityKotlin,
                                        R.drawable.ic_plug_usb
                                    )
                                )

                                return
                            }
                            5 -> {
                                Log.d("Smart Card", "state_insert_driver_card")
                                this@ReaderActivityKotlin.textViewState!!.setText(R.string.state_insert_driver_card)
                                progressBar!!.setProgress(0f, false)

                                keyy = getRandomString(10)
                                this@ReaderActivityKotlin.imageViewState!!.setImageDrawable(
                                    ContextCompat.getDrawable(this@ReaderActivityKotlin,
                                        R.drawable.ic_plug_in_card
                                    )
                                )
                                index = 1
                                return
                            }
                            7 -> {
                                Log.d("Smart Card", "state_reading")
                                this@ReaderActivityKotlin.textViewState!!.setText(R.string.state_reading)
                                start()
                                return
                            }
                            6 -> {
                                Log.d("Smart Card", "state_use_driver_card")
                                this@ReaderActivityKotlin.textViewState!!.setText(R.string.state_use_driver_card)
                                this@ReaderActivityKotlin.imageViewState!!.setImageDrawable(
                                    ContextCompat.getDrawable(this@ReaderActivityKotlin,
                                        R.drawable.ic_plug_out_card
                                    )
                                )
                                return
                            }
                            8 -> {
                                Log.d("Smart Card", "state_remove_driver_card")
                                this@ReaderActivityKotlin.textViewState!!.setText(R.string.state_remove_driver_card)
                                this@ReaderActivityKotlin.imageViewState!!.setImageDrawable(
                                    ContextCompat.getDrawable(this@ReaderActivityKotlin,
                                        R.drawable.ic_plug_out_card
                                    )
                                )
                                return
                            }
                            else -> return
                        }
                    }
                }
            } else if (action == STATE_READ) {

            }
        }
    }
    private var Reading: Boolean = false
    private var isNewCard: Boolean = false
    private lateinit var applicationIdentification: ApplicationIdentification

    internal var handler = Handler()

    private val cardInsertationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return

            if (action == SMART_CARD_PUT_IN) {

                PowerOff()
                PowerOn()
                SETSTATE(this@ReaderActivityKotlin, State.reading)


            } else if (action != SMART_CARD_GET_OUT) {
                Log.d("Smart Card", "SMART_CARD_GET_OUT")
                progressBar!!.visibility = View.INVISIBLE
                imageViewState!!.visibility = View.VISIBLE
                SETSTATE(this@ReaderActivityKotlin, State.insert_driver_card)

            } else {
                Log.d("Smart Card", "SMART_CARD_GET_OUT")
                if (state != State.connect_card_reader) {
                    progressBar!!.visibility = View.INVISIBLE
                    imageViewState!!.visibility = View.VISIBLE
                    SETSTATE(
                        this@ReaderActivityKotlin,
                        State.insert_driver_card
                    )
                    return
                }

            }
        }
    }

    private var textViewState: TextView? = null
    private var imageViewState: ImageView? = null
    private var btnBack: ImageView? = null
    private var progressBar: PercentageChartView? = null
    internal lateinit var loader: Loader

    private val usbAttachmentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null) {
                val sb = StringBuilder()
                sb.append("LobolReader: usbAttachmentReceiver = ")
                sb.append(action)
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
                            val usbDevice = extras.get(str) as UsbDevice?
                            val booleanValue =
                                (extras.get(EXTRA_ACTION_USB_REQUEST_PERMISSION_ONCREATE) as Boolean)
                            if (usbDevice == null) {
                                return
                            }
                            if (!usbmanager!!.hasPermission(usbDevice)) {
                                usbdevice = null
                                usbdeviceconnection = null
                                SETSTATE(this@ReaderActivityKotlin, State.grant_refused)
                            } else if (this@ReaderActivityKotlin.OPENUSBDEVICE(usbDevice)) {
                                SETSTATE(this@ReaderActivityKotlin, State.insert_driver_card)

                                write(
                                    openDevice!!,
                                    usbEndpoint,
                                    hexStringToByteArray("63000000000000000000")
                                )
                                val bArr = readPowerOFF(openDevice!!, usbEndpoint2!!)
                                val b = bArr[0]
                                if (b.toInt() == -127 || b == java.lang.Byte.MIN_VALUE || b.toInt() == -126) {
                                    val b2 = bArr[7]
                                    val b3 = (b2 and 3).toByte()
                                    if (b3.toInt() == 0) {
                                        ICCstate = ICCSSTATUS.ICC_PRESENT_AND_ACTIVE
                                    }
                                    if (b3.toInt() == 1) {
                                        ICCstate = ICCSSTATUS.ICC_PRESENT_AND_INACTIVE
                                    }
                                    if (b3.toInt() == 2) {
                                        ICCstate = ICCSSTATUS.ICC_NOT_PRESENT
                                    }
                                    if (b3.toInt() == 3) {
                                        ICCstate = ICCSSTATUS.RFU
                                    }
                                    //                                Log.d("ICCstate",ICCstate.name());

                                }
                                if (booleanValue) {

                                    if (ICCstate == ICCSSTATUS.ICC_PRESENT_AND_ACTIVE || ICCstate == ICCSSTATUS.ICC_PRESENT_AND_INACTIVE) {
                                        this@ReaderActivityKotlin.sendBroadcast(
                                            Intent(
                                                SMART_CARD_PUT_IN
                                            )
                                        )
                                    }
                                    if (ICCstate == ICCSSTATUS.ICC_NOT_PRESENT) {
                                        this@ReaderActivityKotlin.sendBroadcast(
                                            Intent(
                                                SMART_CARD_GET_OUT
                                            )
                                        )
                                    }

                                }
                            } else {
                                SETSTATE(
                                    this@ReaderActivityKotlin,
                                    State.use_ccid_card_reader
                                )
                            }
                        }
                    } else if (extras != null) {
                        val usbDevice2 = extras.get(str) as UsbDevice?
                        if (usbDevice2 != null && usbDevice2 == usbdevice) {
                            usbdevice = null
                            usbdeviceconnection = null
                            SETSTATE(this@ReaderActivityKotlin, State.connect_card_reader)
                            this@ReaderActivityKotlin.sendBroadcast(Intent(SMART_CARD_GET_OUT))
                        }
                    }
                } else if (extras != null) {
                    val usbDevice3 = extras.get(str) as UsbDevice?
                    if (usbDevice3 != null) {
                        usbdevice = null
                        usbdeviceconnection = null
                        SETSTATE(this@ReaderActivityKotlin, State.grant_usb_device)
                        this@ReaderActivityKotlin.REQUESTPERMISSION(usbDevice3, false)
                    }
                }
            }
        }
    }
    internal var openDevice: UsbDeviceConnection? = null
    internal var usbEndpoint: UsbEndpoint? = null
    internal var usbEndpoint2: UsbEndpoint? = null

    internal var sbM = StringBuilder()

    lateinit var keyy: String

    internal var br = ByteArray(0)
    internal var Number = 0

    override fun onStart() {
        super.onStart()
        if (intent.hasExtra("from")) {
            if (intent.extras!!.containsKey("from")) {
                if (intent.getStringExtra("from") == "scan") {
                    ScanMode = true
                }
            }
        }
        validatePermissions()
    }

    fun SetDownloadTime(j: Long): Boolean {
        return if (SelectFile(1294)) {
            doUpdateBinary(j)
        } else false
    }

    private fun doUpdateBinary(j: Long): Boolean {

        val holder = Holder()
        if (!this.IO(this.XferBlock(UpdateBinary(j)), holder)) {
            return false
        }
        val GetByteArray = holder.GetByteArray()
        val holder2 = Holder()
        val holder3 = Holder()
        return DataBlockInfo(
                GetByteArray,
                GetByteArray.size,
                holder2,
                holder3,
                Holder(),
                Holder()
            )
    }

    var ResultData : String = ""

    var readCardThread:Thread? = null
    var t1:Thread? = null
    var index = 1;

    private fun start() {
        Log.i("TestThreads","start")
        Reading = true
        progressBar!!.visibility = View.VISIBLE
        imageViewState!!.visibility = View.INVISIBLE
        // btnBack!!.isEnabled = false now stop read is ok

        readCardThread = object : Thread() {
            override fun run() {
                try {
                    val calendarUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    var time = calendarUtc.timeInMillis / 1000
                    SetDownloadTime(time)
                    val TACHO_Card_Download = ReadFile(FILE_TACHO_Card_Download, 4, true)
                    val cardDownload = ReadCardDownload(BinaryReader(hexStringToByteArray(TACHO_Card_Download.resultData.substring(20, TACHO_Card_Download.resultData.length - 4))))
                    val TACHO_Driving_Licence_Info = ReadFile(FILE_TACHO_Driving_Licence_Info, 53, true)
                    val (cardDrivingLicenceInformation) = ReadDrivingLicenceInfo(BinaryReader(hexStringToByteArray(TACHO_Driving_Licence_Info.resultData.substring(20, TACHO_Driving_Licence_Info.resultData.length - 4))))
                    runOnUiThread {
                        try {
                            progressBar!!.setProgress(35f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val TACHO_Events_Data = ReadFile(FILE_TACHO_Events_Data, 1728, true)
                    val (cardEventData) = ReadEventsData(BinaryReader(hexStringToByteArray(TACHO_Events_Data.resultData)), applicationIdentification.driverCardApplicationIdentification.noOfEventsPerType)
                    runOnUiThread {
                        try {
                        progressBar!!.setProgress(41f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val TACHO_Faults_Data = ReadFile(FILE_TACHO_Faults_Data, 1152, true)
                    runOnUiThread {
                        try {
                        progressBar!!.setProgress(45f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val TACHO_Driver_Activity_Data = ReadFile(FILE_TACHO_Driver_Activity_Data, 13780, true)
                    runOnUiThread {
                        try {
                        progressBar!!.setProgress(50f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    // no 9000 a la fin de donne
                    var (cardDriverActivity) = ReadDriverActivityData(BinaryReader(hexStringToByteArray(TACHO_Driver_Activity_Data.resultData)), 13776)
                    runOnUiThread {
                        try {
                        progressBar!!.setProgress(63f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val TACHO_Vehicles_Used = ReadFile(FILE_TACHO_Vehicles_Used, 6202, true)
                    runOnUiThread {
                        try {
                        progressBar!!.setProgress(65f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val cardVehiclesUsed = ReadCardVehiclesUsed(BinaryReader(hexStringToByteArray(TACHO_Vehicles_Used.resultData)), applicationIdentification.driverCardApplicationIdentification.noOfCardVehicleRecords)
                    runOnUiThread {
                        try {
                        progressBar!!.setProgress(72f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val TACHO_Places = ReadFile(FILE_TACHO_Places, 1121, true)
                    val TACHO_Current_Usage = ReadFile(FILE_TACHO_Current_Usage, 19, true)
                    val (cardCurrentUse) = ReadCurrentUsage(BinaryReader(hexStringToByteArray(TACHO_Current_Usage.resultData.substring(20, TACHO_Current_Usage.resultData.length - 4))))
                    runOnUiThread {
                        try {
                        progressBar!!.setProgress(79f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val TACHO_Control_Activity_Data = ReadFile(FILE_TACHO_Control_Activity_Data, 46, true)
                    val (cardControlActivityDataRecord) = ReadControlActivityData(BinaryReader(hexStringToByteArray(TACHO_Control_Activity_Data.resultData.substring(20, TACHO_Control_Activity_Data.resultData.length - 4))))
                    runOnUiThread {
                        try {
                        progressBar!!.setProgress(85f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val TACHO_Specific_Conditions = ReadFile(FILE_TACHO_Specific_Conditions, 280, true)
                    runOnUiThread {
                        try {
                        progressBar!!.setProgress(93f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }

                    ResultData=ResultData +
                            TACHO_Card_Download.toString(4)+
                            TACHO_Driving_Licence_Info.toString(53)+
                            TACHO_Events_Data.toString(1728)+
                            TACHO_Faults_Data.toString(1152)+
                            TACHO_Driver_Activity_Data.toString(13780)+
                            TACHO_Vehicles_Used.toString(6202)+
                            TACHO_Places.toString(1121)+
                            TACHO_Current_Usage.toString(19)+
                            TACHO_Control_Activity_Data.toString(46)+
                            TACHO_Specific_Conditions.toString(280)

                    if (ChangeDirectoryTACHO_G2()){
                        val ICC = ReadFile(FILE_ICC, 25, false,"SMRDT")
                        val IC = ReadFile(FILE_IC, 8, false,"SMRDT")
                        val TACHOG2_Application_Identifier = ReadFile(FILE_TACHO_Application_Identifier, 17, true,"SMRDT")
                        var test = hexStringToByteArray(TACHOG2_Application_Identifier.resultData.substring(20, TACHOG2_Application_Identifier.resultData.length - 4))
                        var Application_Identifier_G2 = DataParsingG2.ReadApplicationIdentification(BinaryReader(test))

                        val TACHOG2_Card_Certificate = ReadFile(FILE_TACHOG2_CardMA_Certificate, 205, false,"SMRDT")
                        val TACHOG2_CardSignCertificate = ReadFile(FILE_TACHOG2_CardSignCertificate, 205, false,"SMRDT")
                        val TACHOG2_CA_Certificate = ReadFile(FILE_TACHO_CA_Certificate, 205, false,"SMRDT")
                        val TACHOG2_Link_Certificat = ReadFile(FILE_TACHOG2_Link_Certificat, 204, false,"SMRDT")

                        val TACHOG2_Identifier = ReadFile(FILE_TACHO_Identification, 143, true,"SMRDT")
                        runOnUiThread {
                            try {
                            progressBar!!.setProgress(95f, false)
                            }catch (ex:Exception){
                                Log.i("ExceptionProgressBar",ex.message)
                            }

                        }
                        val TACHOG2_Card_Download = ReadFile(FILE_TACHO_Card_Download, 4, true ,"SMRDT")
                        val TACHOG2_Driving_Licence_Info = ReadFile(FILE_TACHO_Driving_Licence_Info, 53, true,"SMRDT")
                        val TACHOG2_Events_Data = ReadFile(FILE_TACHO_Events_Data, 3168, true,"SMRDT")
                        val TACHOG2_Faults_Data = ReadFile(FILE_TACHO_Faults_Data, 1152, true,"SMRDT")

                        val TACHOG2_Driver_Activity_Data = ReadFile(FILE_TACHO_Driver_Activity_Data, 13780, true,"SMRDT")
                        runOnUiThread {
                            try {
                            progressBar!!.setProgress(96f, false)
                            }catch (ex:Exception){
                                Log.i("ExceptionProgressBar",ex.message)
                            }
                        }

                        val TACHOG2_Vehicles_Used = ReadFile(FILE_TACHO_Vehicles_Used, 9602, true,"SMRDT")
                        runOnUiThread {
                            try {
                            progressBar!!.setProgress(97f, false)
                            }catch (ex:Exception){
                                Log.i("ExceptionProgressBar",ex.message)
                            }
                        }

                        val TACHOG2_Specific_Conditions = ReadFile(FILE_TACHO_Specific_Conditions, 562, true,"SMRDT")
                        val TACHOG2_VehicleUnits_Used = ReadFile(FILE_TACHO_VehiclesUNITIS_Used, 2002, true,"SMRDT")
                        runOnUiThread {
                            try {
                            progressBar!!.setProgress(98f, false)
                            }catch (ex:Exception){
                                Log.i("ExceptionProgressBar",ex.message)
                            }
                        }

                        val TACHOG2_PLACES = ReadFile(FILE_TACHO_Places, 2354, true,"SMRDT")
                        runOnUiThread {
                            try {
                            progressBar!!.setProgress(99f, false)
                            }catch (ex:Exception){
                                Log.i("ExceptionProgressBar",ex.message)
                            }
                        }
                        val TACHOG2_Current_Usage = ReadFile(FILE_TACHO_Current_Usage, 19, true,"SMRDT")

                        val TACHO_GNSS_PLACES = ReadFile(FILE_TACHOG2_Gnss_Places, 6050, true,"SMRDT")
                        var GNSS_PLACES = DataParsingG2.ReadGnssPlaces(BinaryReader(hexStringToByteArrayy(TACHO_GNSS_PLACES.resultData)),Application_Identifier_G2.driverCardApplicationIdentification!!.noOfGNSSCDRecords)
                        val TACHOG2_Control_Activity_Data = ReadFile(FILE_TACHO_Control_Activity_Data, 46, true,"SMRDT")

                        ResultData = ResultData +
                                ICC.toStringDataG2(17)+
                                IC.toStringDataG2(8)+
                                TACHOG2_Card_Certificate.toStringDataG2(205)+
                                TACHOG2_CardSignCertificate.toStringDataG2(205)+
                                TACHOG2_CA_Certificate.toStringDataG2(205)+
                                TACHOG2_Link_Certificat.toStringDataG2(204)+
                                TACHOG2_Card_Download.toString(4,3)+
                                TACHOG2_Driving_Licence_Info.toString(53,3)+
                                TACHOG2_Events_Data.toString(3168,3)+
                                TACHOG2_Faults_Data.toString(1152,3)+
                                TACHOG2_Application_Identifier.toString(17,3) +
                                TACHOG2_Identifier.toString(143,3) +
                                TACHOG2_PLACES.toString(2354,3) +
                                TACHO_GNSS_PLACES.toString(6050,3) +
                                TACHOG2_Driver_Activity_Data.toString(13780,3) +
                                TACHOG2_Vehicles_Used.toString(9602,3) +
                                TACHOG2_Specific_Conditions.toString(562,3) +
                                TACHOG2_VehicleUnits_Used.toString(2002,3)+
                                TACHOG2_Current_Usage.toString(19,3)+
                                TACHOG2_Control_Activity_Data.toString(46,3)

                    }

                    PowerOff()
                    runOnUiThread {
                        try {
                        progressBar!!.setProgress(97f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    runOnUiThread {

                        if (isNewCard) {

                            createUpdateFile("F_"+identification!!.cardIdentification.cardNumber!!.driverIdentification+SimpleDateFormat("yyMMddHHmm").format(Date())+"."+SharedPreferencesUtils.fileType,ResultData)
                            StradaApp.instance!!.saveUserName(identification!!.driverCardHolderIdentification.cardHolderName.holderSurname, identification!!.cardIdentification.cardNumber!!.driverIdentification)
                            var emailUser= if(SharedPreferencesUtils.isLoggedIn) StradaApp.instance!!.getUser().email else ""
                            val lectureHistory = LectureHistory(file.path, identification!!.cardIdentification.cardNumber!!.driverIdentification, Date().toString(),emailUser)
                            RealmManager.clearCard()
                            RealmManager.saveHistory(lectureHistory)
                            cardDriverActivity!!.CardId = identification!!.cardIdentification.cardNumber!!.driverIdentification
                            RealmManager.saveCardDriverActivity(cardDriverActivity)
                            RealmManager.saveCardIdentification(identification!!.cardIdentification)
                            RealmManager.saveCardVehiclesUsed(cardVehiclesUsed)
                            RealmManager.saveCardDownload(cardDownload)
                            startActivity(Intent(this@ReaderActivityKotlin, MainActivity::class.java))
                            finish()
                            try {
                                progressBar!!.setProgress(100f, false)
                            }catch (ex:Exception){
                                Log.i("ExceptionProgressBar",ex.message)
                            }
                            Toast.makeText(this@ReaderActivityKotlin, getString(R.string.lecture_termine), Toast.LENGTH_SHORT).show()

                        } else {
                            createUpdateFile("F_"+identification!!.cardIdentification.cardNumber!!.driverIdentification+SimpleDateFormat("yyMMddHHmm").format(Date())+"."+SharedPreferencesUtils.fileType,ResultData)
                            SharedPreferencesUtils.isSCANNEDInFirstTime = true
                            StradaApp.instance!!.saveUserName(identification!!.driverCardHolderIdentification.cardHolderName.holderSurname, identification!!.cardIdentification.cardNumber!!.driverIdentification)
                            // code stable mais en cour de test
                            RealmManager.clearActivitiesAndVehicule()
                            ///
                            var emailUser= if(SharedPreferencesUtils.isLoggedIn) StradaApp.instance!!.getUser().email else ""
                            val lectureHistory = LectureHistory(file.path, identification!!.cardIdentification.cardNumber!!.driverIdentification, Date().toString(),emailUser)
                            RealmManager.saveHistory(lectureHistory)
                            cardDriverActivity!!.CardId = identification!!.cardIdentification.cardNumber!!.driverIdentification
                            RealmManager.saveCardDriverActivity(cardDriverActivity)
                            RealmManager.saveCardIdentification(identification!!.cardIdentification)
                            RealmManager.saveCardVehiclesUsed(cardVehiclesUsed)
                            RealmManager.saveCardDownload(cardDownload)
                            startActivity(Intent(this@ReaderActivityKotlin, MainActivity::class.java))
                            finish()
                            try {
                                progressBar!!.setProgress(100f, false)
                            }catch (ex:Exception){
                                Log.i("ExceptionProgressBar",ex.message)
                            }
                            Toast.makeText(this@ReaderActivityKotlin, getString(R.string.lecture_termine), Toast.LENGTH_SHORT).show()
                        }

                    }
                }catch (e: Exception){

                    runOnUiThread {
                        btnBack!!.isEnabled = true
                        try {
                            progressBar!!.visibility = View.INVISIBLE
                            imageViewState!!.visibility = View.VISIBLE
                            SETSTATE(this@ReaderActivityKotlin, ReaderActivityKotlin.State.use_driver_card)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }

                    }
                }

            }
        }


        t1 = object : Thread() {
            override fun run() {
                try {
                    Log.i("TestThreads","run thred 1")
                    val ICC = ReadFile(FILE_ICC, 25, false)
                    val card = ReadCardIccIdentification(BinaryReader(HexStringToByteArray(ICC.resultData.substring(20, ICC.resultData.length - 4))))
                    runOnUiThread {
                        try {
                          progressBar!!.setProgress(2f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val IC = ReadFile(FILE_IC, 8, false)
                    val ic = ReadIc(BinaryReader(HexStringToByteArray(IC.resultData.substring(20, IC.resultData.length - 4))))
                    runOnUiThread {
                        try {
                          progressBar!!.setProgress(6f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    ChangeDirectoryTACHO()
                    runOnUiThread {
                        try {
                          progressBar!!.setProgress(10f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val TACHO_Card_Certificate = ReadFile(FILE_TACHO_Card_Certificate, 194, false)
                    val TACHO_CA_Certificate = ReadFile(FILE_TACHO_CA_Certificate, 194, false)
                    runOnUiThread {
                        try {
                           progressBar!!.setProgress(15f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val TACHO_Application_Identifier = ReadFile(FILE_TACHO_Application_Identifier, 10, true)
                    val hexIden = TACHO_Application_Identifier.resultData.substring(20, TACHO_Application_Identifier.resultData.length - 4)
                    val byteIden = hexStringToByteArrayy(hexIden)
                    applicationIdentification = ReadApplicationIdentification(BinaryReader(byteIden))
                    runOnUiThread {
                        try {
                           progressBar!!.setProgress(23f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }
                    }
                    val TACHO_Identification = ReadFile(FILE_TACHO_Identification, 143, true)
                    identification = ReadIdentification(BinaryReader(HexStringToByteArray(TACHO_Identification.resultData.substring(20, TACHO_Identification.resultData.length - 4))))


                    runOnUiThread {


                        ResultData = ICC.toStringData(25)+
                                IC.toStringData(8)+
                                TACHO_Card_Certificate.toStringData(194)+
                                TACHO_CA_Certificate.toStringData(194)+
                                TACHO_Application_Identifier.toString(10)+
                                TACHO_Identification.toString(143)

                        try {
                            progressBar!!.setProgress(29f, false)
                        }catch (ex:Exception){
                            Log.i("ExceptionProgressBar",ex.message)
                        }

                        if (SharedPreferencesUtils.isSCANNEDInFirstTime && StradaApp.instance!!.getUser().cardNumber != identification!!.cardIdentification.cardNumber!!.driverIdentification) {
                            val datePickerView = Dialog(this@ReaderActivityKotlin)
                            datePickerView.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            datePickerView.setCancelable(false)
                            datePickerView.setContentView(R.layout.dialog_detect_new)
                            datePickerView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            datePickerView.findViewById<View>(R.id.btnDone).setOnClickListener {
                                if((readCardThread as Thread).isAlive == false) {

                                    isNewCard = true
                                    (readCardThread as Thread).start()
                                }
                                datePickerView.dismiss()
                            }

                            datePickerView.findViewById<View>(R.id.btnCancel).setOnClickListener {
                                startActivity(Intent(this@ReaderActivityKotlin, MainActivity::class.java))
                                finish()
                                datePickerView.dismiss()
                            }
                            if(index == 1) { // for fixe bug
                                index = 2
                                datePickerView.show()
                            }
                        }else {
                            // same carte or first scan
                            if((readCardThread as Thread).isAlive==false) {
                                (readCardThread as Thread).start()
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        try {
                            btnBack!!.isEnabled = true
                            progressBar!!.visibility = View.INVISIBLE
                            imageViewState!!.visibility = View.VISIBLE
                            SETSTATE(this@ReaderActivityKotlin, ReaderActivityKotlin.State.use_driver_card)
                        }catch (ex:Exception)
                        {
                            Log.i("ExceptionProgressBar",ex.message)
                        }

                    }

                }

                //do stuff
            }
        }
        (t1 as Thread).start()
    }

    fun convertHex4(n : Int) : String{
            return String.format("%04X", n)
    }
    lateinit var file : File

    private fun createUpdateFile(filenameExternal : String,cashback: String) {
        var state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED != state) {
            return
        }
        file = File(filesDir.parent + "/files/", filenameExternal)
        var outputStream : FileOutputStream? = null
        try {
            file.createNewFile()
            outputStream =  FileOutputStream(file, true)
            var bytes = Hex.decodeHex(cashback.toLowerCase().toCharArray())
            outputStream.write(bytes)
            outputStream.flush()
            outputStream.close()
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }



    fun XferBlock(bArr: ByteArray): ByteArray {
        val bArr2 = ByteArray(bArr.size + 10)
        bArr2[0] = PC_to_RDR_XfrBlock
        bArr2[1] = ToByte(bArr.size and 255)
        bArr2[2] = ToByte((bArr.size and MotionEventCompat.ACTION_POINTER_INDEX_MASK).ushr(8))
        bArr2[3] = ToByte((bArr.size and 16711680).ushr(16))
        bArr2[4] = ToByte((bArr.size and ViewCompat.MEASURED_STATE_MASK).ushr(24))
        bArr2[5] = 0
        bArr2[6] = sequence_number
        bArr2[7] = -1
        bArr2[8] = 0
        bArr2[9] = 0
        System.arraycopy(bArr, 0, bArr2, 10, bArr.size)
        IncreaseSequenceNumber()
        return bArr2
    }

    fun ToHexString(i: Int): String {
        return String.format("%08X", *arrayOf<Any>(Integer.valueOf(i)))
    }

    private fun SelectFile(i: Int): Boolean {
        val sb = StringBuilder()
        sb.append("SelectFile=")
        sb.append(ToHexString(i))
        Log.i("CardReaderTest", sb.toString())
        val holder = Holder()
        return this.IO(this.XferBlock(Selectfile(Integer.valueOf(i))), holder)
    }

    @Throws(Exception::class)
    fun ReadFile(fileId: Int, expectedLength: Int, computeSignature: Boolean , CardPath : String = "Tacho"): DataCard {

        val holder = Holder()
        val holder2 = Holder()
        val holder3 = Holder()

        val resSelect: Boolean
        val resRead: Boolean
        resSelect = SelectFile(fileId)
        resRead = ReadFile(expectedLength, holder, holder2)
        if (!resRead || !resSelect) {
            if (CardPath == "SMRDT")
                holder.Put(ByteArray(0))
            else
            throw Exception("NO CARD DETECTED")
        }

        if (computeSignature){

            var ok = PerformHashOfFile()
            if (ok){
                var isComputeDigitalSignature = ComputeDigitalSignature(holder3,CardPath)
            }else{
                do {
                    ok = PerformHashOfFile()
                    if (ok){
                       var isComputeDigitalSignature =  ComputeDigitalSignature(holder3,CardPath)
                    }
                }while (!ok)
            }

        }

        br = ByteArray(0)

     /*   return if (expectedLength > 255)
             DataCard(convertHex4(fileId),convertHex4(expectedLength),holder2.GetString(),"")

        else {
            DataCard(convertHex4(fileId),convertHex4(expectedLength),ToHexString(holder.GetByteArray(), 0, holder.GetByteArray().size),"")

        }

        */
      

        return if (expectedLength > 255)
            if (computeSignature)
                DataCard(convertHex4(fileId),convertHex4(expectedLength),holder2.GetString(),holder3.GetString(),CardPath)
            else
                DataCard(convertHex4(fileId),convertHex4(expectedLength),holder2.GetString(),"",CardPath)

        else {
            if(computeSignature)
            DataCard(convertHex4(fileId),convertHex4(expectedLength),ToHexString(holder.GetByteArray(), 0, holder.GetByteArray().size),holder3.GetString(),CardPath)
            else
            DataCard(convertHex4(fileId),convertHex4(expectedLength),ToHexString(holder.GetByteArray(), 0, holder.GetByteArray().size),"",CardPath)

        }



    }

    private fun validatePermissions() {
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (!report!!.areAllPermissionsGranted()){
                        validatePermissions()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {

                }
            })
            .check()
    }

     fun PerformHashOfFile() : Boolean  {
         Log.i("CardReaderTest", "PerformHashOfFile")

         var holder =  Holder()
         if (this.IO(XferBlock(PerformHashOfFileByte(0)), holder)) {
            var GetByteArray = holder.GetByteArray()
            var holder2 =  Holder()
            var holder3 =  Holder()
            var holder4 =  Holder()
            var holder5 =  Holder()
            if (DataBlockInfo(GetByteArray, GetByteArray.size, holder2, holder3, holder4, holder5)) {
                var holder6 =  Holder()
                var holder7 =  Holder()
                var holder8 =  Holder()
                if (Info(holder5.GetByteArray(), holder6, holder7, holder8)) {
                    var GetByte = holder7.GetByte()
                    var GetByte2 = holder8.GetByte()
                    if (GetByte.toInt() == -112 && GetByte2.toInt()== 0) {
                        return true
                    }
                }
            }
        }
        return false
     }

     fun ComputeDigitalSignature(holder : Holder, cardPath : String) : Boolean {
         Log.i("CardReaderTest", "ComputeDigitalSignature")
         var b = Byte.MIN_VALUE
         if (cardPath == "SMRDT") {
             b = 64
         }
         var holder2 =  Holder()
        if (this.IO(this.XferBlock(Convert.ComputeDigitalSignature(b)), holder2)) {
            var GetByteArray = holder2.GetByteArray()
            var holder3 =  Holder()
            var holder4 =  Holder()
            var holder5 =  Holder()
            var holder6 =  Holder()
            if (DataBlockInfo(GetByteArray, GetByteArray.size, holder3, holder4, holder5, holder6)) {
                var holder7 =  Holder()
                var holder8 =  Holder()
                if (Info(holder6.GetByteArray(), holder, holder7, holder8)) {
                    var GetByte = holder7.GetByte()
                    var GetByte2 = holder8.GetByte()
                    if (GetByte.toInt() == -112 && GetByte2.toInt() == 0) {
                        holder.Put(ToHexString(GetByteArray).substring(20, ToHexString(GetByteArray).length - 4))
                        return true
                    }
                }
            }
        }
        return false
    }

    // String hex = "";

    private fun ReadFile(i: Int, holder: Holder, holder2: Holder): Boolean {
        val bArr = ByteArray(i)
        var i5 = 0
        var hex = ""

        do {
            val i6 = i - i5
            val b = if (i6 < 254) i6.toByte() else -2

            if (!this.IO(this.XferBlock(ReadFile(Integer.valueOf(i5), b)), holder)) {
                return false
            }
            val GetByteArray = holder.GetByteArray()
            if (i > 255) {
                hex += ToHexString(GetByteArray).substring(20, ToHexString(GetByteArray).length - 4)
                //  br = (Bytes.concat(br,holder.GetByteArray()));
                holder2.Put(hex)
                //   holder2.Put(Bytes.concat(holder2.GetByteArray(),GetByteArray));
            }

            val holder3 = Holder()
            val holder4 = Holder()
            val holder5 = Holder()
            val holder6 = Holder()

            if (DataBlockInfo(
                    GetByteArray,
                    GetByteArray.size,
                    holder3,
                    holder4,
                    holder5,
                    holder6
                )
            ) {
                val holder7 = Holder()
                val holder8 = Holder()
                val holder9 = Holder()
                if (Info(holder6.GetByteArray(), holder7, holder8, holder9)) {
                    val GetByte = holder8.GetByte()
                    val GetByte2 = holder9.GetByte()
                    val GetByteArray2 = holder7.GetByteArray()
                    val length = GetByteArray2.size
                    if (GetByte.toInt() != -112) {
                        if (GetByte.toInt() == 97 || GetByte.toInt() != 103) {
                        }
                        return false
                    } else if (GetByte2.toInt() != 0) {
                        continue
                    } else if (length <= 0) {
                        return false
                    } else {
                        System.arraycopy(GetByteArray2, 0, bArr, i5, length)
                        i5 += length
                        //                        taskReadCard.SetProgress((i4 * 100) / i2);
                        continue
                    }
                } else {
                    continue
                }
            }
        } while (i5 != i)



        return true
    }

    internal fun DataBlockInfo(
        bArr: ByteArray,
        i: Int,
        holder: Holder,
        holder2: Holder,
        holder3: Holder,
        holder4: Holder
    ): Boolean {
        val holder5 = Holder()
        val holder6 = Holder()
        val holder7 = Holder()
        if (!Info(
                bArr,
                i,
                holder5,
                holder,
                holder2,
                holder6,
                holder7
            ) || holder5.GetByte() != java.lang.Byte.MIN_VALUE
        ) {
            holder4.Put(ByteArray(0))
            return false
        }
        holder4.Put(Arrays.copyOfRange(bArr, 10, holder7.GetInt() + 10))
        val GetByte = holder6.GetByte()
        return if (GetByte >= 1 && GetByte <= java.lang.Byte.MAX_VALUE || GetByte.toInt() == 0) {
            true
        } else true

    }

    fun Info(bArr: ByteArray, holder: Holder, holder2: Holder, holder3: Holder): Boolean {
        val length = bArr.size
        if (length < 2) {
            return false
        }
        val i = length - 2
        holder2.Put(bArr[i])
        holder3.Put(bArr[length - 1])
        holder.Put(Arrays.copyOfRange(bArr, 0, i))
        return true
    }

    internal fun Info(
        bArr: ByteArray,
        i: Int,
        holder: Holder,
        holder2: Holder,
        holder3: Holder,
        holder4: Holder,
        holder5: Holder
    ): Boolean {
        if (i >= 10) {
            val b = bArr[0]
            holder.Put(b)
            if (b.toInt() == -127 || b == java.lang.Byte.MIN_VALUE || b.toInt() == -126) {
                holder5.Put(ToInt(bArr, 1))
                val b2 = bArr[7]
                val b3 = (b2 and 3).toByte()
                holder4.Put(bArr[8])
                return true
            }
        }
        return false
    }

    internal fun ToInt(bArr: ByteArray, i: Int): Int {
        return ToInt(bArr[i + 3]) shl 24 or ToInt(bArr[i]) or (ToInt(bArr[i + 1]) shl 8) or (ToInt(
            bArr[i + 2]
        ) shl 16)
    }

    fun ToInt(b: Byte): Int {
        return b.toInt() and 255
    }

    private fun ChangeDirectoryTACHO(): Boolean {
        val sb = StringBuilder()
        sb.append("CardReader: ChangeDirectory = ")
        val str = "TACHO"
        sb.append(str)
        Log.i("CardReaderTest", sb.toString())

        val holder = Holder()
        if (this.IO(this.XferBlock(ChangeDirectoryd(str)), holder)) {
            val GetByteArray = holder.GetByteArray()
            val holder2 = Holder()
            val holder3 = Holder()
            val holder4 = Holder()
            val holder5 = Holder()
            if (DataBlockInfo(
                    GetByteArray,
                    GetByteArray.size,
                    holder2,
                    holder3,
                    holder4,
                    holder5
                )
            ) {
                val holder6 = Holder()
                val holder7 = Holder()
                val holder8 = Holder()
                if (Info(holder5.GetByteArray(), holder6, holder7, holder8)) {
                    val GetByte = holder7.GetByte()
                    val GetByte2 = holder8.GetByte()
                    if (GetByte.toInt() == -112 && GetByte2.toInt() == 0 || GetByte.toInt() == 106) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun ChangeDirectoryTACHO_G2(): Boolean {
        val sb = StringBuilder()
        sb.append("CardReader: ChangeDirectory = ")
        val str = "SMRDT"
        sb.append(str)
        Log.i("CardReaderTest", sb.toString())

        val holder = Holder()
        if (this.IO(this.XferBlock(ChangeDirectoryd(str)), holder)) {
            val GetByteArray = holder.GetByteArray()
            val holder2 = Holder()
            val holder3 = Holder()
            val holder4 = Holder()
            val holder5 = Holder()
            if (DataBlockInfo(
                    GetByteArray,
                    GetByteArray.size,
                    holder2,
                    holder3,
                    holder4,
                    holder5
                )
            ) {
                val holder6 = Holder()
                val holder7 = Holder()
                val holder8 = Holder()
                if (Info(holder5.GetByteArray(), holder6, holder7, holder8)) {
                    val GetByte = holder7.GetByte()
                    val GetByte2 = holder8.GetByte()
                    if (GetByte.toInt() == -112 && GetByte2.toInt() == 0 || GetByte.toInt() == 106) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun PowerOff(): Boolean? {
        Log.w("CardReader:", " PowerOff")
        val holder = Holder()
        return this.IO(this.IccPowerOff(), holder)
    }

    fun IccPowerOff(): ByteArray {
        val bArr = byteArrayOf(PC_to_RDR_IccPowerOff, 0, 0, 0, 0, 0, sequence_number, 0, 0, 0)
        IncreaseSequenceNumber()
        return bArr
    }

    enum class VoltageSelection {
        AUTOMATIC,
        V50,
        V30,
        V18
    }

    fun IccPowerOn(voltageSelection: VoltageSelection): ByteArray {
        val bArr = ByteArray(10)
        bArr[0] = PC_to_RDR_IccPowerOn
        bArr[1] = 0
        bArr[2] = 0
        bArr[3] = 0
        bArr[4] = 0
        bArr[5] = 0
        bArr[6] = sequence_number
        if (voltageSelection == VoltageSelection.AUTOMATIC) {
            bArr[7] = 0
        }
        if (voltageSelection == VoltageSelection.V50) {
            bArr[7] = 1
        }
        if (voltageSelection == VoltageSelection.V18) {
            bArr[7] = 2
        }
        if (voltageSelection == VoltageSelection.V30) {
            bArr[7] = 3
        }
        bArr[8] = 0
        bArr[9] = 0
        IncreaseSequenceNumber()
        return bArr
    }

    fun PowerOn(): Boolean {
        Log.w("CardReader:", " PowerON")
        val holder = Holder()
        for (voltageSelection in arrayOf(
            VoltageSelection.AUTOMATIC,
            VoltageSelection.V18,
            VoltageSelection.V30,
            VoltageSelection.V50
        )) {
            if (this.IO(this.IccPowerOn(voltageSelection), holder)) {
                return true
            }
        }
        return false
    }

    override fun onBackPressed()
    {

        if(t1!=null) {
            t1!!.interrupt()
        }

        if(readCardThread!=null) {
            readCardThread!!.interrupt()
        }

        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)
        RealmManager.open()
        loader = Loader.getInstance()

        textViewState = findViewById(R.id.textViewState)
        imageViewState = findViewById(R.id.imageViewState)
        btnBack = findViewById(R.id.btnBack)
        btnBack!!.setOnClickListener {
            onBackPressed()
        }
        progressBar = findViewById(R.id.progressBar)
        usbmanager = null
        usbdevice = null
        usbdeviceconnection = null

        val intentFilter = IntentFilter()
        intentFilter.addAction(STATE)
        intentFilter.addAction(STATE_READ)
        registerReceiver(this.stateChangeReceiver, intentFilter)

        val intentFilter2 = IntentFilter()
        intentFilter2.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED")
        intentFilter2.addAction("android.hardware.usb.action.USB_ACCESSORY_DETACHED")
        intentFilter2.addAction(ACTION_USB_REQUEST_PERMISSION)
        registerReceiver(this.usbAttachmentReceiver, intentFilter2)

        val intentFilter3 = IntentFilter()
        intentFilter3.addAction(SMART_CARD_PUT_IN)
        intentFilter3.addAction(SMART_CARD_GET_OUT)
        registerReceiver(this.cardInsertationReceiver, intentFilter3)
        SETSTATE(this, State.connect_card_reader)
        usbmanager = getSystemService(Context.USB_SERVICE) as UsbManager
        val usbManager = usbmanager
        if (usbManager != null) {
            for (REQUESTPERMISSION in usbManager.deviceList.values) {
                REQUESTPERMISSION(REQUESTPERMISSION, true)
            }
        }

    }

    internal enum class State {
        connect_card_reader,
        grant_usb_device,
        grant_refused,
        use_ccid_card_reader,
        insert_driver_card,
        use_driver_card,
        reading,
        remove_driver_card
    }

    internal enum class ICCSSTATUS {
        ICC_PRESENT_AND_ACTIVE,
        ICC_PRESENT_AND_INACTIVE,
        ICC_NOT_PRESENT,
        RFU
    }

    fun OPENUSBDEVICE(usbDevice: UsbDevice?): Boolean {
        val usbManager = usbmanager
        if (usbManager != null) {
            openDevice = usbManager.openDevice(usbDevice)
            if (openDevice != null) {

                var usbEndpoint3: UsbEndpoint? = null
                for (i in 0 until usbDevice!!.interfaceCount) {
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


                // comm = new COMM(openDevice, usbEndpoint, usbEndpoint2);
                // card = new CARD(comm, SLOT);
                val maxPacketSize = usbEndpoint3.maxPacketSize
                val allocate = ByteBuffer.allocate(maxPacketSize)
                val usbRequest = UsbRequest()
                usbRequest.initialize(openDevice, usbEndpoint3)
                usbRequest.clientData = allocate
                val usbDeviceConnection = openDevice
                val r3 = object : Thread() {
                    override fun run() {
                        while (true) {
                            usbRequest.queue(allocate, maxPacketSize)
                            val requestWait = usbDeviceConnection!!.requestWait()
                            if (requestWait == null) {
                                //MyLog.Write("LobolReader: USBREQUEST usbrequest = null");
                                usbDeviceConnection.close()
                                return
                            } else if (requestWait == usbRequest) {
                                val byteBuffer = usbRequest.clientData as ByteBuffer
                                val bArr = ByteArray(byteBuffer.position())
                                byteBuffer.rewind()
                                byteBuffer.get(bArr)
                                if (bArr.size == 2) {
                                    var z = false
                                    if (bArr[0].toInt() == 80) {
                                        if (bArr[1].toInt() and 1 == 1) {
                                            z = true
                                        }
                                        if (z) {
                                            this@ReaderActivityKotlin.sendBroadcast(
                                                Intent(
                                                    SMART_CARD_PUT_IN
                                                )
                                            )
                                        }
                                        if (!z) {
                                            this@ReaderActivityKotlin.sendBroadcast(
                                                Intent(
                                                    SMART_CARD_GET_OUT
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                r3.start()
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

    fun write(connection: UsbDeviceConnection, epOut: UsbEndpoint?, command: ByteArray) {
        result = StringBuilder()
        connection.bulkTransfer(epOut, command, command.size, 5000)
        //For Printing logs you can use result variable
        for (bb in command) {
            result!!.append(String.format(" %02X ", bb))
        }
        Log.d("Smart Card send was", result!!.toString())

    }

    fun readPowerOFF(connection: UsbDeviceConnection, epIn: UsbEndpoint): ByteArray {
        result = StringBuilder()
        val buffer = ByteArray(epIn.maxPacketSize)
        var byteCount = 0
        byteCount = connection.bulkTransfer(epIn, buffer, buffer.size, 5000)
        if (byteCount >= 0) {
            for (bb in buffer) {
                result!!.append(String.format(" %02X ", bb))
            }

            Log.d("Smart Card received was", ToHexString(buffer, 0, byteCount))

            //Buffer received was : result.toString()
        } else {
            //Something went wrong as count was : " + byteCount
        }
        return buffer
    }

    @SuppressLint("WrongConstant")
    fun REQUESTPERMISSION(usbDevice: UsbDevice?, z: Boolean) {
        if (usbmanager != null) {
            val intent = Intent(ACTION_USB_REQUEST_PERMISSION)
            intent.putExtra(EXTRA_ACTION_USB_REQUEST_PERMISSION_ONCREATE, z)
            usbmanager!!.requestPermission(
                usbDevice,
                PendingIntent.getBroadcast(this, 0, intent, 134217728)
            )
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        val usbDeviceConnection = usbdeviceconnection
        if (usbDeviceConnection != null) {
            usbDeviceConnection.close()
            usbdeviceconnection = null
        }
        unregisterReceiver(this.cardInsertationReceiver)
        unregisterReceiver(this.usbAttachmentReceiver)
        unregisterReceiver(this.stateChangeReceiver)
        RealmManager.close()
    }

    fun IO(bArr: ByteArray, holder: Holder): Boolean {
        val sb = StringBuilder()
        sb.append(">>> ")


        sb.append(ToHexString(bArr))
        val holder2 = Holder()
        val holder3 = Holder()
        val holder4 = Holder()
        val holder5 = Holder()
        val holder6 = Holder()
        Log.i("CardReaderTest", sb.toString())
        if (usbdeviceconnection!!.bulkTransfer(usbEndpoint, bArr, bArr.size, 1000) != bArr.size) {
            return false
        }
        while (true) {
            val usbDeviceConnection = usbdeviceconnection
            val usbEndpoint = this.usbEndpoint2
            val bArr4 = this.buffer
            var bulkTransfer = usbDeviceConnection!!.bulkTransfer(usbEndpoint, bArr4, bArr4.size, 5000)
            if (bulkTransfer < 0) {
                return false
            }else{
              var res =   ToHexString(this.buffer, 0, bulkTransfer)
              while (res.endsWith("0100")){
                  bulkTransfer = usbDeviceConnection.bulkTransfer(usbEndpoint, bArr4, bArr4.size, 5000)
                  val sb2 = StringBuilder()
                  sb2.append("<<< ")
                  //sb2.append(Convert.ToHexString(this.buffer, 0, bulkTransfer));
                  sb2.append(ToHexString(this.buffer, 0, bulkTransfer))
                  var result = sb2.toString()
                  //if (result.length>20)
                  Log.i("CardReaderTest", result)
                  res =   ToHexString(this.buffer, 0, bulkTransfer)
              }
            }


            val sb2 = StringBuilder()
            sb2.append("<<< ")
            //sb2.append(Convert.ToHexString(this.buffer, 0, bulkTransfer));
            sb2.append(ToHexString(this.buffer, 0, bulkTransfer))
            var result = sb2.toString()
            //if (result.length>20)
            Log.i("CardReaderTest", result)

         //   myRef.child(keyy).push().setValue(sb.toString())
         //   myRef.child(keyy).push().setValue(sb2.toString())

            if (!Info(this.buffer, bulkTransfer, holder2, holder3, holder4, holder6, holder5)) {
                return false
            }
            val sb3 = StringBuilder()
            sb3.append("Command=0x")
            sb3.append(ToHexString(holder2.GetByte()))
            sb3.append(", Error=")
            sb3.append(holder6.GetByte().toInt())
            sb3.append(", Length=")
            sb3.append(holder5.GetInt())
            Log.w("CardReaderTest", sb3.toString())
            return if (ToHexString(this.buffer, 0, bulkTransfer).endsWith("9000")) {
                holder.Put(bArr4.copyOfRange(0, bulkTransfer))
                true
            } else
                false
        }



    }

    companion object {
        private var sequence_number: Byte = 0
        val FILE_IC = 5
        val FILE_ICC = 2
        val FILE_TACHO_Application_Identifier = 1281
        val FILE_TACHO_CA_Certificate = 49416
        val FILE_TACHO_Card_Certificate = 49408
        val FILE_TACHO_Card_Download = 1294
        val FILE_TACHO_Control_Activity_Data = 1288
        val FILE_TACHO_Current_Usage = 1287
        val FILE_TACHO_Driver_Activity_Data = 1284
        val FILE_TACHO_Driving_Licence_Info = 1313
        val FILE_TACHO_Events_Data = 1282
        val FILE_TACHO_Faults_Data = 1283
        val FILE_TACHO_Identification = 1312
        val FILE_TACHO_Places = 1286
        val FILE_TACHO_Specific_Conditions = 1314
        val FILE_TACHO_Vehicles_Used = 1285
        val FILE_TACHO_VehiclesUNITIS_Used = 1315

        val FILE_TACHOG2_Gnss_Places = 1316
        val FILE_TACHOG2_CardMA_Certificate = 49408
        val FILE_TACHOG2_CardSignCertificate = 49409
        val FILE_TACHOG2_Link_Certificat = 49417

        private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
        private val ACTION_USB_REQUEST_PERMISSION = "ACTION_USB_REQUEST_PERMISSION"
        private val EXTRA_ACTION_USB_REQUEST_PERMISSION_ONCREATE = "oncreate"
        /* access modifiers changed from: private */
        var usbdevice: UsbDevice? = null
        /* access modifiers changed from: private */
        var usbdeviceconnection: UsbDeviceConnection? = null
        /* access modifiers changed from: private */
        var usbmanager: UsbManager? = null

        internal var state: State? = State.connect_card_reader
        internal lateinit var ICCstate: ICCSSTATUS
        private val EXTRA_STATE = "state"
        private val EXTRA_STATE_READ_PERCENT = "percent"
        private val SMART_CARD_GET_OUT = "SMART_CARD_GET_OUT"
        private val SMART_CARD_PUT_IN = "SMART_CARD_PUT_IN"
        private val STATE = "STATE"
        private val STATE_READ = "STATE_READ"

        fun UpdateBinary(j: Long): ByteArray {
            return byteArrayOf(
                0,
                -42,
                0,
                0,
                4,
                (-16777216 and j.toInt()).ushr(24).toByte(),
                (16711680 and j.toInt()).ushr(16).toByte(),
                (65280 and j.toInt()).ushr(8).toByte(),
                (j and 255).toInt().toByte()
            )
        }

        fun ReadFile(num: Int, b: Byte): ByteArray {
            return byteArrayOf(
                0,
                -80,
                ToByte((num and MotionEventCompat.ACTION_POINTER_INDEX_MASK).ushr(8)),
                ToByte(num and 255),
                b
            )
        }

        private fun IncreaseSequenceNumber() {
            sequence_number = (sequence_number + 1).toByte()
        }

        fun Selectfile(num: Int): ByteArray {
            return byteArrayOf(
                0,
                -92,
                2,
                12,
                2,
                ToByte((num and MotionEventCompat.ACTION_POINTER_INDEX_MASK).ushr(8)),
                ToByte(num and 255)
            )
        }

        private val PC_to_RDR_XfrBlock: Byte = 111

      /*  fun ChangeDirectory(str: String): ByteArray {
            val bArr = ByteArray(str.length + 6)
            bArr[0] = 0
            bArr[1] = -92
            bArr[2] = 4
            bArr[3] = 12
            bArr[4] = ToByte(str.length + 1)
            bArr[5] = -1
            bArr[6] = 84
            bArr[7] = 65
            bArr[8] = 67
            bArr[9] = 72
            bArr[10] = 79
            return bArr
        }
*/
        fun ChangeDirectoryd(str: String): ByteArray {
            var bArr = ByteArray(str.length + 6)
        bArr[0] = 0
          bArr[1] = -92
          bArr[2] = 4
          bArr[3] = 12
          bArr[4] = ToByte(str.length + 1)
          var bytes = str.toByteArray(Charset.forName("US-ASCII"))
          bArr[5] = -1
          bArr[6] = bytes[0]
          bArr[7] = bytes[1]
          bArr[8] = bytes[2]
          bArr[9] = bytes[3]
          bArr[10] = bytes[4]
          return bArr
      }

        private val PC_to_RDR_IccPowerOff: Byte = 99
        private val PC_to_RDR_IccPowerOn: Byte = 98

        internal fun SETSTATE(context: Context, state2: State) {
            val intent = Intent(STATE)
            intent.putExtra(EXTRA_STATE, state2)
            context.sendBroadcast(intent)
        }




        internal fun SETSTATEREAD(context: Context, i: Int) {
            val intent = Intent(STATE_READ)
            intent.putExtra(EXTRA_STATE_READ_PERCENT, i)
            context.sendBroadcast(intent)
        }

        private val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
        private fun getRandomString(sizeOfRandomString: Int): String {
            val random = Random()
            val sb = StringBuilder(sizeOfRandomString)
            for (i in 0 until sizeOfRandomString)
                sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
            return sb.toString()
        }
    }


}
