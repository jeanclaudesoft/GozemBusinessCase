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

class LoginActivity : AppCompatActivity() {

    private lateinit var tvRegister: TextView
    private lateinit var loginButton: Button
    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tvRegister = findViewById(R.id.tvRegister)
        loginButton = findViewById(R.id.loginBtn)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)

        tvRegister.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        loginButton.setOnClickListener {
            if (edtEmail.text.toString().isBlank()) {
                edtEmail.error = "The email field is required"
                Toast.makeText(this, "The email field is required", Toast.LENGTH_SHORT).show()
            } else if (!edtEmail.text.toString().contains("@")) {
                edtEmail.error = "Please enter a valid email address"
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT)
                    .show()
            } else if (edtPassword.text.toString().isBlank()) {
                edtPassword.error = "The password field is required"
                Toast.makeText(this, "The password field is required", Toast.LENGTH_SHORT).show()
            } else if (edtPassword.text.toString().length < 8) {
                edtPassword.error = "The password must be at least 8 character"
                Toast.makeText(
                    this, "The password must be at least 8 character", Toast.LENGTH_SHORT
                ).show()
            } else {

                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Wait...")
                progressDialog.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                    Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }, 2000)
            }

        }

    }
}