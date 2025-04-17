package cit.edu.WildcatFreshFinds

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LandingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN


        val loginNavButton = findViewById<Button>(R.id.landing_login_button)
        val registerNavButton = findViewById<Button>(R.id.landing_register_button)

        loginNavButton.setOnClickListener {
            Log.e("Button Click", "Login Navigation Button clicked")
            Toast.makeText(this, "Login", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        registerNavButton.setOnClickListener {
            Log.e("Button Click", "Register Navigation Button clicked")
            Toast.makeText(this, "Register", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}