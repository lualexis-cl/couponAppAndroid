package triple.solution.mycoupon.viewhelpers

import android.app.Activity
import cn.pedant.SweetAlert.SweetAlertDialog

class MessageDialog {
    private val activity: Activity

    constructor(activity: Activity) {
        this.activity = activity
    }

    fun showDialog(titleText: String, contextText: String, alertType: Int) {
        val alert = SweetAlertDialog(this.activity, alertType)
        alert.titleText = titleText
        alert.contentText = contextText
        alert.setConfirmClickListener {
            it.dismissWithAnimation()
        }
        alert.setCancelable(false)
        alert.show()
    }
}