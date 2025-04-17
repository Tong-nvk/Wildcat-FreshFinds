package cit.edu.WildcatFreshFinds

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

        val firstNameField = findViewById<EditText>(R.id.first_name_input) // <-- Add this
        val lastNameField = findViewById<EditText>(R.id.last_name_input)   // <-- Add this
        val emailField = findViewById<EditText>(R.id.email_input)
        val passwordField = findViewById<EditText>(R.id.password_input)
        val confirmPasswordField = findViewById<EditText>(R.id.confirm_password_input)
        val registerButton = findViewById<Button>(R.id.register_button)
        val loginButton = findViewById<TextView>(R.id.sign_in_click)
        val commGuidelinesButton = findViewById<TextView>(R.id.tv_community_guidelines)
        val checkBox = findViewById<CheckBox>(R.id.cb_community_guidelines) // Find Checkbox here is fine

        try {
            UserManager.initialize(this)
        } catch (e: IllegalStateException) {
            // Handle cases where initialization might fail or context is invalid, though unlikely here
            Log.e("RegisterActivity", "UserManager initialization failed potentially.", e)
            showToast("Initialization error. Please restart the app.")
            // Potentially finish the activity or disable the button
            registerButton.isEnabled = false
            return // Exit onCreate early if initialization fails critically
        }

        registerButton.setOnClickListener {
            // --- Get Input Text ---
            // val fullName = fullNameField.text.toString().trim() // <-- Remove this
            val firstName = firstNameField.text.toString().trim() // <-- Add this
            val lastName = lastNameField.text.toString().trim()   // <-- Add this
            val email = emailField.text.toString().trim()         // <-- Add trim()
            val password = passwordField.text.toString()        // No trim for password
            val confirmPassword = confirmPasswordField.text.toString() // No trim for password
            // val checkBox = findViewById<CheckBox>(R.id.cb_community_guidelines) // Already found above
            // --- End Get Input Text ---


            // --- Validation ---
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
            // Replace full name check with first/last name checks
            if (firstName.isBlank()) {
                showToast("First Name field is empty.")
                return@setOnClickListener
            }
            if (lastName.isBlank()) {
                showToast("Last Name field is empty.")
                return@setOnClickListener
            }
            // Keep password checks
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
            // --- End Validation ---


            // --- Call Register User ---
            lifecycleScope.launch {
                // Call updated UserManager function
                val resultMessage = UserManager.registerUser(firstName, lastName, email, password) // <-- Pass separate names

                // Use main thread for UI updates like Toast
                withContext(Dispatchers.Main) {
                    showToast(resultMessage)
                    // Only navigate if registration was truly successful
                    if (resultMessage == "Registration Successful!") {
                        Log.d("RegisterActivity", "Registration success for $email. Navigating to Login.")
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        // Optional: Add flags to clear previous activities if needed
                        // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        // finish() // Optional: finish RegisterActivity so user can't go back
                    } else {
                        Log.w("RegisterActivity", "Registration failed/user exists for $email. Message: $resultMessage")
                    }
                }

                // Logging users is fine for debug, keep it if needed
                try {
                    val users = withContext(Dispatchers.IO) { UserManager.getUsers() }
                    Log.d("RegisterActivity", "Current Users in DB: $users")
                } catch (e: Exception) {
                    Log.e("RegisterActivity", "Error fetching users after registration", e)
                }
            }
            // --- End Call Register User ---
        } // End registerButton listener


        // --- Keep other listeners ---
        loginButton.setOnClickListener {
            Log.d("RegisterActivity", "Login Navigation Button Clicked") // Use Log.d or Log.i for navigation
            // showToast("Login") // Toast is optional if navigating immediately
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        commGuidelinesButton.setOnClickListener {
            Log.d("RegisterActivity", "Community Guidelines Navigation Button Clicked")
            // showToast("Community Guidelines")
            val intent = Intent(this, CommunityGuidelinesActivity::class.java)
            startActivity(intent)
        }
        // --- End other listeners ---

    } // End onCreate


    // --- Keep helper functions ---
    private fun isValidEmail(email: String): Boolean {
        // Consider adding trim() here as well for safety, though done above now
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    // --- End helper functions ---

}