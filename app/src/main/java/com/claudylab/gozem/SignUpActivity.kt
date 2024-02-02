package com.claudylab.gozem

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {

    private lateinit var tvLogin: TextView
    private lateinit var signUpButton: Button
    private lateinit var edtName: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        tvLogin = findViewById(R.id.tvLogin)
        signUpButton = findViewById(R.id.signUpBtn)
        edtName = findViewById(R.id.edtName)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        signUpButton.setOnClickListener {
            if (edtName.text.toString().isBlank()) {
                edtName.error = "Please write your full name"
                Toast.makeText(this, "Please write your full name", Toast.LENGTH_SHORT).show()
            } else if (edtEmail.text.toString().isBlank()) {
                edtEmail.error = "Please write your email address"
                Toast.makeText(this, "Please write your email address", Toast.LENGTH_SHORT).show()
            } else if (!edtEmail.text.toString().contains("@")) {
                edtEmail.error = "Please enter a valid  email address"
                Toast.makeText(this, "Please enter a valid  email address", Toast.LENGTH_SHORT)
                    .show()
            } else if (edtPassword.text.toString().isBlank()) {
                edtPassword.error = "Please enter a password"
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            } else if (edtPassword.text.toString().length < 8) {
                edtPassword.error = "The password must be at least 8 character"
                Toast.makeText(
                    this,
                    "The password must be at least 8 character",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Creating account...")
                progressDialog.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                    Toast.makeText(this, "Signup Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }, 2000)
            }
        }
    }
}