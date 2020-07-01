package fr.strada.screens.home.fragments


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shrikanthravi.collapsiblecalendarview.data.CalendarAdapter
import com.shrikanthravi.collapsiblecalendarview.data.Day
import com.shrikanthravi.collapsiblecalendarview.data.Event
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import fr.strada.R
import fr.strada.models.*
import fr.strada.screens.home.MainActivity
import fr.strada.utils.*
import io.realm.RealmList
import kotlinx.android.synthetic.main.dialog_date.*
import kotlinx.android.synthetic.main.fragment_activites.*
import kotlinx.android.synthetic.main.fragment_jour_calender.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.widget.Toast
import android.view.View.OnFocusChangeListener
import android.view.MotionEvent
import android.view.View.OnTouchListener
import fr.strada.StradaApp


class JournalierCalenderFragment : Fragment()  {

    private lateinit var mContext: Context
    var driverActivityData = CardDriverActivity()
    var cardVehicleRecords : RealmList<CardVehicleRecords> = RealmList()
    lateinit var adapter : RecyclerJourAdapter
    lateinit var commentAdapter : RecyclerCommentsAdapter

    var loader : Loader = Loader.getInstance()

    var activityFragment : ActivityFragment = ActivityFragment()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootview = inflater.inflate(R.layout.fragment_jour_calender, container, false)
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        RealmManager.open()
        return rootview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            driverActivityData = RealmManager.loadAllCardDriverActivity()[0]
            cardVehicleRecords  = RealmManager.loadAllCardVehiclesUsed()[0].cardVehicleRecords
            rv_jour.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        }catch (e: Exception){
            Log.e("Exception",e.message)
        }

        (activity as MainActivity).txttitle.setOnClickListener {
        }
        (activity as MainActivity).txtdateJour.setOnClickListener {
            calendarViewJour.changeToToday()
        }

        var year = arguments!!.getString("year")
        var day = arguments!!.getString("day")
        var month = arguments!!.getString("month")
        (activity as MainActivity).txttitle.text = getMonthName(month!!.toInt())+" "+year


        calendarViewJour.month = month.toInt()
        calendarViewJour.year = year!!.toInt()
        var cal = Calendar.getInstance()
        cal.set(year.toInt(), month.toInt(),day!!.toInt())
        calendarViewJour.setAdapter(CalendarAdapter(activity!!,cal))
        calendarViewJour.select(Day(year.toInt(), month.toInt(), day.toInt()))


        adapter =  RecyclerJourAdapter(day!!.toInt(),month.toInt(),year.toInt(),ActivityDailyRecords(),cardVehicleRecords)
        rv_jour.adapter = adapter

        // calendarViewJour.select(Day(year!!.toInt(),month!!.toInt(),day!!.toInt()))
        loader.show(activity)
        Log.d("params","onDaySelect")
        ///////////////////////////////////
        calendarViewJour.expand(100)
        Handler().postDelayed({
            calendarViewJour.collapse(100)
        },100)
        ///////////////////////////////////
        Handler().postDelayed({
            calcule(driverActivityData,month!!.toInt(),year!!.toInt(), day.toInt())
        },200)

        // traitement pour affichage
        var utc = if (!SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty()) SharedPreferencesUtils.timeFormatAUTO!!.substring(3).toLong() else if (SharedPreferencesUtils.timeFormat!!.substring(3).isEmpty()) 0L else SharedPreferencesUtils.timeFormat!!.substring(3).toLong()
        var decalageMinutes = 0
        var lastSundayInMarch = getLastSundayInMarch()
        var lastSundayInOctobre = getLastSundayInOctobre()
        var lastSundayInMarchByYear = getLastSundayInMarchByYear(year!!.toInt())
        var lastSundayInOctobreByYear = getLastSundayInOctobreByYear(year!!.toInt())
        var currentDate = Date()
        //
        var dateActivity=cal.time
        //
        if(SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty() && !SharedPreferencesUtils.timeFormat.equals("UTC")) // le cas manuelle
        {
            if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear)) // heure ete hiver et appliquer dans des certen contidition
            {
                decalageMinutes = SharedPreferencesUtils.decalageMinutes!!
            }else{
                decalageMinutes = 0
            }

        }else if (!SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty()) // automatique
        {
            if(currentDate.after(lastSundayInMarch) && currentDate.before(lastSundayInOctobre)) // date system periode ete
            {
                if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear)) // date system periode ete / date activite periode ete
                {
                    decalageMinutes = SharedPreferencesUtils.decalageMinutes!!

                }else{  // date system periode ete / date activite periode hiver // a discuter

                    decalageMinutes = 0 // on elimine le parametrage
                }

            }else{

                if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear)) // date system periode hiver / date activite periode ete
                {
                    decalageMinutes = SharedPreferencesUtils.decalageMinutes!!

                }else{  // date system periode hiver / date activite periode hiver

                    decalageMinutes = 0
                }

            }

        }else
        {
            if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear))
            {
                decalageMinutes = SharedPreferencesUtils.decalageMinutes!!
            }else{
                decalageMinutes = 0
            }
        }
        ////////////////////////////////////////////////////////////////////
        var heures= (decalageMinutes!!.toDouble() / 60.0 ).toInt()
        Log.i("test","heures" + heures.toString())
        Log.i("test","minute" + decalageMinutes.toString())
        var minutes = decalageMinutes - heures * 60
        var newHeures = utc + heures
        var strNewHeures = ""
        var strMinutes = ""

        if( newHeures >= 0 )
        { if(newHeures.toString().length==1)
          { strNewHeures="+"+"0"+newHeures

          }else{
            strNewHeures="+"+newHeures
          }

          if(minutes.toString().length==1)
          {
              strMinutes="0"+ minutes
          }else{
              strMinutes= minutes.toString()
          }
        }else
        {

            if(minutes > 0)
            {
                minutes = 60 - minutes
                newHeures --
            }
            if(newHeures.toString().length==2)
            { strNewHeures =  newHeures.toString().replace("-","-0")

            }else{
              strNewHeures = newHeures.toString()
            }

            if(minutes.toString().length==1)
            {
                strMinutes = "0"+ minutes
            }else
            {
                strMinutes= minutes.toString()
            }
        }

        //txtJUTC.text = if (!SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty()) SharedPreferencesUtils.timeFormatAUTO  else SharedPreferencesUtils.timeFormat
        txtJUTC.text="UTC"+ strNewHeures+":"+strMinutes
        ///////////////////////////////////////////////////////////////////


        //////////////////////////////////////////
        calendarViewJour.setCalendarListener(object: CollapsibleCalendar.CalendarListener{
            override fun onDaySelect() {
                loader.show(activity)
                Log.d("params","onDaySelect")
                ////// afichage des utc
                affichageUTC(calendarViewJour.selectedDay!!.day,calendarViewJour.selectedDay!!.month,calendarViewJour.selectedDay!!.year)
                ////// colaps if select day
                Handler().postDelayed({
                    calendarViewJour.collapse(100)
                },100)
                //////
                Handler().postDelayed({
                    calcule(driverActivityData,calendarViewJour.selectedDay!!.month,calendarViewJour.selectedDay!!.year,calendarViewJour.selectedDay!!.day)
                },150)
                adapter.notifyDataSetChanged()
                var date = calendarViewJour.selectedDay!!.day.toString()+"/"+calendarViewJour.selectedDay!!.month.toString()+"/"+calendarViewJour.selectedDay!!.year.toString()
                var emailUser=if(SharedPreferencesUtils.isLoggedIn) StradaApp.instance!!.getUser().email else ""
                commentAdapter =  RecyclerCommentsAdapter(RealmManager.loadCommentsByUser(date,emailUser))
                rv_comment.layoutManager = LinearLayoutManager(activity)
                rv_comment.adapter = commentAdapter
            }

            override fun onItemClick(v: View) {
                Log.d("params","onItemClick")

            }

            override fun onDataUpdate() {
                Log.d("params","onDataUpdate")

                var month = calendarViewJour.month
                var year = calendarViewJour.year

                (activity as MainActivity).txttitle.text = getMonthName(month)+" "+year

                var date = calendarViewJour.selectedDay!!.day.toString()+" "+getMonthName(calendarViewJour.selectedDay!!.month)+" "+calendarViewJour.selectedDay!!.year.toString()
                var emailUser=if(SharedPreferencesUtils.isLoggedIn) StradaApp.instance!!.getUser().email else ""
                commentAdapter =  RecyclerCommentsAdapter(RealmManager.loadCommentsByUser(date,emailUser))
                rv_comment.layoutManager = LinearLayoutManager(activity)
                rv_comment.adapter = commentAdapter

            }

            override fun onMonthChange()
            {
                var month = calendarViewJour.month
                var year = calendarViewJour.year
                (activity as MainActivity).txttitle.text = getMonthName(month)+" "+year
                refrechEventsCalendar(driverActivityData,year,month)
            }

            override fun onWeekChange(position: Int,isNext : Boolean)
            {

            }

            override fun onClickListener() {

            }

            override fun onDayChanged() {

            }
        })

        (activity as MainActivity).txttitle.setOnClickListener {

            var year : Int = calendarViewJour.year
            var month : Int = calendarViewJour.month
            val datePickerView = Dialog(activity!!)
            datePickerView.requestWindowFeature(Window.FEATURE_NO_TITLE)
            datePickerView.setCancelable(false)
            datePickerView.setContentView(R.layout.dialog_date)
            datePickerView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            datePickerView.txtDateSelected.text = getMonthName(month)+" "+year

            var picker =  datePickerView.findViewById(R.id.date_picker) as DatePicker
            picker.setDate(year,month)

            picker.setOnDateClickedListener { _, i, i2, _ ->
                year = i
                month = i2
                Log.d("TimeJour",getMonthName(month-1)+year)
            }

            datePickerView.btnDone.setOnClickListener {

                calendarViewJour.year = picker.year
                calendarViewJour.month = picker.month
                calendarViewJour.prevMonth()
                (activity as MainActivity).txttitle.text = getMonthName(month-1)+" "+year
                datePickerView.dismiss()
            }

            datePickerView.btnCancel.setOnClickListener {
                datePickerView.dismiss()
            }

            datePickerView.show()

        }
        selectJourUI()



        btnSend.setOnClickListener {
            if (inputComment.text.trim().isNotEmpty())
            {
                var date = calendarViewJour.selectedDay!!.day.toString()+" "+getMonthName(calendarViewJour.selectedDay!!.month)+" "+calendarViewJour.selectedDay!!.year.toString()
                RealmManager.open()
                var emailUser= if(SharedPreferencesUtils.isLoggedIn) StradaApp.instance!!.getUser().email else ""
                RealmManager.saveComment(Comments(date,inputComment.text.toString(),emailUser))
                RealmManager.close()
                inputComment.setText("")
                commentAdapter.notifyDataSetChanged()

            }else
            {
                inputComment.error = context!!.getString(R.string.inserer_un_commentaire)
            }
        }
        ////// collapst calendar when commentaire is focus
        inputComment.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                calendarViewJour.collapse(200)
                return false
            }
        })
        //////
    }


    private fun affichageUTC(day:Int,month:Int,year:Int){

        var cal = Calendar.getInstance()
        cal.set(year, month,day!!)
        // traitement pour affichage
        var utc = if (!SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty()) SharedPreferencesUtils.timeFormatAUTO!!.substring(3).toLong() else if (SharedPreferencesUtils.timeFormat!!.substring(3).isEmpty()) 0L else SharedPreferencesUtils.timeFormat!!.substring(3).toLong()
        var decalageMinutes = 0
        var lastSundayInMarch = getLastSundayInMarch()
        var lastSundayInOctobre = getLastSundayInOctobre()
        var lastSundayInMarchByYear = getLastSundayInMarchByYear(year)
        var lastSundayInOctobreByYear = getLastSundayInOctobreByYear(year)
        var currentDate = Date()
        //
        var dateActivity= cal.time
        //
        if(SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty() && !SharedPreferencesUtils.timeFormat.equals("UTC")) // le cas manuelle
        {
            if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear)) // heure ete hiver et appliquer dans des certen contidition
            {
                decalageMinutes = SharedPreferencesUtils.decalageMinutes!!
            }else{
                decalageMinutes = 0
            }

        }else if (!SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty()) // automatique
        {
            if(currentDate.after(lastSundayInMarch) && currentDate.before(lastSundayInOctobre)) // date system periode ete
            {
                if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear)) // date system periode ete / date activite periode ete
                {
                    decalageMinutes = SharedPreferencesUtils.decalageMinutes!!

                }else{  // date system periode ete / date activite periode hiver // a discuter

                    decalageMinutes = 0 // on elimine le parametrage
                }

            }else
            {

                if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear)) // date system periode hiver / date activite periode ete
                {
                    decalageMinutes = SharedPreferencesUtils.decalageMinutes!!

                }else{  // date system periode hiver / date activite periode hiver

                    decalageMinutes = 0
                }

            }

        }else
        {
            if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear)) // date system periode hiver / date activite periode ete
            {
                decalageMinutes = SharedPreferencesUtils.decalageMinutes!!

            }else{  // date system periode hiver / date activite periode hiver

                decalageMinutes = 0
            }
        }
        ////////////////////////////////////////////////////////////////////
        var heures= (decalageMinutes!!.toDouble() / 60.0 ).toInt()
        var minutes = decalageMinutes - heures * 60
        var newHeures = utc + heures
        var strNewHeures = ""
        var strMinutes = ""

        if( newHeures >= 0 )
        { if(newHeures.toString().length==1)
        { strNewHeures="+"+"0"+newHeures

        }else{
            strNewHeures="+"+newHeures
        }

            if(minutes.toString().length==1)
            {
                strMinutes="0"+ minutes
            }else{
                strMinutes= minutes.toString()
            }
        }else
        {

            if(minutes > 0)
            {
                minutes = 60 - minutes
                newHeures --
            }
            if(newHeures.toString().length==2)
            { strNewHeures =  newHeures.toString().replace("-","-0")

            }else{
                strNewHeures = newHeures.toString()
            }

            if(minutes.toString().length==1)
            {
                strMinutes = "0"+ minutes
            }else
            {
                strMinutes= minutes.toString()
            }
        }

        //txtJUTC.text = if (!SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty()) SharedPreferencesUtils.timeFormatAUTO  else SharedPreferencesUtils.timeFormat
        txtJUTC.text="UTC"+ strNewHeures+":"+strMinutes
        ///////////////////////////////////////////////////////////////////
    }

    fun selectJourUI(){

         (activity as MainActivity).mensuel_horizental_view.visibility=View.INVISIBLE
         (activity as MainActivity).hebdomadaire_horizental_view.visibility=View.INVISIBLE

         (activity as MainActivity).tv_hebdomadaire.setTextColor(Color.parseColor("#7F889A"))
         (activity as MainActivity).tv_mensuel.setTextColor(Color.parseColor("#7F889A"))

    }

    private fun refrechEventsCalendar(driverActivityData : CardDriverActivity,year:Int,month: Int)
    {
        var listEvent : ArrayList<Event> = ArrayList()

        for (data in driverActivityData.activityDailyRecords)
        {
            // var formatter1=SimpleDateFormat("dd/MM/yyyy");
            var eventDate : Date? = Date(data.activityRecordDate)
            var cal : Calendar = Calendar.getInstance()
            cal.time = eventDate
            var Eyear = cal.get(Calendar.YEAR)
            var Emonth = cal.get(Calendar.MONTH)
            var Eday = cal.get(Calendar.DAY_OF_MONTH)
            if (Eyear==year && (Emonth==month || Emonth==(month+1) || Emonth==(month-1))) {
                listEvent.add(Event(Eyear, Emonth, Eday))
            }
        }

        Handler().postDelayed({
            calendarViewJour.clearListEventTag()
            calendarViewJour.addListEventTag(listEvent)
        }, 100)

    }

    fun calcule(driverActivityData : CardDriverActivity, month: Int, year:Int,day: Int){


        var dataevent : ActivityDailyRecords = ActivityDailyRecords()
        var vehice : String = ""
        var KM  = 0
        val hConduite = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hRepos = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hTravail = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hMAD = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hService = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hNuit = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        var listEvent : ArrayList<Event> = ArrayList()
        for (data in driverActivityData.activityDailyRecords){
                // var formatter1=SimpleDateFormat("dd/MM/yyyy");
                var eventDate : Date? = Date(data.activityRecordDate)
                var cal : Calendar = Calendar.getInstance()
                cal.time = eventDate
                var Eyear = cal.get(Calendar.YEAR)
                var Emonth = cal.get(Calendar.MONTH)
                var Eday = cal.get(Calendar.DAY_OF_MONTH)
                if (Eyear==year && (Emonth==month || Emonth==(month+1) || Emonth==(month-1))) {
                  listEvent.add(Event(Eyear, Emonth, Eday))
                }


                if (Eyear==year && Emonth==month && Eday==day){
                    dataevent = data
                    //calendarView.addEventTag(Eyear,Emonth,Eday)

                    KM+=data.activityDayDistance
                    for (activityInfo in data.activityChangeInfo.indices){
                        if(data.activityChangeInfo[activityInfo].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Conduite.name) && data.activityChangeInfo[activityInfo].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                            try {

                                var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1].time)
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hConduite.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                hConduite.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                // hConduite.add(TimeSpan())
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
                            }catch (e:Exception){
                                ///////// code added new new forfix bug if code is false delete it
                                var nextEvent =  getDateTime("23:59:00")
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hConduite.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                hConduite.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
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
                                ////////////////////////////////////////////////////////////
                            }

                        }
                        if(data.activityChangeInfo[activityInfo].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Repos.name) && data.activityChangeInfo[activityInfo].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                            try {
                                var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1].time)
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                            }catch (e:Exception)
                            {   //just for test
                                var nextEvent =  getDateTime("23:59:00")
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                ////////

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
                            }catch (e:Exception){
                                ///// for test if false delete it
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
                        if(data.activityChangeInfo[activityInfo].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Disponibilite.name) && data.activityChangeInfo[activityInfo].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name) ){
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
                            }catch (e:Exception){
                                ///// for test if false delete it
                                var nextEvent =  getDateTime("23:59:00")
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
                                ///

                            }
                        }
                    }





                }




            }

        Handler().postDelayed({
            calendarViewJour.clearListEventTag()
            calendarViewJour.addListEventTag(listEvent)
        }, 100)

        adapter =  RecyclerJourAdapter(day,month,year,dataevent,cardVehicleRecords)
        adapter.notifyDataSetChanged()
        rv_jour.adapter = adapter

        hService.add(hConduite)
        hService.add(hMAD)
        hService.add(hTravail)
        loader.dismiss()
        if (SharedPreferencesUtils.isTimeModeHHMM){
            txtJConduit.text = hConduite.toStringHHMM()
            txtJKM.text = KM.toString()
            txtJMAD.text = hMAD.toStringHHMM()
            txtJNuit.text = hRepos.toStringHHMM()
            txtJTravail.text = hTravail.toStringHHMM()
            txtJService.text = hService.toStringHHMM()
        }else{
            txtJConduit.text = hConduite.toStringHHH()
            txtJKM.text = KM.toString()
            txtJMAD.text = hMAD.toStringHHH()
            txtJNuit.text = hRepos.toStringHHH()
            txtJTravail.text = hTravail.toStringHHH()
            txtJService.text = hService.toStringHHH()
        }
    }


    //------------------------------ fun changeFragment() ---------------------------------------------------------

    fun getMonthName(month:Int) : String{
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

    fun getDateTime(dtStart : String) : Date {
        var format = SimpleDateFormat("HH:mm:ss")

        try {
            return format.parse(dtStart)
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.d("Current Date Time 2: " , e.message)

        }
        return Date()
    }

    fun getLastSundayInMarch(): Date {
        val cal = Calendar.getInstance()
        var currentYear = Calendar.getInstance().get(Calendar.YEAR)
        cal.set(currentYear, 3, 1)
        cal.add(Calendar.DATE, -1)
        cal.add(Calendar.DAY_OF_MONTH, -(cal.get(Calendar.DAY_OF_WEEK) - 1))
        cal.set(Calendar.HOUR_OF_DAY, 3)
        cal.set(Calendar.MINUTE,0)
        cal.set(Calendar.SECOND,0)
        cal.set(Calendar.MILLISECOND,0)
        return cal.time
    }

    fun getLastSundayInMarchByYear(year:Int): Date {
        val cal = Calendar.getInstance()
        var currentYear = year
        cal.set(currentYear, 3, 1)
        cal.add(Calendar.DATE, -1)
        cal.add(Calendar.DAY_OF_MONTH, -(cal.get(Calendar.DAY_OF_WEEK) - 1))
        cal.set(Calendar.HOUR_OF_DAY, 3)
        cal.set(Calendar.MINUTE,0)
        cal.set(Calendar.SECOND,0)
        cal.set(Calendar.MILLISECOND,0)
        return cal.time
    }

    fun getLastSundayInOctobre(): Date {
        val cal = Calendar.getInstance()
        var currentYear = Calendar.getInstance().get(Calendar.YEAR)
        cal.set(currentYear, 10, 1)
        cal.add(Calendar.DATE, -1)
        cal.add(Calendar.DAY_OF_MONTH, -(cal.get(Calendar.DAY_OF_WEEK) - 1))
        cal.set(Calendar.HOUR_OF_DAY, 3)
        cal.set(Calendar.MINUTE,0)
        cal.set(Calendar.SECOND,0)
        cal.set(Calendar.MILLISECOND,0)
        return cal.time
    }

    fun getLastSundayInOctobreByYear(year:Int): Date {
        val cal = Calendar.getInstance()
        var currentYear = year
        cal.set(currentYear, 10, 1)
        cal.add(Calendar.DATE, -1)
        cal.add(Calendar.DAY_OF_MONTH, -(cal.get(Calendar.DAY_OF_WEEK) - 1))
        cal.set(Calendar.HOUR_OF_DAY, 3)
        cal.set(Calendar.MINUTE,0)
        cal.set(Calendar.SECOND,0)
        cal.set(Calendar.MILLISECOND,0)
        return cal.time
    }
}