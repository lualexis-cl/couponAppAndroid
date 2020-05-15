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
import triple.solution.mycoupon.viewhelpers.MessageDialog

/**
 * A simple [Fragment] subclass.
 */
class ForgotPasswordFragment : Fragment() {

    private lateinit var currentView: View;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.currentView = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        backToLogin(null)
        sendEmailForgotPassword()

        return this.currentView
    }

    private fun backToLogin(message: String?) {
        this.currentView.back_button_forgotPassword.setOnClickListener {
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
    }

    private fun sendEmailForgotPassword() {
        this.currentView.sendEmail_button_forgotPassword.setOnClickListener {
            if (this.currentView.email_editText_forgotPassword.text.isEmpty()) {
                this.currentView.email_editText_forgotPassword.error = "Debe ingresar un email"

                Toast.makeText(context, "Debe ingresar un email", Toast.LENGTH_LONG)

                return@setOnClickListener
            }

            FirebaseAuth.getInstance()
                .sendPasswordResetEmail(this.currentView.email_editText_forgotPassword.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        backToLogin("Se le ha enviado un email con las instrucciones")
                    }
                }.addOnFailureListener {
                    val message = MessageDialog(activity!!)
                    message.showDialog("", "Se produjo un error ${it.message}", SweetAlertDialog.ERROR_TYPE)
                }
        }
    }
}
