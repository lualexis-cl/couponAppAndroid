package triple.solution.mycoupon.activities.employees

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import triple.solution.mycoupon.R
import triple.solution.mycoupon.enums.StatusClientCoupon

/**
 * A simple [Fragment] subclass.
 */
class CouponDeclinedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_coupon_declined, container, false)

    }

}
