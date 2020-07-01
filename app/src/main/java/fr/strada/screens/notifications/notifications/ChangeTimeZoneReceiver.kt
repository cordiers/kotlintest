package fr.strada.screens.notifications.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import fr.strada.screens.notifications.broadcast_receivers.ServiceState
import fr.strada.screens.notifications.broadcast_receivers.getServiceState
import fr.strada.screens.splash.SplachScreenActivity
import fr.strada.utils.SharedPreferencesUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ChangeTimeZoneReceiver:BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        //Toast.makeText(context,"renisialisation",Toast.LENGTH_LONG).show()
        GlobalScope.launch {
            if(getServiceState(context!!) == ServiceState.STARTED)
            {   cancelAlarmManager(context!!)
                delay(4000)
                setAlarmManager(context!!)
            }
        }

    }

    private fun cancelAlarmManager(context:Context) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val myIntent = Intent(context, DailyNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1, myIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager!!.cancel(pendingIntent)
    }

    fun setAlarmManager(context:Context)
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

        val intent = Intent(context, DailyNotificationReceiver::class.java)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        val pendingIntent =
            PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}