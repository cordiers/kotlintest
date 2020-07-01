package fr.strada.screens.home.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import fr.strada.R
import fr.strada.models.LectureHistory
import io.realm.RealmResults
import kotlinx.android.synthetic.main.rv_lecture_row.view.*
import java.io.File
import java.util.*
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials
import com.auth0.android.result.Delegation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.JsonObject
import fr.strada.BuildConfig
import fr.strada.StradaApp
import fr.strada.models.HistoryReponceSendFile
import fr.strada.network.Webservices
import fr.strada.utils.*
import fr.strada.utils.SharedPreferencesUtils.idToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger


class RecyclerLectureAdapter(var context: Activity,var webservices: Webservices) : RecyclerView.Adapter<RecyclerLectureAdapter.CustomViewHolder>(){

    var emailUser=if(SharedPreferencesUtils.isLoggedIn) StradaApp.instance!!.getUser().email else ""

    var documentArray   : RealmResults<LectureHistory> = RealmManager.loadHistoryByUser(emailUser)
    private var auth0: Auth0 = Auth0(context.getString(R.string.com_auth0_client_id),context.getString(R.string.com_auth0_domain))
    private var authenticationAPIClient = AuthenticationAPIClient(auth0)

    var loader : Loader = Loader.getInstance()

    var databaseRef:DatabaseReference = FirebaseDatabase.getInstance().reference.child("HistoryReponceSendFiles")

    override fun getItemCount() = documentArray.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        auth0.isOIDCConformant= true
        authenticationAPIClient = AuthenticationAPIClient(auth0)
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.rv_lecture_row, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        holder.txtCardNumber.text = documentArray[position].cardNumber

        var c : Calendar = Calendar.getInstance()
        c.time = Date(documentArray[position].time)
        holder.txtTime.text =  c.get(Calendar.DAY_OF_MONTH).toString() + " " + getMonthName(c.get(Calendar.MONTH),holder.txtCardNumber.context) + " " + c.get(Calendar.YEAR)

        holder.right_view.setOnClickListener {
            removeItem(holder)
        }
        holder.btnExport.setOnClickListener {

              try {
                  //////////////////////
                  loader.show(context)
                  if(holder.txtTime.context.getString(R.string.isVersionTest).equals("true")) // version test
                  {
                      var path = FileProvider.getUriForFile(
                          it.context,
                          BuildConfig.APPLICATION_ID + ".file",
                          File(documentArray[position].file)
                      )
                      // ici si l'extention de fichier est changer donc il faut changer l'extention dans le file provider et il faut changer le lien dans la base de donne
                      var actualFile = File(documentArray[position].file) // log contenue file
                      ////
                      if (actualFile.extension != SharedPreferencesUtils.fileType) // ici il faut rename le fichier
                      {
                          var newFile = File(
                              actualFile.path.replace(
                                  actualFile.extension,
                                  SharedPreferencesUtils.fileType!!
                              )
                          )
                          var renamed = actualFile.renameTo(newFile)
                          if (renamed) {
                              RealmManager.updateLectureHistory(
                                  documentArray[position].time,
                                  documentArray[position].file.replace(
                                      actualFile.extension,
                                      SharedPreferencesUtils.fileType!!
                                  )
                              )
                              path = FileProvider.getUriForFile(
                                  it.context,
                                  BuildConfig.APPLICATION_ID + ".file",
                                  File(documentArray[position].file)
                              )
                          }
                      }

                      ////
                      var strFile= ToHexString(actualFile.inputStream().readBytes())
                      var hashStrFile = ToHexString(Hash256.SHA256.checksum(actualFile))
                      Log.d("ServiceGenerator",hashStrFile)
                      var items= arrayListOf<String>()
                      var itemsHash= arrayListOf<String>()
                      for (i in 0..(strFile.length-2))
                      {
                          if(i % 2 == 0)
                          {
                              items.add(strFile.substring(i,i+2))
                          }
                      }
                      for (j in 0..(hashStrFile.length-2))
                      {
                          if(j % 2 == 0)
                          {
                              itemsHash.add(hashStrFile.substring(j,j+2))
                          }
                      }
                      strFile = TextUtils.join("-",items)
                      hashStrFile = TextUtils.join("-",itemsHash)

                      var cardNumber= documentArray[position].cardNumber
                      var nameFile=actualFile.name.toString().replace(actualFile.extension,SharedPreferencesUtils.fileType.toString())
                      var manager= CredentialsManager(authenticationAPIClient, SharedPreferencesStorage(context))

                      manager.getCredentials(object : BaseCallback<Credentials, CredentialsManagerException>{
                          override fun onSuccess(payload: Credentials?)
                          {
                              Log.i("onAuthentication",payload!!.expiresAt.toString())
                              Log.i("onAuthentication",payload!!.idToken)
                              val jsonObject = JsonObject()
                              //jsonObject.addProperty("driverCardNumber", cardNumber)
                              //jsonObject.addProperty("driverData", strFile)
                              //jsonObject.addProperty("dataName", nameFile)
                              jsonObject.addProperty("checksum", hashStrFile)
                              jsonObject.addProperty("data", strFile)
                              // jsonObject.addProperty("driverId", cardNumber)
                              jsonObject.addProperty("fileName", nameFile)
                              //jsonObject.addProperty("receiptDate", "2020-05-23T07:17:41.119Z")
                              webservices.fileSending(jsonObject,"Bearer "+payload!!.idToken).enqueue(object : Callback<ResponseBody> {

                                  override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                                      loader.dismiss()
                                      var code=response!!.code()
                                      if(code==200)
                                      {
                                           Toast.makeText(context,context.getString(R.string.le_fichier_est_envoye_avec_succes),Toast.LENGTH_LONG).show()
                                           databaseRef.push().setValue(HistoryReponceSendFile(context.getString(R.string.le_fichier_est_envoye_avec_succes)))

                                      }else if(code==428)
                                      {
                                          Toast.makeText(context,context.getString(R.string.Precondition_Required_no_associated_driver),Toast.LENGTH_LONG).show()
                                          databaseRef.push().setValue(HistoryReponceSendFile(context.getString(R.string.Precondition_Required_no_associated_driver)))

                                      }else
                                      {
                                          databaseRef.push().setValue(HistoryReponceSendFile("Problem inconnue"))
                                      }

                                  }

                                  override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                                      loader.dismiss()
                                      Toast.makeText(context,t.toString(),Toast.LENGTH_LONG).show()
                                      databaseRef.push().setValue(HistoryReponceSendFile(t.toString()))
                                  }
                              })
                          }

                          override fun onFailure(error: CredentialsManagerException?)
                          {
                              Toast.makeText(context,error.toString(),Toast.LENGTH_LONG).show()
                              loader.dismiss()
                              databaseRef.push().setValue(HistoryReponceSendFile(error!!.message!!))
                          }

                      })

                      //val i = Intent(Intent.ACTION_SEND)
                      //i.putExtra(Intent.EXTRA_TEXT, "Strada File")
                      //i.putExtra(Intent.EXTRA_STREAM, path)
                      //i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                      //i.type = "plan/*"
                      //it.context.startActivity(i)



                  }
              }catch (ex:Exception){

              }
        }
    }








    fun removeItem (viewHolder : RecyclerView.ViewHolder){
        RealmManager.open()
        RealmManager.deleteByTimeHistory(documentArray[viewHolder.adapterPosition].time)
        RealmManager.close()
        notifyItemRemoved(viewHolder.adapterPosition)
     }


 class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

       val btnExport : ImageView = itemView.findViewById(R.id.btnExport)
    val  right_view :  ImageView = itemView.btnDelete
     val  txtTime :  TextView = itemView.txtTime
     val  txtCardNumber :  TextView = itemView.txtCardNumber









 }

    fun getMonthName(month:Int,context:Context) : String{
        when (month) {
            0 -> {
                return context.resources.getString(R.string.janvier)
            }
            1 -> {
                return context.resources.getString(R.string.fevrier)
            }
            2 -> {
                return context.resources.getString(R.string.mars)
            }
            3 -> {
                return context.resources.getString(R.string.avril)
            }
            4 -> {
                return context.resources.getString(R.string.mai)
            }
            5 -> {
                return context.resources.getString(R.string.juin)
            }
            6 -> {
                return context.resources.getString(R.string.juillet)
            }
            7 -> {
                return context.resources.getString(R.string.aout)
            }
            8 -> {
                return context.resources.getString(R.string.septembre)
            }
            9 -> {
                return context.resources.getString(R.string.octobre)
            }
            10 -> {
                return context.resources.getString(R.string.novembre)
            }
            11 -> {
                return context.resources.getString(R.string.decembre)
            }
        }
        return ""
    }


    @JvmOverloads
    fun ToHexString(bArr: ByteArray, i: Int = 0, i2: Int = bArr.size): String {
        val sb = StringBuilder()
        for (i3 in 0 until i2) {
            sb.append(Convert.ToHexString(bArr[i + i3]))
            sb.append("")
        }
        return sb.toString().trim { it <= ' ' }
    }

    fun toHexStringHash256(hash: ByteArray): String {
        // Convert byte array into signum representation
        val number = BigInteger(1, hash)

        // Convert message digest into hex value
        val hexString = StringBuilder(number.toString(16))

        // Pad with leading zeros
        while (hexString.length < 32) {
            hexString.insert(0, '0')
        }

        return hexString.toString()
    }


}