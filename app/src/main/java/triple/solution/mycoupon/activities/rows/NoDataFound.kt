package triple.solution.mycoupon.activities.rows

import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.coupon_no_data_found.view.*
import triple.solution.mycoupon.R

class NoDataFound(private val title: String, private val detail: String)
    : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.coupon_no_data_found
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.title_textView_noDataFound.text = title
        viewHolder.itemView.detail_textView_noDataFound.text = detail
    }
}