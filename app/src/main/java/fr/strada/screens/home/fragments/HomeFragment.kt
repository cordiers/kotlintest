package fr.strada.screens.home.fragments


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import fr.strada.R
import fr.strada.models.CardDriverActivity
import fr.strada.models.InfoCard
import fr.strada.screens.document.AjoutDocument
import fr.strada.screens.home.MainActivity
import fr.strada.screens.home.ReaderActivityKotlin
import fr.strada.screens.notifications.NotificationsActivity
import fr.strada.utils.LocaleHelper
import fr.strada.utils.RealmManager
import fr.strada.utils.SharedPreferencesUtils
import fr.strada.utils.TimeSpan
import fr.strada.utils.Utils.getMonthName
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {

    lateinit var rootview : View


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootview = inflater.inflate(R.layout.fragment_home, container, false)
        RealmManager.open()
        if (!SharedPreferencesUtils.isSCANNEDInFirstTime)
            showDialog()
        return rootview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).floatingActionButtonSettings.visibility = View.INVISIBLE
        (activity as MainActivity).rlTitle.visibility = View.GONE
        (activity as MainActivity).imgLogo.visibility = View.VISIBLE
        (activity as MainActivity).rlIcon.visibility = View.VISIBLE
        (activity as MainActivity).btnIconToolbar.setImageResource(R.drawable.ic_notifications)
        (activity as MainActivity).txtdateJour.visibility = View.INVISIBLE
        // get default luange of device
        var locale:Locale? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
           locale = Resources.getSystem().getConfiguration().getLocales().get(0)
        } else
        {
           locale = Resources.getSystem().getConfiguration().locale
        }
        // Today Phone Date
        val sdf = SimpleDateFormat("EEEE, d MMM yyyy",locale)
        val currentDateandTime = sdf.format(Date())
        rootview.txtToday.text = currentDateandTime
        // Notification Icon Click
        (activity as MainActivity).rlIcon.setOnClickListener {
            startActivity(Intent(activity, NotificationsActivity::class.java))
        }

        try{
            Log.i("justForTest","test1")
            var  driverActivityData  = RealmManager.loadAllCardDriverActivity()
            var dtStart = driverActivityData[0].activityDailyRecords.last().activityRecordDate
            var eventDate : Date? = Date(dtStart)
            var cal : Calendar = Calendar.getInstance()
            cal.time = eventDate
            var year = cal.get(Calendar.YEAR)
            var month = cal.get(Calendar.MONTH)
            var day = cal.get(Calendar.DAY_OF_MONTH)
            rootview.txtLastLecture.text = getMonthName(month) +" "+ year

            calcule(driverActivityData,month,year)

            Log.i("justForTest","test2")

            var  cardIdentification = RealmManager.loadAllCardIdentification()
            var dtStartex = cardIdentification.cardExpiryDate
            var df =SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH)
            df.setTimeZone(TimeZone.getTimeZone("GMT"))
            var cardExpiryDate = df.parse(dtStartex)
            cardExpiryDate = removeDay(cardExpiryDate) // remove un day
            cardExpiryDate = removeTime(cardExpiryDate) //remove time
            var currentDate=Date()
            currentDate = removeTime(currentDate) // remove time
            var diff = cardExpiryDate.getTime() - currentDate.getTime()
            var seconds=diff / 1000
            var minutes= seconds / 60
            var hours = minutes / 60
            var days = hours / 24
            // days++ // il faut ajouter ce jour
            if(days == 1L || days== 0L || days == -1L)
            {
                lblNombreJourEcheance.text = ""+ days +" "+resources.getString(R.string.jour_restant)
            }else
            {
                lblNombreJourEcheance.text = ""+ days +" "+resources.getString(R.string.jours_restants)
            }
            // process colors
            if (days <= 0) { // date passer

                lblNombreJourEcheance.setTextColor(getResources().getColor(R.color.error))
                if(days == -1L)
                {
                    lblNombreJourEcheance.text = ""+ Math.abs(days) +" "+resources.getString(R.string.jour_de_retard)
                }else if(days == 0L)
                {
                    lblNombreJourEcheance.text = resources.getString(R.string.expire_aujourd_hui)
                }else
                {
                    lblNombreJourEcheance.text = ""+ Math.abs(days) +" "+resources.getString(R.string.jours_de_retard)
                }

            }else if(days <= SharedPreferencesUtils.delaisAvertisementCarte!!)
            {
                lblNombreJourEcheance.setTextColor(getResources().getColor(R.color.orange))
            }else
            {
                lblNombreJourEcheance.setTextColor(getResources().getColor(R.color.colorPrimaryBlue))
            }
            ///////
            rootview.txtDateExpCard.text = getResources().getString(R.string.votre_carte_arrivera_a_echeance_le)+" "+ getDatestring(dtStartex!!).get(Calendar.DAY_OF_MONTH) +" "+ getMonthName(getDatestring(dtStartex!!).get(Calendar.MONTH)) +" "+ getDatestring(dtStartex!!).get(Calendar.YEAR)

            var d3 = Date()
            var diff3 = d3.day - eventDate!!.day
            /*
            var p2 = diff3.toFloat()*100/30
            if (p2>0){
                rootview.txtPDateEchCard.text = p2.toInt().toString() + "%"
                rootview.progPDateEchCard.progress = p2
            }else{
                rootview.txtPDateEchCard.text = "0 %"
                rootview.progPDateEchCard.progress = 0F
            }
            */
            linearLayout2.setOnClickListener {
                changeFragment(ActivityFragment(),year.toString(),month.toString())
            }
            linearLayout.setOnClickListener {
                changeFragment(ActivityFragment(),year.toString(),month.toString())
            }

        }catch (e:Exception){
            llEch.visibility = View.INVISIBLE
            txtProchaineEch.visibility = View.INVISIBLE
            Log.i("showEror",e.message)
        }

        try {

            var CardDownload = RealmManager.loadCardDownload()
            var c : Calendar = Calendar.getInstance()
            c.time = Date(CardDownload.LastCardDownload)
            var d3 = Date()
            var diff3 = d3.day - c!!.get(Calendar.DAY_OF_MONTH)
            ///////////
            var dfDechargementCarte = SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH)
            var lastDechargementDate = dfDechargementCarte.parse(CardDownload.LastCardDownload)
            lastDechargementDate = removeTime(lastDechargementDate)
            var c28 = Calendar.getInstance()
            c28.time = lastDechargementDate
            c28.add(Calendar.DATE, 28)
            var lastDechargementPlus28jour= c28.getTime()
            var currentDate = Date()
            var diff = lastDechargementPlus28jour.getTime() - currentDate.getTime()
            var seconds= diff / 1000
            var minutes= seconds / 60
            var hours = minutes / 60
            var days = hours / 24
            days ++
            if(days == 1L || days == 0L || days == -1L)
            {
                txtPDateEchCard.text = ""+ days +" "+resources.getString(R.string.jour_restant)
            }else
            {
                txtPDateEchCard.text = ""+ days +" "+resources.getString(R.string.jours_restants)
            }
            //////// process colors
            if (days <= 0) { // date passer

                txtPDateEchCard.setTextColor(getResources().getColor(R.color.error))
                if(days == -1L)
                {
                    txtPDateEchCard.text = ""+ Math.abs(days) +" "+resources.getString(R.string.jour_de_retard)
                }else
                {
                    txtPDateEchCard.text = ""+ Math.abs(days) +" "+resources.getString(R.string.jours_de_retard)
                }

            }else if(days <= SharedPreferencesUtils.delaisAvertisement!!)
            {
                txtPDateEchCard.setTextColor(getResources().getColor(R.color.orange))
            }else
            {
                txtPDateEchCard.setTextColor(getResources().getColor(R.color.colorPrimaryBlue))
            }
            ////////
            rootview.txtDateDechargement.text = resources.getString(R.string.dernier_dechargement_de_carte_le)+" "+ c.get(Calendar.DAY_OF_MONTH).toString() + " " + getMonthName(c.get(Calendar.MONTH)) + " " + c.get(Calendar.YEAR)

        }catch (e:Exception){
            cdDateDechargement.visibility = View.INVISIBLE
        }
    }

    fun removeTime(date : Date) : Date
    {   var cal = Calendar.getInstance()
        cal.setTime(date)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.getTime()
    }

    fun removeDay(date : Date) : Date
    {   var cal = Calendar.getInstance()
        cal.setTime(date)
        cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
        return cal.getTime()
    }

    fun getDatestring(date : String) : Calendar{
        var eventDate : Date? = Date(date)
        var cal : Calendar = Calendar.getInstance()
        cal.time = eventDate
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return cal
    }

    fun calcule(driverActivityData : RealmResults<CardDriverActivity>, month: Int, year:Int){

        var KM  = 0
        val hConduite = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hRepos = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hTravail = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hMAD = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hService = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hNuit = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);

        for (data in driverActivityData){

            for (data in data.activityDailyRecords){
                // var formatter1=SimpleDateFormat("dd/MM/yyyy");
                var eventDate : Date? = Date(data.activityRecordDate)
                var cal : Calendar = Calendar.getInstance()
                cal.time = eventDate
                var Eyear = cal.get(Calendar.YEAR)
                var Emonth = cal.get(Calendar.MONTH)

                if (Eyear==year && Emonth==month){

                    KM+=data.activityDayDistance;

                    for (activityInfo in data.activityChangeInfo.indices){

                        if(data.activityChangeInfo[activityInfo].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Conduite.name) && data.activityChangeInfo[activityInfo].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                            try {

                                var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1].time)
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hConduite.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                hConduite.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))

                                var timeSpanCurrentEvent = TimeSpan.parse(currentEvent.hours.toString()+":"+currentEvent.minutes)

                                if (TimeSpan.parse(SharedPreferencesUtils.nightStart).hours > TimeSpan.parse(SharedPreferencesUtils.nightend).hours){
                                    if ((TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && currentEvent.hours<=0) || (currentEvent.hours>=0 && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend)))){
                                        hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                        hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                    }
                                }
                                else if (TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend))){
                                    hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                    hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                }
                            }catch (e: Exception){
                                ////////////////// si ce code est false el faut le supprimer
                                var nextEvent =  getDateTime("23:59:00")
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hConduite.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                hConduite.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))

                                var timeSpanCurrentEvent = TimeSpan.parse(currentEvent.hours.toString()+":"+currentEvent.minutes)

                                if (TimeSpan.parse(SharedPreferencesUtils.nightStart).hours > TimeSpan.parse(SharedPreferencesUtils.nightend).hours){
                                    if ((TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && currentEvent.hours<=0) || (currentEvent.hours>=0 && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend)))){
                                        hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                        hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                    }
                                }
                                else if (TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend))){
                                    hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                    hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                }
                                /////////////////
                            }

                        }
                        if(data.activityChangeInfo[activityInfo].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Repos.name) && data.activityChangeInfo[activityInfo].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                            try {
                                var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1].time)
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))

                            }catch (e: Exception){
                                // si ce code est false il faut le supprimer
                                var nextEvent =  getDateTime("23:59:00")
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                /////////////
                            }
                        }
                        if(data.activityChangeInfo[activityInfo].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Travail.name) && data.activityChangeInfo[activityInfo].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                            try {
                                var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1].time)
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hTravail.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                hTravail.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                var timeSpanCurrentEvent = TimeSpan.parse(currentEvent.hours.toString()+":"+currentEvent.minutes)

                                if (TimeSpan.parse(SharedPreferencesUtils.nightStart).hours > TimeSpan.parse(SharedPreferencesUtils.nightend).hours){
                                    if ((TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && currentEvent.hours<=0) || (currentEvent.hours>=0 && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend)))){
                                        hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                        hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))                                    }
                                }
                                else if (TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend))){
                                    hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                    hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                }
                            }catch (e: Exception){
                                // si ce code est false il faut le supprimer
                                var nextEvent =  getDateTime("23:59:00")
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hTravail.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                hTravail.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                var timeSpanCurrentEvent = TimeSpan.parse(currentEvent.hours.toString()+":"+currentEvent.minutes)

                                if (TimeSpan.parse(SharedPreferencesUtils.nightStart).hours > TimeSpan.parse(SharedPreferencesUtils.nightend).hours){
                                    if ((TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && currentEvent.hours<=0) || (currentEvent.hours>=0 && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend)))){
                                        hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                        hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))                                    }
                                }
                                else if (TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend))){
                                    hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                    hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                }
                                /////////////
                            }
                        }
                        if(data.activityChangeInfo[activityInfo].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Disponibilite.name) && data.activityChangeInfo[activityInfo].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                            try {
                                var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1].time)
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hMAD.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                hMAD.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                var timeSpanCurrentEvent = TimeSpan.parse(currentEvent.hours.toString()+":"+currentEvent.minutes)

                                if (TimeSpan.parse(SharedPreferencesUtils.nightStart).hours > TimeSpan.parse(SharedPreferencesUtils.nightend).hours){
                                    if ((TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && currentEvent.hours<=0) || (currentEvent.hours>=0 && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend)))){
                                        hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                        hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))                                    }
                                }
                                else if (TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend))){
                                    hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                    hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                }
                            }catch (e: Exception){
                                //////// si le code est false il faut le supprimer
                                var nextEvent =  getDateTime("23:59:00")
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hMAD.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                hMAD.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                var timeSpanCurrentEvent = TimeSpan.parse(currentEvent.hours.toString()+":"+currentEvent.minutes)

                                if (TimeSpan.parse(SharedPreferencesUtils.nightStart).hours > TimeSpan.parse(SharedPreferencesUtils.nightend).hours){
                                    if ((TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && currentEvent.hours<=0) || (currentEvent.hours>=0 && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend)))){
                                        hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                        hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))                                    }
                                }
                                else if (TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightStart)) && !TimeSpan.isAfter(timeSpanCurrentEvent,TimeSpan.parse(SharedPreferencesUtils.nightend))){
                                    hNuit.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                    hNuit.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                }
                                ////////
                            }
                        }
                    }





                }


            }
        }

        hService.add(hConduite)
        hService.add(hMAD)
        hService.add(hTravail)

        if (SharedPreferencesUtils.isTimeModeHHMM){
            txtHConduite.text = hConduite.toStringHHMM()
            txtHCoupure.text = KM.toString()
            txtHMAD.text = hMAD.toStringHHMM()
            txtHNuit.text = hNuit.toStringHHMM()
            txtHTravail.text = hTravail.toStringHHMM()
            txtHService.text = hService.toStringHHMM()
        }else{
            txtHConduite.text = hConduite.toStringHHH()
            txtHCoupure.text = KM.toString()
            txtHMAD.text = hMAD.toStringHHH()
            txtHNuit.text = hNuit.toStringHHH()
            txtHTravail.text = hTravail.toStringHHH()
            txtHService.text = hService.toStringHHH()
        }





    }


    fun showDialog() {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_scan)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnExit = dialog.findViewById(R.id.btnExit) as ImageView
        val btnScan : Button = dialog.findViewById(R.id.btnScan)
        val btnDoc : Button = dialog.findViewById(R.id.btnAddDoc)
        btnExit.setOnClickListener { dialog.dismiss() }
        btnScan.setOnClickListener {
            startActivity(Intent(activity!!,ReaderActivityKotlin::class.java))
            dialog.dismiss()
        }
        btnDoc.setOnClickListener {
            startActivity(Intent(activity!!,AjoutDocument::class.java))
            dialog.dismiss()
        }

        dialog.show()

    }

    fun getDateTime(dtStart : String) : Date {
        var format = SimpleDateFormat("HH:mm:ss")

        try {
            var date = format.parse(dtStart)
            System.out.println(date)
            return date
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.d("Current Date Time 2: " , e.message)
        }
        return Date()
    }

    fun changeFragment(
        f: Fragment,
        year: String?,
        month: String?
    ) {
        val  fragmentManager : FragmentManager? = fragmentManager
        val  fragmentTransaction : FragmentTransaction = fragmentManager!!.beginTransaction()
        var fragment   = f
        fragmentTransaction.remove(f)

            fragment.arguments
            var args =  Bundle()
            args.putString("year", year)
            args.putString("month", month)
        args.putString("fromMain", "fromMain")

        f.arguments = args


        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.replace(R.id.container_body, fragment)
        fragmentTransaction.detach(fragment)
        fragmentTransaction.attach(fragment)
        fragmentTransaction.commit()
    }


    fun getMonthName(month:Int) : String {
        when (month) {
            0 -> {
                return resources.getString(R.string.janvier)
            }
            1 -> {
                return resources.getString(R.string.fevrier)
            }
            2 -> {
                return resources.getString(R.string.mars)
            }
            3 -> {
                return resources.getString(R.string.avril)
            }
            4 -> {
                return resources.getString(R.string.mai)
            }
            5 -> {
                return resources.getString(R.string.juin)
            }
            6 -> {
                return resources.getString(R.string.juillet)
            }
            7 -> {
                return resources.getString(R.string.aout)
            }
            8 -> {
                return resources.getString(R.string.septembre)
            }
            9 -> {
                return resources.getString(R.string.octobre)
            }
            10 -> {
                return resources.getString(R.string.novembre)
            }
            11 -> {
                return resources.getString(R.string.decembre)
            }
        }
        return ""
    }


}