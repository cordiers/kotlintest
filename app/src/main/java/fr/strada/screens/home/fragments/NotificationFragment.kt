package fr.strada.screens.home.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import fr.strada.R
import fr.strada.screens.auth.AuthentificationActivity
import fr.strada.screens.home.MainActivity
import fr.strada.utils.LocaleHelper
import fr.strada.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notification.view.*
import kotlinx.android.synthetic.main.toolbar.*


class NotificationFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootview = inflater.inflate(R.layout.fragment_notification, container, false)
        rootview.title.setText(tag)
        if (tag.equals(resources.getStringArray(R.array.menu).get(4))){
            rootview.btnLogOut.visibility = View.VISIBLE
        }else{
            rootview.btnLogOut.visibility = View.INVISIBLE
        }

        rootview.btnLogOut.setOnClickListener {
            SharedPreferencesUtils.isLoggedIn = false
            var i = Intent(activity, AuthentificationActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(i)
            activity!!.finish()

        }
        return rootview
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).floatingActionButtonSettings.visibility = View.INVISIBLE
        (activity as MainActivity).rlTitle.visibility = View.GONE
        (activity as MainActivity).imgLogo.visibility = View.VISIBLE
        (activity as MainActivity).rlIcon.visibility = View.INVISIBLE
    }
    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
    }
}// Required empty public constructor