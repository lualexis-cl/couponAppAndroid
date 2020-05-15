package triple.solution.mycoupon.activities.coupons

import android.graphics.Color
import android.os.Build
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.coupon_list_row.view.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.helpers.stringToDate
import triple.solution.mycoupon.helpers.toNow
import triple.solution.mycoupon.models.Coupon
import java.util.*

class CouponListRow(val coupon: Coupon, val key: String) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.coupon_list_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Picasso.get()
            .load(coupon.urlImage)
            .fit()
            .into(viewHolder.itemView.couponList_imageView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewHolder.itemView.couponList_imageView.clipToOutline = true
        }

        viewHolder.itemView.detailCoupon_textView_couponRow.setTextColor(Color.parseColor("#020202"))
        viewHolder.itemView.couponName_textView_couponRow.text = coupon.nameCoupon
        viewHolder.itemView.detailCoupon_textView_couponRow.text =
            "Cupones disponibles: ${coupon.couponAvailable} de ${coupon.totalCoupon}"

        if (coupon.couponAvailable <= 0) {
            viewHolder.itemView.detailCoupon_textView_couponRow.setTextColor(Color.RED)
            viewHolder.itemView.detailCoupon_textView_couponRow.text = "No hay cupones disponibles"
        }

        if (coupon.expiration.stringToDate() < Date().toNow()) {
            viewHolder.itemView.detailCoupon_textView_couponRow.setTextColor(Color.RED)
            viewHolder.itemView.detailCoupon_textView_couponRow.text = "Cupón vencido"
        }
    }
}