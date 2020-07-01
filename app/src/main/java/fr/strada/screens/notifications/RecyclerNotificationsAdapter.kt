package fr.strada.screens.notifications

import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import fr.strada.R
import fr.strada.models.Notifications
import fr.strada.utils.RealmManager
import fr.strada.utils.Utils.getFilterList
import kotlinx.android.synthetic.main.rv_document_row.view.card
import kotlinx.android.synthetic.main.rv_document_row.view.right_view
import kotlinx.android.synthetic.main.rv_notifcation_row.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class RecyclerNotificationsAdapter(removeListener : MyAdapterListener,var emailUser:String) : RecyclerView.Adapter<RecyclerNotificationsAdapter.CustomViewHolder>(){


    var NotificationsArray   : ArrayList<Notifications>   = getFilterList(RealmManager.loadNotificationsByUser(emailUser))
    var removeListener : MyAdapterListener = removeListener

    override fun getItemCount() = NotificationsArray.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.rv_notifcation_row, parent, false)
        return CustomViewHolder(view)

    }

    interface MyAdapterListener {
        fun onContainerClick(size : Int)
    }


    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.notName.text = NotificationsArray[position].title
        val timeOff9 = Calendar.getInstance()
        var fmt = SimpleDateFormat("dd MMMM yyyy",Locale.FRENCH)
        timeOff9.time = removeTime(fmt.parse(NotificationsArray[position].date))
        var numberOfDays = Calendar.getInstance()
        numberOfDays.time = removeTime(Date())
        var Difference_In_Days =
            (timeOff9.time.time - numberOfDays.time.time) / (1000 * 3600 * 24)
        holder.notDesc.text = holder.right_view.context.getString(R.string.votre_document_arrive_a_echeance_dans)+" "+ (Difference_In_Days) +" "+holder.right_view.context.getString(R.string.jours)
        // process set date woth current language
        // get default luange of device
        var locale:Locale? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0)
        } else
        {   locale = Resources.getSystem().getConfiguration().locale
        }
        var fmmk = SimpleDateFormat("dd MMMM yyyy",locale)
        ///////////////////////////////
        holder.notDate.text = fmmk.format(fmt.parse(NotificationsArray[position].date))
        ////////////
        var cal = Calendar.getInstance()
        var calToday = Calendar.getInstance()
        calToday.time = Date()
        cal.time = getDate(NotificationsArray[position].date)
        holder.right_view.setOnClickListener {
            Toast.makeText(holder.right_view.context, holder.right_view.context.getString(R.string.notification_supprimee),Toast.LENGTH_LONG).show()
            removeItem(holder)
        }
    }

    private fun getDate(dtStart : String) : Date {
        var format = SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)
        try {
            return format.parse(dtStart)
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.d("Current Date Time 2: " , e.message)
        }
        return Date()
    }



    fun removeItem (viewHolder : RecyclerView.ViewHolder){
        RealmManager.UpdateDocField(NotificationsArray[viewHolder.adapterPosition].id,false)
        RealmManager.deleteNotification(NotificationsArray[viewHolder.adapterPosition].id)
        notifyItemRemoved(viewHolder.adapterPosition)
        notifyDataSetChanged()
        NotificationsArray  =  getFilterList(RealmManager.loadNotificationsByUser(emailUser))
        removeListener.onContainerClick(NotificationsArray.size)
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


 class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    //     val nb_percent : TextView = itemView.findViewById(R.id.nb_percent)
    val  right_view : ImageView = itemView.right_view
    val  notName : TextView = itemView.NotName
    val  notDate : TextView = itemView.notDate
    val  notDesc : TextView = itemView.notDesc
    val  btnDetails : CardView = itemView.card
 }

}