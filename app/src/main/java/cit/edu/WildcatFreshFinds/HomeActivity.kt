package cit.edu.WildcatFreshFinds

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN


        val profileButton = findViewById<ImageView>(R.id.profile_nav_icon)

        profileButton.setOnClickListener {
            Log.e("Button Click", "Profile Navigation Button Clicked!");
            showToast("Profile")

            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent);
        }

        val fullName = findViewById<TextView>(R.id.full_name_label)
        val userName = " " + UserManager.getSignedIn()?.fullName
        fullName.text = userName



    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}