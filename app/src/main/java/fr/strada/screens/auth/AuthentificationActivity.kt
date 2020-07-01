package fr.strada.screens.auth

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.BaseCallback
import com.auth0.android.lock.AuthenticationCallback
import com.auth0.android.lock.Lock
import com.auth0.android.lock.utils.LockException
import com.auth0.android.management.ManagementException
import com.auth0.android.management.UsersAPIClient
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.models.*
import fr.strada.screens.home.MainActivity
import fr.strada.screens.home.ReaderActivityKotlin
import fr.strada.screens.home.fragments.RecyclerLectureAdapter
import fr.strada.utils.*
import fr.strada.utils.Constants
import fr.strada.utils.cardlib.BinaryReader
import fr.strada.utils.cardlib.DataParsing
import fr.strada.utils.cardlib.DataParsingG2
import kotlinx.android.synthetic.main.activity_authentification.*
import kotlinx.android.synthetic.main.activity_authentification.btnSignIn
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.dialog_beta.*
import kotlinx.android.synthetic.main.fragment_lecture.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AuthentificationActivity : AppCompatActivity() {

    private var auth0: Auth0? = null
    private var usersClient: UsersAPIClient?= null
    private var authenticationAPIClient: AuthenticationAPIClient? = null
    private var lock: Lock? = null
    private var manager: CredentialsManager? = null
    var loader : Loader = Loader.getInstance()


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentification)
        checkIfCloseAppTrue()
        showDialogAlertVersionBeta()
        /// setting up adapter // just for design
        val adapter = ViewPagerAdapter(this)
        viewPage.adapter = adapter
        indicator.setViewPager(viewPage)
        ///
        btnSignIn.setOnClickListener {
            login()
        }
        ///
        btnScan.setOnClickListener {
            startActivity(Intent(this@AuthentificationActivity, ReaderActivityKotlin::class.java).putExtra("from","scan"))
        }
        ///
    }

    private fun checkIfCloseAppTrue()
    {
        if (intent.getBooleanExtra(Constants.EXIT, false)) {
            finish()
        }
    }

    private fun login()
    {
        auth0 =  Auth0(getString(R.string.com_auth0_client_id),getString(R.string.com_auth0_domain))
        auth0?.let {
            it.isOIDCConformant = true
            it.isLoggingEnabled = true
        }
        authenticationAPIClient =  AuthenticationAPIClient(auth0!!)
        manager = CredentialsManager(authenticationAPIClient!!, SharedPreferencesStorage(this@AuthentificationActivity))

        lock = Lock.newBuilder(auth0!!, object : AuthenticationCallback()
        {
            override fun onAuthentication(credentials: Credentials?) {
                runOnUiThread {
                    loader.show(this@AuthentificationActivity)
                    //Toast.makeText(this@AuthentificationActivity, "onAuthentication: " + credentials?.accessToken, Toast.LENGTH_SHORT).show()
                    manager!!.saveCredentials(credentials!!)
                    SharedPreferencesUtils.refreshToken = credentials.refreshToken
                    SharedPreferencesUtils.idToken = credentials.idToken
                    Log.i("onAuthentication",credentials?.accessToken)
                    Log.i("onAuthentication",credentials?.expiresAt.toString())
                    Log.i("onAuthentication",credentials?.idToken.toString())
                    usersClient =  UsersAPIClient(auth0, credentials?.accessToken)
                    getProfile(credentials?.accessToken!!)
                }
            }

            override fun onCanceled() {
                runOnUiThread {
                    //Toast.makeText(this@AuthentificationActivity, "onCanceled: ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(error: LockException?) {
                runOnUiThread {
                    //Toast.makeText(this@AuthentificationActivity, "accessToken: " + error!!.message, Toast.LENGTH_SHORT).show()
                }
            }


        })  .withScheme("app")
            .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
            .withScope("openid offline_access")
            .allowSignUp(false)
            .hideMainScreenTitle(true)
            .allowForgotPassword(false)
            .closable(true)
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


    private fun getProfile(accessToken : String)
    {
        authenticationAPIClient?.userInfo(accessToken)!!.start(object :
            BaseCallback<UserProfile, AuthenticationException> {
            override fun onSuccess(payload: UserProfile?) {
                runOnUiThread {
                    // StradaApp.instance!!.saveUser(User(payload!!.email,payload.,))
                    RealmManager.open()
                    RealmManager.clear()
                    //SharedPreferencesUtils.clear()
                    StradaApp.instance?.clearUser()
                    // if (checkbox.isChecked){
                    //     SharedPreferencesUtils.isLoggedIn =  true
                    SharedPreferencesUtils.isLoggedIn =  true
                    SharedPreferencesUtils.isSCANNEDInFirstTime = false
                    StradaApp.instance!!.saveUser(User(payload!!.email,"",""))
                    // just for test
                    var lectureHistorys= RealmManager.loadHistoryByUser(payload!!.email)
                    var list:ArrayList<LectureHistory> = arrayListOf()
                    list.addAll(lectureHistorys)
                    Collections.sort(list)
                    if(list.size >0){
                        var lastFileReadedByUser = File(list.get(0).file)
                        readFile(lastFileReadedByUser.inputStream().readBytes())
                    }
                    ///
                    //  }
                    var i =  Intent(this@AuthentificationActivity, MainActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i).also {
                        loader.dismiss()
                        finish()
                    }
                }
            }

            override fun onFailure(error: AuthenticationException?)
            {

            }
        })

    }


    fun showDialogAlertVersionBeta()
    {
        val dialogAlert = Dialog(this)
        dialogAlert.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogAlert.setCancelable(false)
        dialogAlert.setContentView(R.layout.dialog_beta)
        dialogAlert.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAlert.btnDone.setOnClickListener {
            dialogAlert.dismiss()
        }
        dialogAlert.show()
    }


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

    fun readFile(files: ByteArray){
        try {
            var DataList = decodeStrignC1bFile(Convert.ToHexString(files))


            for (data in DataList){
                when(data.substring(0,6)){
                    FILE_IC-> {
                        IC = DataParsing.ReadIc(BinaryReader(Convert.HexStringToByteArray(data.substring(6))))
                    }
                    FILE_ICC-> {
                        ICC = DataParsing.ReadCardIccIdentification(BinaryReader(Convert.HexStringToByteArray(data.substring(6))))
                    }
                    FILE_TACHO_Application_Identifier-> {
                        applicationIdentification = DataParsing.ReadApplicationIdentification(
                            BinaryReader(Convert.HexStringToByteArray(data.substring(6)))
                        )
                    }
                    FILE_TACHO_CA_Certificate-> {
                    }
                    FILE_TACHO_Card_Certificate-> {
                    }
                    FILE_TACHO_Card_Download-> {
                        cardDownload = DataParsing.ReadCardDownload(BinaryReader(Convert.HexStringToByteArray(data.substring(6))))
                    }
                    FILE_TACHO_Control_Activity_Data-> {
                    }
                    FILE_TACHO_Current_Usage-> {
                        currentUsage = DataParsing.ReadCurrentUsage(BinaryReader(Convert.HexStringToByteArray(data.substring(6))))
                    }
                    FILE_TACHO_Driver_Activity_Data-> {
                        driverActivityData = DataParsing.ReadDriverActivityData(BinaryReader(Convert.HexStringToByteArray(data.substring(6))),applicationIdentification!!.driverCardApplicationIdentification.activityStructureLength)
                    }
                    FILE_TACHO_Driving_Licence_Info-> {
                    }
                    FILE_TACHO_Events_Data-> {
                        eventsData = DataParsing.ReadEventsData(BinaryReader(Convert.HexStringToByteArray(data.substring(6))),applicationIdentification!!.driverCardApplicationIdentification.noOfEventsPerType)
                    }
                    FILE_TACHO_Faults_Data-> {
                    }
                    FILE_TACHO_Identification-> {
                        identification  = DataParsing.ReadIdentification(BinaryReader(Convert.HexStringToByteArray(data.substring(6))))
                    }
                    FILE_TACHO_Places-> {
                    }
                    FILE_TACHO_Specific_Conditions-> {
                    }
                    FILE_TACHO_Vehicles_Used-> {
                        cardVehiclesUsed = DataParsing.ReadCardVehiclesUsed(BinaryReader(Convert.HexStringToByteArray(data.substring(6))),applicationIdentification!!.driverCardApplicationIdentification.noOfCardVehicleRecords)
                    }
                    FILE_TACHOG2_Application_Identifier->{
                        applicationIdentificationG2 = DataParsingG2.ReadApplicationIdentification(
                            BinaryReader(Convert.HexStringToByteArray(data.substring(6)))
                        )
                    }
                    FILE_TACHOG2_Identification->{

                    }
                    FILE_TACHOG2_Gnss_Places->{
                        gnssPlaces = DataParsingG2.ReadGnssPlaces(BinaryReader(Convert.HexStringToByteArray(data.substring(6))),applicationIdentificationG2!!.driverCardApplicationIdentification!!.noOfGNSSCDRecords)
                    }
                    else -> {
                    }
                }
            }
            /////////////////
            SharedPreferencesUtils.isSCANNEDInFirstTime = true
            StradaApp.instance!!.saveUserName(identification!!.driverCardHolderIdentification.cardHolderName.holderSurname, identification!!.cardIdentification.cardNumber!!.driverIdentification)
            // code stable mais en cour de test
            RealmManager.clearActivitiesAndVehicule()
            //
            driverActivityData!!.cardDriverActivity.CardId = identification!!.cardIdentification.cardNumber!!.driverIdentification
            RealmManager.saveCardDriverActivity(driverActivityData!!.cardDriverActivity)
            RealmManager.saveCardIdentification(identification!!.cardIdentification)
            RealmManager.saveCardVehiclesUsed(cardVehiclesUsed)
            if (cardDownload!=null)
                RealmManager.saveCardDownload(cardDownload)
            /////////////////
        }catch (e:Exception){
            Log.d("LectureExp",e.message)
        }

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
