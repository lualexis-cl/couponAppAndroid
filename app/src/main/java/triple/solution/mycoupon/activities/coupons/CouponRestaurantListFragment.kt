package triple.solution.mycoupon.activities.coupons

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso

import triple.solution.mycoupon.R

/**
 * A simple [Fragment] subclass.
 */
class CouponRestaurantListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_coupon_restaurant_list, container, false)

        return view
    }

}
