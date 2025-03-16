package cit.edu.WildcatFreshFinds

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

        val logoutButton = findViewById<CardView>(R.id.log_out_container)
        logoutButton.setOnClickListener {
            Log.e("Button Clicked", "Log Out Navigation Button Clicked")
            showToast("Log Out")

//            val intent = Intent(this, SignOutActivity::class.java)
//            startActivity(intent)
        }

        val backButton = findViewById<ImageView>(R.id.back_icon)
        backButton.setOnClickListener {
            Log.e("Button Clicked", "Back Button Navigation Button Clicked")
            showToast("Profile")

            finish()
        }

        val developersButton = findViewById<CardView>(R.id.developer_container)
        developersButton.setOnClickListener {
            Log.e("Button Clicked", "Developer Button Navigation Button Clicked")
            showToast("Developer")

//            val intent = Intent(this, DevelopersActivity::class.java)
//            startActivity(intent)
        }
    }

    private fun showToast(message: String)  {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}