package triple.solution.mycoupon.activities.client

import android.os.Build
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.client_coupon_list_row.view.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.helpers.countDays
import triple.solution.mycoupon.helpers.stringToDate
import triple.solution.mycoupon.models.ClientCoupon
import triple.solution.mycoupon.models.Coupon
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

class ClientCouponListRow(private val coupon: ClientCoupon, val keyCoupon: String) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.client_coupon_list_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.couponName_textView_clientCouponRow.text =
            coupon.nameCoupon
        val days = Date().countDays(coupon.expiration.stringToDate())
        viewHolder.itemView.detailCoupon_textView_couponRow.text =
            "Expira en $days días"
        Picasso.get().load(coupon.urlImage)
            .fit()
            .into(viewHolder.itemView.coupon_imageView_clientCouponRow)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Rounded Image
            viewHolder.itemView.coupon_imageView_clientCouponRow.clipToOutline = true
        }
    }
}