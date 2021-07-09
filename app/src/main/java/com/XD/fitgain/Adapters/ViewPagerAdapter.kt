import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.XD.fitgain.views.fragments.*

internal class PagerViewAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                Home()
            }
            1 -> {
                Discover()
            }
            2 -> {
                Activity()
            }
            3 -> {
                Cupons()
            }
            4 -> {
                Profile()
            }
            else -> Home()
        }
    }

    override fun getCount(): Int {
        return 5
    }

}