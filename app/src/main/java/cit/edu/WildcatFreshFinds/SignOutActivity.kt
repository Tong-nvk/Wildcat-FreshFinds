//package cit.edu.WildcatFreshFinds
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//class SignOutActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_sign_out)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        val log_out_button = findViewById<Button>(R.id.log_out_button)
//        val cancel_button = findViewById<Button>(R.id.cancel_button)
//
//        cancel_button.setOnClickListener {
//            Log.e("Button Click", "Cancel Button Navigation Clicked")
//            showToast("Settings")
//
//            val intent = Intent(this, SettingsActivity::class.java)
//            startActivity(intent)
//        }
//
//        log_out_button.setOnClickListener {

//        }
//
//    }
//
//    private fun showToast(message: String)  {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//}