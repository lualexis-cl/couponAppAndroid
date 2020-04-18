package triple.solution.mycoupon.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class Coupon(var couponAvailable: Int,
                  var expiration: String,
                  var nameCoupon: String,
                  var text: String,
                  var totalCoupon: Int,
                  var urlImage: String) : Parcelable {

    constructor() : this(0, "", "", "", 0, ""){
    }
}