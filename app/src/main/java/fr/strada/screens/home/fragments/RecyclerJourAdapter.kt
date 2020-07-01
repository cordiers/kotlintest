package fr.strada.screens.home.fragments

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import fr.strada.R
import fr.strada.models.ActivityChangeInfo
import fr.strada.models.ActivityDailyRecords
import fr.strada.models.CardVehicleRecords
import fr.strada.models.InfoCard
import fr.strada.utils.SharedPreferencesUtils
import fr.strada.utils.TimeSpan
import io.realm.RealmList
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*





class RecyclerJourAdapter(var day:Int,var month:Int ,var year:Int, activityDailyRecords: ActivityDailyRecords, Vehicule: RealmList<CardVehicleRecords>) : RecyclerView.Adapter<RecyclerJourAdapter.CustomViewHolder>(){

    var activityDailyRecords   :ActivityDailyRecords = activityDailyRecords
    var documentArray   : RealmList<ActivityChangeInfo> = activityDailyRecords.activityChangeInfo
    var vehicule   : RealmList<CardVehicleRecords> = Vehicule

    override fun getItemCount() = documentArray.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_jour, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerJourAdapter.CustomViewHolder, position: Int) {
        val res = holder.itemView.context.resources
        val hConduite = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hRepos = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hTravail = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hMAD = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        var utc = ""

        val hConduiteNoCard = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hReposNoCard = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hTravailNoCard = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);
        val hMADNoCard = TimeSpan(TimeSpan.MINUTES, 0)//Timer.FromMinutes(nbreMinutes);



        utc = if (!SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty()) SharedPreferencesUtils.timeFormatAUTO!!.substring(3) else if (SharedPreferencesUtils.timeFormat!!.substring(3).isEmpty()) "0" else SharedPreferencesUtils.timeFormat!!.substring(3)


        var Time =  TimeSpan.parse(documentArray[position].time)
        /*
        code iheb vey bad
        //TIme = 12:02 // UTC+12
        if (Time.isZero && utc.toLong()>0){
            Time.add(TimeSpan.HOURS,utc.toLong())
        }else if (Time.isZero && utc.toLong()<0) {
            Time.add(TimeSpan.HOURS,24+utc.toLong())
        } else if(Time.hours >= 12 && (Time.hours+utc.toLong())>=24) {
            var time = TimeSpan(0)
            time.add(TimeSpan.MINUTES,Time.minutes)
            time.add(TimeSpan.HOURS,24-(Time.hours+utc.toLong()))
            Time = time
        } else if(Time.hours+utc.toLong()<0) {
            var time = TimeSpan(0)
            time.add(TimeSpan.MINUTES,Time.minutes)
            time.add(TimeSpan.HOURS,24+(Time.hours+utc.toLong()))
            Time = time
        }
        else
            Time.add(TimeSpan.HOURS,utc.toLong())
        */

        // precess heuere ete hiver
        var decalageMinutesEteHiver = 0
        var lastSundayInMarch = getLastSundayInMarch()
        var lastSundayInOctobre = getLastSundayInOctobre()
        var lastSundayInMarchByYear = getLastSundayInMarchByYear(year)
        var lastSundayInOctobreByYear = getLastSundayInOctobreByYear(year)
        var currentDate = Date()
        //
        var cal = Calendar.getInstance()
        try {

            cal.set(year, month,day!!)
            var items=documentArray[position].time.split(":") // ex 0:0:0
            cal.set(Calendar.HOUR_OF_DAY, items.get(0).toInt() )   // set hours
            cal.set(Calendar.MINUTE,items.get(1).toInt())  // set minutes

        }catch (ex:Exception)
        {
            Toast.makeText(holder.imgIcActivity.context,ex.toString(),Toast.LENGTH_SHORT).show()
        }
        //
        var dateActivity =  cal.time
        if(SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty() && !SharedPreferencesUtils.timeFormat.equals("UTC")) // le cas manuelle
        {
            if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear)) // heure ete hiver et appliquer dans des certen contidition
            {
                decalageMinutesEteHiver = SharedPreferencesUtils.decalageMinutes!!
            }else{
                decalageMinutesEteHiver = 0
            }

        }else if (!SharedPreferencesUtils.timeFormatAUTO.isNullOrEmpty()) // automatique
        {
            if(currentDate.after(lastSundayInMarch) && currentDate.before(lastSundayInOctobre)) // date system periode ete
            {
                if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear)) // date system periode ete / date activite periode ete
                {
                    decalageMinutesEteHiver = SharedPreferencesUtils.decalageMinutes!!

                }else{  // date system periode ete / date activite periode hiver // a discuter

                    decalageMinutesEteHiver = 0 // on elimine le parametrage
                }

            }else{

                if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear)) // date system periode hiver / date activite periode ete
                {
                    decalageMinutesEteHiver = SharedPreferencesUtils.decalageMinutes!!

                }else{  // date system periode hiver / date activite periode hiver

                    decalageMinutesEteHiver = 0
                }

            }

        }else
        {
            if(dateActivity.after(lastSundayInMarchByYear) && dateActivity.before(lastSundayInOctobreByYear))
            {
                decalageMinutesEteHiver = SharedPreferencesUtils.decalageMinutes!!
            }else{
                decalageMinutesEteHiver = 0
            }
        }
        //
        Time.add(TimeSpan.MINUTES,decalageMinutesEteHiver!!.toLong())
        /////////////////
        if(Time.hours + utc.toLong() < 0) // previus day
        {
            Time.add(TimeSpan.HOURS,24 + (Time.hours + utc.toLong()))

        }else if(Time.hours + utc.toLong() >= 24)
        {
            var time = TimeSpan(0)
            time.add(TimeSpan.MINUTES,Time.minutes)
            time.add(TimeSpan.HOURS,24-(Time.hours+utc.toLong()))
            Time = time

        }else // cas normal
        {
            Time.add(TimeSpan.HOURS,utc.toLong())
        }
        // precees problem when heueres depace 24h
        if(Time.hours >= 24)
        {
            var time = TimeSpan(0)
            time.add(TimeSpan.MINUTES,Time.minutes)
            time.add(TimeSpan.HOURS,24-Time.hours)
            Time = time
        }
        holder.txtTimeStart.text = Time.toStringHHMM()
        //

        if(documentArray[position].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Conduite.name) && (documentArray[position].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name) || documentArray[position].etatConduite.equals(InfoCard.EtatConduite.Equipage.name))){
            try {

                var nextEvent =  getDateTime(documentArray[position+1].time)
                var currentEvent =  getDateTime(documentArray[position].time)
                hConduite.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                hConduite.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))

            }catch (e: Exception) // dernier row of list
            {
                var nextEvent =  getDateTime("23:59:00")
                var currentEvent =  getDateTime(documentArray[position].time)
                hConduite.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1) // car 24:0:0 ne marche pas
                hConduite.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
            }

        }else if (documentArray[position].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Conduite.name)){
            try {
                var nextEvent =  getDateTime(documentArray[position+1].time)
                var currentEvent =  getDateTime(documentArray[position].time)
                hConduiteNoCard.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                hConduiteNoCard.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
            }catch (ex:Exception)
            {

            }
        }

        if(documentArray[position].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Repos.name) && (documentArray[position].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name) || documentArray[position].etatConduite.equals(InfoCard.EtatConduite.Equipage.name))){
            try {
                var nextEvent =  getDateTime(documentArray[position+1].time)
                var currentEvent =  getDateTime(documentArray[position].time)
                hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
            }catch (e: Exception){

                var nextEvent =  getDateTime("23:59:00")
                var currentEvent =  getDateTime(documentArray[position].time)
                hRepos.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1) // car 12:0:0 ne marche pas
                hRepos.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
            }

        } else if(documentArray[position].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Repos.name)){
            try {
                var nextEvent =  getDateTime(documentArray[position+1].time)
                var currentEvent =  getDateTime(documentArray[position].time)
                hReposNoCard.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                hReposNoCard.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
            }catch (e: Exception){

            }
        }
        if(documentArray[position].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Travail.name) && (documentArray[position].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name) || documentArray[position].etatConduite.equals(InfoCard.EtatConduite.Equipage.name))){
            try {
                var nextEvent =  getDateTime(documentArray[position+1].time)
                var currentEvent =  getDateTime(documentArray[position].time)
                hTravail.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                hTravail.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))

            }catch (e: Exception){
                var nextEvent =  getDateTime("23:59:00")
                var currentEvent =  getDateTime(documentArray[position].time)
                hTravail.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1) // car 24:0:0 ne marche pas
                hTravail.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
            }
        }else if(documentArray[position].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Travail.name)){
            try {
                var nextEvent =  getDateTime(documentArray[position+1].time)
                var currentEvent =  getDateTime(documentArray[position].time)
                hTravailNoCard.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                hTravailNoCard.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
            }catch (e: Exception){

            }
        }
        if(documentArray[position].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Disponibilite.name) && (documentArray[position].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name) || documentArray[position].etatConduite.equals(InfoCard.EtatConduite.Equipage.name))){
            try {
                var nextEvent =  getDateTime(documentArray[position+1].time)
                var currentEvent =  getDateTime(documentArray[position].time)
                hMAD.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                hMAD.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))

            }catch (e: Exception){
                var nextEvent =  getDateTime("23:59:00")
                var currentEvent =  getDateTime(documentArray[position].time)
                hMAD.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong())+1) // car 24:0:0 ne marche pas
                hMAD.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))
            }
        }else if(documentArray[position].activite.equals(InfoCard.EnumMapper.ActiviteConducteur.Disponibilite.name)){
            try {
                var nextEvent =  getDateTime(documentArray[position+1].time)
                var currentEvent =  getDateTime(documentArray[position].time)
                hMADNoCard.add(TimeSpan.MINUTES,((nextEvent.minutes-currentEvent.minutes).toLong()))
                hMADNoCard.add(TimeSpan.HOURS,((nextEvent.hours-currentEvent.hours).toLong()))

            }catch (e: Exception){
                Log.i("houssem",e.message+" "+"Disponibilite carte non inserer")
            }
        }

        if (hConduite.isZero)  else {
            holder.txtTime.text = hConduite.toStringHHMM()
            holder.imgIcActivity.setImageResource(R.drawable.ic_conduite)
            holder.imgIcActivity.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_conduit)
            holder.txtVehicule.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_conduit)
            holder.txtTime.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_conduit)
            holder.txtTimeStart.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_j_conduite)
        }
        if (hTravail.isZero)  else {
            holder.txtTime.text = hTravail.toStringHHMM()
            holder.imgIcActivity.setImageResource(R.drawable.ic_travail)
            holder.imgIcActivity.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_travail)
            holder.txtVehicule.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_travail)
            holder.txtTime.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_travail)
            holder.txtTimeStart.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_j_travail)
        }
        if (hMAD.isZero)  else {
            holder.txtTime.text = hMAD.toStringHHMM()
            holder.imgIcActivity.setImageResource(R.drawable.ic_mad)
            holder.imgIcActivity.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_mad)
            holder.txtVehicule.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_mad)
            holder.txtTime.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_mad)
            holder.txtTimeStart.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_j_mad)
        }
        if (hRepos.isZero)  else {
            holder.txtTime.text = hRepos.toStringHHMM()
            holder.imgIcActivity.setImageResource(R.drawable.ic_coupure)
            holder.imgIcActivity.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_repos)
            holder.txtVehicule.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_repos)
            holder.txtTime.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_repos)
            holder.txtTimeStart.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_j_repos)
        }
        if (hConduite.isZero && hMAD.isZero && hTravail.isZero && hRepos.isZero)
        {
            if (!hConduiteNoCard.isZero){
                holder.txtTime.text = hConduiteNoCard.toStringHHMM()
            }else  if (!hTravailNoCard.isZero){
                holder.txtTime.text = hTravailNoCard.toStringHHMM()
            } else  if (!hMADNoCard.isZero){
                holder.txtTime.text = hMADNoCard.toStringHHMM()
            } else  if (!hReposNoCard.isZero){
                holder.txtTime.text = hReposNoCard.toStringHHMM()
            }
            holder.imgIcActivity.setImageResource(R.drawable.ic_no_activity)
            holder.imgIcActivity.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_empty)
            holder.txtVehicule.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_empty)
            holder.txtTime.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_empty)
            holder.txtTimeStart.background = ContextCompat.getDrawable(holder.itemView.context,R.drawable.bg_h_empty)
        }

        // process pour les vehicules
        for (cars in vehicule){
            var vehicleFirstUse : Date? = Date(cars.vehicleFirstUse)
            vehicleFirstUse=removeMilisecond(vehicleFirstUse!!)
            var vehicleLastUse : Date? = Date(cars.vehicleLastUse)
            var sfd = SimpleDateFormat("dd/MM/yyyy hh:mm:ss z")
            try {
                var eventDate=sfd.parse(""+day+"/"+(month+1)+"/"+year+" "+documentArray[position].time+" UTC")
                if(!vehicleFirstUse!!.after(eventDate) && !vehicleLastUse!!.before(eventDate) && documentArray[position].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name)) {
                    if (vehicule.isNotEmpty()) {
                        holder.txtVehicule.text = cars.vehicleRegistration.vehicleRegistrationNumber
                        break
                    }

                }
            }catch (ex:Exception){
                Log.i("eventDate",ex.toString())
            }

            /*

            var calvehicleFirstUse : Calendar = Calendar.getInstance()

            var test = TimeZone.getDefault()
            var isDST = test.inDaylightTime(vehicleFirstUse)
            var dstMillisec = test.dstSavings

            calvehicleFirstUse.time = vehicleFirstUse

            calvehicleFirstUse.add(Calendar.HOUR_OF_DAY,-1)

            calvehicleFirstUse.add(Calendar.MILLISECOND,-dstMillisec)

            var Vyear = calvehicleFirstUse.get(Calendar.YEAR)
            var Vmonth = calvehicleFirstUse.get(Calendar.MONTH)
            var Vday = calvehicleFirstUse.get(Calendar.DAY_OF_MONTH)

            var vehicleLastUse : Date? = Date(cars.vehicleLastUse)
            var calvehicleLastUse : Calendar = Calendar.getInstance()
            calvehicleLastUse.time = vehicleLastUse

            calvehicleLastUse.add(Calendar.HOUR_OF_DAY,-1)

            calvehicleLastUse.add(Calendar.MILLISECOND,-dstMillisec)

            var eventDate : Date? = Date(activityDailyRecords.activityRecordDate)
            var cal : Calendar = Calendar.getInstance()
            cal.time = eventDate
            var Eyear = cal.get(Calendar.YEAR)
            var Emonth = cal.get(Calendar.MONTH)
            var Eday = cal.get(Calendar.DAY_OF_MONTH)

            if (Eyear==Vyear && Emonth==Vmonth && Eday==Vday){

                var date = documentArray[position].time
                var EventDate = TimeSpan(TimeSpan.parse(date).totalMilliseconds)
                var vehicleFirstUse = TimeSpan.parse((calvehicleFirstUse.get(Calendar.HOUR_OF_DAY)).toString()+":"+calvehicleFirstUse.get(Calendar.MINUTE))
                var vehicleLastUse = TimeSpan.parse((calvehicleLastUse.get(Calendar.HOUR_OF_DAY)).toString()+":"+calvehicleLastUse.get(Calendar.MINUTE))
                if (TimeSpan.isAfter(EventDate,vehicleFirstUse) && !TimeSpan.isAfter(EventDate,vehicleLastUse) && documentArray[position].etatCarte.equals(InfoCard.EnumMapper.EtatCarteConducteur.Inseree.name))
                    if (vehicule.isNotEmpty()) holder.txtVehicule.text = cars.vehicleRegistration.vehicleRegistrationNumber

            }
            */



        }




    }
    fun removeMilisecond(date : Date) : Date
    {   var cal = Calendar.getInstance()
        cal.setTime(date)
        cal.set(Calendar.SECOND, 0)
        return cal.getTime()
    }








 class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

     val txtTimeStart : TextView = itemView.findViewById(R.id.txtTimeStart)
     val txtTime : TextView = itemView.findViewById(R.id.txtTime)
     val imgIcActivity : ImageView = itemView.findViewById(R.id.imgIcActivity)
     val txtVehicule : TextView = itemView.findViewById(R.id.txtVehicule)

//val  right_view : ImageView = itemView.right_view

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




}