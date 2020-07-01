package fr.strada.screens.home.fragments


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.models.Document
import fr.strada.screens.document.AjoutDocument
import fr.strada.screens.home.MainActivity
import fr.strada.utils.KeyBoardUtils
import fr.strada.utils.LocaleHelper
import fr.strada.utils.RealmManager
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_document.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*
import kotlin.collections.ArrayList


class DocumentFragment : Fragment() {

    private lateinit var mContext: Context
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private lateinit var colorDrawableBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable
    private lateinit var ListDoc : RealmResults<Document>
    var switshAlphabetiqueChecked = false
    var switshDateEcheanceChecked = false
    var switshAlphabetiqueCheckedAZ = true

    lateinit var adapter : RecyclerDocumentsAdapter

    override fun onStart() {
        super.onStart()
        if(switshAlphabetiqueChecked==false && switshDateEcheanceChecked==false) // ici pas de filtrage
        {
            rv_documents.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)!!
            ListDoc = RealmManager.loadDocsByUser(StradaApp.instance!!.getUser().email)
            adapter = RecyclerDocumentsAdapter(ListDoc, object :
                RecyclerDocumentsAdapter.MyAdapterListener {
                override fun onContainerClick(position: Int) {
                    removeItem(position)
                }
            }
            )
            rv_documents.adapter = adapter
            if (ListDoc.isNullOrEmpty()){
                layout_empty_document.visibility = View.VISIBLE
            }else{
                layout_empty_document.visibility = View.GONE
            }

        }else if(switshDateEcheanceChecked==true) // filtrage par dat echeance
        {
            rv_documents.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
            ListDoc = RealmManager.loadFilteredDocsByUser("docEcheance",Sort.ASCENDING,StradaApp.instance!!.getUser().email) // before sort
            // for test
            var list:ArrayList<Document> = arrayListOf()
            list.addAll(ListDoc)
            Collections.sort(list)
            //
            adapter = RecyclerDocumentsAdapter(list, object :
                RecyclerDocumentsAdapter.MyAdapterListener {
                override fun onContainerClick(position: Int) {
                    removeItem(position)
                }
            })

            rv_documents.adapter = adapter
            if (ListDoc.isNullOrEmpty()){
                layout_empty_document.visibility = View.VISIBLE
            }else{
                layout_empty_document.visibility = View.GONE
            }

        }else if(switshAlphabetiqueChecked && switshAlphabetiqueCheckedAZ)
        {

            rv_documents.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
            ListDoc = RealmManager.loadFilteredDocsByUser("docName",Sort.ASCENDING,StradaApp.instance!!.getUser().email)
            adapter = RecyclerDocumentsAdapter(ListDoc, object :
                RecyclerDocumentsAdapter.MyAdapterListener {
                override fun onContainerClick(position: Int) {
                    removeItem(position)
                }
            }
            )
            rv_documents.adapter = adapter
            if (ListDoc.isNullOrEmpty()){
                layout_empty_document.visibility = View.VISIBLE
            }else{
                layout_empty_document.visibility = View.GONE
            }

        }else
        {
            rv_documents.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
            ListDoc = RealmManager.loadFilteredDocsByUser("docName",Sort.DESCENDING,StradaApp.instance!!.getUser().email)
            adapter = RecyclerDocumentsAdapter(ListDoc, object :
                RecyclerDocumentsAdapter.MyAdapterListener {
                override fun onContainerClick(position: Int) {
                    removeItem(position)
                }
            }
            )
            rv_documents.adapter = adapter
            if (ListDoc.isNullOrEmpty()){
                layout_empty_document.visibility = View.VISIBLE
            }else{
                layout_empty_document.visibility = View.GONE
            }

        }

        // 3 documents pas de plus
        if(ListDoc.size >= 3)
        {
            floatingActionButton.visibility = View.INVISIBLE
        }else
        {
            floatingActionButton.visibility = View.VISIBLE
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d("params","onResume")
        if (ListDoc.isNullOrEmpty()){
            layout_empty_document.visibility = View.VISIBLE
        }else{
            layout_empty_document.visibility = View.GONE
        }
    }




    fun removeItem (pos:Int){
        RealmManager.deleteNotification(ListDoc[pos].docId)
        RealmManager.deleteDocument(ListDoc[pos].docId)
        adapter.notifyDataSetChanged()
        if (ListDoc.isNullOrEmpty()){
            layout_empty_document.visibility = View.VISIBLE
        }else{
            layout_empty_document.visibility = View.GONE
        }

        // 3 documents pas de plus
        if(ListDoc.size >= 3)
        {
            floatingActionButton.visibility = View.INVISIBLE
        }else
        {
            floatingActionButton.visibility = View.VISIBLE
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootview = inflater.inflate(R.layout.fragment_document, container, false)
        RealmManager.open()

        return rootview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        floatingActionButton.setOnClickListener {
            val i = Intent(mContext, AjoutDocument::class.java)
            startActivity(i)
        }

        colorDrawableBackground = ColorDrawable(Color.parseColor("#FF0303"))
        deleteIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_delete)!!


        (activity as MainActivity).rlTitle.visibility = View.VISIBLE
        (activity as MainActivity).dropdown.visibility = View.INVISIBLE

        (activity as MainActivity).txttitle.text = resources.getString(R.string.document)
        //(activity as MainActivity)?.toolbar.minimumHeight = 85

        (activity as MainActivity).txttitle.visibility = View.VISIBLE
        (activity as MainActivity).imgLogo.visibility = View.GONE
        (activity as MainActivity).rlIcon.visibility = View.INVISIBLE
        (activity as MainActivity).floatingActionButtonSettings.visibility = View.VISIBLE


        //------------------------------------ bottom_sheet_dialog -------------------------------------------------------------------------------
        (activity as MainActivity).floatingActionButtonSettings.setOnClickListener(View.OnClickListener {
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            val dialog = BottomSheetDialog(mContext, R.style.AppBottomSheetDialogTheme)
            var switshDateEcheance = view.findViewById<Switch>(R.id.switshDateEcheance)
            var switshAlphabetique = view.findViewById<Switch>(R.id.switshAlphabetique)
            var btnAZ = view.findViewById<Button>(R.id.btnAZ)
            var btnZA = view.findViewById<Button>(R.id.btnZA)

            switshAlphabetique.isChecked = switshAlphabetiqueChecked
            switshDateEcheance.isChecked = switshDateEcheanceChecked
            if (switshAlphabetique.isChecked){
                btnAZ.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_b)
                btnZA.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_w)
                btnZA.setTextColor(ContextCompat.getColor(activity!!,R.color.colorPrimaryBlue))
                btnAZ.isEnabled = true
                btnZA.isEnabled = true
                if (switshAlphabetiqueCheckedAZ){
                    btnAZ.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_b)
                    btnZA.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_w)
                    btnZA.setTextColor(ContextCompat.getColor(activity!!,R.color.colorPrimaryBlue))
                    btnAZ.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                    switshAlphabetiqueCheckedAZ = true
                }
                else{
                    btnAZ.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_w_l)
                    btnZA.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_b_r)
                    btnZA.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                    btnAZ.setTextColor(ContextCompat.getColor(activity!!,R.color.colorPrimaryBlue))
                    switshAlphabetiqueCheckedAZ = false
                }

            }else{
                btnAZ.isEnabled = false
                btnZA.isEnabled = false
            }
            if (switshDateEcheance.isChecked){
                btnAZ.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_g_r)
                btnZA.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_g_l)
                btnZA.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                btnAZ.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                btnAZ.isEnabled = false
                btnZA.isEnabled = false
            }
            switshDateEcheance.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    switshAlphabetiqueChecked = false
                    switshDateEcheanceChecked = isChecked


                    btnAZ.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_g_r)
                    btnZA.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_g_l)
                    btnZA.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                    btnAZ.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                    btnAZ.isEnabled = false
                    btnZA.isEnabled = false

                    switshAlphabetique.isChecked = false
                    ListDoc = RealmManager.loadFilteredDocsByUser("docEcheance",Sort.ASCENDING,StradaApp.instance!!.getUser().email) // before sort
                    // for test
                    var list:ArrayList<Document> = arrayListOf()
                    list.addAll(ListDoc)
                    Collections.sort(list)
                    //
                    adapter = RecyclerDocumentsAdapter(list, object :
                        RecyclerDocumentsAdapter.MyAdapterListener {
                        override fun onContainerClick(position: Int) {
                            removeItem(position)
                        }

                    }
                    )
                    rv_documents.adapter = adapter
                }else{
                    switshAlphabetiqueChecked = switshAlphabetiqueChecked
                    switshDateEcheanceChecked = isChecked
                    ListDoc = RealmManager.loadDocsByUser(StradaApp.instance!!.getUser().email)
                    adapter = RecyclerDocumentsAdapter(ListDoc, object :
                        RecyclerDocumentsAdapter.MyAdapterListener {
                        override fun onContainerClick(position: Int) {
                            removeItem(position)
                        }

                    }
                    )
                    rv_documents.adapter = adapter
                }


            }

            switshAlphabetique.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {
                    switshAlphabetiqueChecked = isChecked
                    switshDateEcheanceChecked =  false
                    btnAZ.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_b)
                    btnZA.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_w)
                    btnZA.setTextColor(ContextCompat.getColor(activity!!,R.color.colorPrimaryBlue))
                    switshDateEcheance.isChecked = false
                    btnAZ.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                    btnAZ.performClick()
                    btnAZ.isEnabled = true
                    btnZA.isEnabled = true
                } else {
                    btnAZ.isEnabled = false
                    btnZA.isEnabled = false
                    switshAlphabetiqueChecked = isChecked
                    switshDateEcheanceChecked =  switshDateEcheanceChecked
                    btnAZ.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_g_r)
                    btnZA.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_g_l)
                    btnZA.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                    btnAZ.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                    ListDoc = RealmManager.loadDocsByUser(StradaApp.instance!!.getUser().email)
                    adapter = RecyclerDocumentsAdapter(ListDoc, object :
                        RecyclerDocumentsAdapter.MyAdapterListener {
                        override fun onContainerClick(position: Int) {
                            removeItem(position)
                        }

                    }
                    )
                    rv_documents.adapter = adapter

                }
            }
            btnAZ.setOnClickListener {
                btnAZ.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_b)
                btnZA.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_w)
                btnZA.setTextColor(ContextCompat.getColor(activity!!,R.color.colorPrimaryBlue))
                btnAZ.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                switshAlphabetiqueCheckedAZ = true
                ListDoc = RealmManager.loadFilteredDocsByUser("docName",Sort.ASCENDING,StradaApp.instance!!.getUser().email)
                adapter = RecyclerDocumentsAdapter(ListDoc, object :
                    RecyclerDocumentsAdapter.MyAdapterListener {
                    override fun onContainerClick(position: Int) {
                        removeItem(position)
                    }

                }
                )
                rv_documents.adapter = adapter
            }
            btnZA.setOnClickListener {
                btnAZ.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_w_l)
                btnZA.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn_filter_b_r)
                btnZA.setTextColor(ContextCompat.getColor(activity!!,R.color.white))
                btnAZ.setTextColor(ContextCompat.getColor(activity!!,R.color.colorPrimaryBlue))
                switshAlphabetiqueCheckedAZ = false

                ListDoc = RealmManager.loadFilteredDocsByUser("docName",Sort.DESCENDING,StradaApp.instance!!.getUser().email)
                adapter = RecyclerDocumentsAdapter(ListDoc, object :
                    RecyclerDocumentsAdapter.MyAdapterListener {
                    override fun onContainerClick(position: Int) {
                        removeItem(position)
                    }

                }
                )
                rv_documents.adapter = adapter
            }


            dialog.setContentView(view)
            dialog.show()
        })
        // _____________________________________ load list of documents ___________________________________________________________________
        // _____________________________________ load list of documents ___________________________________________________________________
    }




    //----------- fun onAttach() getContext in fragment  ------------------
    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
        mContext = context
    }
    //_______________ fun onAttach() getContext in fragment _________________

}// Required empty public constructor