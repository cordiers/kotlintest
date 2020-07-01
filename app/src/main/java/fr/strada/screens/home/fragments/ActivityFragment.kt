package fr.strada.screens.home.fragments


import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.shawnlin.numberpicker.NumberPicker
import fr.strada.R
import fr.strada.screens.home.MainActivity
import fr.strada.utils.LocaleHelper
import fr.strada.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_hebdo.*
import kotlinx.android.synthetic.main.fragment_activites.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat
import java.util.*


class ActivityFragment : Fragment()  {

    private lateinit var mContext: Context
    private  var weekday : String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootview = inflater.inflate(R.layout.fragment_activites, container, false)
        return rootview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).floatingActionButtonSettings.visibility = View.INVISIBLE

        //(activity as MainActivity)?.toolbar.minimumHeight = 85
        (activity as MainActivity).rlTitle.visibility = View.VISIBLE
        (activity as MainActivity).imgLogo.visibility = View.GONE
        (activity as MainActivity).floatingActionButtonSettings.visibility = View.INVISIBLE
        (activity as MainActivity).rlIcon.visibility = View.VISIBLE
        (activity as MainActivity).btnIconToolbar.setImageResource(R.drawable.ic_calender)
        (activity as MainActivity).txtdateJour.visibility = View.VISIBLE
        (activity as MainActivity).dropdown.visibility = View.VISIBLE

        hebdomadaire_horizental_view.visibility=View.INVISIBLE
        mensuel_horizental_view.visibility=View.VISIBLE

        mensuel_horizental_view.setBackgroundColor(Color.parseColor("#FFFFFF"))

        tv_hebdomadaire.setTextColor(Color.parseColor("#7F889A"))
        tv_mensuel.setTextColor(Color.parseColor("#FFFFFF"))

        changeFragment(MensuelCalenderFragment())


        btn_Hebdomadaire_Calender.setOnClickListener {

            val datePickerView = Dialog(activity!!)
            datePickerView.requestWindowFeature(Window.FEATURE_NO_TITLE)
            datePickerView.setCancelable(false)
            datePickerView.setContentView(R.layout.dialog_hebdo)
            datePickerView.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            var picker =  datePickerView.findViewById(R.id.hebdo_picker) as NumberPicker
            val data = Array<String>(52) { "it = $it" }
            val DayStart = Array<String>(52) { "it = $it" }


            getCurrentWeek((activity as MainActivity).txttitle.text.substring((activity as MainActivity).txttitle.text.length-4).toInt())

            for (i in 0..51) {
                var NextWeek = getNextWeek()
                data[i] = resources.getString(R.string.semaine)+" "+ i + " :"+ NextWeek[0] + " - "+ NextWeek[6]
                DayStart[i] = NextWeek[0].toString()
            }


            var s = (activity as MainActivity).txttitle.text.substring(0,((activity as MainActivity).txttitle.text.length-5))

            var c : Calendar = Calendar.getInstance()
            c.set(Calendar.YEAR,(activity as MainActivity).txttitle.text.substring((activity as MainActivity).txttitle.text.length-4).toInt())
            c.set(Calendar.MONTH,getMonthNumber(s))
            c.get(Calendar.WEEK_OF_YEAR)


            picker.minValue = 1
            picker.maxValue = data.size
            picker.displayedValues = data
            picker.value =  c.get(Calendar.WEEK_OF_YEAR)

            datePickerView.btnDone.setOnClickListener {
                weekday = DayStart[picker.value-1]
                datePickerView.dismiss()
                selectHebdoUI()
                changeFragment(HebdomadaireCalenderFragment())
            }

            datePickerView.btnCancel.setOnClickListener {
                datePickerView.dismiss()
            }

            datePickerView.show()

        }


        btn_Mensuel_Calender.setOnClickListener {
           selectMensuelUI()
           // weekday = ""
            changeFragment(MensuelCalenderFragment())
        }





    }

       lateinit var calendar : Calendar


    fun getCurrentWeek(year : Int) : Array<String?> {
        this.calendar = Calendar.getInstance()
        this.calendar.set(Calendar.YEAR,year)
        this.calendar.set(Calendar.WEEK_OF_YEAR,0)
        this.calendar.set(Calendar.MONTH,0)
        if (SharedPreferencesUtils.firstDayOfMonth==1){
            this.calendar.set(Calendar.DAY_OF_MONTH, Calendar.MONDAY);
            this.calendar.firstDayOfWeek = Calendar.MONDAY
            this.calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }else{
            this.calendar.set(Calendar.DAY_OF_MONTH, Calendar.SUNDAY);
            this.calendar.firstDayOfWeek = Calendar.SUNDAY
            this.calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }

        return getNextWeek()
    }
    fun getNextWeek(): Array<String?> {
        // just for test
        var locale:Locale? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0)
        } else {
            locale = Resources.getSystem().getConfiguration().locale
        }
        /////
        var format =  SimpleDateFormat("dd MMM yyyy",locale)
        var days =  arrayOfNulls<String>(7)

        for (i in days.indices) {
            days[i] = format.format(this.calendar.time)
            this.calendar.add(Calendar.DATE, 1)
        }
        return days
    }
    fun getPreviousWeek() : Array<String?>{
        this.calendar.add(Calendar.DATE, -14)
        return getNextWeek()
    }



    fun selectMensuelUI(){
        hebdomadaire_horizental_view.visibility=View.INVISIBLE
        mensuel_horizental_view.visibility=View.VISIBLE

        mensuel_horizental_view.setBackgroundColor(Color.parseColor("#FFFFFF"))

        tv_hebdomadaire.setTextColor(Color.parseColor("#7F889A"))
        tv_mensuel.setTextColor(Color.parseColor("#FFFFFF"))
    }
    fun selectHebdoUI(){

        hebdomadaire_horizental_view.setBackgroundColor(Color.parseColor("#FFFFFF"))
        mensuel_horizental_view.visibility=View.INVISIBLE
        hebdomadaire_horizental_view.visibility=View.VISIBLE

        tv_hebdomadaire.setTextColor(Color.parseColor("#FFFFFF"))
        tv_mensuel.setTextColor(Color.parseColor("#7F889A"))

    }






    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
        mContext = context
    }

    //------------------------------ fun changeFragment() ---------------------------------------------------------
    fun changeFragment(f: Fragment ) {
        val  fragmentManager : FragmentManager? = fragmentManager
        val  fragmentTransaction : FragmentTransaction = fragmentManager!!.beginTransaction()
        var fragment   = f
        fragmentTransaction.remove(f)

          if (weekday.isNotEmpty()){
            fragment.arguments
            var args =  Bundle()
            var cal = Calendar.getInstance()
            //just for test
            var locale:Locale? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                  locale = Resources.getSystem().getConfiguration().getLocales().get(0)
            } else {
                  locale = Resources.getSystem().getConfiguration().locale
            }
            ///////////////
            var format =  SimpleDateFormat("dd MMM yyyy",locale)

            cal.time = Date(format.parse(weekday).time)
            args.putString("year", cal.get(Calendar.YEAR).toString())
            args.putString("day", cal.get(Calendar.DATE).toString())
            args.putString("month", cal.get(Calendar.MONTH).toString())
            args.putString("week", cal.get(Calendar.WEEK_OF_YEAR).toString())
            f.arguments = args
          }
         if (arguments != null && arguments!!.getString("fromMain") != null && arguments!!.getString("fromMain")!!.isNotEmpty()){
            var args =  Bundle()
            args.putString("year", arguments!!.getString("year"))
            args.putString("month", arguments!!.getString("month"))
            f.arguments = args
             arguments!!.putString("fromMain",null)

         }

            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.replace(R.id.container, fragment)
            fragmentTransaction.detach(fragment)
            fragmentTransaction.attach(fragment)
            fragmentTransaction.commit()


    }


    fun getMonthNumber(monthName:String) : Int{
        when (monthName) {
            resources.getString(R.string.janvier) -> {
                return 0
            }
            resources.getString(R.string.fevrier) -> {
                return 1
            }
            resources.getString(R.string.mars)  -> {
                return 2
            }
            resources.getString(R.string.avril) -> {
                return 3
            }
            resources.getString(R.string.mai) -> {
                return 4
            }
            resources.getString(R.string.septembre) -> {
                return 5
            }
            resources.getString(R.string.juillet)  -> {
                return 6
            }
            resources.getString(R.string.aout) -> {
                return 7
            }
            resources.getString(R.string.septembre) -> {
                return 8
            }
            resources.getString(R.string.octobre) -> {
                return 9
            }
            resources.getString(R.string.novembre)-> {
                return 10
            }
            resources.getString(R.string.decembre) -> {
                return 11
            }
        }
        return 0
    }


}// Required empty public constructor