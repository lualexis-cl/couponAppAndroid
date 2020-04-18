package triple.solution.mycoupon.activities.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_login.view.*

import triple.solution.mycoupon.R
import triple.solution.mycoupon.activities.principal.MainActivity
import triple.solution.mycoupon.helpers.hideKeyboard
import triple.solution.mycoupon.viewhelpers.LoadingDialog

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    private var loadingDialog:LoadingDialog? = null//LoadingDialog(this.activity!!)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        loadingDialog = LoadingDialog(this.activity!!)
        register(view)
        login(view)

        return view
    }

    private fun register(view: View) {
        view.register_button_login.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.container, RegisterFragment())

            transaction?.addToBackStack(null)
            transaction?.commit()
        }
    }

    private fun fieldValidation(view: View) : Boolean {
        var result = true

        if (view.email_editText_login.text.toString().isEmpty()) {
            view.email_editText_login.error = "Email es requerido"
            result = false
        }

        if (view.password_editText_login.text.toString().isEmpty()) {
            view.password_editText_login.error = "Contraseña es requerida"
            result = false
        }

        return result
    }

    private fun login(view: View) {
        view.login_button_login.setOnClickListener {

            hideKeyboard()

            if (!fieldValidation(view)) {
                Toast.makeText(context,"Favor verificar los campos solicitados", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            loadingDialog?.startLoadingDialog()

            val email = view.email_editText_login.text.toString()
            val password = view.password_editText_login.text.toString()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    loadingDialog?.dismissDialog()

                    if (!it.isSuccessful) {
                        return@addOnCompleteListener
                    }

                    redirectHome()
                }.addOnFailureListener {
                    loadingDialog?.dismissDialog()
                    Toast.makeText(context,
                        "Se produjo un error inesperado, favor volver a intentar más tarde ${it.message}",
                        Toast.LENGTH_LONG)
                        .show()
                }
        }
    }

    private fun redirectHome() {
        val mainActivity = activity as MainActivity
        mainActivity.visibilityMenu()
        activity?.navigationView?.selectedItemId = R.id.action_home
    }
}
