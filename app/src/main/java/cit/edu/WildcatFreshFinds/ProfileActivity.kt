package cit.edu.WildcatFreshFinds

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

        val editButton = findViewById<Button>(R.id.edit_button)
        editButton.bringToFront()

        val fullName = findViewById<TextView>(R.id.user_name);
        val email = findViewById<TextView>(R.id.email_value);
        val password = findViewById<TextView>(R.id.password_value);

        fullName.text = UserManager.getSignedIn()?.fullName
        email.text = UserManager.getSignedIn()?.email

        val homeButton = findViewById<ImageView>(R.id.home_nav_icon)

        homeButton.setOnClickListener {
            Log.e("Button Click", "Home Navigation Button Clicked");
            showToast("Home");

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        val settingIcon = findViewById<ImageView>(R.id.settings_icon)
        val settingContainer = findViewById<View>(R.id.settings_container)
        val settingLabel = findViewById<TextView>(R.id.settings_label)

        settingIcon.setOnClickListener {
            settingIntent()
        }
        settingContainer.setOnClickListener {
            settingIntent()
        }

        settingLabel.setOnClickListener {
            settingIntent()
        }

    }

    private fun settingIntent() {
        Log.e("Button Click", "Setting Navigation Button Clicked")
        showToast("Settings")

        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    private fun showToast(message: String)  {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}