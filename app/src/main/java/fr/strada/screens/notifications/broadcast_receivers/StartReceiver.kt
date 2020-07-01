package fr.strada.screens.notifications.broadcast_receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import fr.strada.screens.notifications.notifications.DailyNotificationReceiver
import fr.strada.screens.notifications.notifications.EndlessService
import fr.strada.utils.SharedPreferencesUtils
import fr.strada.utils.Utils.log
import java.util.*

class StartReceiver : BroadcastReceiver() {

    private lateinit var context:Context

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        //Toast.makeText(context,"bootcompete",Toast.LENGTH_SHORT)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
            //Toast.makeText(context,"set alarm manager",Toast.LENGTH_SHORT)
            // code of ancien service notification
            /*
            Intent(context, EndlessService::class.java).also {
                it.action = Actions.START.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    log("Starting the service in >=26 Mode from a BroadcastReceiver")
                    context.startForegroundService(it)
                    return
                }
                log("Starting the service in < 26 Mode from a BroadcastReceiver")
                context.startService(it)
            }
            */
            // new code with alam manager
            setAlarmManager()
        }
    }

    fun setAlarmManager()
    {
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

        val intent = Intent(this.context, DailyNotificationReceiver::class.java)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        val pendingIntent =
            PendingIntent.getBroadcast(this.context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = this.context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelAlarmManager()
    {
        val alarmManager = this.context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val myIntent = Intent(this.context!!, DailyNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this.context!!, 1, myIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager!!.cancel(pendingIntent)
    }
}
