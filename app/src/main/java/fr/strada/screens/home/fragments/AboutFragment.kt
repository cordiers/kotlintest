package fr.strada.screens.home.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import fr.strada.BuildConfig
import fr.strada.R
import fr.strada.screens.home.MainActivity
import fr.strada.utils.LocaleHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.toolbar.*


class AboutFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootview = inflater.inflate(R.layout.fragment_about, container, false)

        return rootview
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).txttitle.text = resources.getString(R.string.a_propos)
        (activity as MainActivity).rlTitle.visibility = View.VISIBLE
        (activity as MainActivity).dropdown.visibility = View.INVISIBLE
        //(activity as MainActivity)?.toolbar.minimumHeight = 85
        (activity as MainActivity).txttitle.visibility = View.VISIBLE
        (activity as MainActivity).imgLogo.visibility = View.GONE
        (activity as MainActivity).rlIcon.visibility = View.INVISIBLE
        (activity as MainActivity).floatingActionButtonSettings.visibility = View.INVISIBLE
        txtVersionName.text = BuildConfig.VERSION_NAME
    }

    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
    }
}// Required empty public constructor