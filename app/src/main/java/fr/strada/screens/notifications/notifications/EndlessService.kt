package fr.strada.screens.notifications.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import java.text.SimpleDateFormat
import java.util.*
import fr.strada.R
import fr.strada.screens.home.MainActivity
import fr.strada.screens.notifications.NotificationsActivity
import fr.strada.screens.notifications.broadcast_receivers.Actions
import fr.strada.screens.notifications.broadcast_receivers.ServiceState
import fr.strada.screens.notifications.broadcast_receivers.setServiceState
import fr.strada.utils.RealmManager
import fr.strada.utils.SharedPreferencesUtils
import fr.strada.utils.Utils.log
import kotlinx.coroutines.*


class EndlessService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    override fun onBind(intent: Intent): IBinder? {
        log("Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            log("using an intent with action $action")
            when (action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> log("This should never happen. No action in the received intent")
            }
        } else {
            log(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log("The service has been created".toUpperCase())
        var cal = Calendar.getInstance()
        cal.time = Date()
        // get heure and minute notification
        var heure = 8
        var minute = 0
        try {
            heure = SharedPreferencesUtils.heureNotification!!.substring(0,2).toInt()
            minute = SharedPreferencesUtils.heureNotification!!.substring(3,5).toInt()
        }catch (ex:Exception)
        {
          Toast.makeText(this,ex.message,Toast.LENGTH_SHORT).show()
        }
        ///
        if (cal.get(Calendar.HOUR_OF_DAY) == heure && cal.get(Calendar.MINUTE) == minute)
            callNotificationCheck()
        else{
            val notification = createNotificationTest()
            startForeground(1,notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        log("The service has been destroyed".toUpperCase())
    }

    private fun startService() {
        if (isServiceStarted) return
        log("Starting the foreground service task")
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }


        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
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
                launch(Dispatchers.IO) {
                    var cal = Calendar.getInstance()
                    cal.time = Date()
                    if (cal.get(Calendar.HOUR_OF_DAY) == heure && cal.get(Calendar.MINUTE)  == minute)
                    callNotificationCheck()
                }
                delay(1 * 60 * 1000)
            }
            log("End of the loop for the service")
        }
    }

    private fun stopService() {
        log("Stopping the foreground service")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    private fun createNotificationTest(): Notification {

        val pendingIntent: PendingIntent = Intent(this, NotificationsActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        ) else Notification.Builder(this)
        return builder
            .setContentText("cette application exécute le système d'arrière-plan")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    fun callNotificationCheck() {
        RealmManager.open()
        var listnot = RealmManager.loadNotifications()
        if (!listnot.isNullOrEmpty())
            for (not in listnot) {
                val timeOff9 = Calendar.getInstance()
                var fmt = SimpleDateFormat("dd MMMM yyyy")
                timeOff9.time = fmt.parse(not.date)
                var numberOfDays = Calendar.getInstance()
                numberOfDays.time = Date()
                var Difference_In_Days = (timeOff9.time.time - numberOfDays.time.time) / (1000 * 3600 * 24)
                if ((Difference_In_Days + 1) in 0..not.currentJourNotfication)
                    if (not.checkEd){
                        setDocumentNotification("Attention,  le document ${not.title} expirera dans ${Difference_In_Days + 1} jour", 0, not.id)
                    }
            }

        var lastDownLoadCard = RealmManager.loadCardDownload()
        if (lastDownLoadCard != null && !lastDownLoadCard.LastCardDownload.isNullOrEmpty()) {
            var c: Calendar = Calendar.getInstance()
            c.time = Date(lastDownLoadCard.LastCardDownload)
            //c.add(Calendar.DAY_OF_MONTH,28)
            var numberOfDays = Calendar.getInstance()
            numberOfDays.time = Date()
            var Difference_In_Days = (numberOfDays.time.time - c.time.time) / (1000 * 3600 * 24)
            if ((28-Difference_In_Days) in 0..SharedPreferencesUtils.delaisAvertisement!!.toInt()) {
                if (lastDownLoadCard.notificationChecked)
                {
                    setCardNotification("Attention, il ne vous reste que ${28 - Difference_In_Days} jours pour décharger votre carte", 1, lastDownLoadCard.LastCardDownload)
                }
            }
        }

        var CardIdentification = RealmManager.loadAllCardIdentification()
        if (CardIdentification != null && !CardIdentification.cardExpiryDate.isNullOrEmpty()) {
            var c: Calendar = Calendar.getInstance()
            c.time = Date(CardIdentification.cardExpiryDate)
            var numberOfDays = Calendar.getInstance()
            numberOfDays.time = Date()
            var Difference_In_Days = (c.time.time - numberOfDays.time.time) / (1000 * 3600 * 24)
            if (Difference_In_Days + 1 in 0..SharedPreferencesUtils.delaisAvertisementCarte!!.toInt()) {
                if (CardIdentification.notificationChecked)
                setCardNotification("Attention, votre carte conducteur expirera dans ${Difference_In_Days + 1} jours", 2, CardIdentification.cardExpiryDate.toString())
            }

        }

    }

    private var currentNotificationID = 5
    var NOTIFICATION_CHANNEL_ID = "10001"

    private fun setDocumentNotification(notificationText: String, type: Int, id: String) {

        // Do something. For example, fetch fresh data from backend to create a rich notification?

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle("Strada Notification")
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimaryBlue))
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)

        val mainIntent = Intent(this, NotificationsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        val notification = builder!!.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        currentNotificationID++
        var notificationId = currentNotificationID
        if (notificationId == Integer.MAX_VALUE - 1)
            notificationId = 0
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, notification)
        }
        // builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(this))

        val answerIntent = Intent(this, NotificationsActivity::class.java)
        answerIntent.putExtra("type", type)
        answerIntent.putExtra("id", id)
        answerIntent.action = "Pause"
        answerIntent.putExtra("notificationId",notificationId)
        val pendingIntentNo = PendingIntent.getActivity(this, 1, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        var color = ContextCompat.getColor(this, R.color.colorPrimaryBlue);
        builder.addAction(R.drawable.ic_not_off, HtmlCompat.fromHtml("<font color=" + color + ">" + "Ne plus recevoir cette notification" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY) , pendingIntentNo)

        val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }
    private fun setCardNotification(notificationText: String, type: Int, id: String) {

        // Do something. For example, fetch fresh data from backend to create a rich notification?

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle("Strada Notification")
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimaryBlue))
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)

        val mainIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        val notification = builder!!.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        currentNotificationID++
        var notificationId = currentNotificationID
        if (notificationId == Integer.MAX_VALUE - 1)
            notificationId = 0
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, notification)
        }
        // builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(this))

        val answerIntent = Intent(this, MainActivity::class.java)
        answerIntent.putExtra("type", type)
        answerIntent.putExtra("id", id)
        answerIntent.action = "Pause"
        answerIntent.putExtra("notificationId",notificationId)
        val pendingIntentNo = PendingIntent.getActivity(this, 1, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        var color = ContextCompat.getColor(this, R.color.colorPrimaryBlue);
        builder.addAction(R.drawable.ic_not_off, HtmlCompat.fromHtml("<font color=" + color + ">" + "ne plus recevoir cette notification" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY) , pendingIntentNo)

        val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }


}
