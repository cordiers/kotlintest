package fr.strada.screens.document

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.models.Document
import fr.strada.models.Notifications
import fr.strada.utils.FileUtils
import fr.strada.utils.KeyBoardUtils
import fr.strada.utils.LocaleHelper
import fr.strada.utils.RealmManager
import fr.strada.utils.Utils.getMonthName
import kotlinx.android.synthetic.main.activity_ajout_document.*
import kotlinx.android.synthetic.main.bottom_sheet_dialog_add_document.*
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.toolbar_activity.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class AjoutDocument : AppCompatActivity(), DatePickerDialog.OnDateSetListener {


    private lateinit var sheetBottmDialog: BottomSheetDialog
    private lateinit var sheetBottmView: View
    private val TAKE_PHOTO_REQUEST = 101
    private var mCurrentPhotoPath: String = ""

    var myFile: File? = null
    var myFile1: File? = null
    var myFile2: File? = null

    var originFile: File? = null
    var originFile1: File? = null
    var originFile2: File? = null

    var docID : String = ""
    var currentJourNotfication = 1
    var firstTime :  Boolean =  false

    var docFileUploaded = false

    var fileArrayList = arrayListOf<File?>(null,null,null)



    override fun onStart() {
        super.onStart()
        if (intent.hasExtra("docID") && intent.getStringExtra("docID") != null && !firstTime){
            docID = intent.getStringExtra("docID")!!
            btnAddDocument.visibility = View.GONE
            btnUpdate.visibility = View.VISIBLE
            btnUpdate.isEnabled = false
            btnDelete.visibility = View.VISIBLE
            var result = RealmManager.loadDocs(docID)
            input_name.setText(result.docName)
            input_date.text = result.docEcheance

            switch_notifications.isChecked = result.notification

            myFile = File(result.file)
            myFile1 = File(result.file1)
            myFile2 = File(result.file2)

            originFile = File(result.file)
            originFile1 = File(result.file1)
            originFile2 = File(result.file2)

            if (myFile!=null && myFile!!.path != "") {
                fileArrayList[0] = myFile
                iv_delete_document1.visibility = View.VISIBLE
                document_picture_container.visibility = View.VISIBLE
                iv_document1.setImageURI(Uri.fromFile(myFile))
            }

            if (myFile1!=null && myFile1!!.path != "") {
                fileArrayList[1] = myFile1
                iv_delete_document2.visibility = View.VISIBLE
                document_picture_container2.visibility = View.VISIBLE
                iv_document2.setImageURI(Uri.fromFile(myFile1))
            }
            if (myFile2!=null && myFile2!!.path != "") {
                fileArrayList[2] = myFile2
                iv_delete_document3.visibility = View.VISIBLE
                document_picture_container3.visibility = View.VISIBLE
                iv_document3.setImageURI(Uri.fromFile(myFile2))
            }


            if (myFile?.path!!.endsWith("pdf")){
                iv_document1.setImageResource(R.drawable.ic_pdffile)
                docFileUploaded = true
            }

            input_name.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    btnUpdate.background = ContextCompat.getDrawable(this@AjoutDocument,R.drawable.bg_btn)
                    btnUpdate.isEnabled = true

                }

            })

            input_date.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    btnUpdate.background = ContextCompat.getDrawable(this@AjoutDocument,R.drawable.bg_btn)
                    btnUpdate.isEnabled = true

                }

            })
            currentJourNotfication = result.currentJourNotfication
            if (result.notification)
            txtNotification.visibility = View.VISIBLE

            txtNotification.text = resources.getString(R.string.notification_automatique)+" "+currentJourNotfication+" "+resources.getString(R.string.jours_avant_expiration)

            firstTime = true

        }else firstTime = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajout_document)

        bntBack.setOnClickListener {
            closeKeyBord()
            finish()
        }

        input_date.setOnClickListener {
            KeyBoardUtils.hideKeyboard(this!!)
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(
                this,
                this,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            )


            dpd.show()
        }
        //------------------------------------ bottom_sheet_dialog -------------------------------------------------------------------------------

        sheetBottmView = layoutInflater.inflate(R.layout.bottom_sheet_dialog_add_document, null)
        sheetBottmDialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogThemeTransparent)
        sheetBottmDialog.setContentView(sheetBottmView)


        bt_add_document.setOnClickListener(View.OnClickListener {
            sheetBottmDialog.show()
        })


        sheetBottmDialog.btn_annuler.setOnClickListener { sheetBottmDialog.dismiss() }


        //_____________________________________ bottom_sheet_dialog ___________________________________________________________________
        sheetBottmDialog.bt_camera.setOnClickListener {
            if (!docFileUploaded && (fileArrayList[0] == null ||  fileArrayList[1] == null || fileArrayList[2] == null) )
            validatePermissions(1)
           else Toast.makeText(this,resources.getString(R.string.vous_ne_pouvez_plus_ajouter_de_photos),Toast.LENGTH_SHORT).show()
        }




        sheetBottmDialog.bt_gallerie.setOnClickListener {
            if (!docFileUploaded &&  (fileArrayList[0] == null ||  fileArrayList[1] == null || fileArrayList[2] == null))
            validatePermissions(2)
            else Toast.makeText(this,resources.getString(R.string.vous_ne_pouvez_plus_ajouter_de_photos),Toast.LENGTH_SHORT).show()
        }


        sheetBottmDialog.btn_doc.setOnClickListener {
            if (!docFileUploaded &&  fileArrayList[0] == null &&  fileArrayList[1] == null && fileArrayList[2] == null)
            validatePermissions(3)
            else Toast.makeText(this,resources.getString(R.string.vous_ne_pouvez_plus_ajouter_de_document),Toast.LENGTH_SHORT).show()

        }

        btnAddDocument.setOnClickListener {
            KeyBoardUtils.hideKeyboard(this!!)
            if (input_name.text.replace("\\s".toRegex(), "").isNotEmpty() && input_date.text.isNotEmpty() && (fileArrayList[0] != null ||  fileArrayList[1] != null || fileArrayList[2] != null || docFileUploaded ) ){
                var file = ""
                var file1 = ""
                var file2 = ""

                if (docFileUploaded){
                    try {
                        val selectedVideoFile : File = myFile!!  // 2
                        val selectedVideoFileExtension : String = selectedVideoFile.extension  // 3
                        val internalStorageVideoFileName : String = UUID.randomUUID().toString()+"."+(selectedVideoFileExtension)  // 4
                        storeFileInInternalStorage(selectedVideoFile, internalStorageVideoFileName)  // 5
                        file  = application.getFileStreamPath(internalStorageVideoFileName).path

                    }catch (e:Exception){

                    }
                }else{

                    if (fileArrayList[0] != null){
                        val selectedVideoFile : File = myFile!!  // 2
                        val selectedVideoFileExtension : String = selectedVideoFile.extension  // 3
                        val internalStorageVideoFileName : String = UUID.randomUUID().toString()+"."+(selectedVideoFileExtension)  // 4
                        storeFileInInternalStorage(selectedVideoFile, internalStorageVideoFileName)  // 5
                        file  = application.getFileStreamPath(internalStorageVideoFileName).path
                    }
                    if (fileArrayList[1] != null){
                        val selectedVideoFile : File = myFile1!!  // 2
                        val selectedVideoFileExtension : String = selectedVideoFile.extension  // 3
                        val internalStorageVideoFileName : String = UUID.randomUUID().toString()+"."+(selectedVideoFileExtension)  // 4
                        storeFileInInternalStorage(selectedVideoFile, internalStorageVideoFileName)  // 5
                        file1  = application.getFileStreamPath(internalStorageVideoFileName).path
                    }
                    if (fileArrayList[2] != null){
                        val selectedVideoFile : File = myFile2!!  // 2
                        val selectedVideoFileExtension : String = selectedVideoFile.extension  // 3
                        val internalStorageVideoFileName : String = UUID.randomUUID().toString()+"."+(selectedVideoFileExtension)  // 4
                        storeFileInInternalStorage(selectedVideoFile, internalStorageVideoFileName)  // 5
                        file2  = application.getFileStreamPath(internalStorageVideoFileName).path
                    }

                }

                try {

                    var key = UUID.randomUUID().toString()

                    val doc = Document(key,input_name.text.toString(),input_date.text.toString(),switch_notifications.isChecked,currentJourNotfication,file,file1,file2,StradaApp.instance!!.getUser().email)

                    val docNot = Notifications(key,input_name.text.toString(),input_date.text.toString(),switch_notifications.isChecked,currentJourNotfication,StradaApp.instance!!.getUser().email)

                    RealmManager.saveDoc(doc)
                    RealmManager.saveNotification(docNot)

                    Toast.makeText(this, resources.getString(R.string.le_document_a_ete_ajoute_avec_succes), Toast.LENGTH_LONG).show()
                    finish()
                }catch (e:Exception){
                    Toast.makeText(this, resources.getString(R.string.veuillez_selectionner_un_document_depuis_votre_memoire_interne), Toast.LENGTH_LONG).show()

                }

            }else if (input_name.text.replace("\\s".toRegex(), "").isEmpty()){
                Toast.makeText(this, resources.getString(R.string.mettez_votre_nom_de_document), Toast.LENGTH_LONG).show()
            }
            else if (input_name.text.replace("\\s".toRegex(), "").isNotEmpty() && input_date.text.isEmpty()){
                Toast.makeText(this, resources.getString(R.string.mettez_votre_date_d_echeance_du_document), Toast.LENGTH_LONG).show()
            }
            else if (fileArrayList[0]==null) {
                Toast.makeText(this, resources.getString(R.string.aucun_fichier_n_est_ajoute), Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this, resources.getString(R.string.veuillez_remplir_tous_les_champs), Toast.LENGTH_LONG).show()
            }
            // saveFile(this@AjoutDocument,myFile!!.path)

        }
        btnUpdate.setOnClickListener {
            KeyBoardUtils.hideKeyboard(this!!)
            if (input_name.text.replace("\\s".toRegex(), "").isNotEmpty() && input_date.text.isNotEmpty() && (fileArrayList[0] != null ||  fileArrayList[1] != null || fileArrayList[2] != null || docFileUploaded )){


                if ((fileArrayList[0] != originFile!! || fileArrayList[1] != originFile1!! || fileArrayList[2] != originFile2)){

                    var file = ""
                    var file1 = ""
                    var file2 = ""

                    if (docFileUploaded){
                        try {
                            val selectedVideoFile : File = myFile!!  // 2
                            val selectedVideoFileExtension : String = selectedVideoFile.extension  // 3
                            val internalStorageVideoFileName : String = UUID.randomUUID().toString()+"."+(selectedVideoFileExtension)  // 4
                            storeFileInInternalStorage(selectedVideoFile, internalStorageVideoFileName)  // 5
                            file  = application.getFileStreamPath(internalStorageVideoFileName).path
                        }catch (e:Exception){

                        }
                    }else{

                        if (fileArrayList[0] != null){
                            val selectedVideoFile : File = myFile!!  // 2
                            val selectedVideoFileExtension : String = selectedVideoFile.extension  // 3
                            val internalStorageVideoFileName : String = UUID.randomUUID().toString()+"."+(selectedVideoFileExtension)  // 4
                            storeFileInInternalStorage(selectedVideoFile, internalStorageVideoFileName)  // 5
                            file  = application.getFileStreamPath(internalStorageVideoFileName).path
                        }
                        if (fileArrayList[1] != null){
                            val selectedVideoFile : File = myFile1!!  // 2
                            val selectedVideoFileExtension : String = selectedVideoFile.extension  // 3
                            val internalStorageVideoFileName : String = UUID.randomUUID().toString()+"."+(selectedVideoFileExtension)  // 4
                            storeFileInInternalStorage(selectedVideoFile, internalStorageVideoFileName)  // 5
                            file1  = application.getFileStreamPath(internalStorageVideoFileName).path
                        }
                        if (fileArrayList[2] != null){
                            val selectedVideoFile : File = myFile2!!  // 2
                            val selectedVideoFileExtension : String = selectedVideoFile.extension  // 3
                            val internalStorageVideoFileName : String = UUID.randomUUID().toString()+"."+(selectedVideoFileExtension)  // 4
                            storeFileInInternalStorage(selectedVideoFile, internalStorageVideoFileName)  // 5
                            file2  = application.getFileStreamPath(internalStorageVideoFileName).path
                        }
                    }

                    val doc = Document(docID,input_name.text.toString(),input_date.text.toString(),switch_notifications.isChecked,currentJourNotfication,file,file1,file2,StradaApp.instance!!.getUser().email)
                    val docNot = Notifications(docID,input_name.text.toString(),input_date.text.toString(),switch_notifications.isChecked,currentJourNotfication,StradaApp.instance!!.getUser().email)
                    RealmManager.UpdateDoc(doc)
                    RealmManager.UpdateNotification(docNot)

                }else {
                    val doc = Document(docID,input_name.text.toString(),input_date.text.toString(),switch_notifications.isChecked,currentJourNotfication,originFile!!.path,originFile1!!.path,originFile2!!.path,StradaApp.instance!!.getUser().email)
                    val docNot = Notifications(docID,input_name.text.toString(),input_date.text.toString(),switch_notifications.isChecked,currentJourNotfication,StradaApp.instance!!.getUser().email)
                    RealmManager.open()
                    RealmManager.UpdateDoc(doc)
                    RealmManager.UpdateNotification(docNot)
                }


                Toast.makeText(this,resources.getString(R.string.le_document_a_ete_ajoute_avec_succes), Toast.LENGTH_LONG).show()
                finish()

            }else if (input_name.text.replace("\\s".toRegex(), "").isEmpty())
            {
                Toast.makeText(this, resources.getString(R.string.mettez_votre_nom_de_document), Toast.LENGTH_LONG).show()
            }else if (input_name.text.replace("\\s".toRegex(), "").isNotEmpty() && input_date.text.isEmpty())
            {
                Toast.makeText(this, resources.getString(R.string.mettez_votre_date_d_echeance_du_document), Toast.LENGTH_LONG).show()
            }
            else if (fileArrayList[0]==null) {
                Toast.makeText(this, resources.getString(R.string.aucun_fichier_n_est_ajoute), Toast.LENGTH_LONG).show()
            }

            // saveFile(this@AjoutDocument,myFile!!.path)

        }
        btnDelete.setOnClickListener {
            KeyBoardUtils.hideKeyboard(this!!)
            RealmManager.open()
            RealmManager.deleteDocument(docID)
            RealmManager.deleteNotification(docID)
            RealmManager.close()
            finish()
        }


        switch_notifications.setOnCheckedChangeListener { _, b ->
            if (b && firstTime) {
                txtNotification.visibility = View.VISIBLE
            var minValue = 1
            var maxValue = 30
            var current = 1
            val notificationView = Dialog(this)
            notificationView.requestWindowFeature(Window.FEATURE_NO_TITLE)
            notificationView.setCancelable(false)
            notificationView.setContentView(R.layout.dialog_notification)
            notificationView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            notificationView.increment.setOnClickListener {
                if (current<maxValue){
                    current++
                    notificationView.display.setText(current.toString())

                }
            }
            notificationView.decrement.setOnClickListener {
                if (current>minValue){
                    current--
                    notificationView.display.setText(current.toString())
                }
            }
            notificationView.btnDone.setOnClickListener {
                currentJourNotfication = current
                txtNotification.text = resources.getString(R.string.notification_automatique)+" "+current+" "+resources.getString(R.string.jours_avant_expiration)
                notificationView.dismiss()
                if (intent.hasExtra("docID")){
                    btnUpdate.background = ContextCompat.getDrawable(this@AjoutDocument,R.drawable.bg_btn)
                    btnUpdate.isEnabled = true
                }


            }

            notificationView.btnCancel.setOnClickListener {
                switch_notifications.isChecked = false
                txtNotification.visibility = View.GONE
                notificationView.dismiss()
            }
            notificationView.show()
            }
            else if (!b && firstTime){
                txtNotification.visibility = View.GONE
                if (intent.hasExtra("docID")){
                    btnUpdate.background = ContextCompat.getDrawable(this@AjoutDocument,R.drawable.bg_btn)
                    btnUpdate.isEnabled = true
                }
            }
            else {
                txtNotification.visibility = View.GONE
            }
        }


        iv_delete_document1.setOnClickListener {
            if (docFileUploaded) docFileUploaded = false
            myFile = null
            document_picture_container.visibility = View.GONE
            Picasso.get().cancelRequest(iv_document1)
            fileArrayList[0] = null
            btnUpdate.background = ContextCompat.getDrawable(this@AjoutDocument,R.drawable.bg_btn)
            btnUpdate.isEnabled = true

        }

        iv_delete_document2.setOnClickListener {
            myFile1 = null
            document_picture_container2.visibility = View.GONE
            Picasso.get().cancelRequest(iv_document2)
            fileArrayList[1] = null
            btnUpdate.background = ContextCompat.getDrawable(this@AjoutDocument,R.drawable.bg_btn)
            btnUpdate.isEnabled = true

        }

        iv_delete_document3.setOnClickListener {
            myFile2 = null
            document_picture_container3.visibility = View.GONE
            Picasso.get().cancelRequest(iv_document3)
            fileArrayList[2] = null
            btnUpdate.background = ContextCompat.getDrawable(this@AjoutDocument,R.drawable.bg_btn)
            btnUpdate.isEnabled = true


        }


    }

    private fun closeKeyBord(){
        var inputMethodManager: InputMethodManager =this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if(this.getCurrentFocus()!=null)
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()!!.getWindowToken(), 0)
    }

    // ici le compresse
    private fun storeFileInInternalStorage(selectedFile: File, internalStorageFileName: String) {
        try {
            val inputStream = FileInputStream(selectedFile) // 1
            val outputStream = application.openFileOutput(internalStorageFileName, Context.MODE_PRIVATE)  // 2
            val buffer = ByteArray(1024)
            inputStream.use {  // 3
                while (true) {
                    val byeCount = it.read(buffer)  // 4
                    if (byeCount < 0) break
                    outputStream.write(buffer, 0, byeCount)  // 5
                }
                outputStream.close()  // 6
            }

        }catch (ex:Exception)
        {
            Toast.makeText(this,this.resources.getString(R.string.Ilya_un_probleme_dans_ce_fichier_choisir_un_autre_fichier),Toast.LENGTH_LONG).show()
        }
    }

    private fun validatePermissions(action: Int) = Dexter.withActivity(this)
        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .withListener(object : PermissionListener {
            override fun onPermissionRationaleShouldBeShown(
                permission: com.karumi.dexter.listener.PermissionRequest?,
                token: PermissionToken?
            ) {

                token?.continuePermissionRequest()


            }

            override fun onPermissionGranted(
                response: PermissionGrantedResponse?
            ) {

                when (action) {
                    1 -> launchCamera()
                    2 -> GalleryPictureIntent()
                    3 -> Gallery_Pdf_Intent()
                }


            }


            override fun onPermissionDenied(
                response: PermissionDeniedResponse?
            )
            {
                Toast.makeText(baseContext, resources.getString(R.string.une_autorisation_de_stockage_est_requise_pour_prendre_une_photo) ,Toast.LENGTH_LONG ).show()
            }
        })
        .check()





    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        val fileUri = this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(this.packageManager) != null) {
            mCurrentPhotoPath = fileUri!!.toString()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        }
    }




    val REQUEST_CODE = 2

    private fun GalleryPictureIntent()
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE)
    }

    val pdf_REQUEST_CODE = 3

    private fun Gallery_Pdf_Intent()
    {
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        startActivityForResult(Intent.createChooser(intent, "Select pdf"), pdf_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PHOTO_REQUEST) {
            processCapturedPhoto()
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {

            btnUpdate.background = ContextCompat.getDrawable(this@AjoutDocument,R.drawable.bg_btn)
            btnUpdate.isEnabled = true

            val uri = data!!.data
            var isGoogleDrive = isGoogleDriveUri(uri!!)
            /////
            var cR: ContentResolver = this.getContentResolver()
            var mime: MimeTypeMap = MimeTypeMap.getSingleton()
            var type:String? = mime.getExtensionFromMimeType(cR.getType(uri!!))
            ////
            if(type != null) {
                if(type!!.contains("jpg") || type!!.contains("png") || type!!.contains("jpeg") )
                {
                    if (fileArrayList[0] == null) {
                        if(isGoogleDrive==false)
                        {
                            myFile = FileUtils.getFile(this@AjoutDocument, uri)
                        }else
                        {
                            myFile = saveFileIntoExternalStorageByUri(this,uri)
                        }
                        iv_delete_document1.visibility = View.VISIBLE
                        document_picture_container.visibility = View.VISIBLE
                        Picasso.get().load(uri)
                            .placeholder(R.drawable.bg_add_doc)
                            .resize(720, 0)
                            .into(iv_document1)
                        fileArrayList[0] = myFile
                    } else if (fileArrayList[1] == null) {
                        if(isGoogleDrive==false)
                        {
                            myFile1 = FileUtils.getFile(this@AjoutDocument, uri)
                        }else
                        {
                            myFile1 = saveFileIntoExternalStorageByUri(this,uri)
                        }
                        iv_delete_document2.visibility = View.VISIBLE
                        document_picture_container2.visibility = View.VISIBLE
                        Picasso.get().load(uri)
                            .placeholder(R.drawable.bg_add_doc)
                            .resize(720, 0)
                            .into(iv_document2)
                        fileArrayList[1] = myFile1
                    } else if (fileArrayList[2] == null) {
                        if(isGoogleDrive==false)
                        {
                            myFile2 = FileUtils.getFile(this@AjoutDocument, uri)
                        }else
                        {
                            myFile2 = saveFileIntoExternalStorageByUri(this,uri)
                        }
                        iv_delete_document3.visibility = View.VISIBLE
                        document_picture_container3.visibility = View.VISIBLE
                        Picasso.get().load(uri)
                            .placeholder(R.drawable.bg_add_doc)
                            .resize(720, 0)
                            .into(iv_document3)
                        fileArrayList[2] = myFile2
                    }
                }else
                {
                    Toast.makeText(this,resources.getString(R.string.le_format_selectionne_est_incorrect),Toast.LENGTH_LONG).show()
                }
            }else
            {
                Toast.makeText(this,resources.getString(R.string.le_format_selectionne_est_incorrect),Toast.LENGTH_LONG).show()
            }
            sheetBottmDialog.dismiss()

        } else if (resultCode == Activity.RESULT_OK && requestCode == pdf_REQUEST_CODE){ // pdf code

            val uri = data!!.data
            var isGoogleDriveUri = isGoogleDriveUri(uri!!)
            var tType = data!!.type
            /////
            var cR: ContentResolver = this.getContentResolver()
            var mime: MimeTypeMap = MimeTypeMap.getSingleton()
            var type:String? = mime.getExtensionFromMimeType(cR.getType(uri!!))
            if(type == null)
            {
                type=""
            }
            if(tType==null)
            {
                tType=""
            }
            sheetBottmDialog.dismiss()

             if(isGoogleDriveUri==false)
             {
                 if (type!!.contains("pdf") || tType!!.contains("pdf")) // ci pdf sinon non
                 {
                         myFile = FileUtils.getFile(this@AjoutDocument, uri)

                         btnUpdate.background =
                             ContextCompat.getDrawable(this@AjoutDocument, R.drawable.bg_btn)
                         btnUpdate.isEnabled = true

                         docFileUploaded = true

                         iv_delete_document1.visibility = View.VISIBLE
                         document_picture_container.visibility = View.VISIBLE
                         Picasso.get().load(R.drawable.ic_pdffile)
                             .placeholder(R.drawable.ic_pdffile)
                             .resize(720, 0)
                             .into(iv_document1)
                 } else {
                     Toast.makeText(this, resources.getString(R.string.le_format_selectionne_est_incorrect), Toast.LENGTH_LONG).show()
                 }

             }else
             {
                 //Toast.makeText(this, resources.getString(R.string.veuillez_selectionner_un_document_depuis_votre_memoire_interne), Toast.LENGTH_LONG).show()

                 myFile = saveFileIntoExternalStorageByUri(this,uri)
                 if(myFile!!.extension.equals("pdf"))
                 {
                     btnUpdate.background =
                         ContextCompat.getDrawable(this@AjoutDocument, R.drawable.bg_btn)
                     btnUpdate.isEnabled = true

                     docFileUploaded = true

                     iv_delete_document1.visibility = View.VISIBLE
                     document_picture_container.visibility = View.VISIBLE
                     Picasso.get().load(R.drawable.ic_pdffile)
                         .placeholder(R.drawable.ic_pdffile)
                         .resize(720, 0)
                         .into(iv_document1)

                 }else
                 {
                     myFile = null
                     Toast.makeText(this, resources.getString(R.string.le_format_selectionne_est_incorrect), Toast.LENGTH_LONG).show()
                 }



             }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data)
        }


    }

    //------------------- google drive logique ------------------//

    private fun isGoogleDriveUri(uri:Uri):Boolean{
        return "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority())
    }

    @Throws(Exception::class)
    private fun saveFileIntoExternalStorageByUri(context:Context,uri:Uri):File
    {
        var inputStream = context.getContentResolver().openInputStream(uri)
        var originalSize = inputStream!!.available()

        var  bis: BufferedInputStream? = null
        var  bos: BufferedOutputStream? = null
        var  fileName:String = getFileName(context, uri)
        var file:File = makeEmptyFileIntoExternalStorageWithTitle(fileName)
        bis = BufferedInputStream(inputStream)
        bos = BufferedOutputStream(FileOutputStream(file, false))

        val buf = ByteArray(originalSize)
        bis.read(buf)
        do {
            bos.write(buf)
        } while (bis.read(buf) != -1)

        bos.flush()
        bos.close()
        bis.close()

        return file
    }

    private fun getFileName(context:Context,uri:Uri):String{
        var result:String? = null
        if (uri.getScheme().equals("content"))
        {
            var cursor = context.getContentResolver().query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally
            {
                    cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.getPath()
            var cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun makeEmptyFileIntoExternalStorageWithTitle(title:String):File{
        var root:String =  Environment.getExternalStorageDirectory().getAbsolutePath()
        return File(root,title)
    }

    //------------------- google drive logique ------------------//

    private fun processCapturedPhoto() {
        val cursor = this.contentResolver.query(
            Uri.parse(mCurrentPhotoPath),
            Array(1) { MediaStore.Images.ImageColumns.DATA }, null, null, null
        )
        cursor!!.moveToFirst()
        val photoPath = cursor.getString(0)
        cursor.close()
        val file = File(photoPath)
        val uri = Uri.fromFile(file)

        btnUpdate.background = ContextCompat.getDrawable(this@AjoutDocument,R.drawable.bg_btn)
        btnUpdate.isEnabled = true

        if (fileArrayList[0] == null) {
            myFile = FileUtils.getFile(this@AjoutDocument, uri)
            iv_delete_document1.visibility = View.VISIBLE
            document_picture_container.visibility = View.VISIBLE
            Picasso.get().load(uri)
                .placeholder(R.drawable.bg_add_doc)
                .resize(720, 0)
                .into(iv_document1)
            fileArrayList[0] = myFile
        }
        else if (fileArrayList[1] == null){
            myFile1 =  FileUtils.getFile(this@AjoutDocument,uri)
            iv_delete_document2.visibility = View.VISIBLE
            document_picture_container2.visibility = View.VISIBLE
            Picasso.get().load(uri)
                .placeholder(R.drawable.bg_add_doc)
                .resize(720, 0)
                .into(iv_document2)
            fileArrayList[1] = myFile1
        }
        else if (fileArrayList[2] == null){
            myFile2 =  FileUtils.getFile(this@AjoutDocument,uri)
            iv_delete_document3.visibility = View.VISIBLE
            document_picture_container3.visibility = View.VISIBLE
            Picasso.get().load(uri)
                .placeholder(R.drawable.bg_add_doc)
                .resize(720, 0)
                .into(iv_document3)
            fileArrayList[2] = myFile2
        }
        sheetBottmDialog.dismiss()

    }


    override fun onDateSet(datePicker: DatePicker, i: Int, i1: Int, i2: Int) {
        val datepicked =  i2.toString() + " " + getMonthName(i1).toString() + " " + i
        input_date.text = datepicked
    }

}
