package cit.edu.WildcatFreshFinds

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class HomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN


        val homeFragment = HomeFragment();
        val profileFragment = ProfileFragment();
        val transactionFragment = TransactionFragment();
        var currentButton: ImageButton? = null
        val homeButton = findViewById<ImageButton>(R.id.home_button);
        val transactionButton = findViewById<ImageButton>(R.id.transaction_button);
        val profileButton = findViewById<ImageButton>(R.id.profile_button);



        if (savedInstanceState == null) {
            replaceFragment(homeFragment)
            currentButton = homeButton;
            homeButton.setImageResource(R.drawable.home_active)
        }

        homeButton.setOnClickListener {
            Log.e("Button Click", "Home Navigation Button Clicked!")
            showToast("Home")

            replaceFragment(homeFragment)
            currentButton?.let { navSetInactive(it) };
            currentButton = homeButton;
            homeButton.setImageResource(R.drawable.home_active)
        }

        profileButton.setOnClickListener {
            Log.e("Button Click", "Profile Navigation Button Clicked!")
            showToast("Profile")

            replaceFragment(profileFragment)
            currentButton?.let { navSetInactive(it) };
            currentButton = profileButton;
            profileButton.setImageResource(R.drawable.user_active)
        }

        transactionButton.setOnClickListener {
            Log.e("Button Click", "Transaction Navigation Button Clicked!")
            showToast("Transaction")

            replaceFragment(transactionFragment)
            currentButton?.let { navSetInactive(it) };
            currentButton = transactionButton;
            transactionButton.setImageResource(R.drawable.transaction_active)
        }
    }

    fun navSetInactive(imgButton : ImageButton) {
        if(imgButton.id == R.id.home_button) {
            imgButton.setImageResource(R.drawable.home_inactive)
        } else if(imgButton.id == R.id.transaction_button) {
            imgButton.setImageResource(R.drawable.transaction_inactive)
        } else if (imgButton.id == R.id.profile_button) {
            imgButton.setImageResource(R.drawable.user_inactive)
        }
    }
    fun replaceFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_fragment_container, fragment)
            commit();
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}