package fr.strada.screens.home.fragments


import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shawnlin.numberpicker.NumberPicker
import fr.strada.R
import fr.strada.screens.home.MainActivity
import fr.strada.screens.notifications.broadcast_receivers.Actions
import fr.strada.screens.notifications.broadcast_receivers.ServiceState
import fr.strada.screens.notifications.broadcast_receivers.getServiceState
import fr.strada.screens.notifications.notifications.EndlessService
import fr.strada.utils.LocaleHelper
import fr.strada.utils.RealmManager
import fr.strada.utils.SharedPreferencesUtils
import fr.strada.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_dialog_add_document.btn_annuler
import kotlinx.android.synthetic.main.bottom_sheet_dialog_langue.*
import kotlinx.android.synthetic.main.dialog_fuseau.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import android.widget.CompoundButton
import fr.strada.screens.notifications.broadcast_receivers.setServiceState
import fr.strada.screens.notifications.notifications.DailyNotificationReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


class SettingsFragment : Fragment() {

    private lateinit var sheetBottmView: View
    private lateinit var sheetBottmDialog: BottomSheetDialog

    // reglage entreprise
    private var entreprises = listOf("Entreprise 1" , "Entreprise 2" , "Entreprise 3")


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootview = inflater.inflate(R.layout.fragment_settings, container, false)

        sheetBottmView = layoutInflater.inflate(R.layout.bottom_sheet_dialog_langue, null)
        sheetBottmDialog = BottomSheetDialog(activity!!, R.style.AppBottomSheetDialogThemeTransparent)
        sheetBottmDialog.setContentView(sheetBottmView)

        sheetBottmDialog.btn_annuler.setOnClickListener { sheetBottmDialog.dismiss() }

        sheetBottmDialog.btnFR.setOnClickListener {
            LocaleHelper.setLocale(activity,"fr")
            selelectLan("fr")
            sheetBottmDialog.dismiss()
            val intent = activity!!.intent
            activity!!.finish()
            startActivity(intent)
        }
        sheetBottmDialog.btnENG.setOnClickListener {
            LocaleHelper.setLocale(activity,"en")
            selelectLan("en")
            sheetBottmDialog.dismiss()
            val intent = activity!!.intent
            activity!!.finish()
            startActivity(intent)
        }
        sheetBottmDialog.btnES.setOnClickListener {
            LocaleHelper.setLocale(activity,"es")
            selelectLan("es")
            sheetBottmDialog.dismiss()
            val intent = activity!!.intent
            activity!!.finish()
            startActivity(intent)
        }
        rootview.cdLangue.setOnClickListener {
            //sheetBottmDialog.show()
            Toast.makeText(activity,"EN CONSTRUCTION",Toast.LENGTH_SHORT).show()
        }

        when(LocaleHelper.getLanguage(activity)){
            "fr"->{
                rootview.txtChangeLangue.setText(R.string.francais)
                selelectLan("fr")
            }
            "en"->{
                rootview.txtChangeLangue.setText(R.string.anglais)
                selelectLan("en")

            }
            "es"->{
                rootview.txtChangeLangue.setText(R.string.espagnol)
                selelectLan("es")

            }
        }

        rootview.switch_notifications.isChecked = getServiceState(activity!!) == ServiceState.STARTED
        // presses active desactive notification affichage
        if(getServiceState(activity!!) == ServiceState.STARTED)
        {
            rootview.lblActiveDesactiveNotifications.text = getResources().getString(R.string.active)
        }else
        {
            rootview.lblActiveDesactiveNotifications.text = getResources().getString(R.string.desactive)
        }
        //////
        rootview.switch_notifications.setOnCheckedChangeListener { _, b ->
            if (b) {
                rootview.lblActiveDesactiveNotifications.text = getResources().getString(R.string.active)
                // ancien code
                /*
                actionOnService(Actions.START)
                */
                // new code
                setServiceState(activity!!,ServiceState.STARTED)
                setAlarmManager()
            } else {
                rootview.lblActiveDesactiveNotifications.text = getResources().getString(R.string.desactive)
                // ancian code service
                /*
                actionOnService(Actions.STOP)
                */
                setServiceState(activity!!,ServiceState.STOPPED)
                cancelAlarmManager()
            }

        }
        return rootview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).txttitle.text = resources.getString(R.string.reglages)

        (activity as MainActivity).rlTitle.visibility = View.VISIBLE
        (activity as MainActivity).dropdown.visibility = View.INVISIBLE
        //(activity as MainActivity)?.toolbar.minimumHeight = 85
        (activity as MainActivity).txttitle.visibility = View.VISIBLE
        (activity as MainActivity).imgLogo.visibility = View.GONE
        (activity as MainActivity).rlIcon.visibility = View.INVISIBLE
        (activity as MainActivity).floatingActionButtonSettings.visibility = View.INVISIBLE
        ////////// process collaps and show div notifications
        headerNotifications.setOnClickListener {

            if (llNotificationIsActive.visibility == View.VISIBLE)
            {
                imgFlechNotification.setImageResource(R.drawable.ic_arrow_down)
                llNotificationIsActive.visibility = GONE
                llHeureNotification.visibility = GONE
            }else
            {
                imgFlechNotification.setImageResource(R.drawable.ic_arrow_up)
                llNotificationIsActive.visibility = VISIBLE
                llHeureNotification.visibility = VISIBLE
            }
        }
        ////////// process change heure notification
        txtHeureNotifications.setOnClickListener {
            val c = Calendar.getInstance()
            var mHour = c.get(Calendar.HOUR_OF_DAY)
            var mMinute = c.get(Calendar.MINUTE)
            try
            {
                mHour = SharedPreferencesUtils.heureNotification!!.substring(0,2).toInt() // get hours
                mMinute = SharedPreferencesUtils.heureNotification!!.substring(3,5).toInt()  // get minutes

            }catch (ex:Exception)
            {   Toast.makeText(activity,ex.message,Toast.LENGTH_SHORT).show()
            }

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(activity!!,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    var formatter : NumberFormat =  DecimalFormat("00")
                    txtHeureNotifications.text = formatter.format(hourOfDay)+":"+formatter.format(minute)
                    GlobalScope.launch {
                        SharedPreferencesUtils.heureNotification = formatter.format(hourOfDay)+":"+formatter.format(minute)
                        delay(1000) // seconds pour que time saved in chared prefernces
                        if(getServiceState(activity!!) == ServiceState.STARTED) // ilya des autre notification sets in system
                        {
                             cancelAlarmManager() // cancel anciens alarm
                             delay(1000)
                             setAlarmManager()
                        }
                    }

                }, mHour, mMinute, true
            )
            timePickerDialog.show()
        }
        //////

        ////////// recuperation de heure de notifiation
        txtHeureNotifications.text = SharedPreferencesUtils.heureNotification
        //////////
        cdFuseau.setOnClickListener {
            if (llUTC.visibility == View.VISIBLE){
                txtChangeFuseau.setCompoundDrawablesWithIntrinsicBounds(null,null, ContextCompat.getDrawable(activity!!,R.drawable.ic_arrow_down),null)
                txtFuseauMsg.visibility = View.GONE
                llReglageFuseau.visibility = View.GONE
                llLocal.visibility = View.GONE
                llEteHiver.visibility = View.GONE
                llDecalageEteHiver.visibility = GONE
                llUTC.visibility = GONE
                llHeureLocal.visibility = GONE

            }else
            {
                txtChangeFuseau.setCompoundDrawablesWithIntrinsicBounds(null,null,ContextCompat.getDrawable(activity!!,R.drawable.ic_arrow_up),null)
                txtFuseauMsg.visibility = View.VISIBLE
                llUTC.visibility = VISIBLE
                llHeureLocal.visibility = VISIBLE
                llEteHiver.visibility = View.VISIBLE
                /// precess decalage
                if(SharedPreferencesUtils.isEteHiver)
                {
                    txtDecalageEteHiver.text = "" + SharedPreferencesUtils.decalageMinutes
                    llDecalageEteHiver.visibility = VISIBLE

                }else
                {
                    txtDecalageEteHiver.text = "" + SharedPreferencesUtils.decalageMinutes
                    llDecalageEteHiver.visibility = GONE
                }
                // process de visiblilite
                if (SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty() && !SharedPreferencesUtils.timeFormat.equals("UTC")){
                    llLocal.visibility = View.VISIBLE
                    llReglageFuseau.visibility = View.VISIBLE
                }else if (!SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty()){
                    llLocal.visibility = View.VISIBLE
                    llReglageFuseau.visibility = View.VISIBLE
                }else{
                    llLocal.visibility = View.GONE
                    llReglageFuseau.visibility = View.GONE
                }
            }
        }

        cdFirstDayOfMonth.setOnClickListener {
            if (rgFirstDayOfMonth.visibility == View.VISIBLE){
                txtFirstDayOfMonth.setImageResource(R.drawable.ic_arrow_down)
                rgFirstDayOfMonth.visibility = View.GONE
            }else{
                txtFirstDayOfMonth.setImageResource(R.drawable.ic_arrow_up)
                rgFirstDayOfMonth.visibility = View.VISIBLE
            }
        }

        cdFormat.setOnClickListener {
            if (rgFormat.visibility == View.VISIBLE){
                txtChangeFormat.setImageResource(R.drawable.ic_arrow_down)
                rgFormat.visibility = View.GONE
            }else{
                txtChangeFormat.setImageResource(R.drawable.ic_arrow_up)
                rgFormat.visibility = View.VISIBLE
            }
        }

        cdNuit.setOnClickListener {
            if (llNuit.visibility == View.VISIBLE){
                txtNuit.setImageResource(R.drawable.ic_arrow_down)
                llNuit.visibility = View.GONE
            }else{
                txtNuit.setImageResource(R.drawable.ic_arrow_up)
                llNuit.visibility = View.VISIBLE
            }
        }
        txtNightStart.text = SharedPreferencesUtils.nightStart
        txtNightEnd.text = SharedPreferencesUtils.nightend

        txtNightStart.setOnClickListener {
            val c = Calendar.getInstance()
           var mHour = c.get(Calendar.HOUR_OF_DAY)
           var mMinute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(activity!!,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    var formatter : NumberFormat =  DecimalFormat("00");
                    txtNightStart.text = formatter.format(hourOfDay)+":"+formatter.format(minute)
                    SharedPreferencesUtils.nightStart = formatter.format(hourOfDay)+":"+formatter.format(minute)

                }, mHour, mMinute, true
            )
            timePickerDialog.show()
        }

        txtNightEnd.setOnClickListener {
            val c = Calendar.getInstance()
            var mHour = c.get(Calendar.HOUR_OF_DAY)
            var mMinute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(activity!!,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    var formatter : NumberFormat =  DecimalFormat("00");

                    txtNightEnd.text = formatter.format(hourOfDay)+":"+formatter.format(minute)
                    SharedPreferencesUtils.nightend = formatter.format(hourOfDay)+":"+formatter.format(minute)
                }, mHour, mMinute, true
            )
            timePickerDialog.show()
        }

        if (SharedPreferencesUtils.isTimeModeHHMM)
          rgHM.isChecked = true
        else rgHC.isChecked = true

        if (SharedPreferencesUtils.firstDayOfMonth==1)
            rgMonday.isChecked = true
        else rgSunday.isChecked = true



        rgFormat?.setOnCheckedChangeListener { _, checkedId ->
            SharedPreferencesUtils.isTimeModeHHMM = R.id.rgHM == checkedId
        }

        rgFirstDayOfMonth?.setOnCheckedChangeListener { _, checkedId ->
            if (R.id.rgMonday == checkedId)
                SharedPreferencesUtils.firstDayOfMonth = 1
            else
                SharedPreferencesUtils.firstDayOfMonth = 0
        }

        cdFileType.setOnClickListener {
            if (rgFileType.visibility == View.VISIBLE){
                txtFileType.setImageResource(R.drawable.ic_arrow_down)
                rgFileType.visibility = View.GONE
            }else{
                txtFileType.setImageResource(R.drawable.ic_arrow_up)
                rgFileType.visibility = View.VISIBLE
            }
        }

        when {
            SharedPreferencesUtils.fileType == "C1B" -> rgC1B.isChecked = true
            SharedPreferencesUtils.fileType == "DDD" -> rgDDD.isChecked = true
            else -> rgTGD.isChecked = true
        }

        rgFileType?.setOnCheckedChangeListener { _  , checkedId ->
            when (checkedId) {
                R.id.rgC1B -> SharedPreferencesUtils.fileType = "C1B"
                R.id.rgDDD -> SharedPreferencesUtils.fileType = "DDD"
                else -> SharedPreferencesUtils.fileType = "TGD"
            }
        }


        if (SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty() && !SharedPreferencesUtils.timeFormat.equals("UTC")){
            txtChangeFuseau.text = SharedPreferencesUtils.timeFormat
            swFuseau.isChecked = true
            swLocal.isChecked = false
            swUTC.isChecked = false
            swHeureLocal.isChecked = true
        }else if (!SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty()){
            txtChangeFuseau.text = SharedPreferencesUtils.timeFormatAUTO
            swFuseau.isChecked = false
            swLocal.isChecked = true
            swUTC.isChecked = false
            swHeureLocal.isChecked = true
        }else{
            swFuseau.isChecked = false
            swLocal.isChecked = false
            swUTC.isChecked = true
            swHeureLocal.isChecked = false
        }


        swHeureLocal.setOnCheckedChangeListener { _, b ->
            if (b){
                llLocal.visibility = VISIBLE
                llReglageFuseau.visibility = VISIBLE
                swFuseau.isChecked = false
                swLocal.isChecked = true
                swUTC.isChecked = false
                val timezoneID = TimeZone.getDefault()
                txtChangeFuseau.text = "UTC" + timezoneID.getDisplayName(false, TimeZone.SHORT).substring(3,6)
                SharedPreferencesUtils.timeFormatAUTO = "UTC" + timezoneID.getDisplayName(false, TimeZone.SHORT).substring(3,6)
                SharedPreferencesUtils.timeFormat= ""
                // active ete hive if is not active
                if(SharedPreferencesUtils.isEteHiver==false){
                    if(timezoneID.dstSavings != 0 )
                    {
                        swEteHiver.isChecked = true
                    }
                }
                //
            }else{
                SharedPreferencesUtils.timeFormat="UTC"
                SharedPreferencesUtils.timeFormatAUTO=""
                llLocal.visibility = GONE
                llReglageFuseau.visibility = GONE
                swFuseau.isChecked = false
                swLocal.isChecked = false
                swHeureLocal.isChecked = false
                swUTC.isChecked = true
            }
        }



        swUTC.setOnCheckedChangeListener { _, b ->
            if (b){
                SharedPreferencesUtils.timeFormat = "UTC"
                SharedPreferencesUtils.timeFormatAUTO=""
                llLocal.visibility = GONE
                llReglageFuseau.visibility = GONE
                swFuseau.isChecked = false
                swLocal.isChecked = false
                swHeureLocal.isChecked = false

            }else{
                llLocal.visibility = VISIBLE
                llReglageFuseau.visibility = VISIBLE
                swHeureLocal.isChecked = true
                swLocal.isChecked = true
                swFuseau.isChecked = false
                val timezoneID = TimeZone.getDefault()
                txtChangeFuseau.text = "UTC" + timezoneID.getDisplayName(false, TimeZone.SHORT).substring(3,6)
                SharedPreferencesUtils.timeFormatAUTO = "UTC" + timezoneID.getDisplayName(false, TimeZone.SHORT).substring(3,6)
                SharedPreferencesUtils.timeFormat = ""
                // active ete hive if is not active
                if(SharedPreferencesUtils.isEteHiver==false){
                    if(timezoneID.dstSavings != 0 )
                    {
                        swEteHiver.isChecked = true
                    }
                }
                ///
            }
        }
        /*
        swLocal.setOnCheckedChangeListener { _, b ->
            if (b){
                val timezoneID = TimeZone.getDefault()
                txtChangeFuseau.text = "UTC" + timezoneID.getDisplayName(false, TimeZone.SHORT).substring(3,6)
                swFuseau.isChecked = false
                swHeureLocal.isChecked = true
                swUTC.isChecked = false
                //
                SharedPreferencesUtils.timeFormatAUTO = "UTC" + timezoneID.getDisplayName(false, TimeZone.SHORT).substring(3,6)
            }else{
                if (!swFuseau.isChecked){
                    txtChangeFuseau.text = "UTC"
                }
                SharedPreferencesUtils.timeFormatAUTO = ""
                // retour au utc
                SharedPreferencesUtils.timeFormat="UTC"
                llLocal.visibility = GONE
                llReglageFuseau.visibility = GONE
                swFuseau.isChecked = false
                swLocal.isChecked = false
                swHeureLocal.isChecked = false
                swUTC.isChecked = true

            }
        }
        */
        swLocal.setOnCheckedChangeListener(onCheckedChangeListenerSwLocal)

        swFuseau.setOnCheckedChangeListener { _, p1 ->
            if (p1){
                val datePickerView = Dialog(activity!!)
                datePickerView.requestWindowFeature(Window.FEATURE_NO_TITLE)
                datePickerView.setCancelable(false)
                datePickerView.setContentView(R.layout.dialog_fuseau)
                datePickerView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                var picker =  datePickerView.findViewById(R.id.fuseau_picker) as NumberPicker
                //val data = arrayOf("UTC-12", "UTC-11", "UTC-10", "UTC-09", "UTC-08", "UTC-07", "UTC-06", "UTC-05", "UTC-04", "UTC-03", "UTC-02", "UTC-01", "UTC","UTC+01", "UTC+02", "UTC+03", "UTC+04", "UTC+05", "UTC+06", "UTC+07", "UTC+08", "UTC+09", "UTC+10", "UTC+11", "UTC+12")
                val data = arrayOf("UTC ","UTC+01","UTC+01", "UTC+02")
                val dataReel = arrayOf("UTC: Londres,Lisbonne,Dublin","UTC+01: Paris,Bruxelles,Madrid","UTC+01: Rome,Berlin,Amsterdam", "UTC+02: Athènes,Bucarest")
                picker.minValue = 1
                picker.maxValue = data.size
                picker.displayedValues = dataReel
                picker.value = 1

                datePickerView.btnDone.setOnClickListener {
                    txtChangeFuseau.text = data[picker.value-1]
                    datePickerView.dismiss()
                    SharedPreferencesUtils.timeFormat = data[picker.value-1]
                    SharedPreferencesUtils.timeFormatAUTO = ""
                    swLocal.setOnCheckedChangeListener(null)
                    swLocal.isChecked = false
                    swLocal.setOnCheckedChangeListener(onCheckedChangeListenerSwLocal)
                    // si ete hiver not active il faut activer // ici dans tous les cas
                    if(SharedPreferencesUtils.isEteHiver==false){
                        swEteHiver.isChecked = true
                    }
                }

                datePickerView.btnCancel.setOnClickListener {
                    datePickerView.dismiss()
                    swFuseau.isChecked = false
                }

                datePickerView.show()
            }else{
                if (!swLocal.isChecked){
                    txtChangeFuseau.text = "UTC"
                }
                //SharedPreferencesUtils.timeFormat = "UTC"
                val timezoneID = TimeZone.getDefault()
                txtChangeFuseau.text = "UTC" + timezoneID.getDisplayName(false, TimeZone.SHORT).substring(3,6)
                swLocal.isChecked = true
                swFuseau.isChecked = false
                swHeureLocal.isChecked = true
                swUTC.isChecked = false
                //
                SharedPreferencesUtils.timeFormatAUTO = "UTC" + timezoneID.getDisplayName(false, TimeZone.SHORT).substring(3,6)
                SharedPreferencesUtils.timeFormat = ""
                // active ete hive if is not active
                if(SharedPreferencesUtils.isEteHiver==false){
                    if(timezoneID.dstSavings != 0 )
                    {
                        swEteHiver.isChecked = true
                    }
                }
                ///
            }
        }

        btnDeleteData.setOnClickListener {
            RealmManager.open()
            RealmManager.clear()
            RealmManager.close()
            SharedPreferencesUtils.isSCANNEDInFirstTime = false
            SharedPreferencesUtils.isTimeModeHHMM = true
            SharedPreferencesUtils.timeFormat = "UTC"
            Toast.makeText(activity,getString(R.string.donnees_supprimees),Toast.LENGTH_SHORT).show()
        }

        // process heure hiver et ete
        swEteHiver.isChecked=SharedPreferencesUtils.isEteHiver
        swEteHiver.setOnCheckedChangeListener { _, p1 ->
            SharedPreferencesUtils.isEteHiver = p1
            if(p1) // oui ete hiver
            {   SharedPreferencesUtils.decalageMinutes = 60
                txtDecalageEteHiver.text = "" + SharedPreferencesUtils.decalageMinutes
                llDecalageEteHiver.visibility = VISIBLE

            }else
            {
                SharedPreferencesUtils.decalageMinutes = 0
                txtDecalageEteHiver.text = "0"
                llDecalageEteHiver.visibility = GONE
            }
            Log.i("testSwitch",p1.toString())
        }

        // process change heure hiver et ete
        txtDecalageEteHiver.setOnClickListener {
            val datePickerView = Dialog(activity!!)
            datePickerView.requestWindowFeature(Window.FEATURE_NO_TITLE)
            datePickerView.setCancelable(false)
            datePickerView.setContentView(R.layout.dialog_decalage_ete_hiver)
            datePickerView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            var picker =  datePickerView.findViewById(R.id.decalage_picker) as NumberPicker
            val data = arrayOf("15 min", "30 min", "45 min", "60 min", "75 min" , "105 min", "120 min")
            picker.minValue = 1
            picker.maxValue = data.size
            picker.displayedValues = data
            picker.value = 1

            datePickerView.btnDone.setOnClickListener {
                txtDecalageEteHiver.text = data[picker.value-1].substring(0,2)
                SharedPreferencesUtils.decalageMinutes = data[picker.value-1].substring(0,2).toInt()
                datePickerView.dismiss()
            }

            datePickerView.btnCancel.setOnClickListener {
                datePickerView.dismiss()
            }

            datePickerView.show()
        }
        /// process entreprises
        for (i in entreprises.indices) {
            var item:LinearLayout? = null
            if( i == 0) // first item
            {
                item = createItemEntreprise(entreprises.get(i))
                var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                params.setMargins(0,40,0,10)
                item.layoutParams = params

            }else
            {
                item = createItemEntreprise(entreprises.get(i))
            }
            divEntreprises.addView(item)
        }

        headerEntreprises.setOnClickListener {
            if(divEntreprises.visibility.equals(GONE)) // expend
            {
                divEntreprises.visibility =  VISIBLE
                ckeckEnvoiFile.visibility =  VISIBLE
                imgFlechEntreprises.setImageResource(R.drawable.ic_arrow_up)
            }else // collaps
            {
                divEntreprises.visibility =  GONE
                ckeckEnvoiFile.visibility =  GONE
                imgFlechEntreprises.setImageResource(R.drawable.ic_arrow_down)
            }
        }
        checkPopupShowDispatcherFile.isChecked = SharedPreferencesUtils.isPopupShowDispatcherFile!!
        checkPopupShowDispatcherFile.setOnCheckedChangeListener { compoundButton, b ->
            SharedPreferencesUtils.isPopupShowDispatcherFile =  b
        }
        ///
    }

    var onCheckedChangeListenerSwLocal= object: CompoundButton.OnCheckedChangeListener
    {
        override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
            if (p1){
                val timezoneID = TimeZone.getDefault()
                txtChangeFuseau.text = "UTC" + timezoneID.getDisplayName(false, TimeZone.SHORT).substring(3,6)
                swFuseau.isChecked = false
                swHeureLocal.isChecked = true
                swUTC.isChecked = false
                //
                SharedPreferencesUtils.timeFormatAUTO = "UTC" + timezoneID.getDisplayName(false, TimeZone.SHORT).substring(3,6)
                // active ete hive if is not active
                if(SharedPreferencesUtils.isEteHiver==false){
                    if(timezoneID.dstSavings != 0 )
                    {
                        swEteHiver.isChecked = true
                    }
                }
                ///
            }else{
                if (!swFuseau.isChecked){
                    txtChangeFuseau.text = "UTC"
                }
                SharedPreferencesUtils.timeFormatAUTO = ""
                // retour au utc
                SharedPreferencesUtils.timeFormat="UTC"
                llLocal.visibility = GONE
                llReglageFuseau.visibility = GONE
                swFuseau.isChecked = false
                swLocal.isChecked = false
                swHeureLocal.isChecked = false
                swUTC.isChecked = true

            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
    }

    fun selelectLan(ln : String){
        when(ln){
            "fr"->{
                sheetBottmDialog.checkFR.visibility = View.VISIBLE
                sheetBottmDialog.checkENG.visibility = View.INVISIBLE
                sheetBottmDialog.checkES.visibility = View.INVISIBLE
            }
            "en"->{
                sheetBottmDialog.checkFR.visibility = View.INVISIBLE
                sheetBottmDialog.checkENG.visibility = View.VISIBLE
                sheetBottmDialog.checkES.visibility = View.INVISIBLE
            }
            "es"->{
                sheetBottmDialog.checkFR.visibility = View.INVISIBLE
                sheetBottmDialog.checkENG.visibility = View.INVISIBLE
                sheetBottmDialog.checkES.visibility = View.VISIBLE            }
        }
    }

    private fun actionOnService(action: Actions) {
        if (getServiceState(activity!!) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(activity, EndlessService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Utils.log("Starting the service in >=26 Mode")
                activity!!.startForegroundService(it)
                return
            }
            Utils.log("Starting the service in < 26 Mode")
            activity!!.startService(it)
        }
    }

    private fun createItemEntreprise(name:String) : LinearLayout{

        var linearLayout = LinearLayout(activity)
        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        params.setMargins(0,10,0,10)
        linearLayout.layoutParams = params
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = Gravity.CENTER

        var textView = TextView (activity)
        textView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT,1.0f)
        textView.text = name
        textView.gravity = Gravity.START or Gravity.CENTER
        textView.setTextColor(Color.parseColor("#323643"))
        linearLayout.addView(textView)

        var switch = Switch(ContextThemeWrapper(activity,R.style.SCBSwitch))
        switch.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,0.0f)
        switch.gravity = Gravity.CENTER
        linearLayout.addView(switch)

        return linearLayout
    }

    fun setAlarmManager()
    {
        //Toast.makeText(activity!!, "reminder set ", Toast.LENGTH_SHORT).show()
        // get heure and minute notification
        var heure = 8
        var minute = 0
        try {
            heure = SharedPreferencesUtils.heureNotification!!.substring(0,2).toInt()
            minute = SharedPreferencesUtils.heureNotification!!.substring(3,5).toInt()
        }catch (ex:Exception)
        {
            Log.i("heureNotification",ex.message)
        }
        // Quote in Morning at 09:00:00 AM
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, heure)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val cur = Calendar.getInstance()
        if (cur.after(calendar)) {
            calendar.add(Calendar.DATE, 1)
        }

        val intent = Intent(activity, DailyNotificationReceiver::class.java)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        val pendingIntent =
            PendingIntent.getBroadcast(activity, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = activity!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelAlarmManager() {
        //Toast.makeText(activity!!, "reminder cancel ", Toast.LENGTH_SHORT).show()

        val alarmManager = activity!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val myIntent = Intent(activity!!, DailyNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            activity!!, 1, myIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager!!.cancel(pendingIntent)
    }

}// Required empty public constructor