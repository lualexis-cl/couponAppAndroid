package triple.solution.mycoupon.viewhelpers

import android.app.Activity
import android.graphics.Color
import cn.pedant.SweetAlert.SweetAlertDialog

class LoadingDialog {
    private val progress: SweetAlertDialog

    constructor(activity: Activity) {
        this.progress = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
    }

    fun startLoadingDialog() {
        this.progress.progressHelper.barColor = Color.parseColor("#A5DC86")
        this.progress.titleText = "Cargando..."
        this.progress.setCancelable(false)
        this.progress.show()
    }

    fun dismissDialog() {
        this.progress.dismiss()
    }
}