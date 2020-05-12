package triple.solution.mycoupon.activities.principal

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.activities.client.ClientCouponListFragment
import triple.solution.mycoupon.activities.coupons.CouponListFragment
import triple.solution.mycoupon.activities.employees.EmployeeFragment
import triple.solution.mycoupon.activities.register.EditPerfilFragment
import triple.solution.mycoupon.activities.register.LoginFragment
import triple.solution.mycoupon.enums.TypeClient
import triple.solution.mycoupon.models.User
import triple.solution.mycoupon.models.UserApplication

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        loadFragment(CouponListFragment())
        visibilityMenu()

        navigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_home -> {
                    title = resources.getString(R.string.home)
                    loadFragment(CouponListFragment())
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.action_coupon -> {
                    title = resources.getString(R.string.myCoupons)
                    loadFragment(ClientCouponListFragment())

                    return@setOnNavigationItemSelectedListener true
                }

                R.id.action_scan -> {
                    loadFragment(EmployeeFragment())

                    return@setOnNavigationItemSelectedListener true
                }

                R.id.action_perfil -> {
                    title = resources.getString(R.string.MyPerfil)

                    var fragment: Fragment = EditPerfilFragment()

                    if (FirebaseAuth.getInstance().currentUser == null) {
                        fragment = LoginFragment()
                    }

                    loadFragment(fragment)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false

        }
    }

    override fun onResume() {
        super.onResume()

        Log.d("CouponAdmin", "" + FirebaseAuth.getInstance().currentUser?.metadata?.lastSignInTimestamp.toString())
    }

    fun visibilityMenu() {
        navigationView.menu.findItem(R.id.action_scan).isVisible = false
        navigationView.menu.findItem(R.id.action_coupon).isVisible = false
        deleteSharedPreferences()

        FirebaseAuth.getInstance().currentUser?.reload()
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    navigationView.menu.findItem(R.id.action_coupon).isVisible = true
                    loadAdminMenus()
                }
            }?.addOnFailureListener {
                Log.d("CouponAdmin", "Current Error $it")
            }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun loadAdminMenus() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        val database = FirebaseDatabase.getInstance()
            .getReference("/userApplication")

        database.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(data: DataSnapshot, p1: String?) {
                val userApplication = data.getValue(UserApplication::class.java) ?: return

                if (currentUser?.email?.toLowerCase() == userApplication.email.toLowerCase() &&
                   (userApplication.userType == TypeClient.ADMIN.value ||
                    userApplication.userType == TypeClient.SERVER.value)) {

                    navigationView.menu.findItem(R.id.action_scan).isVisible = true

                    if (userApplication.userType == TypeClient.SERVER.value) {
                        navigationView.menu.findItem(R.id.action_coupon).isVisible = false
                    }

                    if (userApplication.userType == TypeClient.ADMIN.value) {
                        navigationView.menu.findItem(R.id.action_coupon).isVisible = true
                    }

                    writeOnSharedPreferences(userApplication)
                }

                Log.d("CouponAdmin", userApplication.email)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                navigationView.menu.findItem(R.id.action_scan).isVisible = true
            }

        })
    }

    private fun writeOnSharedPreferences(userApplication: UserApplication) {
        val shared = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (shared.edit()) {
            putInt("userType", userApplication.userType)
            commit()
        }
    }

    private fun deleteSharedPreferences() {
        val shared = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (shared.edit()) {
            remove("userType")
            commit()
        }
    }
}
