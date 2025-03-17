package cit.edu.WildcatFreshFinds

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cit.edu.WildcatFreshFinds.R

class CommunityGuidelinesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_guidelines)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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