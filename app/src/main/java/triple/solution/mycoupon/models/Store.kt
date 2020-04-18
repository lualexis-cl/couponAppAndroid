package triple.solution.mycoupon.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Store(val nameStore: String, val urlLogo: String) : Parcelable {
    constructor() : this("", ""){
    }
}