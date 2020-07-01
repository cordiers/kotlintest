package fr.strada.screens.home

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.strada.R
import fr.strada.screens.notifications.NotificationsActivity
import fr.strada.utils.RealmManager
import fr.strada.utils.SharedPreferencesUtils
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import fr.strada.screens.notifications.broadcast_receivers.Actions
import fr.strada.screens.notifications.broadcast_receivers.ServiceState
import fr.strada.screens.notifications.broadcast_receivers.getServiceState
import fr.strada.screens.notifications.notifications.DailyNotificationReceiver
import fr.strada.screens.notifications.notifications.EndlessService
import fr.strada.utils.NotificationPublisher
import fr.strada.utils.Utils

open class BaseActivity : AppCompatActivity() {
    val NOTIFICATION_CHANNEL_ID = "10001"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        //ancian code
        /*
        if (getServiceState(this) == ServiceState.STARTED)
            actionOnService(Actions.START)
         */
        // new code // if first time de alarm
        if(SharedPreferencesUtils.isFirstOpen == true)
        {
            setAlarmManager()
            SharedPreferencesUtils.isFirstOpen = false
        }
    }



    private fun actionOnService(action: Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(this, EndlessService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Utils.log("Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            Utils.log("Starting the service in < 26 Mode")
            startService(it)
        }
    }
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = "Default"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            var notificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager!!.createNotificationChannel(channel)
        }
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

        val intent = Intent(this, DailyNotificationReceiver::class.java)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        val pendingIntent =
            PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = this!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

}