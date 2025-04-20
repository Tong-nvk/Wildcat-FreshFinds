package cit.edu.WildcatFreshFinds

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView // <-- Import ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Group // <-- Import Group
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Remove MaterialToolbar import if not used elsewhere
// import com.google.android.material.appbar.MaterialToolbar

class MyListingsActivity : AppCompatActivity() {

    private val viewModel: MyListingsViewModel by viewModels()
    private lateinit var listingsRecyclerView: RecyclerView
    private lateinit var listingsAdapter: MyListingsAdapter
    // --- Change from TextView to Group ---
    private lateinit var groupEmptyView: Group
    // --- End Change ---
    // --- Remove Toolbar, Add Back Button ---
    // private lateinit var toolbar: MaterialToolbar
    private lateinit var backButton: ImageView
    // --- End Changes ---


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_listings)

        // --- Find Views ---
        // toolbar = findViewById(R.id.my_listings_toolbar) // Removed
        backButton = findViewById(R.id.back_icon) // Find the back ImageView from XML
        listingsRecyclerView = findViewById(R.id.my_listings_recycler_view)
        // Find the Group, not the TextView
        groupEmptyView = findViewById(R.id.group_my_listings_empty) // Use Group ID from XML
        // --- End Find Views ---

        // --- Remove Toolbar Setup ---
        // setSupportActionBar(toolbar)
        // supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // supportActionBar?.setDisplayShowHomeEnabled(true)
        // toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        // --- End Remove ---

        // --- Setup Back Button Click Listener ---
        backButton.setOnClickListener {
            Log.d("MyListingsActivity", "Back button clicked")
            onBackPressedDispatcher.onBackPressed() // Standard way to handle back press
            // Or finish() if that's the desired behavior
        }
        // --- End Back Button Setup ---

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        listingsAdapter = MyListingsAdapter(
            onDeleteClick = { product ->
                showDeleteConfirmation(product)
            }
        )
        listingsRecyclerView.apply {
            adapter = listingsAdapter
            layoutManager = LinearLayoutManager(this@MyListingsActivity)
        }
        Log.d("MyListingsActivity", "RecyclerView setup complete.")
    }

    private fun setupObservers() {
        // Observe listings
        viewModel.userListings.observe(this, Observer { listings ->
            Log.d("MyListingsActivity", "Observed listings: ${listings?.size ?: "null"}")
            listingsAdapter.submitList(listings ?: emptyList())
            updateEmptyView(listings.isNullOrEmpty())
        })

        // Observe toasts
        viewModel.toastMessage.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                showToast(message)
            }
        })
        Log.d("MyListingsActivity", "Observers setup.")
    }

    // --- Updated updateEmptyView function ---
    private fun updateEmptyView(isEmpty: Boolean) {
        Log.d("MyListingsActivity", "Updating empty view visibility: isEmpty=$isEmpty")
        listingsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        // Control the Group's visibility
        groupEmptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
    // --- End Update ---

    // --- showDeleteConfirmation remains the same ---
    private fun showDeleteConfirmation(product: Product) {
        val message = "Are you sure you want to delete '${product.name ?: "this item"}'? This cannot be undone."

        // Call the custom dialog helper function
        showCustomConfirmationDialog(
            context = this,               // Activity context
            message = message,            // Message to display
            positiveButtonText = "Delete", // Text for positive button
            negativeButtonText = "Cancel", // Text for negative button
            onPositiveClick = {           // Action for positive button click
                Log.d("MyListingsActivity", "Confirming delete via custom dialog for ${product.id}")
                viewModel.deleteListing(product) // Call ViewModel to delete
                // Dialog is dismissed automatically by the helper function
            }
            // onNegativeClick = null // No specific action needed for cancel besides dismiss
        )
    }
    // --- END MODIFIED ---


    // --- Add the showCustomConfirmationDialog helper function here ---
    // --- OR ensure it's in a Utility file and imported ---
    private fun showCustomConfirmationDialog(
        context: Context,
        message: String,
        positiveButtonText: String = "Yes",
        negativeButtonText: String = "No",
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false) // Consider making this true for easier dismissal
        dialog.setContentView(R.layout.dialog_confirmation) // Your custom layout
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val messageTextView = dialog.findViewById<TextView>(R.id.dialog_message_text)
        val positiveButton = dialog.findViewById<Button>(R.id.dialog_button_positive)
        val negativeButton = dialog.findViewById<Button>(R.id.dialog_button_negative)

        if (messageTextView == null || positiveButton == null || negativeButton == null) {
            Log.e("CustomDialog", "Error finding views in dialog_confirmation.xml")
            Toast.makeText(context, "Error showing dialog.", Toast.LENGTH_SHORT).show()
            return
        }

        messageTextView.text = message
        positiveButton.text = positiveButtonText
        negativeButton.text = negativeButtonText

        positiveButton.setOnClickListener {
            Log.d("CustomDialog", "Positive button clicked!")
            onPositiveClick()
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            Log.d("CustomDialog", "Negative button clicked.")
            onNegativeClick?.invoke()
            dialog.dismiss()
        }

        dialog.show()
    }
    // --- showToast remains the same ---
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}