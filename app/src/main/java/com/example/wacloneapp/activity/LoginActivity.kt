package com.example.wacloneapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import com.example.wacloneapp.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_login)

        setTextChangedListener(edt_email,til_email)
        setTextChangedListener(edt_password, til_password)
        progress_layout.setOnTouchListener { v, event -> true  }

        btn_login.setOnClickListener {
            onLogin()
        }

        txt_signup.setOnClickListener {
            onRegister()
        }
    }

    private fun setTextChangedListener(edt: EditText, til: TextInputLayout) {
        edt.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                til.isErrorEnabled = false
            }

        })
    }

    private fun onLogin() {
        var procced = true
        if (edt_email.text.isNullOrEmpty()){
            til_email.error = "Required Email"
            til_email.isErrorEnabled = true
            procced = false
        }

        if (edt_password.text.isNullOrEmpty()){
            til_password.error = "Required Email"
            til_password.isErrorEnabled = true
            procced = false
        }


        if (procced){
            progress_layout.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(
                edt_email.text.toString(),
                edt_password.text.toString()
            )

                .addOnCompleteListener { task ->
                    if (!task.isSuccessful){
                        progress_layout.visibility = View.GONE
                        Toast.makeText(this@LoginActivity,
                            "Login error: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {e->
                    progress_layout.visibility = View.GONE
                    e.printStackTrace()
                }

        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    private fun onRegister() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

}