package triple.solution.mycoupon.activities.employees

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_employee.view.*

import triple.solution.mycoupon.R
import triple.solution.mycoupon.activities.register.EditPerfilFragment
import triple.solution.mycoupon.activities.register.LoginFragment
import triple.solution.mycoupon.enums.StatusClientCoupon
import triple.solution.mycoupon.helpers.runOnUiThread
import triple.solution.mycoupon.helpers.stringToDate
import triple.solution.mycoupon.helpers.toNow
import triple.solution.mycoupon.models.ClientCoupon
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class EmployeeFragment : Fragment() {

    private lateinit var codeScanner: CodeScanner
    private val cameraPermissionCode = 1111

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_employee, container, false)
        codeScanner = CodeScanner(context!!, view.scannerView)
        validateQr()
        checkPermission()

        return view
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == cameraPermissionCode &&
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            codeScanner.startPreview()
        } else {
            Toast.makeText(context!!, "Imposible otorgar permisos de la camara", Toast.LENGTH_LONG)
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA), cameraPermissionCode)
        } else {
            codeScanner.startPreview()
        }
    }

    private fun validateQr() {
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                Toast.makeText(context!!, "Scan Result ${it.text}", Toast.LENGTH_LONG)
                Log.d("CodeQR", "Scan Result ${it.text}")

                if (it.text.contains("__")) {
                    validationUsedCoupon(it.text)
                } else {
                    loadFragment(CouponDeclinedFragment())
                }
            }
        }

        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(context!!, "Error Scan Result ${it.message}", Toast.LENGTH_LONG)
                Log.d("CodeQR", "Error Scan Result ${it.message}")
            }
        }
    }

    private fun validationUsedCoupon(code: String) {
        val codes = code.split("__")
        val keyClient = codes[0]
        val keyCoupon = codes[1]

        val database = FirebaseDatabase.getInstance()
            .getReference("/clientCoupon/$keyClient/$keyCoupon")

        database.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(data: DataSnapshot) {
                val coupon = data.getValue(ClientCoupon::class.java) ?: return

                if (coupon.expiration.stringToDate() < Date().toNow()) {
                    loadFragment(CouponDeclinedFragment())
                } else if (coupon.status == StatusClientCoupon.DELETED.value ||
                    coupon.status == StatusClientCoupon.APPROVED.value) {
                    loadFragment(CouponDeclinedFragment())
                } else {
                    val bundle = Bundle()
                    bundle.putString("keyQrCode", code)
                    val fragment = CouponApprovedFragment()
                    fragment.arguments = bundle

                    loadFragment(fragment)
                }
            }

        })
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
