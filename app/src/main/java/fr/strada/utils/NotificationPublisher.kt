package fr.strada.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Parcelable
import fr.strada.screens.home.BaseActivity
import fr.strada.utils.NotificationPublisher.notif.NOTIFICATION
import fr.strada.utils.NotificationPublisher.notif.NOTIFICATION_ID


class NotificationPublisher : BroadcastReceiver() {

    object notif{
        var NOTIFICATION_ID = "notification-id"
        var NOTIFICATION = "notification"
    }
    val NOTIFICATION_CHANNEL_ID = "10001"


    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "NOTIFICATION_CHANNEL_NAME",
                importance
            )
            assert(notificationManager != null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val id = intent.getIntExtra(NOTIFICATION_ID, 0)
        assert(notificationManager != null)
        notificationManager.notify(id, notification)
    }
}