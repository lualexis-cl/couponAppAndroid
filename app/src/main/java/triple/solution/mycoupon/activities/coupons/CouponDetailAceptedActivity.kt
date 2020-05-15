package triple.solution.mycoupon.activities.coupons

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_coupon_detail_acepted.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.enums.StatusClientCoupon
import triple.solution.mycoupon.helpers.stringToDate
import triple.solution.mycoupon.helpers.toNow
import triple.solution.mycoupon.models.ClientCoupon
import triple.solution.mycoupon.models.Coupon
import triple.solution.mycoupon.models.Store
import triple.solution.mycoupon.viewhelpers.LoadingDialog
import triple.solution.mycoupon.viewhelpers.MessageDialog
import java.util.*

class CouponDetailAceptedActivity : AppCompatActivity() {

    private lateinit var store: Store
    private lateinit var keyCoupon: String
    private var clientCoupon = ClientCoupon()
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var messageDialog: MessageDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupon_detail_acepted)
        supportActionBar?.hide()

        status_linearLayout.visibility = View.GONE
        dateStatus_linearLayout.visibility = View.GONE

        this.loadingDialog = LoadingDialog(this)
        this.messageDialog = MessageDialog(this)

        this.store = intent.getParcelableExtra("storeAccepted")
        this.keyCoupon = intent.getStringExtra("keyCouponAccepted")

        configScrollView()
        loadDataStore()
        loadDataCoupon()
        generateQr()
        cancelCoupon()
    }

    private fun configScrollView() {
        condition_textView_couponDetailAccepted.movementMethod = ScrollingMovementMethod()

        scrollViewCouponAccepted.setOnTouchListener { _, _ ->
            condition_textView_couponDetailAccepted.parent
                .requestDisallowInterceptTouchEvent(false)
            false
        }

        condition_textView_couponDetailAccepted.setOnTouchListener { _, _ ->
            condition_textView_couponDetailAccepted.parent
                .requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun loadDataStore() {
        nameStore_textView_couponDetailAccepted.text = "Detalle de mi Cupón"
        Picasso.get().load(store.urlLogo)
            .fit()
            .into(logo_imageView_couponDetailAccepted)
    }

    private fun loadDataCoupon() {

        val uid = FirebaseAuth.getInstance().uid
        val database = FirebaseDatabase.getInstance()
            .getReference("/clientCoupon/$uid/$keyCoupon")

        database.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(data: DataSnapshot) {
                clientCoupon = data.getValue(ClientCoupon::class.java) ?:return

                nameCoupon_textView_couponDetailAccepted.text = clientCoupon.nameCoupon
                expire_textView_couponDetailAccepted.text = clientCoupon.expiration
                condition_textView_couponDetailAccepted.text = clientCoupon.text

                if (clientCoupon.status == StatusClientCoupon.APPROVED.value) {
                    status_linearLayout.visibility = View.VISIBLE
                    dateStatus_linearLayout.visibility = View.VISIBLE

                    status_textView_couponDetailAccepted.text = "Cupón utilizado"
                    dateStatus_textView_couponDetailAccepted.text = clientCoupon.dateStatus
                    qrCode_imageView.visibility = View.GONE
                    cancelCoupon_button.visibility = View.GONE
                }

                if (clientCoupon.expiration.stringToDate() < Date().toNow()) {
                    expire_textView_couponDetailAccepted.text = "Cupón expirado en ${clientCoupon.expiration}"
                    qrCode_imageView.visibility = View.GONE
                    cancelCoupon_button.visibility = View.GONE
                }

                Picasso.get().load(clientCoupon.urlImage)
                    .fit()
                    .into(coupon_imageView_couponDetailAccepted)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    coupon_imageView_couponDetailAccepted.clipToOutline = true
                }
            }

        })
    }

    private fun generateQr(){
        val uid = FirebaseAuth.getInstance().uid
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode("${uid}__${keyCoupon}",
            BarcodeFormat.QR_CODE, 237, 207)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }

        qrCode_imageView.setImageBitmap(bitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            qrCode_imageView.clipToOutline = true
        }
    }

    private fun cancelCoupon() {
        cancelCoupon_button.setOnClickListener {

            val confirmMessage = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            confirmMessage.titleText = "Confirmación"
            confirmMessage.contentText = "¿Está seguro que desea cancelar el cupón?"
            confirmMessage.confirmText = "Si, Seguro"
            confirmMessage.cancelText = "No"
            confirmMessage.setCancelable(false)
            confirmMessage.setCancelClickListener {
                it.dismissWithAnimation()
            }
            .setConfirmClickListener {
                it.dismissWithAnimation()
                this.loadingDialog.startLoadingDialog()
                updateClientCoupon()
            }
            confirmMessage.showCancelButton(true)
            confirmMessage.show()
        }
    }

    private fun updateClientCoupon() {
        val uid = FirebaseAuth.getInstance().uid
        val database = FirebaseDatabase.getInstance()
            .getReference("/clientCoupon/$uid/$keyCoupon")

        this.clientCoupon.status = StatusClientCoupon.DELETED.value

        database.setValue(this.clientCoupon)
            .addOnCompleteListener {
                verifyCoupon()
                cancelCoupon_button.visibility = View.INVISIBLE
                qrCode_imageView.visibility = View.INVISIBLE

            }.addOnFailureListener {
                this.loadingDialog.dismissDialog()
                messageDialog.showDialog("",
                    "Se produjo un error inesperado, favor intentar más tarde",
                    SweetAlertDialog.ERROR_TYPE)
            }
    }

    private fun verifyCoupon() {
        val database = FirebaseDatabase.getInstance()
            .getReference("/coupons/$keyCoupon")

        database.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(data: DataSnapshot) {
                val coupon = data.getValue(Coupon::class.java) ?: return

                if (coupon.expiration.stringToDate() >= Date().toNow()) {
                    coupon.couponAvailable = coupon.couponAvailable + 1

                    updateCoupon(database, coupon)
                } else {
                    loadingDialog.dismissDialog()
                }

            }

        })
    }

    private fun updateCoupon(database: DatabaseReference, coupon: Coupon) {
        database.setValue(coupon)
            .addOnCompleteListener {
                loadingDialog.dismissDialog()
            }.addOnFailureListener {
                messageDialog.showDialog("",
                    "Se produjo un error inesperado, favor intentar más tarde",
                    SweetAlertDialog.ERROR_TYPE)
                this.loadingDialog.dismissDialog()
            }
    }
}
