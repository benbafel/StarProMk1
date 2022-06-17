package com.benbafel.prototipospots

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import com.benbafel.prototipospots.databinding.ActivityRegisterBinding
import com.benbafel.prototipospots.models.Comment
import com.benbafel.prototipospots.models.SmallUser
import com.benbafel.prototipospots.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.view.*
import java.util.*

private const val TAG = "RegisterActivity"
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var users = mutableListOf<SmallUser>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        title = "Registro de cuenta"
        setContentView(binding.root)
        val db = Firebase.firestore
        getUsers(db)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        passwordTextChange()
        usernameTextChange()
        emailTextChange()
        btnRegister.setOnClickListener{
            when(viewsAreFilled()){
                true ->{
                    val userName = etUserName.text.trim().toString()
                    val userMail = etEmail.text.trim().toString()
                    if(validateEmailAddress(userMail) && !userExistsInDatabase(userName)
                        && !emailExistsInDatabase(userMail) && validatePassword()){
                        Log.i(TAG,"todo bien")
                        crearCuenta(db)


                    }else{
                        Toast.makeText(this,"Se deben resolver todas las advertencias para poder continuar",Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                }
                false ->{
                    Toast.makeText(this,"No se han llenado todos los campos",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
        }

    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    private fun crearCuenta(db: FirebaseFirestore) {

        val name = etUserName.text.trim().toString()
        val email = (etEmail.text.trim().toString()).lowercase()
        val areaOfInterest = setInterests()
        val expertise = setExpertise()
        val user = User(name,email,areaOfInterest,expertise)

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email,etPassword.text.toString())
            .addOnCompleteListener{
                if(it.isSuccessful){
                    val currentUser = Firebase.auth.currentUser
                    addUserToFirebase(db.collection("users"),email,user)
                    currentUser!!.sendEmailVerification()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.i(TAG, "Email sent.")
                                val dialog = AlertDialog
                                    .Builder(this)
                                    .setTitle("Su cuenta ha sido creada!")
                                    .setMessage("Se ha creado su cuenta.\n" +
                                            "recuerde revisar su bandeja de entrada para poder realizar" +
                                            " la verificación de su correo antes de poder ingresar a la" +
                                            " aplicación")
                                    .setPositiveButton("Ok",null)
                                    .show()
                                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                                    finish()
                                }
                            }else{
                                Toast
                                    .makeText(this, task.exception?.message ?: "",Toast.LENGTH_LONG)
                                    .show()
                            }
                        }


                }else{
                    showAlert()
                }
            }




    }

    private fun addUserToFirebase(collection: CollectionReference, email: String, user: User) {
        collection.document(email)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")

            }
            .addOnFailureListener{exception ->
                Log.d(TAG, "Error uploading documents: ", exception)
            }
    }

    private fun showAlert() {
        val builder = AlertDialog
            .Builder(this)
            .setTitle("Error")
            .setMessage("Se ha producido un error registrando al usuario")
            .setPositiveButton("Ok",null)
        val dialog = builder.create()
        dialog.show()
    }
/**/
    private fun setExpertise(): String {
        val rbId = radioGroup.checkedRadioButtonId
        return when (resources.getResourceEntryName(rbId)) {
            "rbAficionado" -> {
                getString(R.string.aficionado)
            }
            "rbIntermedio" -> {
                getString(R.string.intermedio)
            }
            else -> {
                getString(R.string.avanzado)
            }
        }
    }

    private fun setInterests(): Int {
        return when{
            cbAstrofoto.isChecked && !cbAstronomia.isChecked ->{
                1
            }
            !cbAstrofoto.isChecked && cbAstronomia.isChecked ->{
                2
            }
            else ->{
                3
            }
        }
    }

    private fun emailTextChange() {
        etEmail.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!validateEmailAddress(s.toString())){
                    etEmail.error = "Formato incorrecto!"
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if(emailExistsInDatabase(s.toString())){
                    etEmail.error = "ya existe un usuario con este Correo!"
                }
            }

        })
    }

    private fun usernameTextChange() {
        etUserName.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if(userExistsInDatabase(s?.trim().toString())){
                    etUserName.error = "ya existe un usuario con este nombre!"
                }
            }

        })
    }

    private fun validatePassword(): Boolean {
        val pass1 = etPassword.text.toString()
        val pass2 = etConfirmPassword.text.toString()
        return pass1 == pass2
    }

    private fun passwordTextChange() {
        etPassword.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!validatePassword() && etConfirmPassword.text.isNotEmpty()){
                    etConfirmPassword.error = "Debe calzar con la contraseña"
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if(etPassword.text.length < 6){
                    etPassword.error = "Contraseña debe tener al menos 6 caracteres"
                }
            }

        })
        etConfirmPassword.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if(!validatePassword()){
                    etConfirmPassword.error = "Debe calzar con la contraseña"
                }
            }

        })
    }

    private fun getUsers(db: FirebaseFirestore) {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val docData = document.data
                    val userName = docData["name"] as String
                    val userMail = docData["email"] as String
                    val user = SmallUser(userName,userMail)
                    Log.i(TAG,"usuario ${user.toString()}")
                    users.add(user)
                }
                Log.i(TAG,"se lleno la lista, con los datos ${users.toString()}")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    private fun viewsAreFilled(): Boolean {
        return (cbAstrofoto.isChecked || cbAstronomia.isChecked) &&
                radioGroup.checkedRadioButtonId != -1 && etEmail.text.trim().isNotEmpty() &&
                etUserName.text.trim().isNotEmpty() && etPassword.text.trim().isNotEmpty() &&
                etConfirmPassword.text.trim().isNotEmpty()
    }

    private fun validateEmailAddress(email:String):Boolean{
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun userExistsInDatabase(userName: String): Boolean {
        for(user in users) {
            if (user.name.equals(userName,ignoreCase = true)) {

                return true
            }
        }
        return false
    }
    private fun emailExistsInDatabase(userMail: String): Boolean {
        for(user in users) {
            if (user.email.equals(userMail,ignoreCase = true)) {

                return true
            }
        }
        return false
    }
}