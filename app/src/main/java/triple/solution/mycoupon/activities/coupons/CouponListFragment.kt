package triple.solution.mycoupon.activities.coupons

import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import kotlinx.android.synthetic.main.fragment_coupon_list.view.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.activities.rows.NoDataFound
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
class CouponListFragment : Fragment() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var store: Store? = null
    private var loadingDialog: LoadingDialog? = null
    private val couponList = HashMap<String, Coupon>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_coupon_list, container, false)

        this.loadingDialog = LoadingDialog(activity!!)
        view.couponList_RecyclerView.adapter = adapter

        fetchCurrentStore(view)
        loadForCoupons()
        redirectToDetail()

        return view
    }

    private fun refreshData() {
        this.adapter.clear()
        var count = 0

        couponList.forEach {

            val coupon = it.value
            val key = it.key

            if (coupon != null &&
                coupon.couponAvailable > 0 &&
                coupon.expiration.stringToDate() >= Date().toNow()
            ) {
                adapter.add(CouponListRow(coupon, key))
                count++
            }
        }

        if (count == 0) {
            val title = "No se han subido nuevos cupones"
            val detail = "Pronto el administrador subira nuevos cupones para que puedas disfrutar"

            this.adapter.add(NoDataFound(title, detail))
        }

        this.loadingDialog?.dismissDialog()
    }

    private fun fetchCurrentStore(view: View) {
        val database = FirebaseDatabase.getInstance()
            .getReference("/Store/1PFFrogCyJaQIFf2ebUiZzM1e0n1")

        database.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    Log.d("CouponList", dataSnapshot.toString())
                    store = dataSnapshot.getValue(Store::class.java)
                    if (store != null) {
                        view.nameStore_textView_couponList.text = store?.nameStore
                        Picasso.get().load(store?.urlLogo)
                            .fit()
                            .into(view.logo_imageView_couponList)
                    }
                } catch (e: Exception) {
                    Log.d("CouponList", e.toString())
                }

            }

        })
    }

    private fun loadForCoupons() {
        val database = FirebaseDatabase.getInstance()
            .getReference("/coupons")

        Log.d("CouponList", "Llamada a Loading")

        this.loadingDialog?.startLoadingDialog()

        database.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {
                val coupon = snapshot.getValue(Coupon::class.java) ?: return
                couponList[snapshot.key!!] = coupon

                refreshData()
            }

            override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
                val coupon = snapshot.getValue(Coupon::class.java) ?: return
                couponList[snapshot.key!!] = coupon

                refreshData()
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

    private fun redirectToDetail() {
        adapter.setOnItemClickListener { item, _ ->

            if (item is CouponListRow) {
                val couponListRow = item as CouponListRow
                val coupon = couponListRow.coupon
                val keyCoupon = couponListRow.key

                existCoupon(keyCoupon, coupon)
            }
        }
    }

    private fun existCoupon(keyCoupon: String, coupon: Coupon) {
        val uid = FirebaseAuth.getInstance().uid
        //If it doesn't authenticated then go to detail
        if (uid == null) {
            goToDetail(coupon, keyCoupon)
            return
        }
        val database = FirebaseDatabase.getInstance()
            .getReference("/clientCoupon/$uid/$keyCoupon")

        database.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val clientCoupon = dataSnapshot.getValue(ClientCoupon::class.java)

                if (clientCoupon != null &&
                    clientCoupon.status) {

                    val intent = Intent(activity, CouponDetailAceptedActivity::class.java)
                    intent.putExtra("storeAccepted", store)
                    intent.putExtra("keyCouponAccepted", keyCoupon)
                    startActivity(intent)

                } else {
                    goToDetail(coupon, keyCoupon)
                }
            }

        })
    }

    private fun goToDetail(coupon: Coupon, keyCoupon: String) {
        val intent = Intent(activity, CouponDetailActivity::class.java)
        intent.putExtra("coupon", coupon)
        intent.putExtra("store", store)
        intent.putExtra("keyCoupon", keyCoupon)

        startActivity(intent)
    }

}
