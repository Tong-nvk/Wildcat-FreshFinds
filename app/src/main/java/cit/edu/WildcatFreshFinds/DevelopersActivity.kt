package cit.edu.WildcatFreshFinds

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DevelopersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_developers)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

        val backButton = findViewById<ImageView>(R.id.back_icon)
        backButton.setOnClickListener {
            finish()

            Log.e("Button Clicked", "Back Button Navigation Button Clicked")
            showToast("Settings")

        }
    }

    private fun showToast(message: String)  {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}