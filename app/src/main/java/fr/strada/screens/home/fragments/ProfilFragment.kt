package fr.strada.screens.home.fragments


import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import fr.strada.R
import fr.strada.StradaApp
import fr.strada.models.User
import fr.strada.screens.auth.AuthentificationActivity
import fr.strada.screens.home.MainActivity
import fr.strada.utils.LocaleHelper
import fr.strada.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_logout.*
import kotlinx.android.synthetic.main.fragment_profil.*
import kotlinx.android.synthetic.main.fragment_profil_old_version.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import java.util.*


class ProfilFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    override fun onDateSet(datePicker: DatePicker, i: Int, i1: Int, i2: Int) {
        val datepicked =  i2.toString() + "/" + (i1 + 1).toString() + "/" + i
        inputDate.text = datepicked

    }
    private fun validateEmail(): String? {
        val emailText = inputEmail.text.toString()

        if (TextUtils.isEmpty(emailText)) {
            inputEmail.error = getString(R.string.input_error_required)
            return null
        } else if (!emailText.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
            inputEmail.error = getString(R.string.input_error_email)
            return null
        }

        return emailText
    }
    private fun validateInput(input : EditText): String? {

        if (TextUtils.isEmpty(input.text)) {
            input.error = getString(R.string.input_error_required)
            return null
        } else if (input.length()<5) {
            input.error = getString(R.string.input_error)
            return null
        }

        return input.text.toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootview = inflater.inflate(R.layout.fragment_profil, container, false)
            
        return rootview
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).txttitle.text = resources.getString(R.string.profil)

        (activity as MainActivity).rlTitle.visibility = View.VISIBLE
        (activity as MainActivity).dropdown.visibility = View.INVISIBLE
        //(activity as MainActivity)?.toolbar.minimumHeight = 85
        (activity as MainActivity).txttitle.visibility = View.VISIBLE
        (activity as MainActivity).imgLogo.visibility = View.GONE
        (activity as MainActivity).txtdateJour.visibility = View.GONE
        (activity as MainActivity).rlIcon.visibility = View.GONE
        (activity as MainActivity).rlIcon.btnIconToolbar.setImageResource(R.drawable.ic_grade)
        (activity as MainActivity).floatingActionButtonSettings.visibility = View.INVISIBLE

        var user =  StradaApp.instance!!.getUser()
        // inputEmail.setText(user.email)
        // inputDate.text = user.date
        // inputPhone.setText(user.phone)
        // txtUserName.text = user.name
        txtCardNumber.text= user.cardNumber
        txtLastName.text =  user.name
        /*
        inputDate.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(
                activity!!,
                this,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            )
            dpd.datePicker.maxDate = System.currentTimeMillis() - 568025136000L
            dpd.show()
        }
        */
        btnDeconnecter.setOnClickListener {
            val datePickerView = Dialog(activity!!)
            datePickerView.requestWindowFeature(Window.FEATURE_NO_TITLE)
            datePickerView.setCancelable(false)
            datePickerView.setContentView(R.layout.dialog_logout)
            datePickerView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


            datePickerView.btnDone.setOnClickListener {
                SharedPreferencesUtils.isLoggedIn = false
                var i = Intent(activity, AuthentificationActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
               // RealmManager.open()
               // RealmManager.clear()
               // RealmManager.clear()
               // SharedPreferencesUtils.clear()
                startActivity(i)
                activity!!.finish()
                datePickerView.dismiss()

            }

            datePickerView.btnCancel.setOnClickListener {
                datePickerView.dismiss()
            }

            datePickerView.show()

        }

        /*
        btnSave.setOnClickListener {
            var email = validateEmail()
            var phone = validateInput(inputPhone)
            if (email!=null && phone!=null && !TextUtils.isEmpty(inputDate.text)){
                    StradaApp.instance!!.saveUser(User(email,phone,inputDate.text.toString()))
                    Toast.makeText(activity!!,"Données sauvegardées",Toast.LENGTH_SHORT).show()
            }
        }


        inputDate.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                btnSave.isEnabled = true
                btnSave.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn)
            }
        })
        inputEmail.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                btnSave.isEnabled = true
                btnSave.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn)
            }
        })
        inputPhone.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                btnSave.isEnabled = true
                btnSave.background = ContextCompat.getDrawable(activity!!,R.drawable.bg_btn)
            }
        })
        */



    }
    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
    }
}// Required empty public constructor