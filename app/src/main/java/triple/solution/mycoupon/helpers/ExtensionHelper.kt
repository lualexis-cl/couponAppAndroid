package triple.solution.mycoupon.helpers

import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


fun AppCompatActivity.hideKeyboard() {
    val view = this.currentFocus

    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
}

fun Fragment.hideKeyboard() {
    val activity = this.activity
    if (activity is AppCompatActivity) {
        activity.hideKeyboard()
    }
}

fun Date.toNow() : Date {
    val format = SimpleDateFormat("yyyy-MM-dd")
    val todayString = format.format(this)
    return format.parse(todayString)
}

fun Date.countDays(date: Date) : Int {
    val format = SimpleDateFormat("yyyy-MM-dd")
    val todayString = format.format(this)
    val timeExpiration = date.time - todayString.stringToDate().time
    return (timeExpiration / (1000 * 60 * 60 * 24)).toInt()
    /*TimeUnit.MILLISECONDS.toDays(timeExpiration)*/
}

fun String.stringToDate() : Date {
    val format = SimpleDateFormat("yyyy-MM-dd")
    return format.parse(this)
}

fun Fragment?.runOnUiThread(action: () -> Unit) {
    this ?: return
    if (!isAdded) return // Fragment not attached to an Activity
    activity?.runOnUiThread(action)
}
