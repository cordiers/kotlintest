package fr.strada.screens.home.fragments

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
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
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar
import fr.strada.R
import fr.strada.models.Document
import fr.strada.screens.document.AjoutDocument
import fr.strada.utils.SharedPreferencesUtils
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.rv_document_row.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RecyclerDocumentsAdapter(listDoc: List<Document> , removeListener : MyAdapterListener) : RecyclerView.Adapter<RecyclerDocumentsAdapter.CustomViewHolder>(){


    var documentArray   : List<Document>   = listDoc
    var removeListener : MyAdapterListener = removeListener

    override fun getItemCount() = if (documentArray!=null) documentArray.size else 0

    interface MyAdapterListener {
        fun onContainerClick(position : Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.rv_document_row, parent, false)
        return CustomViewHolder(view)

    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
       try{
           holder.docName.text = documentArray[position].docName
        //// ici il faut prendre la luange en consideration
        try {
            var locale:Locale? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                locale = Resources.getSystem().getConfiguration().getLocales().get(0)
            } else
            {
                locale = Resources.getSystem().getConfiguration().locale
            }
            var luangageFormatFrancais = SimpleDateFormat("d MMMM yyyy",Locale.FRENCH)
            var luangageFormat = SimpleDateFormat("d MMMM yyyy",locale)

            holder.docTime.text = luangageFormat.format(luangageFormatFrancais.parse(documentArray[position].docEcheance))
        }catch (ex:Exception)
        {
            Log.i("ex",ex.message)
        }
        ////////
        if (documentArray[position].notification){
            holder.notification.setImageResource(R.drawable.ic_not)
            holder.txtNotification.setText(documentArray[position].currentJourNotfication.toString() + " " +holder.docName.context.resources.getString(R.string.jours_avant))
        }else{
            holder.notification.setImageResource(R.drawable.ic_not_off)
        }
        holder.docName.text = documentArray[position].docName
        holder.btnDetails.setOnClickListener {
            var intent = Intent(holder.itemView.context,AjoutDocument::class.java)
            intent.putExtra("docID",documentArray[position].docId)
            holder.itemView.context.startActivity(intent)
        }

        var cal = Calendar.getInstance()
        var calToday = Calendar.getInstance()
        calToday.time = Date()
        cal.time = getDate(documentArray[position].docEcheance)
        //// process jours restants
        var format = SimpleDateFormat("d MMMM yyyy",Locale.FRENCH)
        var dateEcheance = format.parse(documentArray[position].docEcheance)
        dateEcheance = removeTime(dateEcheance) //remove time
        var currentDate = Date()
        currentDate = removeTime(currentDate) //remove time
        var diff = dateEcheance.getTime() - currentDate.getTime()
        var seconds=diff / 1000
        var minutes= seconds / 60
        var hours = minutes / 60
        var days = hours / 24

        // car le resultat toujour manque un jour
        if(days == 1L || days== 0L || days == -1L) // un jour ou 0 jour ou -1 jour
        {
            holder.nbDaysRestants.text = ""+ days + " " + holder.docName.context.getString(R.string.jour_restant)
        }
        else // plusieurs jours
        {
            holder.nbDaysRestants.text = ""+ days +" "+holder.docName.context.getString(R.string.jours_restants)
        }

        // process colors
        if (days <= 0)
        {   // date passer
            holder.nbDaysRestants.setTextColor(Color.parseColor("#DB0E15"))
            if(days == -1L)
            {
                holder.nbDaysRestants.text = ""+ Math.abs(days) +" " + holder.docName.context.getString(R.string.jour_de_retard)
            }else if(days == 0L)
            {
                holder.nbDaysRestants.text = holder.nbDaysRestants.resources.getString(R.string.expire_aujourd_hui)
            }
            else
            {
                holder.nbDaysRestants.text = ""+ Math.abs(days) +" " + holder.docName.context.getString(R.string.jours_de_retard)
            }
        }else{

            if (documentArray[position].notification) // existe notification
            {
                if(days <= documentArray[position].currentJourNotfication)
                {
                    holder.nbDaysRestants.setTextColor(Color.parseColor("#FF8C00"))
                }else
                {
                    holder.nbDaysRestants.setTextColor(Color.parseColor("#354360"))
                }

            }else{
                holder.nbDaysRestants.setTextColor(Color.parseColor("#354360"))
            }
        }



        ////////
        /*
        if (calToday.get(Calendar.YEAR) >= cal.get(Calendar.YEAR)){
            var diff3 = calToday.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR)
            var p2 = diff3.toFloat()*100/364

            if (p2>0){
                holder.docProgress.text = (100-p2.toInt()).toString() + "%"
                holder.docProgressBar.progress = 100-p2
            }else if (p2 == 0F){
                holder.docProgress.text = "100 %"
                holder.docProgressBar.progress = 100F
            }
        }else{
            holder.docProgress.text = "100 %"
            holder.docProgressBar.progress = 100F
        }
        */




        holder.right_view.setOnClickListener {
            Toast.makeText(holder.right_view.context,holder.right_view.context.resources.getString(R.string.document_supprimee),Toast.LENGTH_LONG).show()
            this.removeListener.onContainerClick(position)
            //removeItem(holder)
        }

       }catch (ex:Exception)
       { Log.i("ex",ex.message)
       }
    }

    fun getDate(dtStart : String) : Date {
        var format = SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)
        try {
            return format.parse(dtStart)
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.d("Current Date Time 2: " , e.message)
        }
        return Date()
    }






 class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

//     val nb_percent : TextView = itemView.findViewById(R.id.nb_percent)
     val  right_view : ImageView = itemView.right_view
     val  docName : TextView = itemView.docName
     val  docTime : TextView = itemView.docDate
     val  notification : ImageView = itemView.docNotif
     val  btnDetails : CardView = itemView.card
     val  nbDaysRestants : TextView = itemView.nbDaysRestants
     val txtNotification : TextView = itemView.txtNotification
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






}