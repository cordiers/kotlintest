package fr.strada.screens.auth

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import fr.strada.R

class ViewPagerAdapter(private val context : Context) : PagerAdapter() {
    private var layoutInflater : LayoutInflater? = null
    val Image = arrayOf(R.drawable.ic_signin_blue , R.drawable.ic_phone_blue )
    val Text = arrayOf(R.string.label_msg1 , R.string.label_msg2 )


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view ===  `object`
    }

    override fun getCount(): Int {
        return Image.size
    }

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = layoutInflater!!.inflate(R.layout.item_view , null)
        val image = v.findViewById<View>(R.id.imgIcon) as ImageView
        val text = v.findViewById<View>(R.id.txtMsg) as TextView


        image.setImageResource(Image[position])
        text.setText(Text[position])
        text.textSize = 15F
        val vp = container as ViewPager
        vp.addView(v , 0)


        return v

    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val v = `object` as View
        vp.removeView(v)
    }


}