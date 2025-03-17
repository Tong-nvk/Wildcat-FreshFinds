package cit.edu.WildcatFreshFinds

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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

            showCustomDialogBox()

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

            val intent = Intent(this, DevelopersActivity::class.java)
            startActivity(intent)
        }

        val communityGuidelinesButton = findViewById<CardView>(R.id.community_guidelines_container)
        communityGuidelinesButton.setOnClickListener {
            Log.e("Button Clicked", "Community Guidelines Button Navigation Button Clicked")
            showToast("Community Guidelines")

            val intent = Intent(this, CommunityGuidelinesActivity::class.java)
            startActivity(intent)
        }


    }

    fun showCustomDialogBox() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_sign_out)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)
        val logOutButton = dialog.findViewById<Button>(R.id.log_out_button)


        logOutButton.setOnClickListener {
            UserManager.signOut()

            Log.e("Button Click", "Log Out Button Navigation Clicked")
            showToast("Landing")
            finish()
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)

        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun showToast(message: String)  {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}