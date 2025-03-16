package cit.edu.WildcatFreshFinds

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editButton = view.findViewById<Button>(R.id.edit_button)
        editButton.bringToFront()

        val fullName = view.findViewById<TextView>(R.id.user_name);
        val email = view.findViewById<TextView>(R.id.email_value);

        fullName.text = UserManager.getSignedIn()?.fullName
        email.text = UserManager.getSignedIn()?.email

        editButton.setOnClickListener {
            showCustomDialogBox()
        }

        val settingsContainer: CardView = view.findViewById(R.id.settings_container)

        settingsContainer.setOnClickListener {
            Log.e("Button Click", "Settings Navigation Button Clicked")
            showToast("Settings")

            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }


    }



    fun showCustomDialogBox() {
        val dialog = Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_edit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)
        val editButton = dialog.findViewById<Button>(R.id.edit_button)

        val etName = dialog.findViewById<EditText>(R.id.et_name)
        val etPassword = dialog.findViewById<EditText>(R.id.et_password)
        etName.setText(UserManager.getSignedIn()?.fullName)
        etPassword.setText(UserManager.getSignedIn()?.password)



        editButton.setOnClickListener {
            val name = etName.text.toString()
            val password = etPassword.text.toString()

            if(name.isBlank() || password.isBlank()) {
                showToast("The inputs are empty.")
                return@setOnClickListener
            }

            if(name == UserManager.getSignedIn()?.fullName && password == UserManager.getSignedIn()?.password) {
                showToast("The data was not edited.")
                return@setOnClickListener
            }

            Log.e("Button Click", "Edit Button Clicked")
            showToast("Edit Successful!")

            UserManager.editUser(name, password)
            dialog.dismiss()

            view?.findViewById<TextView>(R.id.user_name)?.text = UserManager.getSignedIn()?.fullName
            view?.findViewById<TextView>(R.id.email_value)?.text = UserManager.getSignedIn()?.email

        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun showToast(message : String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


}