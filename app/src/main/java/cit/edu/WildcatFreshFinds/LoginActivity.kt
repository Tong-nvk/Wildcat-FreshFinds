package cit.edu.WildcatFreshFinds

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        val emailField = findViewById<EditText>(R.id.email_input)
        val passwordField = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)
        val registerButton = findViewById<TextView>(R.id.sign_up_click)

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if(email.isBlank()) {
                showToast("Email field is empty.")
                return@setOnClickListener
            }

            if(password.isBlank()) {
                showToast("Password field is empty.")
                return@setOnClickListener
            }

            if(!UserManager.loginUser(email, password)) {
                showToast("Email and Password do not match.")
                return@setOnClickListener
            }

            Log.e("Button Click", "Login Button Clicked")
            showToast("Login Successful!")

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        registerButton.setOnClickListener {
            Log.e("Button Click", "Register Navigation Button Clicked")
            showToast("Register")

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showToast(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}