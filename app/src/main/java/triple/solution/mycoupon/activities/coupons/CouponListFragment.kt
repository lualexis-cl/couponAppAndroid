package triple.solution.mycoupon.activities.coupons

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
import kotlinx.android.synthetic.main.fragment_coupon_list.view.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.helpers.stringToDate
import triple.solution.mycoupon.helpers.toNow
import triple.solution.mycoupon.models.ClientCoupon
import triple.solution.mycoupon.models.Coupon
import triple.solution.mycoupon.models.Store
import triple.solution.mycoupon.viewhelpers.LoadingDialog
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class CouponListFragment : Fragment() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var store: Store? = null
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_coupon_list, container, false)

        this.loadingDialog = LoadingDialog(activity!!)
        view.couponList_RecyclerView.adapter = adapter
        view.couponList_RecyclerView.addItemDecoration(
            DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        fetchCurrentStore(view)
        loadForCoupons()
        redirectToDetail()

        return view
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
        //
        //https://bk-ca-prd-01.s3.amazonaws.com/sites/burgerking.ca/files/04025-49-DIG-Fresh-Offer-Banner_1000x550_EN-CR.jpg
        //https://bk-ca-prd-01.s3.amazonaws.com/sites/burgerking.ca/files/03162-78%20299%20Nuggets%20FreshOffersBanner%201000x550%20ENG_CR_0.jpg
        //https://bk-ca-prd-01.s3.amazonaws.com/sites/burgerking.ca/files/02280-07-2for5-Croissanwich-FreshOffer-Banner-1000x550_CR_EN_0_0.jpg

        var count = 0
        val database = FirebaseDatabase.getInstance()
            .getReference("/coupons")

        this.loadingDialog?.startLoadingDialog()
        database.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {

                val coupon = snapshot.getValue(Coupon::class.java)

                if (coupon != null &&
                    coupon.couponAvailable > 0 &&
                    coupon.expiration.stringToDate() >= Date().toNow()
                ) {
                    adapter.add(CouponListRow(coupon, snapshot.key!!))
                }



                if (count == 0) {
                    loadingDialog?.dismissDialog()
                }
                Log.d("CouponList", "Count $count de ${snapshot.children.count()}")
                count++


            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

    private fun redirectToDetail() {
        adapter.setOnItemClickListener { item, _ ->

            val couponListRow = item as CouponListRow
            val coupon = couponListRow.coupon
            val keyCoupon = couponListRow.key

            existCoupon(keyCoupon, coupon)
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
