package com.benbafel.prototipospots

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.benbafel.prototipospots.databinding.ActivityLoginBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope


private const val TAG = "LoginActivity"
const val EXTRA_USER_MAIL = "EXTRA_USER_MAIL"
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var  fusedLocationProviderClient: FusedLocationProviderClient
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        emailTextChange()
        registerButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
        loginButton.setOnClickListener {
            when (viewsAreFilled()){
                true ->{
                    if (ActivityCompat
                            .checkSelfPermission(this, android.Manifest.permission
                                .ACCESS_FINE_LOCATION) != PackageManager
                            .PERMISSION_GRANTED && ActivityCompat
                            .checkSelfPermission(this,android.Manifest
                                .permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"Debe dar permisos de ubicaci??n a la aplicaci??n",
                                Toast.LENGTH_LONG).show()
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
                        return@setOnClickListener
                    }
                    val userEmail = etLoginEmail.text.trim().toString()
                    val userPassword = etLoginPassword.text.toString()
                    Firebase.auth.signInWithEmailAndPassword(userEmail,userPassword)
                        .addOnCompleteListener{ task ->
                            if(task.isSuccessful){
                                if(Firebase.auth.currentUser!!.isEmailVerified){
                                    intent = Intent(this,MapsActivity::class.java)
                                    intent.putExtra(EXTRA_USER_MAIL,userEmail)
                                    startActivity(intent)
                                    finish()
                                }else{
                                    val currentUser = Firebase.auth.currentUser
                                    val dialog = AlertDialog
                                        .Builder(this)
                                        .setTitle("Verificaci??n de correo")
                                        .setMessage("Para poder ingresar, debe verificar su correo " +
                                                "electronico. Se le ha enviado un link de verificaci??n" +
                                                " automaticamente al registrarse.\n\n" +
                                                "Si no lo encuentra, revise la carpeta de Spam en su correo.\n" +
                                                "Si el link de verificaci??n ha caducado, haga click en \"Reenviar verificaci??n\"")
                                        .setNegativeButton("Reenviar verificaci??n",null)
                                        .setPositiveButton("Aceptar",null)
                                        .show()
                                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
                                        currentUser!!.sendEmailVerification()
                                            dialog.setMessage("Se ha reenviado su link de verificaci??n")
                                        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
                                    }
                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                                        dialog.dismiss()
                                    }
                                }
                            }

                        }
                        .addOnFailureListener{ exception ->
                            Log.d(TAG, "Error in login ", exception)
                            Toast.makeText(this,"el correo o la contrase??a son incorrectos",Toast.LENGTH_LONG)
                                .show()
                        }
                }
                false ->{
                    Toast.makeText(this,"Debe llenar todos los datos",Toast.LENGTH_LONG)
                        .show()
                }

            }
        }

    }
    private fun emailTextChange() {
        etLoginEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!validateEmailAddress(s.toString())){
                    etLoginEmail.error = "Formato incorrecto!"
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun validateEmailAddress(email:String):Boolean{
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun viewsAreFilled(): Boolean {
        return etLoginEmail.text.isNotEmpty() && etLoginPassword.text.isNotEmpty()
    }
}