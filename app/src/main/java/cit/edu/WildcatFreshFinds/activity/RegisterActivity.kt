package cit.edu.WildcatFreshFinds.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cit.edu.WildcatFreshFinds.R
import cit.edu.WildcatFreshFinds.utility.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

        val firstNameField = findViewById<EditText>(R.id.first_name_input)
        val lastNameField = findViewById<EditText>(R.id.last_name_input)
        val emailField = findViewById<EditText>(R.id.email_input)
        val passwordField = findViewById<EditText>(R.id.password_input)
        val confirmPasswordField = findViewById<EditText>(R.id.confirm_password_input)
        val registerButton = findViewById<Button>(R.id.register_button)
        val loginButton = findViewById<TextView>(R.id.sign_in_click)
        val commGuidelinesButton = findViewById<TextView>(R.id.tv_community_guidelines)
        val checkBox = findViewById<CheckBox>(R.id.cb_community_guidelines)

        try {
            UserManager.initialize(this)
        } catch (e: IllegalStateException) {
            Log.e("RegisterActivity", "UserManager initialization failed potentially.", e)
            showToast("Initialization error. Please restart the app.")
            registerButton.isEnabled = false
            return
        }

        registerButton.setOnClickListener {

            val firstName = firstNameField.text.toString().trim()
            val lastName = lastNameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()


            if (email.isBlank()) {
                showToast("Email field is empty.")
                return@setOnClickListener
            }
            if (!isValidEmail(email)) {
                showToast("Email is invalid.")
                return@setOnClickListener
            }
            if (!email.contains("@cit.edu")) { // Keep CIT email check
                showToast("Please use departmental email.")
                return@setOnClickListener
            }
            if (firstName.isBlank()) {
                showToast("First Name field is empty.")
                return@setOnClickListener
            }
            if (lastName.isBlank()) {
                showToast("Last Name field is empty.")
                return@setOnClickListener
            }
            if (password.isBlank()) {
                showToast("Password field is empty.")
                return@setOnClickListener
            }
            if (!isValidPassword(password)) {
                showToast("Password must contain at least 8 characters.")
                return@setOnClickListener
            }
            if (confirmPassword.isBlank()) {
                showToast("Confirm Password field is empty.")
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                showToast("Passwords do not match.")
                return@setOnClickListener
            }
            if (!checkBox.isChecked) {
                showToast("Please agree to the Community Guidelines")
                return@setOnClickListener
            }


            lifecycleScope.launch {
                val resultMessage = UserManager.registerUser(
                    firstName,
                    lastName,
                    email,
                    password
                ) // <-- Pass separate names

                withContext(Dispatchers.Main) {
                    showToast(resultMessage)
                    if (resultMessage == "Registration Successful!") {
                        Log.d("RegisterActivity", "Registration success for $email. Navigating to Login.")
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)

                        startActivity(intent)
                    } else {
                        Log.w("RegisterActivity", "Registration failed/user exists for $email. Message: $resultMessage")
                    }
                }

                try {
                    val users = withContext(Dispatchers.IO) { UserManager.getUsers() }
                    Log.d("RegisterActivity", "Current Users in DB: $users")
                } catch (e: Exception) {
                    Log.e("RegisterActivity", "Error fetching users after registration", e)
                }
            }
        }


        loginButton.setOnClickListener {
            Log.d("RegisterActivity", "Login Navigation Button Clicked")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        commGuidelinesButton.setOnClickListener {
            Log.d("RegisterActivity", "Community Guidelines Navigation Button Clicked")
            val intent = Intent(this, CommunityGuidelinesActivity::class.java)
            startActivity(intent)
        }

    }


    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}