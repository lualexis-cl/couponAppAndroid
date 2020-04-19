package triple.solution.mycoupon.activities.coupons

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_coupon_detail.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.helpers.stringToDate
import triple.solution.mycoupon.helpers.toNow
import triple.solution.mycoupon.models.ClientCoupon
import triple.solution.mycoupon.models.Coupon
import triple.solution.mycoupon.models.Store
import triple.solution.mycoupon.viewhelpers.LoadingDialog
import triple.solution.mycoupon.viewhelpers.MessageDialog
import java.util.*

class CouponDetailActivity : AppCompatActivity() {

    private var coupon: Coupon = Coupon()
    private var store: Store = Store()
    private var loadingDialog: LoadingDialog? = null
    private var keyCoupon: String = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupon_detail)
        supportActionBar?.hide()
        condition_textView_couponDetail.movementMethod = ScrollingMovementMethod()
        loadingDialog = LoadingDialog(this)

        coupon = intent.getParcelableExtra("coupon")
        store = intent.getParcelableExtra("store")
        keyCoupon = intent.getStringExtra("keyCoupon")

        loadDataStore()
        loadDataCoupon()
        acceptedCoupon()
    }

    private fun loadDataStore(){
        nameStore_textView_couponDetail.text = store.nameStore
        Picasso.get().load(store.urlLogo)
            .fit()
            .into(logo_imageView_couponDetail)
    }

    private fun loadDataCoupon() {
        Picasso.get().load(coupon.urlImage)
            .fit()
            .into(coupon_imageView_couponDetail)
        nameCoupon_textView_couponDetail.text = coupon.nameCoupon
        numberCoupons_textView_couponDetail.text = coupon.couponAvailable.toString()
        expire_textView_couponDetail.text = coupon.expiration
        condition_textView_couponDetail.text = coupon.text
    }

    private fun acceptedCoupon(){
        couponDetail_button.setOnClickListener {

            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {

                val messageDialog = MessageDialog(this)
                messageDialog.showDialog("Advertencia",
                    "Debe iniciar sesión para solicitar un cupón",
                    SweetAlertDialog.WARNING_TYPE)

                return@setOnClickListener
            }

            val confirmMessage = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            confirmMessage.titleText = "Confirmación"
            confirmMessage.contentText = "¿Está seguro que desea el cupón?"
            confirmMessage.confirmText = "Si, Seguro"
            confirmMessage.cancelText = "No"
            confirmMessage.setCancelable(false)
            confirmMessage.setCancelClickListener {
                it.dismissWithAnimation()
            }
            .setConfirmClickListener {
                it.dismissWithAnimation()
                this.loadingDialog?.startLoadingDialog()
                validateData()
            }
            confirmMessage.showCancelButton(true)
            confirmMessage.show()
        }
    }

    private fun redirectToAccepted(keyCoupon: String) {
        val intent = Intent(this, CouponDetailAceptedActivity::class.java)
        intent.putExtra("storeAccepted", store)
        intent.putExtra("keyCouponAccepted", keyCoupon)
        startActivity(intent)

        finish()
    }

    private fun validateData() {
        val database = FirebaseDatabase.getInstance()
            .getReference("/coupons/${keyCoupon}")

        database.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val coupon = snapshot.getValue(Coupon::class.java) ?: return

                if (coupon.couponAvailable <= 0 ||
                    coupon.expiration.stringToDate() < Date().toNow()) {
                    loadingDialog?.dismissDialog()

                    val messageDialog = MessageDialog(this@CouponDetailActivity)
                    messageDialog.showDialog("", "Cupón vencido o no disponible",
                        SweetAlertDialog.WARNING_TYPE)

                    return
                }

                coupon.couponAvailable = coupon.couponAvailable - 1
                updateCoupon(coupon)
            }

        })
    }

    private fun updateCoupon(coupon: Coupon) {
        val database = FirebaseDatabase.getInstance()
            .getReference("/coupons/${keyCoupon}")

        database.setValue(coupon)
            .addOnCompleteListener {
                createClientCoupon(coupon)
            }.addOnFailureListener {
                loadingDialog?.dismissDialog()

                val messageDialog = MessageDialog(this@CouponDetailActivity)
                messageDialog.showDialog("Error",
                    "Se produjo un error inesperado, favor intentar más tarde",
                    SweetAlertDialog.ERROR_TYPE)
            }
    }

    private fun createClientCoupon(coupon: Coupon){
        val uid = FirebaseAuth.getInstance().uid
        val database = FirebaseDatabase.getInstance()
            .getReference("/clientCoupon/$uid/${keyCoupon}")

        val clientCoupon = ClientCoupon()
        clientCoupon.couponAvailable = coupon.couponAvailable
        clientCoupon.expiration = coupon.expiration
        clientCoupon.nameCoupon = coupon.nameCoupon
        clientCoupon.text = coupon.text
        clientCoupon.totalCoupon = coupon.totalCoupon
        clientCoupon.urlImage = coupon.urlImage
        clientCoupon.status = true

        database.setValue(clientCoupon)
            .addOnCompleteListener {
                loadingDialog?.dismissDialog()
                redirectToAccepted(keyCoupon)
            }.addOnFailureListener {
                loadingDialog?.dismissDialog()

                val messageDialog = MessageDialog(this@CouponDetailActivity)
                messageDialog.showDialog("Error",
                    "Se produjo un error inesperado, favor intentar más tarde",
                    SweetAlertDialog.ERROR_TYPE)
            }
    }
}
