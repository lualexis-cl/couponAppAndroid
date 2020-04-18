package triple.solution.mycoupon.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ClientCoupon(var status: Boolean)  : Coupon(), Parcelable {

    constructor() : this(false) {

    }

}