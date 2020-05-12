package triple.solution.mycoupon.activities.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_edit_perfil.view.*
import triple.solution.mycoupon.R
import triple.solution.mycoupon.activities.principal.MainActivity
import triple.solution.mycoupon.helpers.hideKeyboard
import triple.solution.mycoupon.models.User
import triple.solution.mycoupon.viewhelpers.LoadingDialog
import triple.solution.mycoupon.viewhelpers.MessageDialog

class EditPerfilFragment : Fragment() {

    private var loadingDialog: LoadingDialog? = null
    private var messageDialog: MessageDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =
            inflater.inflate(R.layout.fragment_edit_perfil, container, false)
        loadingDialog = LoadingDialog(activity!!)
        messageDialog = MessageDialog(activity!!)

        logout(view)
        loadDataClient(view)
        saveEdit(view)

        return view
    }

    private fun loadDataClient(view: View) {
        val uid = FirebaseAuth.getInstance().uid
        val database = FirebaseDatabase.getInstance()
            .getReference("/users/$uid")

        database.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java) ?: return

                view.email_editText_editPerfil.setText(user.email)
                view.name_editText_editPerfil.setText(user.name)
                view.lastName_editText_editPerfil.setText(user.lastName)
            }

        })
    }

    private fun logout(view: View) {
        view.logout_button_editPerfil.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val mainActivity = activity as MainActivity
            mainActivity.visibilityMenu()
            activity?.navigationView?.selectedItemId = R.id.action_perfil

        }
    }

    private fun saveEdit(view: View) {
        view.edit_button.setOnClickListener {
            if (!validateEdit(view)) {
                return@setOnClickListener
            }
            hideKeyboard()
            loadingDialog?.startLoadingDialog()

            val uid = FirebaseAuth.getInstance().uid
            val user = User(view.email_editText_editPerfil.text.toString(),
                            view.name_editText_editPerfil.text.toString(),
                            view.lastName_editText_editPerfil.text.toString(),
                            uid!!)

            val database = FirebaseDatabase.getInstance()
                .getReference("/users/$uid")

            database.setValue(user)
                .addOnSuccessListener {

                    if (view.oldPassword_editText_editPerfil.text.toString().isEmpty()) {
                        this.loadingDialog?.dismissDialog()
                        this.messageDialog?.showDialog("",
                            "Información personal actualizada correctamente",
                            SweetAlertDialog.SUCCESS_TYPE)
                    } else {
                        setPassword(view)
                    }

                }.addOnFailureListener {
                    loadingDialog?.dismissDialog()
                }



        }
    }

    private fun setPassword(view: View) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(currentUser?.email!!,
                                view.oldPassword_editText_editPerfil.text.toString())

        currentUser.reauthenticate(credential)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    currentUser.updatePassword(view.password_editText_editPerfil.text.toString())
                        .addOnCompleteListener { passResult ->

                            loadingDialog?.dismissDialog()

                            if (!passResult.isSuccessful) {
                                this.messageDialog?.showDialog("Error",
                                    "Autenticación fallida, no fue posible cambiar el password",
                                    SweetAlertDialog.ERROR_TYPE)
                            } else {
                                this.messageDialog?.showDialog("",
                                    "Password actualizado correctamente",
                                    SweetAlertDialog.SUCCESS_TYPE)
                                view.oldPassword_editText_editPerfil.setText("")
                                view.password_editText_editPerfil.setText("")
                                view.rePassword_editText_editPerfil.setText("")
                            }
                        }.addOnFailureListener {error ->

                            this.messageDialog?.showDialog("Error",
                                "Autenticación fallida, ${error.message}",
                                SweetAlertDialog.ERROR_TYPE)
                        }
                } else {
                    loadingDialog?.dismissDialog()

                    this.messageDialog?.showDialog("Error",
                        "Autenticación fallida, no fue posible cambiar el password",
                        SweetAlertDialog.ERROR_TYPE)
                }
            }
    }

    private fun validateEdit(view: View) : Boolean {
        var result = true

        if (view.name_editText_editPerfil.text.toString().isEmpty()) {
            view.name_editText_editPerfil.error = "Nombre es obligatorio"
            result = false
        }

        if (view.lastName_editText_editPerfil.text.toString().isEmpty()) {
            view.lastName_editText_editPerfil.error = "Apellido es obligatorio"
            result = false
        }

        val oldPassword = view.oldPassword_editText_editPerfil.text.toString()
        val newPassword = view.password_editText_editPerfil.text.toString()
        val rePassword = view.rePassword_editText_editPerfil.text.toString()

        if (oldPassword.isNotEmpty()) {
            if (newPassword.isEmpty()) {
                view.password_editText_editPerfil.error = "Debe ingresar el nuevo password"
                result = false
            }
        }

        if (newPassword.isNotEmpty()) {
            if (oldPassword.isEmpty()) {
                view.oldPassword_editText_editPerfil.error = "Debe ingresar el actual password"
                result = false
            }

            if (newPassword.length <= 5) {
                this.messageDialog?.showDialog("Error",
                    "El nuevo password debe tener un largo minimo de 5 caracteres",
                    SweetAlertDialog.ERROR_TYPE)
                result = false
            } else if (newPassword != rePassword) {
                this.messageDialog?.showDialog("Error",
                    "El nuevo password no coincide en la confirmación",
                    SweetAlertDialog.ERROR_TYPE)
                result = false
            }
        }

        if (rePassword.isNotEmpty()) {
            if (newPassword.isEmpty()) {
                view.password_editText_editPerfil.error = "Debe ingresar el nuevo password"
                result = false
            }
        }

        return result
    }

}
