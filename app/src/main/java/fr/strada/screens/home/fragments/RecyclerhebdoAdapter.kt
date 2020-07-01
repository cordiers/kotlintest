package fr.strada.screens.home.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import fr.strada.R
import fr.strada.models.ActivityDailyRecords
import fr.strada.models.InfoCard
import fr.strada.utils.SharedPreferencesUtils
import fr.strada.utils.TimeSpan
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RecyclerhebdoAdapter(var context: Context,dataevent: ArrayList<ActivityDailyRecords>) : RecyclerView.Adapter<RecyclerhebdoAdapter.CustomViewHolder>(){


    var documentArray   : ArrayList<ActivityDailyRecords>   = dataevent
    var daysArry : ArrayList<String> = arrayListOf( context.getString(R.string.di),context.getString(R.string.lu),context.getString(R.string.ma),context.getString(R.string.me),context.getString(R.string.je),context.getString(R.string.ve), context.getString(R.string.sa) )
    var daysArryL : ArrayList<String> = arrayListOf( context.getString(R.string.lu),context.getString(R.string.ma), context.getString(R.string.me), context.getString(R.string.je), context.getString(R.string.ve), context.getString(R.string.sa),context.getString(R.string.di) )

    var daysArryPOS : ArrayList<Int> = arrayListOf(1,2 , 3, 4, 5, 6, 7)
    var daysArryPOSL : ArrayList<Int> = arrayListOf(2, 3, 4, 5, 6, 7,1)

    override fun getItemCount() = daysArry.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_hebdo, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerhebdoAdapter.CustomViewHolder, position: Int) {
        if (SharedPreferencesUtils.firstDayOfMonth==0)
            holder.day.text = daysArry[position]
        else
            holder.day.text = daysArryL[position]

        var KM  = 0
        val hConduite = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        val hRepos = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        val hTravail = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        val hMAD = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        val hService = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        val hNuit = TimeSpan(TimeSpan.MINUTES, 0) //Timer.FromMinutes(nbreMinutes);
        for (data in documentArray){
            // var formatter1=SimpleDateFormat("dd/MM/yyyy");
            var eventDate : Date? = Date(data.activityRecordDate)
            var cal : Calendar = Calendar.getInstance()
            cal.time = eventDate
            // var Eyear = cal.get(Calendar.YEAR)
            // var Emonth = cal.get(Calendar.MONTH)
            // var Eday = cal.get(Calendar.DAY_OF_MONTH)
            var EdayofWeek = cal.get(Calendar.DAY_OF_WEEK)
            var indexEdayofWeek= if (SharedPreferencesUtils.firstDayOfMonth==0) daysArryPOS[position] else daysArryPOSL[position]
            // code of iheb EdayofWeek-(1+SharedPreferencesUtils.firstDayOfMonth!!))==position
            if (indexEdayofWeek == EdayofWeek){
                // calendarView.addEventTag(Eyear,Emonth,Eday)
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
                            // just for test if false remove it
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
                            ///////////
                        }
                    }

                    if(data.activityChangeInfo[activityInfo].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Repos.name) && data.activityChangeInfo[activityInfo].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)){
                        try {
                            var nextEvent =  getDateTime(data.activityChangeInfo[activityInfo+1].time)
                            var currentEvent =  getDateTime(data.activityChangeInfo[activityInfo].time)
                            hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                            hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))

                        }catch (e:Exception){
                            /////// just for test il false remove it
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
                            // just for test if false remove it
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
                            /////////
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
                            // just for test if false remove it
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
                            ////

                        }
                    }
                }

            }

        }


        hService.add(hConduite)
        hService.add(hMAD)
        hService.add(hTravail)

        if (SharedPreferencesUtils.isTimeModeHHMM){
            holder.txtHebConduite.text = hConduite.toStringHHMM()
            holder.txtHebKM.text = KM.toString()
            holder.txtHebMAD.text = hMAD.toStringHHMM()
            holder.txtHebNuit.text = hNuit.toStringHHMM()
            holder.txtHebTravail.text = hTravail.toStringHHMM()
            holder.txtHebService.text = hService.toStringHHMM()
        }else{
            holder.txtHebConduite.text = hConduite.toStringHHH()
            holder.txtHebKM.text = KM.toString()
            holder.txtHebMAD.text = hMAD.toStringHHH()
            holder.txtHebNuit.text = hNuit.toStringHHH()
            holder.txtHebTravail.text = hTravail.toStringHHH()
            holder.txtHebService.text = hService.toStringHHH()
        }

        if (KM == 0 && hConduite.isZero && hRepos.isZero && hTravail.isZero && hMAD.isZero &&  hService.isZero && hNuit.isZero ){
            holder.btnInfo.background = ContextCompat.getDrawable(holder.itemView.context,R.color.black)
            holder.btnInfo.isEnabled = false
            holder.btnInfo.isClickable = false
        }else {
            holder.btnInfo.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_btn_next_hebdo)
            holder.btnInfo.isEnabled = true
            holder.btnInfo.isClickable = true
        }


        holder.btnInfo.setOnClickListener {
            changeFragment(JournalierCalenderFragment(),holder.itemView.context,true, if (SharedPreferencesUtils.firstDayOfMonth==0) daysArryPOS[position] else  daysArryPOSL[position])
        }





    }

    fun changeFragment(f: Fragment, mActivity: Context, cleanStack: Boolean = true, day : Int) {
        val ft = (mActivity as AppCompatActivity).supportFragmentManager.beginTransaction()
        if (cleanStack) {
            // clearBackStack();
            ft.remove(f)
        }

        var cal = Calendar.getInstance()

        /*
        if (((7-documentArray.size)-pos)>0){
            datePos = (7-documentArray.size)-pos
        }else if (((7-documentArray.size)-pos)<0){
            datePos = -((7-documentArray.size)-pos)
        } else if (documentArray.size == 7){
            datePos = pos
        }
        if (documentArray.size<4){
            datePos -= 1
        }*/

        for (data in documentArray){
            cal.time = Date(data.activityRecordDate)
            val sdf = SimpleDateFormat("EE")
            val nu = cal.get(Calendar.DAY_OF_WEEK)




            if (nu == day){
                var args =  Bundle()
                args.putString("year", cal.get(Calendar.YEAR).toString())
                args.putString("day", cal.get(Calendar.DAY_OF_MONTH).toString())
                args.putString("month", cal.get(Calendar.MONTH).toString())
                f.arguments = args
                ft.replace(R.id.container, f)
                ft.addToBackStack(null)
                ft.commit()
            }

        }



    }



    fun removeItem (viewHolder : RecyclerView.ViewHolder){
        documentArray.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)
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



    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

     val day : TextView = itemView.findViewById(R.id.txt_day_of_week)
        val txtHebConduite : TextView = itemView.findViewById(R.id.txtHebConduite)
        val txtHebKM : TextView = itemView.findViewById(R.id.txtHebKM)
        val txtHebMAD : TextView = itemView.findViewById(R.id.txtHebMAD)
        val txtHebNuit : TextView = itemView.findViewById(R.id.txtHebNuit)
        val txtHebTravail : TextView = itemView.findViewById(R.id.txtHebTravail)
        val txtHebService : TextView = itemView.findViewById(R.id.txtHebService)
        val btnInfo : RelativeLayout = itemView.findViewById(R.id.btnInfo)

//val  right_view : ImageView = itemView.right_view









 }






}