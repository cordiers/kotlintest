package fr.strada.screens.notifications

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import fr.strada.R
import fr.strada.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.activity_notifications_settings.*

class NotificationsSettingsActivity : AppCompatActivity() {

    var minValue = 1
    var maxValue = 28
    var maxValueCarte = 200

    var current = SharedPreferencesUtils.delaisAvertisement!!
    var currentCarte = SharedPreferencesUtils.delaisAvertisementCarte!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_settings)
        bntBack.setOnClickListener { finish() }
        loadCarteDelay()
        loadDelay()
    }

    fun loadCarteDelay(){
        displayCarte.setText(currentCarte.toString())
        cdDelaisAvertissementCarte.setOnClickListener {
            if (llDelaisCarte.visibility == View.VISIBLE){
                txtDelaisAvertissementCarte.setImageResource(R.drawable.ic_arrow_down)
                llDelaisCarte.visibility = View.GONE
            }else{
                txtDelaisAvertissementCarte.setImageResource(R.drawable.ic_arrow_up)
                llDelaisCarte.visibility = View.VISIBLE
            }
        }
        incrementCarte.setOnClickListener {
            if (currentCarte<maxValueCarte){
                currentCarte++
                displayCarte.setText(currentCarte.toString())
                SharedPreferencesUtils.delaisAvertisementCarte = currentCarte
            }
        }
        decrementCarte.setOnClickListener {
            if (currentCarte>minValue){
                currentCarte--
                displayCarte.setText(currentCarte.toString())
                SharedPreferencesUtils.delaisAvertisementCarte = currentCarte
            }
        }
    }
    fun loadDelay(){
        display.setText(current.toString())
        cdDelaisAvertissement.setOnClickListener {
            if (llDelais.visibility == View.VISIBLE){
                txtDelaisAvertissement.setImageResource(R.drawable.ic_arrow_down)
                llDelais.visibility = View.GONE
            }else{
                txtDelaisAvertissement.setImageResource(R.drawable.ic_arrow_up)
                llDelais.visibility = View.VISIBLE
            }
        }
        increment.setOnClickListener {
            if (current<maxValue){
                current++
                display.setText(current.toString())
                SharedPreferencesUtils.delaisAvertisement = current
            }
        }
        decrement.setOnClickListener {
            if (current>minValue){
                current--
                display.setText(current.toString())
                SharedPreferencesUtils.delaisAvertisement = current
            }
        }
    }
}
