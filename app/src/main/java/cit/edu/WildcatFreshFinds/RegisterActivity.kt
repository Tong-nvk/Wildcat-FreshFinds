package cit.edu.WildcatFreshFinds

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cit.edu.WildcatFreshFinds.UserManager
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

        val fullNameField = findViewById<EditText>(R.id.full_name_input)
        val emailField = findViewById<EditText>(R.id.email_input)
        val passwordField = findViewById<EditText>(R.id.password_input)
        val confirmPasswordField = findViewById<EditText>(R.id.confirm_password_input)
        val registerButton = findViewById<Button>(R.id.register_button)
        val loginButton = findViewById<TextView>(R.id.sign_in_click)

        registerButton.setOnClickListener {
            val fullName = fullNameField.text.toString();
            val email = emailField.text.toString();
            val password = passwordField.text.toString();
            val confirmPassword = confirmPasswordField.text.toString();

            if(email.isBlank()) {
                showToast("Email field is empty.")
                return@setOnClickListener
            }

            if(!isValidEmail(email)) {
                showToast("Email is invalid.")
                return@setOnClickListener
            }

            if(fullName.isBlank()) {
                showToast("Full Name field is empty.")
                return@setOnClickListener
            }

            if(password.isBlank()) {
                showToast("Password field is empty.")
                return@setOnClickListener
            }

            if(!isValidPassword(password)) {
                showToast("Password must contain at least 8 characters.")
                return@setOnClickListener

            }

            if(confirmPassword.isBlank()) {
                showToast("Confirm Password field is empty.")
                return@setOnClickListener
            }

            if(password != confirmPassword) {
                showToast("Passwords do not match.")
                return@setOnClickListener

            }

            val resultMessage = UserManager.registerUser(fullName, email, password);
            showToast(resultMessage);

            println(UserManager.getUsers());
         }

        loginButton.setOnClickListener {
            Log.e("Button Click", "Login Navigation Button Clicked");
            showToast("Login");

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isValidEmail(email : String) : Boolean {
        return email.isNotEmpty()  && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private fun isValidPassword(password : String) : Boolean {
        return password.length >= 8;
    }

    private fun showToast(message: String)  {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}