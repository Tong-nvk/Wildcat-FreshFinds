package cit.edu.WildcatFreshFinds

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

        val homeFragment = HomeFragment()
        val profileFragment = ProfileFragment()
        val transactionFragment = TransactionFragment()
        var currentButton: ImageButton? = null
        val homeButton = findViewById<ImageButton>(R.id.home_button)
        val transactionButton = findViewById<ImageButton>(R.id.transaction_button)
        val profileButton = findViewById<ImageButton>(R.id.profile_button)

        if (savedInstanceState == null) {
            replaceFragment(homeFragment)
            currentButton = homeButton
            homeButton.setImageResource(R.drawable.home_active)
        }

        homeButton.setOnClickListener {
            Log.e("Button Click", "Home Navigation Button Clicked!")

            replaceFragment(homeFragment)
            currentButton?.let { navSetInactive(it) }
            currentButton = homeButton
            homeButton.setImageResource(R.drawable.home_active)
        }

        profileButton.setOnClickListener {
            Log.e("Button Click", "Profile Navigation Button Clicked!")

            replaceFragment(profileFragment)
            currentButton?.let { navSetInactive(it) }
            currentButton = profileButton
            profileButton.setImageResource(R.drawable.user_active)
        }

        transactionButton.setOnClickListener {
            Log.e("Button Click", "Transaction Navigation Button Clicked!")

            replaceFragment(transactionFragment)
            currentButton?.let { navSetInactive(it) }
            currentButton = transactionButton
            transactionButton.setImageResource(R.drawable.transaction_active)
        }
    }

    private fun navSetInactive(imgButton: ImageButton) {
        when (imgButton.id) {
            R.id.home_button -> imgButton.setImageResource(R.drawable.home_inactive)
            R.id.transaction_button -> imgButton.setImageResource(R.drawable.transaction_inactive)
            R.id.profile_button -> imgButton.setImageResource(R.drawable.user_inactive)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_fragment_container, fragment)
            commit()
        }
    }


}

