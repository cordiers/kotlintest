package fr.strada.screens.home.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.models.Comments
import fr.strada.utils.RealmManager
import io.realm.RealmResults
import kotlinx.android.synthetic.main.dialog_logout.btnCancel
import kotlinx.android.synthetic.main.dialog_logout.btnDone
import kotlinx.android.synthetic.main.dialog_update_comment.*
import kotlinx.android.synthetic.main.fragment_jour_calender.*
import kotlinx.android.synthetic.main.item_comment.view.*

class RecyclerCommentsAdapter(listcomment: RealmResults<Comments>) : RecyclerView.Adapter<RecyclerCommentsAdapter.ViewHolder>() {

    var commentArray :  RealmResults<Comments> = listcomment

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = if (commentArray!=null) commentArray.size else 0


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtComment.text = commentArray[position].comment
        holder.textUsername.text = commentArray[position].date + " - " + StradaApp.instance!!.getUser().name
        holder.btnDelete.setOnClickListener {
            Toast.makeText(holder.btnDelete.context, holder.btnDelete.context.getString(R.string.commentaire_supprime), Toast.LENGTH_LONG).show()
            removeItem(holder)
        }
        holder.root.setOnClickListener {
            val datePickerView = Dialog(holder.root.context!!)
            datePickerView.requestWindowFeature(Window.FEATURE_NO_TITLE)
            datePickerView.setCancelable(false)
            datePickerView.setContentView(R.layout.dialog_update_comment)
            datePickerView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


            datePickerView.btnDone.setOnClickListener {
                if (!datePickerView.InputComment.text.isNullOrEmpty()){
                    RealmManager.open()
                    RealmManager.UpdateComments(Comments(commentArray[position].commID,commentArray[position].date,datePickerView.InputComment.text.toString()))
                    RealmManager.close()
                    datePickerView.dismiss()
                    notifyDataSetChanged()
                }else{
                    datePickerView.InputComment.error = ""
                }

            }

            datePickerView.btnCancel.setOnClickListener {
                datePickerView.dismiss()
            }
            datePickerView.InputComment.setText(commentArray[position].comment)
            datePickerView.show()
        }


    }


    fun removeItem (viewHolder : RecyclerView.ViewHolder){
        RealmManager.open()
        RealmManager.deleteComments(commentArray[viewHolder.adapterPosition].commID)
        RealmManager.close()
        notifyItemRemoved(viewHolder.adapterPosition)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val  btnDelete : ImageView = itemView.btnDelete
        val  root : CardView = itemView.root

        var txtComment = itemView.findViewById<TextView>(R.id.txtComment)!!
        var textUsername = itemView.findViewById<TextView>(R.id.txtUserName)!!


    }
}