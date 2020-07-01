package fr.strada.screens.home.fragments


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shawnlin.numberpicker.NumberPicker
import com.shrikanthravi.collapsiblecalendarview.data.CalendarAdapter
import com.shrikanthravi.collapsiblecalendarview.data.Day
import com.shrikanthravi.collapsiblecalendarview.data.Event
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import fr.strada.R
import fr.strada.models.ActivityDailyRecords
import fr.strada.models.CardDriverActivity
import fr.strada.models.InfoCard
import fr.strada.screens.home.MainActivity
import fr.strada.utils.LocaleHelper
import fr.strada.utils.RealmManager
import fr.strada.utils.SharedPreferencesUtils
import fr.strada.utils.TimeSpan
import kotlinx.android.synthetic.main.dialog_date.*
import kotlinx.android.synthetic.main.fragment_hebdomadaire_calender.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HebdomadaireCalenderFragment : Fragment()  {

    private lateinit var mContext: Context
    var driverActivityData = CardDriverActivity()
    lateinit var adapter : RecyclerhebdoAdapter
    var cal = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootview = inflater.inflate(R.layout.fragment_hebdomadaire_calender, container, false)
        return rootview
    }

    fun makeEventTag(){
        var listEvent : ArrayList<Event> = ArrayList()

        //  var driverActivityDataused  = RealmManager.loadAllCardVehiclesUsed()[0].cardVehicleRecords
        for (data in driverActivityData.activityDailyRecords) {
                // var formatter1=SimpleDateFormat("dd/MM/yyyy");
                var eventDate: Date? = Date(data.activityRecordDate)
                var cal: Calendar = Calendar.getInstance()
                cal.time = eventDate
                var Eyear = cal.get(Calendar.YEAR)
                var Emonth = cal.get(Calendar.MONTH)
                var Eday = cal.get(Calendar.DAY_OF_MONTH)
                if (Eyear == calendarView.year)
                    listEvent.add(Event(Eyear, Emonth, Eday))
        }
        Handler().postDelayed({
            try {
                calendarView.clearListEventTag()
                calendarView.addListEventTag(listEvent)
            }catch (e:Exception){

            }
        },10)
    }


    fun makeUiConfig(){
        var year = arguments!!.getString("year")
        var day = arguments!!.getString("day")
        var month = arguments!!.getString("month")
        var week = arguments!!.getString("week")

        (activity as MainActivity).txttitle.text = getMonthName(month!!.toInt())+" "+year

        calendarView.month = month.toInt()
        calendarView.year = year!!.toInt()
        cal.set(year!!.toInt(),month.toInt(),day!!.toInt())
        cal.set(Calendar.WEEK_OF_YEAR,week!!.toInt()-1)
        calendarView.setAdapter(CalendarAdapter(activity!!,cal))
        calendarView.select(Day(year.toInt(),month.toInt(),day.toInt()))

        calendarView.expand(100)
        Handler().postDelayed({
            if (calendarView != null){
                calendarView.collapse(200)
                calcule(driverActivityData,calendarView.month,calendarView.year,calendarView.selectedDay!!.day)
            }
        },1000)


        var calender = Calendar.getInstance()
        calender.set(Calendar.YEAR,calendarView.year)
        calender.set(Calendar.MONTH,calendarView.month)
        calender.set(Calendar.WEEK_OF_MONTH,0)
        if (SharedPreferencesUtils.firstDayOfMonth==1){
            calender.firstDayOfWeek = Calendar.MONDAY
            calender.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }else{
            calender.firstDayOfWeek = Calendar.SUNDAY
            calender.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        calendarView.isSwipeMode(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)

        try {
            driverActivityData = RealmManager.loadAllCardDriverActivity()[0]!!
            makeUiConfig()
            makeEventTag()
        }catch (e: Exception){
            Log.e("Exception",e.message)
        }


        txtUTC.text = SharedPreferencesUtils.timeFormat

        rv_hebdo.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)

        (activity as MainActivity).txttitle.setOnClickListener {
        }
        (activity as MainActivity).txtdateJour.setOnClickListener {
            calendarView.changeToToday()
        }





        calendarView.setCalendarListener(object: CollapsibleCalendar.CalendarListener{
            override fun onDaySelect() {
                Log.d("params","onDaySelect")
            }

            override fun onItemClick(v: View) {
                Log.d("params","onItemClick")
                changeFragment(JournalierCalenderFragment(),activity!!,true)
            }

            override fun onDataUpdate() {
                System.out.println("Start onDataUpdate");

                var month = calendarView.month
                var year = calendarView.year
                (activity as MainActivity).txttitle.text = getMonthName(month)+" "+year

                calcule(driverActivityData,calendarView.month,calendarView.year,calendarView.selectedDay!!.day)

            }

            override fun onMonthChange() {
                System.out.println("Start ISnext week:   onMonthChange");
                var month = calendarView.month
                var year = calendarView.year
                (activity as MainActivity).txttitle.text = getMonthName(month)+" "+year
                makeEventTag()
            }

            override fun onWeekChange(position: Int,isNext : Boolean) {


            }

            override fun onClickListener() {
                Log.d("params","onClickListener")

            }

            override fun onDayChanged() {
                Log.d("params","onDayChanged")

            }
        })

        (activity as MainActivity).txttitle.setOnClickListener {

            var year : Int = calendarView.year
            var month : Int = calendarView.month
            val datePickerView = Dialog(activity!!)
            datePickerView.requestWindowFeature(Window.FEATURE_NO_TITLE)
            datePickerView.setCancelable(false)
            datePickerView.setContentView(R.layout.dialog_year)
            datePickerView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            //datePickerView.txtDateSelected.text = getMonthName(month)+" "+year

            var picker =  datePickerView.findViewById(R.id.year_picker) as fr.strada.utils.NumberPicker
           // picker.value = year



            datePickerView.btnDone.setOnClickListener {


               // changeFragment(MensuelCalenderFragment(),activity!!)
                datePickerView.dismiss()

                val datePickerViewWeek = Dialog(activity!!)
                datePickerViewWeek.requestWindowFeature(Window.FEATURE_NO_TITLE)
                datePickerViewWeek.setCancelable(false)
                datePickerViewWeek.setContentView(R.layout.dialog_hebdo)
                datePickerViewWeek.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                var Weekpicker =  datePickerViewWeek.findViewById(R.id.hebdo_picker) as NumberPicker
                val data = Array<String>(53) { "it = $it" }
                val DayStart = Array<String>(53) { "it = $it" }

                getCurrentWeek(picker.currentNumber)

                for (i in 0..52) {
                    var NextWeek = getNextWeek()
                    data[i] = resources.getString(R.string.semaine)+" "+ i + " :"+ NextWeek[0] + " - "+ NextWeek[6]
                    DayStart[i] = NextWeek[0].toString()
                }

                Weekpicker.minValue = 1
                Weekpicker.maxValue = data.size
                Weekpicker.displayedValues = data
                Weekpicker.value = 1

                datePickerViewWeek.btnDone.setOnClickListener {
                    // just for test dangerous
                    var locale:Locale? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        locale = Resources.getSystem().getConfiguration().getLocales().get(0);
                    } else {
                        locale = Resources.getSystem().getConfiguration().locale;
                    }
                    ///////////
                    var format =  SimpleDateFormat("dd MMM yyyy",locale)
                    calendarView.year = picker.currentNumber
                    calendarView.prevMonth()
                    (activity as MainActivity).txttitle.text = getMonthName(month-1)+" "+year
                    cal.time = Date(format.parse(DayStart[Weekpicker.value-1]).time)
                    var year = cal.get(Calendar.YEAR)
                    var day= cal.get(Calendar.DATE)
                    var month= cal.get(Calendar.MONTH)
                    var week= cal.get(Calendar.WEEK_OF_YEAR)
                    calendarView.year = year
                    calendarView.month = month
                    calendarView.select(Day(year.toInt(),month.toInt(),day!!.toInt()))

                    (activity as MainActivity).txttitle.text = getMonthName(month)+" "+year


                    calendarView.expand(100)
                    Handler().postDelayed({
                        if (calendarView != null)
                            calendarView.collapse(200)
                    },1000)


                    datePickerViewWeek.dismiss()
                }

                datePickerViewWeek.btnCancel.setOnClickListener {
                    datePickerViewWeek.dismiss()
                }

                datePickerViewWeek.show()


            }

            datePickerView.btnCancel.setOnClickListener {
                datePickerView.dismiss()
            }

            datePickerView.show()

        }


    }

    lateinit var calendar : Calendar


    fun getCurrentWeek(year : Int) : Array<String?> {
        this.calendar = Calendar.getInstance()
        this.calendar.set(Calendar.YEAR,year)
        this.calendar.set(Calendar.WEEK_OF_YEAR,0)
        this.calendar.set(Calendar.MONTH,0)
        if (SharedPreferencesUtils.firstDayOfMonth==1){
            this.calendar.set(Calendar.DAY_OF_MONTH, Calendar.MONDAY);
            this.calendar.firstDayOfWeek = Calendar.MONDAY
            this.calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }else{
            this.calendar.set(Calendar.DAY_OF_MONTH, Calendar.SUNDAY);
            this.calendar.firstDayOfWeek = Calendar.SUNDAY
            this.calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        return getNextWeek()
    }
    fun getNextWeek(): Array<String?> {
        //just for test
        var locale:Locale? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0)
        } else {
            locale = Resources.getSystem().getConfiguration().locale
        }
        //////
        var format =  SimpleDateFormat("dd MMM yyyy",locale)
        var days =  arrayOfNulls<String>(7)

        for (i in days.indices) {
            days[i] = format.format(this.calendar.time)
            this.calendar.add(Calendar.DATE, 1)
        }
        return days
    }


    fun calcule(driverActivityData : CardDriverActivity, month: Int, year:Int, day: Int){

        var dataevent  = ArrayList<ActivityDailyRecords>()
        var KM  = 0
        val hConduite = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        val hRepos = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        val hTravail = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        val hMAD = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        val hService = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        val hNuit = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        var listEvent : ArrayList<Event> = ArrayList()

        for (data in driverActivityData.activityDailyRecords){

            var eventDate : Date? = Date(data.activityRecordDate)
            var cal : Calendar = Calendar.getInstance()
            cal.time = eventDate
            var Eyear = cal.get(Calendar.YEAR)
            var Emonth = cal.get(Calendar.MONTH)
            var Eday = cal.get(Calendar.DAY_OF_MONTH)

            if (Eyear==year && Emonth==month){
                for (i in 0..6){
                    if (Eday==(day+i)){
                        dataevent.add(data)
                        KM+=data.activityDayDistance
                        for (activityInfo in data.activityChangeInfo.indices){
                            if(data.activityChangeInfo[activityInfo]!!.activite == InfoCard.EnumMapper.ActiviteConducteur.Conduite.name && data.activityChangeInfo[activityInfo]!!.etatCarte.equals(
                                    InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                                try {

                                    var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1]!!.time)
                                    var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo]!!.time)
                                    hConduite.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                    hConduite.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
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
                                    // juste pour le calcule last event in day not calculated in last version

                                    var nextEvent =  getDateTime("23:59:00")
                                    var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo]!!.time)
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

                                }

                            }
                            if(data.activityChangeInfo[activityInfo]!!.activite == InfoCard.EnumMapper.ActiviteConducteur.Repos.name && data.activityChangeInfo[activityInfo]!!.etatCarte.equals(
                                    InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                                try {
                                    var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1]!!.time)
                                    var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo]!!.time)
                                    hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                    hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))

                                }catch (e: Exception){
                                    // just for test if false remove it
                                    var nextEvent =  getDateTime("23:59:00")
                                    var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo]!!.time)
                                    hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                    hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                    ////
                                }
                            }
                            if(data.activityChangeInfo[activityInfo]!!.activite == InfoCard.EnumMapper.ActiviteConducteur.Travail.name && data.activityChangeInfo[activityInfo]!!.etatCarte.equals(
                                    InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                                try {
                                    var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1]!!.time)
                                    var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo]!!.time)
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
                                    // just for test if false remove it
                                    var nextEvent =  getDateTime("23:59:00")
                                    var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo]!!.time)
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
                                    /////////////////////////////////////

                                }
                            }
                            if(data.activityChangeInfo[activityInfo]!!.activite == InfoCard.EnumMapper.ActiviteConducteur.Disponibilite.name && data.activityChangeInfo[activityInfo]!!.etatCarte.equals(
                                    InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name) ){
                                try {
                                    var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1]!!.time)
                                    var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo]!!.time)
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
                                    // just for test if dalse remove it
                                    var nextEvent =  getDateTime("23:59:00")
                                    var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo]!!.time)
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
                                    ////////////

                                }
                            }
                        }
                    }
                }



            }




        }



        adapter =  RecyclerhebdoAdapter(activity!!,dataevent)
        adapter.notifyDataSetChanged()
        rv_hebdo.adapter = adapter

        hService.add(hConduite)
        hService.add(hMAD)
        hService.add(hTravail)

        if (SharedPreferencesUtils.isTimeModeHHMM){
            txtHebConduite.text = hConduite.toStringHHMM()
            txtHebKM.text = KM.toString()
            txtHebMAD.text = hMAD.toStringHHMM()
            txtHebNuit.text = hNuit.toStringHHMM()
            txtHebTravail.text = hTravail.toStringHHMM()
            txtHebService.text = hService.toStringHHMM()
        }else{
            txtHebConduite.text = hConduite.toStringHHH()
            txtHebKM.text = KM.toString()
            txtHebMAD.text = hMAD.toStringHHH()
            txtHebNuit.text = hNuit.toStringHHH()
            txtHebTravail.text = hTravail.toStringHHH()
            txtHebService.text = hService.toStringHHH()
        }


    }

    fun getDateTime(dtStart : String) : Date {
        var format = SimpleDateFormat("HH:mm:ss")

        try {
            var date = format.parse(dtStart)
            return date
        } catch (e: ParseException) {
            e.printStackTrace()

        }
        return Date()
    }

    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
        mContext = context
    }

    //------------------------------ fun changeFragment() ---------------------------------------------------------
    fun changeFragment(f: Fragment, mActivity: Activity, cleanStack: Boolean = true) {
        val ft = (mActivity as AppCompatActivity).supportFragmentManager.beginTransaction()
        if (cleanStack) {
            // clearBackStack();
            ft.remove(f)
        }

        var args =  Bundle()
        args.putString("year", calendarView.selectedDay!!.year.toString())
        args.putString("day", calendarView.selectedDay!!.day.toString())
        args.putString("month", calendarView.selectedDay!!.month.toString())
        f.arguments = args
        ft.replace(R.id.container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

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




}// Required empty public constructor