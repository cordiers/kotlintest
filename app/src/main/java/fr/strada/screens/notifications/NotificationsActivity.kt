package fr.strada.screens.notifications

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.strada.R
import fr.strada.models.Notifications
import fr.strada.screens.home.BaseActivity
import fr.strada.screens.home.MainActivity

import fr.strada.utils.RealmManager
import fr.strada.utils.Utils
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_notifications.*
import kotlinx.android.synthetic.main.toolbar_notif.*
import android.app.NotificationManager


import android.content.Context
import fr.strada.StradaApp
import fr.strada.utils.SharedPreferencesUtils


class NotificationsActivity : BaseActivity() {

    private lateinit var ListDoc : RealmResults<Notifications>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        RealmManager.open()
    }

    override fun onStart() {
        super.onStart()
        val nMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (intent.action=="Pause"){
            var type = intent.getIntExtra("type",-1)
            when(type){
                0->{
                    RealmManager.UpdateNotification(intent.getStringExtra("id"),false)
                    nMgr.cancel(intent.getIntExtra("notificationId",0))

                }
               /* 1->{
                    RealmManager.UpdateLastDownloadNotification(false)
                    nMgr.cancelAll()

                }
                2->{
                    RealmManager.UpdateCardIdentificationNotification(false)
                    nMgr.cancelAll()

                }*/
                else->{

                }
            }

        }


        Log.d("params","onStart")
        rv_notification.layoutManager = LinearLayoutManager(this@NotificationsActivity, RecyclerView.VERTICAL, false)
        var emailUser=""
        if(SharedPreferencesUtils.isLoggedIn==true){
            emailUser = StradaApp.instance!!.getUser().email
        }
        ListDoc = RealmManager.loadNotificationsByUser(emailUser)
        rv_notification.adapter = RecyclerNotificationsAdapter(object : RecyclerNotificationsAdapter.MyAdapterListener{
            override fun onContainerClick(size: Int) {
                if (size == 0)
                {
                    layout_empty_not.visibility = View.VISIBLE
                    rv_notification.visibility = View.GONE

                }else if (size>0)
                {
                    layout_empty_not.visibility = View.GONE
                    rv_notification.visibility = View.VISIBLE
                }
            }

        },emailUser)
        bntBack.setOnClickListener {
            onBackPressed()
        }
        btnSetting.setOnClickListener { startActivity(Intent(this@NotificationsActivity,NotificationsSettingsActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        if (ListDoc.isNullOrEmpty()){
            layout_empty_not.visibility = View.VISIBLE
            rv_notification.visibility = View.GONE

        }else if (Utils.getFilterList(ListDoc).size>0){
            layout_empty_not.visibility = View.GONE
            rv_notification.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@NotificationsActivity,MainActivity::class.java))
        finish()
    }



}
