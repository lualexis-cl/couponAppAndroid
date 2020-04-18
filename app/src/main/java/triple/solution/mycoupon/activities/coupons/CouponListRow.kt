package triple.solution.mycoupon.activities.coupons

import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.coupon_list_row.view.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.models.Coupon

class CouponListRow(val coupon: Coupon, val key: String) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.coupon_list_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Picasso.get().load(coupon.urlImage)
            .fit()
            .into(viewHolder.itemView.couponList_imageView)
        viewHolder.itemView.couponName_textView_couponRow.text = coupon.nameCoupon
        viewHolder.itemView.detailCoupon_textView_couponRow.text =
            "Cupones disponibles: ${coupon.couponAvailable} de ${coupon.totalCoupon}"
    }
}