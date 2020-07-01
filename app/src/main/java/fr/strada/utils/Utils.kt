package fr.strada.utils

import android.util.Log
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.models.Notifications
import io.realm.RealmResults
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun getFilterList(listNotifications: RealmResults<Notifications>) : ArrayList<Notifications> {
        var list: ArrayList<Notifications> = ArrayList()
        for (i in listNotifications){
            var cal = Calendar.getInstance()
            var fmt =  SimpleDateFormat("dd MMMM yyyy")
            cal.time =  fmt.parse(i.date)
            cal.add(Calendar.DAY_OF_MONTH,-i.currentJourNotfication)
            //  var test =  cal.time.before(Date())
            if (cal.time.before(Date()) && i.checkEd){
                list.add(i)
            }
        }

        return list
    }

    fun log(msg: String) {
        Log.d("STRADA-SERVICE", msg)
    }

    fun getMonthName(month:Int) : String{
        when (month) {
            0 -> {
                return StradaApp.instance!!.resources.getString(R.string.janvier)
            }
            1 -> {
                return StradaApp.instance!!.resources.getString(R.string.fevrier)
            }
            2 -> {
                return StradaApp.instance!!.resources.getString(R.string.mars)
            }
            3 -> {
                return StradaApp.instance!!.resources.getString(R.string.avril)
            }
            4 -> {
                return StradaApp.instance!!.resources.getString(R.string.mai)
            }
            5 -> {
                return StradaApp.instance!!.resources.getString(R.string.juin)
            }
            6 -> {
                return StradaApp.instance!!.resources.getString(R.string.juillet)
            }
            7 -> {
                return StradaApp.instance!!.resources.getString(R.string.aout)
            }
            8 -> {
                return StradaApp.instance!!.resources.getString(R.string.septembre)
            }
            9 -> {
                return StradaApp.instance!!.resources.getString(R.string.octobre)
            }
            10 -> {
                return StradaApp.instance!!.resources.getString(R.string.novembre)
            }
            11 -> {
                return StradaApp.instance!!.resources.getString(R.string.decembre)
            }
        }
        return ""
    }

}