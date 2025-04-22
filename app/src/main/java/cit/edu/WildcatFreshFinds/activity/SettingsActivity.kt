package cit.edu.WildcatFreshFinds.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import cit.edu.WildcatFreshFinds.R
import cit.edu.WildcatFreshFinds.utility.UserManager

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

            showCustomDialogBox()

        }

        val backButton = findViewById<ImageView>(R.id.back_icon)
        backButton.setOnClickListener {
            Log.e("Button Clicked", "Back Button Navigation Button Clicked")

            finish()
        }

        val developersButton = findViewById<CardView>(R.id.developer_container)
        developersButton.setOnClickListener {
            Log.e("Button Clicked", "Developer Button Navigation Button Clicked")

            val intent = Intent(this, DevelopersActivity::class.java)
            startActivity(intent)
        }

        val communityGuidelinesButton = findViewById<CardView>(R.id.community_guidelines_container)
        communityGuidelinesButton.setOnClickListener {
            Log.e("Button Clicked", "Community Guidelines Button Navigation Button Clicked")

            val intent = Intent(this, CommunityGuidelinesActivity::class.java)
            startActivity(intent)
        }
        val allSellingItemsButton = findViewById<CardView>(R.id.all_selling_items_container)
        allSellingItemsButton.setOnClickListener {
            Log.e("Button Clicked", "All Selling Items Navigation Button Clicked ")

            val intent = Intent(this, MyListingsActivity::class.java)
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
            Log.d("SettingsActivity", "User signed out.")

            val intent = Intent(this, LandingActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)


            finish()
            Log.d("SettingsActivity", "Navigating to LandingActivity and finishing SettingsActivity.")

        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

}