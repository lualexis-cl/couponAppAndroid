package triple.solution.mycoupon.activities.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_register.view.*

import triple.solution.mycoupon.R
import triple.solution.mycoupon.helpers.hideKeyboard
import triple.solution.mycoupon.models.User
import triple.solution.mycoupon.viewhelpers.LoadingDialog

/**
 * A simple [Fragment] subclass.
 */
class RegisterFragment : Fragment() {

    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =
            inflater.inflate(R.layout.fragment_register, container, false)
        loadingDialog = LoadingDialog(activity!!)
        registerUser(view)

        return view
    }

    private fun fieldValidation(view: View) : User? {
        var user: User? = null
        var isGonnaRegisterUser = true
        val email = view.email_editText_register
        val name = view.name_editText_register
        val lastName = view.lastName_editText_register
        val cellPhone = view.cellPhone_editText_register
        val password = view.password_editText_register
        val rePassword = view.rePassword_editText_register

        if (email.text.isEmpty()) {
            email.error = "Email es obligatorio"
            isGonnaRegisterUser = false
        }

        if (name.text.isEmpty()) {
            name.error = "Nombre es obligatorio"
            isGonnaRegisterUser = false
        }

        if (lastName.text.isEmpty()) {
            lastName.error = "Apellido es obligatorio"
            isGonnaRegisterUser = false
        }

        if (cellPhone.text.isEmpty()) {
            cellPhone.error = "Celular es obligatorio"
            isGonnaRegisterUser = false
        }

        if (password.text.isEmpty()) {
            password.error = "Contraseña es obligatorio"
            isGonnaRegisterUser = false
        }

        if (password.text.toString() != rePassword.text.toString()){
            password.error = "Contraseñas no concuerdan"
            rePassword.error = "Contraseñas no concuerdan"
            isGonnaRegisterUser = false
        }

        if (isGonnaRegisterUser) {

            user = User(email.text.toString(), name.text.toString(),
                lastName.text.toString(), cellPhone.text.toString(), "")
        }

        return user
    }

    private fun registerUser(view: View) {
        view.register_button_register.setOnClickListener {
            hideKeyboard()
            val user = fieldValidation(view)

            if (user == null) {
                Toast.makeText(context, "Favor revisar todos los campos",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val password = view.password_editText_register.text
            this.loadingDialog?.startLoadingDialog()

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(user.email, password.toString())
                .addOnCompleteListener {

                    if (!it.isSuccessful) {
                        this.loadingDialog?.dismissDialog()
                        return@addOnCompleteListener
                    }

                    saveUser(view, user)

                }.addOnFailureListener {
                    this.loadingDialog?.dismissDialog()
                    Toast.makeText(context,
                        " Se produjo un error al crear el usuario ${it.message}",
                        Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun saveUser(view: View, user: User) {
        val uid = FirebaseAuth.getInstance().uid

        user.uid = uid.toString()
        val database = FirebaseDatabase.getInstance()
            .getReference("/users/$uid")


        database.setValue(user)
            .addOnSuccessListener {
                this.loadingDialog?.dismissDialog()
                redirectHome()
            }.addOnFailureListener {
                this.loadingDialog?.dismissDialog()
            }

    }

    private fun redirectHome() {
        activity?.navigationView?.selectedItemId = R.id.action_home
    }
}
