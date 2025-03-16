package cit.edu.WildcatFreshFinds

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var uniformButton: Button
    private lateinit var bookButton: Button
    private lateinit var allButton: Button
    private var currentButton: Button? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fullName = view.findViewById<TextView>(R.id.full_name_label)
        val userName = " " + (UserManager.getSignedIn()?.fullName ?: "Guest")
        fullName.text = userName


        val txt = view.findViewById<TextView>(R.id.no_items_txt)

        allButton = view.findViewById<Button>(R.id.all_category_button)
        uniformButton = view.findViewById<Button>(R.id.uniform_category_button)
        bookButton = view.findViewById<Button>(R.id.book_category_button)
        if(savedInstanceState == null) {
            currentButton = allButton
        }

        allButton.setOnClickListener {
            Log.e("Button Clicked!", "All Category Button Clicked")
            showToast("All Category Button")


            txt.setText("There are no items for sale yet");
            updateButtonState(allButton)
        }

        uniformButton.setOnClickListener {
            Log.e("Button Clicked!", "Uniform Category Button Clicked")
            showToast("Uniform Category Button")

            txt.setText("There are no uniforms for sale yet");
            updateButtonState(uniformButton)
        }

        bookButton.setOnClickListener {
            Log.e("Button Clicked!", "Book Category Button Clicked")
            showToast("Book Category Button")

            txt.setText("There are no books for sale yet");
            updateButtonState(bookButton)
        }


    }

    private fun updateButtonState(newActiveButton: Button) {
        currentButton?.let { setInactiveButton(it) }
        setActiveButton(newActiveButton)
        currentButton = newActiveButton
    }

    private fun setActiveButton(button: Button) {
        button.setBackgroundResource(R.drawable.category_active)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold))
    }

    private fun setInactiveButton(button: Button) {
        button.setBackgroundResource(R.drawable.category_inactive)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.maroon))
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
