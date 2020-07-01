package fr.strada.screens.home.fragments


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.shrikanthravi.collapsiblecalendarview.data.CalendarAdapter
import com.shrikanthravi.collapsiblecalendarview.data.Event
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.models.CardDriverActivity
import fr.strada.models.InfoCard
import fr.strada.screens.home.MainActivity
import fr.strada.screens.notifications.NotificationsActivity
import fr.strada.utils.*
//import fr.strada.utils.Utils.getMonthName
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_date.*
import kotlinx.android.synthetic.main.fragment_mensuel_calender.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MensuelCalenderFragment : Fragment()   {

    private lateinit var mContext: Context
    private var eventList = ArrayList<Date>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootview = inflater.inflate(R.layout.fragment_mensuel_calender, container, false)
        RealmManager.open()

        return rootview
    }

    lateinit var driverActivityData : RealmResults<CardDriverActivity>

    fun makeEventTag(year : Int){

        var listEvent : ArrayList<Event> = ArrayList()

        for (data in driverActivityData) {

            for (data in data.activityDailyRecords) {

                var eventDate: Date? = Date(data.activityRecordDate)
                var cal: Calendar = Calendar.getInstance()
                cal.time = eventDate
                var Eyear = cal.get(Calendar.YEAR)
                var Emonth = cal.get(Calendar.MONTH)
                var Eday = cal.get(Calendar.DAY_OF_MONTH)
                if (Eyear == year)
                    listEvent.add(Event(Eyear, Emonth, Eday))

            }
        }
        Handler().postDelayed({
            try {
                calendarView.clearListEventTag()
                calendarView.addListEventTag(listEvent)
            }catch (e:Exception){

            }
        },10)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)

        val sdf = SimpleDateFormat("dd")
        val currentDate = sdf.format(Date())
        (activity as MainActivity).rlTitle.visibility = View.VISIBLE
        (activity as MainActivity).imgLogo.visibility = View.GONE
        (activity as MainActivity).floatingActionButtonSettings.visibility = View.INVISIBLE
        (activity as MainActivity).rlIcon.visibility = View.VISIBLE
        (activity as MainActivity).btnIconToolbar.setImageResource(R.drawable.ic_calender)
        (activity as MainActivity).txtdateJour.visibility = View.VISIBLE
        (activity as MainActivity).dropdown.visibility = View.VISIBLE
        (activity as MainActivity).txtdateJour.text = currentDate


        (activity as MainActivity).rlIcon.setOnClickListener {
            // nothing
        }

        (activity as MainActivity).txtdateJour.setOnClickListener {
            calendarView.changeToToday()
            calendarView.prevMonth()
            calendarView.nextMonth()
        }

        var l = Loader.getInstance()
        l.show(activity)

        try {
            Handler().postDelayed({
                try {
                    l.dismiss()
                    driverActivityData  = RealmManager.loadAllCardDriverActivity()
                    makeEventTag(calendarView.year)

                    calendarView.prevMonth()
                    calendarView.nextMonth()

                }catch (e:Exception){
                    l.dismiss()

                    Log.e("Exception",e.message)
                }
            },100)
        }catch (e:Exception){
            l.dismiss()
        }


        if (arguments != null && !arguments!!.isEmpty){
            var year = arguments!!.getString("year")
            var day = arguments!!.getString("day")
            var month = arguments!!.getString("month")
            (activity as MainActivity).txttitle.text = getMonthName(month!!.toInt())+" "+year

            calendarView.month = month.toInt()
            calendarView.year = year!!.toInt()
            var cal = Calendar.getInstance()
            cal.set(year.toInt(),month.toInt(),1)
            calendarView.setAdapter(CalendarAdapter(activity!!,cal))
        }


        calendarView.setCalendarListener(object:CollapsibleCalendar.CalendarListener{
            override fun onDaySelect() {
                Log.d("params","onDaySelect")

                changeFragment(JournalierCalenderFragment(),activity!!,true)
            }

            override fun onItemClick(v: View) {
                Log.d("params","onItemClick")

            }

            override fun onDataUpdate() {

                Log.d("params","onDataUpdate")
                var month = calendarView.month
                var year = calendarView.year
                (activity as MainActivity).txttitle.text = getMonthName(month)+" "+year

            }

            override fun onMonthChange() {
                Log.d("params","onMonthChange")
                var month = calendarView.month
                var year = calendarView.year
                Log.d("params",month.toString())
                (activity as MainActivity).txttitle.text = getMonthName(month)+" "+year
                calcule(driverActivityData,month,year)
                makeEventTag(calendarView.year)


            }

            override fun onWeekChange(position: Int, isNext : Boolean) {
                Log.d("params","onWeekChange"+position)


            }

            override fun onClickListener() {
                Log.d("params","onClickListener")

            }

            override fun onDayChanged() {
                Log.d("params","onDayChanged")

            }
        })

        // change month and year
        (activity as MainActivity).txttitle.setOnClickListener {

            var year : Int = calendarView.year
            var month : Int = calendarView.month
            val datePickerView = Dialog(activity!!)
            datePickerView.requestWindowFeature(Window.FEATURE_NO_TITLE)
            datePickerView.setCancelable(false)
            datePickerView.setContentView(R.layout.dialog_date)
            datePickerView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            datePickerView.txtDateSelected.text = getMonthName(month)+" "+year

            var picker =  datePickerView.findViewById(R.id.date_picker) as DatePicker
            picker.setDate(year,month)
            picker.setOnDateClickedListener { _, i, i2, i3 ->
                year = i
                month = i2
                Log.d("TimeMensuel",getMonthName(month-1)+year)
            }



            datePickerView.btnDone.setOnClickListener {

                calendarView.year = picker.year
                calendarView.month = picker.month
                calendarView.prevMonth()

                (activity as MainActivity).txttitle.text = getMonthName(month-1)+" "+year
                datePickerView.dismiss()
            }

            datePickerView.btnCancel.setOnClickListener {
                datePickerView.dismiss()
            }

            datePickerView.show()

        }

        l.dismiss()

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
                var Eday = cal.get(Calendar.DAY_OF_MONTH)


                if (Eyear==year && Emonth==month){

                    var dataInfo = data.activityChangeInfo
                    KM+=data.activityDayDistance
                    for (activityInfo in dataInfo.indices){
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
                                ///// si cette code est false enleve le
                                var nextEvent =  getDateTime("23:59:00")
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hConduite.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                hConduite.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                // hConduite.add(TimeSpan())
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
                                ///////
                            }

                        }
                        if(data.activityChangeInfo[activityInfo].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Repos.name) && data.activityChangeInfo[activityInfo].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                            try {
                                var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1].time)
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                                hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))

                            }catch (e:Exception){
                                //// si cette code est false enleve le
                                var nextEvent =  getDateTime("23:59:00")
                                var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                                hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1)
                                hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
                                ///////
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
                                ////////// ci cette code est false enleve le
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
                                //////////
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
                            }catch (e:Exception){
                                /////// si cette code est false eneleve le
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
                                ///////
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
            txtConduit.text = hConduite.toStringHHMM()
            txtKM.text = KM.toString()
            txtMAD.text = hMAD.toStringHHMM()
            txtNuit.text = hNuit.toStringHHMM()
            txtTravail.text = hTravail.toStringHHMM()
            txtService.text = hService.toStringHHMM()
        }else{
            txtConduit.text = hConduite.toStringHHH()
            txtKM.text = KM.toString()
            txtMAD.text = hMAD.toStringHHH()
            txtNuit.text = hNuit.toStringHHH()
            txtTravail.text = hTravail.toStringHHH()
            txtService.text = hService.toStringHHH()
        }





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

    //just for test si elle est false il faut le supprimer
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