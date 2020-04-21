package triple.solution.mycoupon.activities.client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_client_coupon_list.view.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.activities.coupons.CouponDetailAceptedActivity
import triple.solution.mycoupon.activities.rows.NoDataFound
import triple.solution.mycoupon.enums.StatusClientCoupon
import triple.solution.mycoupon.helpers.stringToDate
import triple.solution.mycoupon.helpers.toNow
import triple.solution.mycoupon.models.ClientCoupon
import triple.solution.mycoupon.models.Store
import triple.solution.mycoupon.viewhelpers.LoadingDialog
import java.util.*
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass.
 */
class ClientCouponListFragment : Fragment() {
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var store: Store = Store()
    private var myView: View? = null
    private val couponHashMap = HashMap<String, ClientCoupon>()
    private var loading: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView =
            inflater.inflate(R.layout.fragment_client_coupon_list, container, false)

        this.loading = LoadingDialog(activity!!)
        myView?.clientCoupon_RecyclerView?.adapter = adapter

        loadDataStore(myView!!)

        goToCouponDetail()
        loadDataCoupons()
        return myView
    }

    override fun onResume() {
        super.onResume()

        adapter.notifyDataSetChanged()
    }

    private fun loadDataStore(view: View) {
        val database = FirebaseDatabase.getInstance()
            .getReference("/Store/1PFFrogCyJaQIFf2ebUiZzM1e0n1")

        database.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                store = dataSnapshot.getValue(Store::class.java) ?: return

                Picasso.get()
                    .load(store.urlLogo)
                    .fit()
                    .into(view.logo_imageView_clientCouponList)
            }

        })
    }

    private fun refreshData() {
        adapter.clear()
        var count = 0
        couponHashMap.forEach {
            val coupon = it.value
            if (coupon.expiration.stringToDate() >= Date().toNow() &&
                coupon.status == StatusClientCoupon.VALID.value) {
                adapter.add(ClientCouponListRow(it.value, it.key))
                count++
            }
        }

        if (count == 0) {
            showNoDataFound()
        }

        this.loading?.dismissDialog()
    }

    private fun showNoDataFound() {
        val title = "No se han encontrado registros"
        val detail = "Al parecer no posee ningún cupón vigente, favor ir a la sección cupones y seleccionar el de su interes"

        adapter.add(NoDataFound(title, detail))
    }

    private fun loadDataCoupons() {
        val uid = FirebaseAuth.getInstance().uid
        val database = FirebaseDatabase.getInstance()
            .getReference("/clientCoupon/$uid")

        this.loading?.startLoadingDialog()
        var existsData = false

        database.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                val coupon = dataSnapshot.getValue(ClientCoupon::class.java) ?: return

                val keyCoupon = dataSnapshot.key!!
                couponHashMap[keyCoupon] = coupon
                Log.d("ClientCoupon", "Updated data")
                existsData = true
                refreshData()
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                val coupon = dataSnapshot.getValue(ClientCoupon::class.java) ?: return

                val keyCoupon = dataSnapshot.key!!
                couponHashMap[keyCoupon] = coupon
                existsData = false
                refreshData()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

        if (!existsData) {
            this.loading?.dismissDialog()
            showNoDataFound()
        }
    }

    private fun goToCouponDetail() {
        adapter.setOnItemClickListener { item, _ ->

            if (item is ClientCouponListRow) {
                val row = item as ClientCouponListRow
                val keyCoupon = row.keyCoupon

                val intent = Intent(activity, CouponDetailAceptedActivity::class.java)
                intent.putExtra("storeAccepted", store)
                intent.putExtra("keyCouponAccepted", keyCoupon)

                startActivity(intent)
            }
        }
    }
}
