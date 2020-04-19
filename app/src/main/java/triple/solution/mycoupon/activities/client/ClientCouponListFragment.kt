package triple.solution.mycoupon.activities.client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_client_coupon_list.view.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.activities.coupons.CouponDetailAceptedActivity
import triple.solution.mycoupon.helpers.stringToDate
import triple.solution.mycoupon.helpers.toNow
import triple.solution.mycoupon.models.ClientCoupon
import triple.solution.mycoupon.models.Coupon
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView =
            inflater.inflate(R.layout.fragment_client_coupon_list, container, false)

        myView?.clientCoupon_RecyclerView?.adapter = adapter
        myView?.clientCoupon_RecyclerView?.addItemDecoration(
            DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        )

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

        couponHashMap.forEach {
            val coupon = it.value
            if (coupon.expiration.stringToDate() >= Date().toNow() &&
                coupon.status) {
                adapter.add(ClientCouponListRow(it.value, it.key))
            }


        }

        Log.d("ClientCoupon", "fin foreach")
    }

    private fun loadDataCoupons() {
        val uid = FirebaseAuth.getInstance().uid
        val database = FirebaseDatabase.getInstance()
            .getReference("/clientCoupon/$uid")
        val loading = LoadingDialog(activity!!)
        loading.startLoadingDialog()
        var count = 0
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

                refreshData()
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                val coupon = dataSnapshot.getValue(ClientCoupon::class.java) ?: return

                val keyCoupon = dataSnapshot.key!!
                couponHashMap[keyCoupon] = coupon

                if (count == 0) {
                    loading.dismissDialog()
                }
                count++
                refreshData()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

        if (count == 0) {
            loading.dismissDialog()
        }
    }

    private fun goToCouponDetail() {
        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(activity, CouponDetailAceptedActivity::class.java)
            val row = item as ClientCouponListRow
            val keyCoupon = row.keyCoupon

            intent.putExtra("storeAccepted", store)
            intent.putExtra("keyCouponAccepted", keyCoupon)

            startActivity(intent)
        }
    }
}
