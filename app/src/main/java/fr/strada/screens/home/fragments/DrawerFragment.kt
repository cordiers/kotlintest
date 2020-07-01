package fr.strada.screens.home.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.strada.R
import fr.strada.screens.home.DrawerAdapter
import fr.strada.screens.home.DrawerModel
import fr.strada.screens.home.ReaderActivityKotlin
import fr.strada.utils.Constants
import fr.strada.utils.LocaleHelper
import fr.strada.utils.SharedPreferencesUtils
import java.util.*


class DrawerFragment : Fragment() {

    private var views: View? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var drawerAdapter: DrawerAdapter? = null
    private var containerView: View? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var  names : Array<String>
   // private var mainActivity:MainActivity = MainActivity()

    private var ScanMode = SharedPreferencesUtils.isLoggedIn
    private val images = intArrayOf(R.drawable.ic_home, R.drawable.ic_scanner,R.drawable.ic_activite,R.drawable.ic_documents,R.drawable.ic_profil,R.drawable.ic_settings,R.drawable.ic_propos)
    private val imagesScan = intArrayOf(R.drawable.ic_home,R.drawable.ic_scanner,R.drawable.ic_activite,R.drawable.ic_settings,R.drawable.ic_propos)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        views = inflater.inflate(R.layout.fragment_drawer, container, false)
        recyclerView = views!!.findViewById<View>(R.id.listview) as RecyclerView
        drawerAdapter = if (ScanMode) DrawerAdapter(activity!!, populateList()) else DrawerAdapter(activity!!, populateListScan())
        recyclerView!!.adapter = drawerAdapter
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        recyclerView!!.addOnItemTouchListener(RecyclerTouchListener(activity!!, recyclerView!!, object : ClickListener {
            override fun onClick(view: View, position: Int) {
                if (ScanMode)
                    openFragment(position)
                else
                    openModeScanFragment(position)

                mDrawerLayout!!.closeDrawer(containerView!!)
            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))

        if (ScanMode)
            openFragment(0)
        else
            openModeScanFragment(0)

        return views
    }


    private fun openFragment(position: Int) {

        when (position) {
            0 -> {
                removeAllFragment(HomeFragment(), names[1])
            }
            1 -> {
                removeAllFragment(LectureFragment(), "")
                //startActivity(Intent(activity,ReaderActivity::class.java))
            }
            2 -> {
                removeAllFragment(ActivityFragment(), names[2])

            }
            3 -> {
                //Doc
               removeAllFragment(DocumentFragment(), names[3])
             // removeAllFragment(UnderConstraFragment(), names[3])
            }
            4 -> {
                //profile
                removeAllFragment(ProfilFragment(), names[4])
            }
            5 -> {
                // settings
                removeAllFragment(SettingsFragment(), names[5])
            }
            6 -> {
                removeAllFragment(AboutFragment(), names[6])
            }

            else -> {
            }
        }
    }

    private fun openModeScanFragment(position: Int) {

        when (position) {

            0 -> {

                removeAllFragment(HomeFragment(), names[1])
            }
            1 -> {
                // removeAllFragment(NotificationFragment(), names[1])
                startActivity(Intent(activity,ReaderActivityKotlin::class.java))


            }
            2 -> {
                removeAllFragment(ActivityFragment(), names[2])


            }
            3 -> {
                // settings
                removeAllFragment(SettingsFragment(), names[3])
            }
            4 -> {
                removeAllFragment(AboutFragment(), names[4])

            }

            else -> {

            }
        }
    }


   /* fun removeAllFragment(replaceFragment: Fragment, tag: String) {
        val manager = activity!!.supportFragmentManager
        val ft = manager.beginTransaction()
        manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        ft.replace(R.id.container_body, replaceFragment,tag)
        ft.commitAllowingStateLoss()
    }
*/
    fun removeAllFragment(f: Fragment,tag: String,cleanStack: Boolean = true) {
        val ft = activity!!.supportFragmentManager.beginTransaction()
        if (cleanStack) {
            // clearBackStack();
            ft.remove(f)
        }

        ft.replace(R.id.container_body, f,tag)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun setUpDrawer(fragmentId: Int, drawerLayout: DrawerLayout, toolbar: Toolbar) {
        containerView = activity!!.findViewById(fragmentId)
        mDrawerLayout = drawerLayout
        mDrawerToggle = object : ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                activity!!.invalidateOptionsMenu()
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                activity!!.invalidateOptionsMenu()
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                toolbar.alpha = 1 - slideOffset / 2
            }
        }

        //mDrawerLayout!!.setDrawerListener(mDrawerToggle)
        mDrawerLayout!!.addDrawerListener(mDrawerToggle!!)

        mDrawerLayout!!.post { mDrawerToggle!!.syncState() }

    }

    private fun populateList(): ArrayList<DrawerModel> {

        names = resources.getStringArray(R.array.menu)
        val list = ArrayList<DrawerModel>()

        for (i in names.indices) {
            val drawerModel = DrawerModel()
            drawerModel.name = names[i]
            drawerModel.image = images[i]
            list.add(drawerModel)
        }
        return list
    }

    private fun populateListScan(): ArrayList<DrawerModel> {

        names = resources.getStringArray(R.array.menuScan)
        val list = ArrayList<DrawerModel>()

        for (i in names.indices) {
            val drawerModel = DrawerModel()
            drawerModel.name = names[i]
            drawerModel.image = imagesScan[i]
            list.add(drawerModel)
        }
        return list
    }

    interface ClickListener {
        fun onClick(view: View, position: Int)

        fun onLongClick(view: View?, position: Int)
    }

    internal class RecyclerTouchListener(context: Context, recyclerView: RecyclerView, private val clickListener: ClickListener?) : RecyclerView.OnItemTouchListener {

        private val gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child = recyclerView.findChildViewUnder(e.x, e.y)
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child))
                    }
                }
            })
        }

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {

            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child))
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
    }


}// Required empty public constructor