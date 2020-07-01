package fr.strada.screens.home.fragments


import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import com.developer.filepicker.model.DialogConfigs
import com.developer.filepicker.model.DialogProperties
import com.developer.filepicker.view.FilePickerDialog
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.models.*
import fr.strada.network.Webservices
import fr.strada.screens.home.MainActivity
import fr.strada.screens.home.ReaderActivityBluetoothKotlin
import fr.strada.screens.home.ReaderActivityKotlin
import fr.strada.screens.splash.SplachScreenActivity
import fr.strada.utils.Convert.HexStringToByteArray
import fr.strada.utils.Convert.ToHexString
import fr.strada.utils.LocaleHelper
import fr.strada.utils.RealmManager
import fr.strada.utils.SharedPreferencesUtils
import fr.strada.utils.Utils.log
import fr.strada.utils.cardlib.BinaryReader
import fr.strada.utils.cardlib.DataParsing
import fr.strada.utils.cardlib.DataParsingG2
import io.realm.Realm
import io.realm.internal.IOException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_lecture.*
import kotlinx.android.synthetic.main.toolbar.*
import org.apache.commons.codec.binary.Hex
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class LectureFragment : Fragment() {

    internal var database = FirebaseDatabase.getInstance()
    internal var myRef = database.getReference("cardImport")

    private var cardVehiclesUsed: CardVehiclesUsed? = null
    private var identification: Identification? = null
    private var eventsData: EventsData? = null
    private var driverActivityData: DriverActivityData? = null
    private var currentUsage: CurrentUsage? = null
    private var cardDownload: CardDownload? = null
    private var applicationIdentification: ApplicationIdentification? = null


    private var applicationIdentificationG2: ApplicationIdentificationTG2? = null
    private var gnssPlaces: GnssPlaces? = null

    private var ICC: CardIccIdentification? = null
    lateinit var IC : Ic

    private lateinit var mContext: Context

    private lateinit var adapter : RecyclerLectureAdapter

    // FILE TACHO G1
    val FILE_IC = "000500"
    val FILE_ICC = "000200"
    val FILE_TACHO_Application_Identifier = "050100"
    val FILE_TACHO_CA_Certificate = "C10800"
    val FILE_TACHO_Card_Certificate = "C10000"
    val FILE_TACHO_Card_Download = "050E00"
    val FILE_TACHO_Control_Activity_Data = "050800"
    val FILE_TACHO_Current_Usage = "050700"
    val FILE_TACHO_Driver_Activity_Data = "050400"
    val FILE_TACHO_Driving_Licence_Info = "052100"
    val FILE_TACHO_Events_Data = "050200"
    val FILE_TACHO_Faults_Data = "050300"
    val FILE_TACHO_Identification = "052000"
    val FILE_TACHO_Places = "050600"
    val FILE_TACHO_Specific_Conditions = "052200"
    val FILE_TACHO_Vehicles_Used = "050500"
    // FILE TACHO G2
    val FILE_TACHOG2_IC = "000502"
    val FILE_TACHOG2_ICC = "000202"
    val FILE_TACHOG2_Application_Identifier = "050102"
    val FILE_TACHOG2_Identification = "052002"
    val FILE_TACHOG2_CardMA_Certificate = "C10002"
    val FILE_TACHOG2_CardSignCertificate = "C10102"
    val FILE_TACHOG2_CA_Certificate = "C10802"
    val FILE_TACHOG2_Link_Certificate = "C10902"
    val FILE_TACHOG2_Card_Download = "050E02"
    val FILE_TACHOG2_Driving_Licence_Info = "052102"
    val FILE_TACHOG2_Events_Data = "050202"
    val FILE_TACHOG2_Faults_Data = "050302"
    val FILE_TACHOG2_Driver_Activity_Data = "050402"
    val FILE_TACHOG2_Vehicles_Used = "050502"
    val FILE_TACHOG2_Places = "050602"
    val FILE_TACHOG2_Current_Usage = "050702"
    val FILE_TACHOG2_Control_Activity_Data = "050802"
    val FILE_TACHOG2_Specific_Conditions = "052202"
    val FILE_TACHOG2_VehiclesUNITIS_Used = "052302"
    val FILE_TACHOG2_Gnss_Places = "052402"

    private lateinit var api:Webservices


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootview = inflater.inflate(R.layout.fragment_lecture, container, false)
        RealmManager.open()
        validatePermissions()
        return rootview
    }

    override fun onResume() {
        super.onResume()
        var emailUser=if(SharedPreferencesUtils.isLoggedIn) StradaApp.instance!!.getUser().email else ""
        if (RealmManager.loadHistoryByUser(emailUser).isNullOrEmpty()){
            layout_empty_lecture.visibility = View.VISIBLE
        }else{
            layout_empty_lecture.visibility = View.GONE
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        api= Webservices.create()

        btnLecture.setOnClickListener {
            val i = Intent(mContext, ReaderActivityKotlin::class.java)
            startActivity(i)
        }
        //--------------------app test or debug----------------//
        if(activity!!.getString(R.string.isVersionTest).equals("false")) // version prod
        {
            btnImport.visibility= GONE
            btnble.visibility = GONE
        }
        //--------------------app test or debug----------------//
        btnImport.setOnClickListener {

            var properties =  DialogProperties()
            properties.selection_mode = DialogConfigs.SINGLE_MODE
            properties.selection_type = DialogConfigs.FILE_SELECT
            properties.root =  File("mnt/sdcard")
            properties.error_dir =  File(DialogConfigs.DEFAULT_DIR)
            properties.offset =  File(DialogConfigs.DEFAULT_DIR)
            properties.extensions = arrayOf("c1b","tgd","ddd","C1B","TGD","DDD","Ddd","C1b","Tgd")
            properties.show_hidden_files = false
            var dialog =  FilePickerDialog(activity,properties)
            dialog.setTitle(resources.getString(R.string.selectionner_un_fichier))
            dialog.setDialogSelectionListener {
                files ->
                var file= File(files[0])
                readFile(file.inputStream().readBytes())
            }
            dialog.show()
        }


        btnble.setOnClickListener {
            val i = Intent(mContext, ReaderActivityBluetoothKotlin::class.java)
            startActivity(i)
        }

        rv_lecture.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        adapter = RecyclerLectureAdapter(activity!!,api)
        rv_lecture.adapter = adapter

        (activity as MainActivity).rlTitle.visibility = View.VISIBLE
        (activity as MainActivity).dropdown.visibility = View.INVISIBLE

        (activity as MainActivity).txttitle.text = resources.getString(R.string.lecture_de_la_carte)
        //(activity as MainActivity)?.toolbar.minimumHeight = 85

        (activity as MainActivity).txttitle.visibility = View.VISIBLE
        (activity as MainActivity).imgLogo.visibility = View.GONE
        (activity as MainActivity).rlIcon.visibility = View.INVISIBLE
        (activity as MainActivity).floatingActionButtonSettings.visibility = View.INVISIBLE


        try {

            if (!SplachScreenActivity.file!!.path.isNullOrEmpty()){
                var file = activity!!.contentResolver.openInputStream(SplachScreenActivity.file!!)
                readFile(file!!.readBytes())
                // set file null
                SplachScreenActivity.file = null
            }
        }catch (e:Exception){
            log("No file Found")
            Log.i("receivedUri","No file Found")
            // Toast.makeText(activity,"No file Found",Toast.LENGTH_SHORT).show()
        }
    }

    fun readFile(files: ByteArray){
        try {
            var DataList = decodeStrignC1bFile(ToHexString(files))


            for (data in DataList){
                when(data.substring(0,6)){
                    FILE_IC-> {
                        IC = DataParsing.ReadIc(BinaryReader(HexStringToByteArray(data.substring(6))))
                    }
                    FILE_ICC-> {
                        ICC = DataParsing.ReadCardIccIdentification(BinaryReader(HexStringToByteArray(data.substring(6))))
                    }
                    FILE_TACHO_Application_Identifier-> {
                        applicationIdentification = DataParsing.ReadApplicationIdentification(BinaryReader(HexStringToByteArray(data.substring(6))))
                    }
                    FILE_TACHO_CA_Certificate-> {
                    }
                    FILE_TACHO_Card_Certificate-> {
                    }
                    FILE_TACHO_Card_Download-> {
                        cardDownload = DataParsing.ReadCardDownload(BinaryReader(HexStringToByteArray(data.substring(6))))
                    }
                    FILE_TACHO_Control_Activity_Data-> {
                    }
                    FILE_TACHO_Current_Usage-> {
                        currentUsage = DataParsing.ReadCurrentUsage(BinaryReader(HexStringToByteArray(data.substring(6))))
                    }
                    FILE_TACHO_Driver_Activity_Data-> {
                        driverActivityData = DataParsing.ReadDriverActivityData(BinaryReader(HexStringToByteArray(data.substring(6))),applicationIdentification!!.driverCardApplicationIdentification.activityStructureLength)
                    }
                    FILE_TACHO_Driving_Licence_Info-> {
                    }
                    FILE_TACHO_Events_Data-> {
                        eventsData = DataParsing.ReadEventsData(BinaryReader(HexStringToByteArray(data.substring(6))),applicationIdentification!!.driverCardApplicationIdentification.noOfEventsPerType)
                    }
                    FILE_TACHO_Faults_Data-> {
                    }
                    FILE_TACHO_Identification-> {
                        identification  = DataParsing.ReadIdentification(BinaryReader(HexStringToByteArray(data.substring(6))))
                    }
                    FILE_TACHO_Places-> {
                    }
                    FILE_TACHO_Specific_Conditions-> {
                    }
                    FILE_TACHO_Vehicles_Used-> {
                        cardVehiclesUsed = DataParsing.ReadCardVehiclesUsed(BinaryReader(HexStringToByteArray(data.substring(6))),applicationIdentification!!.driverCardApplicationIdentification.noOfCardVehicleRecords)
                    }
                    FILE_TACHOG2_Application_Identifier->{
                        applicationIdentificationG2 = DataParsingG2.ReadApplicationIdentification(BinaryReader(HexStringToByteArray(data.substring(6))))
                    }
                    FILE_TACHOG2_Identification->{

                    }
                    FILE_TACHOG2_Gnss_Places->{
                        gnssPlaces = DataParsingG2.ReadGnssPlaces(BinaryReader(HexStringToByteArray(data.substring(6))),applicationIdentificationG2!!.driverCardApplicationIdentification!!.noOfGNSSCDRecords)
                    }
                    else -> {
                    }
                }
            }


            if (SharedPreferencesUtils.isSCANNEDInFirstTime && StradaApp.instance!!.getUser().cardNumber != identification!!.cardIdentification.cardNumber!!.driverIdentification) {
                val datePickerView = Dialog(activity!!)
                datePickerView.requestWindowFeature(Window.FEATURE_NO_TITLE)
                datePickerView.setCancelable(false)
                datePickerView.setContentView(R.layout.dialog_detect_new)
                datePickerView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                datePickerView.findViewById<View>(R.id.btnDone).setOnClickListener {
                    StradaApp.instance!!.saveUserName(identification!!.driverCardHolderIdentification.cardHolderName.holderSurname, identification!!.cardIdentification.cardNumber!!.driverIdentification)
                    createUpdateFile("F_"+identification!!.cardIdentification.cardNumber!!.driverIdentification+ SimpleDateFormat("yyMMddHHmm").format(Date())+"."+SharedPreferencesUtils.fileType,ToHexString(files))
                    var emailUser=if(SharedPreferencesUtils.isLoggedIn) StradaApp.instance!!.getUser().email else ""
                    val lectureHistory = LectureHistory(file.path, identification!!.cardIdentification.cardNumber!!.driverIdentification, Date().toString(),emailUser)
                    RealmManager.clearCard()
                    RealmManager.saveHistory(lectureHistory)
                    driverActivityData!!.cardDriverActivity.CardId = identification!!.cardIdentification.cardNumber!!.driverIdentification
                    RealmManager.saveCardDriverActivity(driverActivityData!!.cardDriverActivity)
                    RealmManager.saveCardIdentification(identification!!.cardIdentification)
                    RealmManager.saveCardVehiclesUsed(cardVehiclesUsed)
                    if (cardDownload!=null)
                        RealmManager.saveCardDownload(cardDownload)
                    Toast.makeText(activity, getString(R.string.lecture_termine), Toast.LENGTH_SHORT).show()

                /*    var realm = Realm.getDefaultInstance()

                    try {
    var file =  File(Environment.getExternalStorageDirectory().path + "/stradaDB.realm")
    if (file.exists()) {
        //noinspection ResultOfMethodCallIgnored
        file.delete()
    }

    realm.writeCopyTo(file)
    Toast.makeText(activity!!, "Success export realm file", Toast.LENGTH_SHORT).show()
} catch (e : IOException) {
    realm.close()
    e.printStackTrace()
}*/
                    datePickerView.dismiss()
                    adapter.notifyDataSetChanged()

                }

                datePickerView.findViewById<View>(R.id.btnCancel).setOnClickListener {
                    datePickerView.dismiss()
                }

                datePickerView.show()
            }else{
                SharedPreferencesUtils.isSCANNEDInFirstTime = true
                StradaApp.instance!!.saveUserName(identification!!.driverCardHolderIdentification.cardHolderName.holderSurname, identification!!.cardIdentification.cardNumber!!.driverIdentification)
                createUpdateFile("F_"+identification!!.cardIdentification.cardNumber!!.driverIdentification+ SimpleDateFormat("yyMMddHHmm").format(Date())+"."+SharedPreferencesUtils.fileType,ToHexString(files))
                // code stable mais en cour de test
                RealmManager.clearActivitiesAndVehicule()
                //
                var emailUser= if(SharedPreferencesUtils.isLoggedIn) StradaApp.instance!!.getUser().email else ""
                val lectureHistory = LectureHistory(file.path, identification!!.cardIdentification.cardNumber!!.driverIdentification, Date().toString(),emailUser)
                RealmManager.saveHistory(lectureHistory)
                driverActivityData!!.cardDriverActivity.CardId = identification!!.cardIdentification.cardNumber!!.driverIdentification
                RealmManager.saveCardDriverActivity(driverActivityData!!.cardDriverActivity)
                RealmManager.saveCardIdentification(identification!!.cardIdentification)
                RealmManager.saveCardVehiclesUsed(cardVehiclesUsed)
                if (cardDownload!=null)
                    RealmManager.saveCardDownload(cardDownload)

               /* var realm = Realm.getDefaultInstance()

                try {
                    var file =  File(Environment.getExternalStorageDirectory().path + "/stradaDB.realm")
                    if (file.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete()
                    }

                    realm.writeCopyTo(file)
                    Toast.makeText(activity!!, "Success export realm file", Toast.LENGTH_SHORT).show()
                } catch (e : IOException) {
                    realm.close()
                    e.printStackTrace()
                }*/

                Toast.makeText(activity, getString(R.string.lecture_termine), Toast.LENGTH_SHORT).show()
            }
            var emailUser=if(SharedPreferencesUtils.isLoggedIn) StradaApp.instance!!.getUser().email else ""
            if (RealmManager.loadHistoryByUser(emailUser).isNullOrEmpty()){
                layout_empty_lecture.visibility = View.VISIBLE
            }else{
                layout_empty_lecture.visibility = View.GONE
            }
            adapter.notifyDataSetChanged()
        }catch (e:Exception){
            Log.d("LectureExp",e.message)
            Toast.makeText(activity, resources.getString(R.string.erreur_de_lecture_du_fichier), Toast.LENGTH_SHORT).show()
        }

    }

    lateinit var file : File


    private fun createUpdateFile(filenameExternal : String, cashback: String) {
        var state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED != state) {
            return
        }
        file = File(activity!!.filesDir.parent + "/files/", filenameExternal)
        var outputStream : FileOutputStream? = null
        try {
            file.createNewFile()
            outputStream =  FileOutputStream(file, false)
            var bytes = Hex.decodeHex(cashback.toLowerCase().toCharArray())
            outputStream.write(bytes)
            outputStream.flush()
            outputStream.close()
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }




    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
        mContext = context
    }
    private fun validatePermissions() {
        Dexter.withActivity(activity)
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
                ) {  }
            })
            .check()
    }

    private fun decodeStrignC1bFile(data: String) : ArrayList<String>
    {
        var dataList : ArrayList<String> = ArrayList()

        var star = 0
        var i = data.length
        var size = 0

		while (star<=data.length){
            size = getsizeOfFile(data.substring(star,star+6))*2

            if (size != 256 && size != 128)
                dataList.add(data.substring(star,star+6) + data.substring(star+10, Math.min(data.length, star+10 + size)))

            star+=size+10

            if (star==i)
                break
        }
        return dataList
    }



    private fun getsizeOfFile(fileId : String) : Int {
        when(fileId) {
            FILE_IC-> {
                return 8
            }
            FILE_ICC-> {
                return 25
            }
            FILE_TACHO_Application_Identifier-> {
                return 10
            }

            FILE_TACHO_CA_Certificate-> {
                return 194
            }
            FILE_TACHO_Card_Certificate-> {
                return 194
            }
            FILE_TACHO_Card_Download-> {
                return 4
            }
            FILE_TACHO_Control_Activity_Data-> {
                return 46
            }
            FILE_TACHO_Current_Usage-> {
                return 19
            }
            FILE_TACHO_Driver_Activity_Data-> {
                return 13780
            }
            FILE_TACHO_Driving_Licence_Info-> {
                return 53
            }
            FILE_TACHO_Events_Data-> {
                return 1728
            }
            FILE_TACHO_Faults_Data-> {
                return 1152
            }
            FILE_TACHO_Identification-> {
                return 143
            }
            FILE_TACHO_Places-> {
                return 1121
            }
            FILE_TACHO_Specific_Conditions-> {
                return 280
            }
            FILE_TACHO_Vehicles_Used-> {
                return 6202
            }

            FILE_TACHOG2_IC-> {
                return 8
            }
            FILE_TACHOG2_ICC-> {
                return 25
            }
            FILE_TACHOG2_CardMA_Certificate->{
                return 205
            }
            FILE_TACHOG2_CardSignCertificate->{
                return 205
            }

            FILE_TACHOG2_Application_Identifier-> {
                return 17
            }
            FILE_TACHOG2_Control_Activity_Data->{
                return 46
            }
            FILE_TACHOG2_Current_Usage->{
                return 19
            }
            FILE_TACHOG2_Faults_Data->{
                return 1152
            }
            FILE_TACHOG2_Events_Data->{
                return 3168
            }
            FILE_TACHOG2_Card_Download-> {
                return 4
            }
            FILE_TACHOG2_CA_Certificate->{
                return 205
            }
            FILE_TACHOG2_Link_Certificate->{
                return 204
            }
            FILE_TACHOG2_Gnss_Places-> {
                return 6050
            }
            FILE_TACHOG2_Identification->{
                return 143
            }
            FILE_TACHOG2_Driver_Activity_Data->{
                return 13780
            }
            FILE_TACHOG2_Driving_Licence_Info-> {
                return 53
            }
            FILE_TACHOG2_Vehicles_Used->{
                return 9602

             }
            FILE_TACHOG2_Places->{
                return 2354

            }
            FILE_TACHOG2_Specific_Conditions->{
                return 562

            }
            FILE_TACHOG2_VehiclesUNITIS_Used->{
                return 2002
            }

            else -> {
                return if (fileId.endsWith("03"))
                    64
                else
                    128
            }


        }


    }

}