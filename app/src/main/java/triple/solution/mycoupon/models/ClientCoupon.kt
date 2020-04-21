package triple.solution.mycoupon.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ClientCoupon(var status: Int,
                   var uidEmployee: String? = "",
                   var dateStatus: String? = "")  : Coupon(), Parcelable {

    constructor() : this(0) {

    }

}