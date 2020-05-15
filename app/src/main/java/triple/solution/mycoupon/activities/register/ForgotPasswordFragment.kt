package triple.solution.mycoupon.activities.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_forgot_password.view.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.viewhelpers.LoadingDialog
import triple.solution.mycoupon.viewhelpers.MessageDialog

/**
 * A simple [Fragment] subclass.
 */
class ForgotPasswordFragment : Fragment() {

    private lateinit var currentView: View
    private lateinit var loading: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.currentView = inflater.inflate(R.layout.fragment_forgot_password, container, false)
        this.loading = LoadingDialog(activity!!)
        backToLogin()
        sendEmailForgotPassword()

        return this.currentView
    }

    private fun backToLogin() {
        this.currentView.back_button_forgotPassword.setOnClickListener {
            backToLoginAction(null)
        }
    }

    private fun backToLoginAction(message: String?) {
        val transaction =
            activity?.supportFragmentManager?.beginTransaction()

        val fragment = LoginFragment()

        if (message != null) {
            val bundle = Bundle()
            bundle.putString("RequiredMessage", message)
            fragment.arguments = bundle
        }

        transaction?.replace(R.id.container, fragment)

        transaction?.addToBackStack(null)
        transaction?.commit()
    }

    private fun sendEmailForgotPassword() {
        this.currentView.sendEmail_button_forgotPassword.setOnClickListener {
            if (this.currentView.email_editText_forgotPassword.text.isEmpty()) {
                this.currentView.email_editText_forgotPassword.error = "Debe ingresar un email"

                Toast.makeText(context, "Debe ingresar un email", Toast.LENGTH_LONG)

                return@setOnClickListener
            }
            this.loading.startLoadingDialog()

            FirebaseAuth.getInstance()
                .sendPasswordResetEmail(this.currentView.email_editText_forgotPassword.text.toString())
                .addOnCompleteListener {
                    this.loading.dismissDialog()
                    if (it.isSuccessful) {
                        backToLoginAction("Se le ha enviado un email con las instrucciones")
                    }
                }.addOnFailureListener {
                    this.loading.dismissDialog()
                    val message = MessageDialog(activity!!)
                    message.showDialog("", "Se produjo un error ${it.message}", SweetAlertDialog.ERROR_TYPE)
                }
        }
    }
}
