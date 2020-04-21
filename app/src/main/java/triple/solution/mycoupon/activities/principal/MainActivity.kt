package triple.solution.mycoupon.activities.principal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.activities.client.ClientCouponListFragment
import triple.solution.mycoupon.activities.coupons.CouponListFragment
import triple.solution.mycoupon.activities.employees.EmployeeFragment
import triple.solution.mycoupon.activities.register.EditPerfilFragment
import triple.solution.mycoupon.activities.register.LoginFragment
import triple.solution.mycoupon.enums.TypeClient
import triple.solution.mycoupon.models.User

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

    fun visibilityMenu() {
        var visibilityCoupons = true
        navigationView.menu.findItem(R.id.action_scan).isVisible = false

        if (FirebaseAuth.getInstance().currentUser == null){
            visibilityCoupons = false
        } else {
            loadAdminMenus()
        }

        navigationView.menu.findItem(R.id.action_coupon).isVisible = visibilityCoupons
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
        val uid = FirebaseAuth.getInstance().uid

        val database = FirebaseDatabase.getInstance()
            .getReference("/users/$uid")

        database.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(data: DataSnapshot) {
                val user = data.getValue(User::class.java) ?: return

                if (user.typeClient == TypeClient.ADMIN.value ||
                    user.typeClient == TypeClient.SERVER.value) {
                    navigationView.menu.findItem(R.id.action_scan).isVisible = true
                }
            }

        })
    }
}
