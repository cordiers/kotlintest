package fr.strada.screens.notifications.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.screens.home.MainActivity
import fr.strada.screens.notifications.NotificationsActivity
import fr.strada.utils.RealmManager
import fr.strada.utils.SharedPreferencesUtils
import java.text.SimpleDateFormat
import java.util.*

class DailyNotificationReceiver : BroadcastReceiver() {

    private lateinit var context:Context

    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context!!
        // just for test
        //Toast.makeText(context,"notif",Toast.LENGTH_SHORT).show()
        if(SharedPreferencesUtils.isLoggedIn)
        {
            callNotificationCheck()
        }
    }

    fun callNotificationCheck() {
        RealmManager.open()
        var listnot = RealmManager.loadNotificationsByUser(StradaApp.instance!!.getUser().email)
        if (!listnot.isNullOrEmpty())
            for (not in listnot) {
                val timeOff9 = Calendar.getInstance()
                var fmt = SimpleDateFormat("dd MMMM yyyy")
                timeOff9.time = removeTime(fmt.parse(not.date))
                var numberOfDays = Calendar.getInstance()
                numberOfDays.time = removeTime(Date())
                var Difference_In_Days = (timeOff9.time.time - numberOfDays.time.time) / (1000 * 3600 * 24)
                if ((Difference_In_Days) in 0..not.currentJourNotfication)
                    if (not.checkEd){
                        var joursOrJour= if(Difference_In_Days >1) context.getString(R.string.jours) else context.getString(R.string.jour)
                        setDocumentNotification(context.getString(R.string.attention_le_document) +" "+ not.title +" "+context.getString(R.string.expirera_dans)+" "+(Difference_In_Days)+" "+ joursOrJour, 0, not.id)
                    }
            }

        var lastDownLoadCard = RealmManager.loadCardDownload()
        if (lastDownLoadCard != null && !lastDownLoadCard.LastCardDownload.isNullOrEmpty()) {
            var c: Calendar = Calendar.getInstance()
            c.time = removeTime(Date(lastDownLoadCard.LastCardDownload))
            //c.add(Calendar.DAY_OF_MONTH,28)
            var numberOfDays = Calendar.getInstance()
            numberOfDays.time = removeTime(Date())
            var Difference_In_Days = (numberOfDays.time.time - c.time.time) / (1000 * 3600 * 24)
            if ((28-Difference_In_Days) in 0..SharedPreferencesUtils.delaisAvertisement!!.toInt()) {
                if (lastDownLoadCard.notificationChecked)
                {
                    setCardNotification(context.getString(R.string.attention_il_ne_vous_reste_que)+" "+(28 - Difference_In_Days)+" "+context.getString(R.string.jours_pour_decharger_votre_carte), 1, lastDownLoadCard.LastCardDownload)
                }
            }
        }

        var CardIdentification = RealmManager.loadAllCardIdentification()
        if (CardIdentification != null && !CardIdentification.cardExpiryDate.isNullOrEmpty()) {
            var c: Calendar = Calendar.getInstance()
            c.time = removeTime(Date(CardIdentification.cardExpiryDate))
            var numberOfDays = Calendar.getInstance()
            numberOfDays.time = removeTime(Date())
            var Difference_In_Days = (c.time.time - numberOfDays.time.time) / (1000 * 3600 * 24)
            if (Difference_In_Days in 0..SharedPreferencesUtils.delaisAvertisementCarte!!.toInt()) {
                if (CardIdentification.notificationChecked)
                    setCardNotification(context.getString(R.string.attention_votre_carte_conducteur_expirera_dans)+" "+(Difference_In_Days)+" "+context.getString(R.string.jours), 2, CardIdentification.cardExpiryDate.toString())
            }

        }

    }

    fun removeTime(date : Date) : Date
    {   var cal = Calendar.getInstance()
        cal.setTime(date)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.getTime()
    }

    private var currentNotificationID = 5
    var NOTIFICATION_CHANNEL_ID = "10001"

    private fun setDocumentNotification(notificationText: String, type: Int, id: String) {

        // Do something. For example, fetch fresh data from backend to create a rich notification?

        val builder = NotificationCompat.Builder(this.context, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle("Strada Notification")
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this.context, R.color.colorPrimaryBlue))
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_jaune)

        val mainIntent = Intent(this.context, NotificationsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this.context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        val notification = builder!!.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        currentNotificationID++
        var notificationId = currentNotificationID
        if (notificationId == Integer.MAX_VALUE - 1)
            notificationId = 0
        with(NotificationManagerCompat.from(this.context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, notification)
        }
        // builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(this))

        val answerIntent = Intent(this.context, NotificationsActivity::class.java)
        answerIntent.putExtra("type", type)
        answerIntent.putExtra("id", id)
        answerIntent.action = "Pause"
        answerIntent.putExtra("notificationId",notificationId)
        val pendingIntentNo = PendingIntent.getActivity(this.context, 1, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        var color = ContextCompat.getColor(this.context, R.color.colorPrimaryBlue);
        builder.addAction(R.drawable.ic_not_off, HtmlCompat.fromHtml("<font color=" + color + ">" + "Ne plus recevoir cette notification" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY) , pendingIntentNo)

        val manager = this.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }

    private fun setCardNotification(notificationText: String, type: Int, id: String) {

        // Do something. For example, fetch fresh data from backend to create a rich notification?

        val builder = NotificationCompat.Builder(this.context, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle("Strada Notification")
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this.context, R.color.colorPrimaryBlue))
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_jaune)

        val mainIntent = Intent(this.context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this.context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        val notification = builder!!.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        currentNotificationID++
        var notificationId = currentNotificationID
        if (notificationId == Integer.MAX_VALUE - 1)
            notificationId = 0
        with(NotificationManagerCompat.from(this.context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, notification)
        }
        // builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(this))

        val answerIntent = Intent(this.context, MainActivity::class.java)
        answerIntent.putExtra("type", type)
        answerIntent.putExtra("id", id)
        answerIntent.action = "Pause"
        answerIntent.putExtra("notificationId",notificationId)
        val pendingIntentNo = PendingIntent.getActivity(this.context, 1, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        var color = ContextCompat.getColor(this.context, R.color.colorPrimaryBlue);
        builder.addAction(R.drawable.ic_not_off, HtmlCompat.fromHtml("<font color=" + color + ">" + "ne plus recevoir cette notification" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY) , pendingIntentNo)

        val manager = this.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }
}