package triple.solution.mycoupon.activities.employees

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.client_coupon_list_row.view.*
import kotlinx.android.synthetic.main.fragment_coupon_approved.view.*

import triple.solution.mycoupon.R
import triple.solution.mycoupon.enums.StatusClientCoupon
import triple.solution.mycoupon.helpers.todayToString
import triple.solution.mycoupon.models.ClientCoupon
import triple.solution.mycoupon.models.Coupon
import triple.solution.mycoupon.viewhelpers.LoadingDialog
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class CouponApprovedFragment : Fragment() {

    private lateinit var keyQrCode: String
    private lateinit var loading: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =
            inflater.inflate(R.layout.fragment_coupon_approved, container, false)

        keyQrCode = arguments!!.getString("keyQrCode")
        loading = LoadingDialog(activity!!)
        getDataCoupon(view)

        //Rounded Image
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.coupon_imageView_couponAppoved.clipToOutline = true
        }

        return view
    }

    private fun getDataCoupon(view: View) {
        val codes = keyQrCode.split("__")
        val keyClient = codes[0]
        val keyCoupon = codes[1]

        loading.startLoadingDialog()

        val database = FirebaseDatabase.getInstance()
            .getReference("/clientCoupon/$keyClient/$keyCoupon")

        database.addListenerForSingleValueEvent(object: ValueEventListener  {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(data: DataSnapshot) {
                val coupon = data.getValue(ClientCoupon::class.java) ?: return

                view.nameCoupon_textView_couponApproved.text = coupon.nameCoupon

                Picasso.get()
                    .load(coupon.urlImage)
                    .fit()
                    .into(view.coupon_imageView_couponAppoved)

                updateClientCoupon(database, coupon)
            }

        })
    }

    private fun updateClientCoupon(database: DatabaseReference, clientCoupon: ClientCoupon) {
        val uidEmployee = FirebaseAuth.getInstance().uid

        clientCoupon.uidEmployee = uidEmployee
        clientCoupon.status = StatusClientCoupon.APPROVED.value
        clientCoupon.dateStatus = Date().todayToString()

        database.setValue(clientCoupon)
            .addOnCompleteListener {
                loading.dismissDialog()
            }
            .addOnFailureListener {
                loading.dismissDialog()
            }
    }

}
