package cit.edu.WildcatFreshFinds.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cit.edu.WildcatFreshFinds.data.Product
import cit.edu.WildcatFreshFinds.R
import cit.edu.WildcatFreshFinds.activity.SettingsActivity
import cit.edu.WildcatFreshFinds.dao.AppDatabase
import cit.edu.WildcatFreshFinds.utility.UserManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions // Import for circleCrop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO (Architecture): Move database interactions (UserManager calls, AppDatabase access)
//  into a dedicated ProfileViewModel for better separation of concerns, testability,
//  and handling configuration changes more robustly.
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    // --- UI Elements ---
    private lateinit var userNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var cancellationCountTextView: TextView
    private lateinit var profileImageView: ImageView // The main profile icon ImageView

    // --- Image Picking ---
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    // References ONLY for the Sell Dialog's image preview
    // These help route the single imagePickerLauncher result correctly
    private var currentSellDialogImageView: ImageView? = null
    private var currentSellDialogImageFilePath: String? = null

    // --- Lifecycle Methods ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the launcher here; it needs to be registered before the Fragment is Created.
        setupImagePicker()
        Log.d("ProfileFragment", "onCreate: ImagePickerLauncher initialized.")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ProfileFragment", "onViewCreated")
        findViews(view) // Initialize UI variable references
        setupClickListeners(view) // Setup button/image clicks
        updateUI() // Load initial data including profile image and counts
    }

    // --- Setup Functions ---

    private fun findViews(view: View) {
        Log.d("ProfileFragment", "findViews called")
        try {
            userNameTextView = view.findViewById(R.id.user_name)
            emailTextView = view.findViewById(R.id.email_value)
            // Ensure this ID exists in fragment_profile.xml
            cancellationCountTextView = view.findViewById(R.id.tv_cancellations_received_count)
            profileImageView = view.findViewById(R.id.profile_icon)
            Log.d("ProfileFragment", "Views found successfully.")
        } catch (e: Exception) {
            // Catch potential exceptions if IDs are wrong or layout is incorrect
            Log.e("ProfileFragment", "Error finding views: Check IDs in fragment_profile.xml! ${e.message}", e)
            showToast("Error initializing profile screen.")
            // Consider finishing the activity or showing an error state if critical views are missing
        }
    }

    private fun setupClickListeners(view: View) {
        Log.d("ProfileFragment", "setupClickListeners called")
        // Use safe calls ?.apply or ?.setOnClickListener to avoid crashes if views aren't found
        view.findViewById<Button>(R.id.edit_button)?.apply {
            bringToFront() // If needed for layout overlap issues
            setOnClickListener { showCustomDialogBox() }
        } ?: Log.e("ProfileFragment", "Edit button (R.id.edit_button) not found!")

        view.findViewById<CardView>(R.id.settings_container)?.setOnClickListener {
            Log.d("ProfileFragment", "Settings CardView clicked")
            // Navigate to SettingsActivity
            val intent = Intent(requireContext(), SettingsActivity::class.java) // Assume SettingsActivity exists
            startActivity(intent)
            // No finish() or onBackPressed() here unless intended
        } ?: Log.e("ProfileFragment", "Settings container (R.id.settings_container) not found!")

        view.findViewById<CardView>(R.id.sell_container)?.setOnClickListener {
            showSellDialog()
        } ?: Log.e("ProfileFragment", "Sell container (R.id.sell_container) not found!")

        // Set Click Listener for Profile Image - Check if initialized first
        if (::profileImageView.isInitialized) {
            profileImageView.setOnClickListener {
                Log.d("ProfileFragment", "Profile Icon clicked - launching image picker")
                // Clear sell dialog references to ensure the result routes to profile update
                currentSellDialogImageView = null
                currentSellDialogImageFilePath = null
                try {
                    imagePickerLauncher.launch("image/*") // Launch image picker
                } catch (e: Exception) {
                    Log.e("ProfileFragment", "Error launching image picker", e)
                    showToast("Cannot open image picker.")
                }
            }
        } else {
            Log.e("ProfileFragment", "profileImageView not initialized in setupClickListeners.")
        }
    }

    private fun setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                Log.d("ProfileFragment", "Image selected URI: $uri")
                // Check if this result is expected by the sell dialog
                if (currentSellDialogImageView != null) {
                    Log.d("ProfileFragment", "Image result is routed to Sell Dialog")
                    handleImageForSellDialog(uri)
                } else {
                    // Otherwise, assume it's for the main profile picture update
                    Log.d("ProfileFragment", "Image result is routed to Profile Update")
                    handleImageForProfileUpdate(uri)
                }
            } else {
                Log.d("ProfileFragment", "Image selection cancelled")
                // Reset sell dialog path if it was active and cancelled
                if (currentSellDialogImageView != null) {
                    currentSellDialogImageFilePath = null
                }
            }
        }
    }

    // --- UI Update ---

    private fun updateUI() {
        Log.d("ProfileFragment", "updateUI: Starting")
        // Prevent updates if fragment is not attached to context or view is destroyed
        if (!isAdded || context == null || view == null) {
            Log.w("ProfileFragment", "updateUI: Fragment not added or context/view is null. Skipping update.")
            return
        }
        viewLifecycleOwner.lifecycleScope.launch { // Use lifecycleScope tied to view's lifecycle
            var displayName = "Guest"
            var displayEmail = ""
            var cancellationsReceived = 0
            var profileImagePath: String? = null // Holds the path fetched from DB
            var isUserLoggedIn = false

            try {
                // Fetch the complete User object from the database via UserManager
                val signedInUser = withContext(Dispatchers.IO) { UserManager.getSignedIn() }
                if (signedInUser != null) {
                    // User found, extract data
                    isUserLoggedIn = true
                    displayName = "${signedInUser.firstName ?: ""} ${signedInUser.lastName ?: ""}".trim().ifBlank { "User" }
                    displayEmail = signedInUser.email
                    cancellationsReceived = signedInUser.receivedCancellationCount // Get the count
                    profileImagePath = signedInUser.profileImagePath // Get the image path
                    Log.d("ProfileFragment", "updateUI: Fetched User: ${signedInUser.email}, Reports: $cancellationsReceived, ImgPath: $profileImagePath")
                } else {
                    // No user signed in
                    isUserLoggedIn = false
                    Log.w("ProfileFragment", "updateUI: signedInUser is null")
                }
            } catch (e: Exception) {
                // Handle errors during data fetch
                Log.e("ProfileFragment", "updateUI: Error getting user data", e)
                if(isAdded) showToast("Error loading profile data.") // Show toast only if fragment still added
                displayName = "Error"; displayEmail = "Error"
            }

            // Check if fragment is still attached before updating UI elements
            if (!isAdded) {
                Log.w("ProfileFragment","updateUI: Fragment detached before UI update completion.")
                return@launch
            }

            // --- Update UI Elements on Main Thread ---
            // Use isInitialized checks for safety with lateinit vars
            if (::userNameTextView.isInitialized) userNameTextView.text = displayName else Log.e("ProfileFragment", "userNameTextView not init!")
            if (::emailTextView.isInitialized) emailTextView.text = displayEmail else Log.e("ProfileFragment", "emailTextView not init!")

            // Update Cancellation Count TextView
            if (::cancellationCountTextView.isInitialized) {
                if (isUserLoggedIn) {
                    cancellationCountTextView.text = "Reports Received: $cancellationsReceived"
                    cancellationCountTextView.visibility = View.VISIBLE
                } else {
                    cancellationCountTextView.visibility = View.GONE
                }
            } else { Log.e("ProfileFragment", "cancellationCountTextView not init!") }

            // Update Profile Image View
            if (::profileImageView.isInitialized) {
                val safeContext = context // Use captured context
                if (safeContext != null) {
                    // Determine what image source to load
                    val imageToLoad: Any? = if (isUserLoggedIn && !profileImagePath.isNullOrBlank()) {
                        // Load the saved file path if user logged in and path exists
                        Log.d("ProfileFragment", "updateUI: Loading profile image from path: $profileImagePath")
                        profileImagePath
                    } else {
                        // Load default placeholder if not logged in or no path saved
                        Log.d("ProfileFragment", "updateUI: Loading default profile icon.")
                        R.drawable.profile_icon // Your default drawable resource ID
                    }

                    // Use Glide to load the image
                    Glide.with(safeContext) // Use safe context
                        .load(imageToLoad)
                        .apply(RequestOptions.circleCropTransform()) // Apply circular crop
                        .placeholder(R.drawable.profile_icon) // Default icon as placeholder
                        .error(R.drawable.profile_icon)     // Default icon if loading fails
                        .into(profileImageView) // Target ImageView
                } else { Log.e("ProfileFragment", "updateUI: Context was null, cannot use Glide.") }
            } else { Log.e("ProfileFragment", "updateUI: profileImageView not initialized!") }
            // --- End UI Updates ---
        } // End Coroutine Scope
    } // End updateUI

    // --- Image Handling Logic ---

    private fun handleImageForProfileUpdate(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch { // Use lifecycleScope
            var filePath: String? = null
            var userUpdated = false
            var oldImagePath: String? = null

            try {
                // Copy image on IO thread, ensure context is valid
                filePath = withContext(Dispatchers.IO) {
                    context?.let { safeContext ->
                        Log.d("ProfileFragment", "Starting profile image copy...")
                        copyImageToInternalStorage(safeContext, uri)
                    } ?: run {
                        Log.e("ProfileFragment", "Context null during image copy.")
                        null // Return null if context is gone
                    }
                }

                if (filePath != null) {
                    Log.d("ProfileFragment", "Profile image copied to: $filePath. Updating DB...")
                    // Update User DB on IO thread
                    val currentUser = withContext(Dispatchers.IO) { UserManager.getSignedIn() }
                    if (currentUser != null && context != null) { // Check context again for DB access
                        oldImagePath = currentUser.profileImagePath // Get old path for potential deletion
                        val updatedUser = currentUser.copy(profileImagePath = filePath)
                        withContext(Dispatchers.IO) {
                            AppDatabase.getDatabase(requireContext()).userDao().update(updatedUser)
                            // Delete OLD image file only AFTER successful DB update
                            if (!oldImagePath.isNullOrBlank() && oldImagePath != filePath) {
                                Log.d("ProfileFragment", "Attempting to delete old image: $oldImagePath")
                                try { File(oldImagePath).delete() } catch (e: Exception) { Log.e("ProfileFragment", "Error deleting old profile pic", e)}
                            }
                        }
                        userUpdated = true
                        Log.d("ProfileFragment", "User profile DB updated successfully.")
                    } else {
                        Log.e("ProfileFragment", "Cannot save image path, user null or context null during update.")
                        filePath?.let { if(File(it).exists()) File(it).delete() } // Clean up if needed
                    }
                } else { Log.e("ProfileFragment", "Failed to copy profile image (filePath is null).") }

            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error processing profile image update", e)
                filePath?.let { if(File(it).exists()) File(it).delete() } // Clean up on general error
                userUpdated = false
            }

            // Update UI on Main thread
            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext // Final check before UI interaction
                if (userUpdated && filePath != null && ::profileImageView.isInitialized && context != null) {
                    Glide.with(this@ProfileFragment) // Use Fragment context
                        .load(filePath)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.profile_icon).error(R.drawable.profile_icon)
                        .into(profileImageView)
                    showToast("Profile picture updated.")
                } else {
                    showToast("Failed to update profile picture.")
                    // Consider reloading the UI to revert to the previous state if needed
                    // updateUI()
                }
            }
        }
        // TODO: Move database update logic to a ProfileViewModel.
    }

    private fun handleImageForSellDialog(uri: Uri) {
        // This function handles the preview for the sell dialog
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val safeContext = context ?: return@launch Unit.also { Log.e("ProfileFragment", "Context null for sell dialog image copy.") }
            val filePath = copyImageToInternalStorage(safeContext, uri)

            withContext(Dispatchers.Main) {
                if(!isAdded) return@withContext
                if (filePath != null) {
                    currentSellDialogImageFilePath = filePath
                    Log.d("ProfileFragment", "Stored file path for sell dialog: $currentSellDialogImageFilePath")
                    // Update preview ONLY if the dialog/imageView still exists
                    currentSellDialogImageView?.let { imageView ->
                        Glide.with(this@ProfileFragment)
                            .load(filePath)
                            .placeholder(R.drawable.empty_img).error(R.drawable.empty_img)
                            .centerCrop().into(imageView)
                    } ?: Log.w("ProfileFragment", "Sell dialog image view reference was null during UI update.")
                } else {
                    if(isAdded) showToast("Failed to process selected image.")
                    currentSellDialogImageFilePath = null
                    currentSellDialogImageView?.let { Glide.with(this@ProfileFragment).load(R.drawable.empty_img).into(it) }
                }
            }
        }
    }

    // Use separate directory for profile images
    private fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
        var newFilePath: String? = null
        val fileName = "pfp_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
        val imageDir = File(context.filesDir, "profile_images") // Specific directory
        if (!imageDir.exists()) {
            if (!imageDir.mkdirs()) {
                Log.e("ProfileFragment", "Failed to create profile_images directory")
                return null
            }
        }
        val destinationFile = File(imageDir, fileName)
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IOException("ContentResolver failed to open InputStream for $uri")

            if (destinationFile.exists() && destinationFile.length() > 0) {
                newFilePath = destinationFile.absolutePath
                Log.d("ProfileFragment", "Image copied to: $newFilePath")
            } else {
                throw IOException("Copy resulted in empty or non-existent file")
            }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error copying image to ${destinationFile.absolutePath}", e)
            if (destinationFile.exists()) destinationFile.delete() // Clean up partial file
            newFilePath = null
        }
        return newFilePath
    }


    // --- Dialog Functions ---

    fun showCustomDialogBox() { // Edit Profile Dialog
        if (!isAdded || context == null) return
        Log.d("ProfileFragment", "showCustomDialogBox called")
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_edit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etFirstName = dialog.findViewById<EditText>(R.id.et_first_name)
        val etLastName = dialog.findViewById<EditText>(R.id.et_last_name)
        val etPassword = dialog.findViewById<EditText>(R.id.et_password)
        val editDialogButton = dialog.findViewById<Button>(R.id.edit_button)
        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)

        if (etFirstName == null || etLastName == null || etPassword == null || editDialogButton == null || cancelButton == null) {
            Log.e("ProfileFragment", "Error finding views in dialog_edit.xml")
            showToast("Error opening edit dialog."); return
        }

        // Populate fields
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val signedInUser = withContext(Dispatchers.IO) { UserManager.getSignedIn() }
                if (!isAdded) return@launch
                withContext(Dispatchers.Main){
                    if (signedInUser != null) {
                        etFirstName.setText(signedInUser.firstName ?: "")
                        etLastName.setText(signedInUser.lastName ?: "")
                        etPassword.setText(signedInUser.password ?: "") // Consider not showing password
                    } else { Log.e("ProfileFragment", "Edit Dialog: Cannot populate, user is null."); dialog.dismiss() }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Edit Dialog: Error getting user data", e);
                if(isAdded) withContext(Dispatchers.Main){ showToast("Error loading data.") }
                dialog.dismiss()
            }
        }

        editDialogButton.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val password = etPassword.text.toString()

            if (firstName.isBlank() || lastName.isBlank() || password.isBlank()) { showToast("All fields required."); return@setOnClickListener }
            if (password.length < 8) { showToast("Password >= 8 chars."); return@setOnClickListener }

            viewLifecycleOwner.lifecycleScope.launch {
                var dataChanged = false // Assume false unless proven otherwise
                var proceed = true // Flag to check if comparison succeeded
                try {
                    val signedInUser = withContext(Dispatchers.IO) { UserManager.getSignedIn() }
                    if (signedInUser != null) {
                        if (firstName != signedInUser.firstName || lastName != signedInUser.lastName || password != signedInUser.password) {
                            dataChanged = true
                        } else {
                            if(isAdded) withContext(Dispatchers.Main) { showToast("Data was not edited.") }
                        }
                    } else {
                        proceed = false; if(isAdded) withContext(Dispatchers.Main){ showToast("Error verifying data.") }
                    }
                } catch (e: Exception) {
                    Log.e("ProfileFragment", "Error checking if data changed", e)
                    if(isAdded) withContext(Dispatchers.Main){ showToast("Error checking data.") }
                    proceed = false
                }

                if (proceed && dataChanged) {
                    Log.d("ProfileFragment", "Edit: Data changed. Updating.")
                    var success = false
                    try {
                        // TODO: Move UserManager.editUser to ViewModel
                        success = withContext(Dispatchers.IO) { UserManager.editUser(firstName, lastName, password) }
                    } catch (e: Exception) { Log.e("ProfileFragment", "Error calling UserManager.editUser", e); success = false }

                    if(isAdded){ // Check fragment state before UI update
                        withContext(Dispatchers.Main) {
                            if (success) { showToast("Edit Successful!"); dialog.dismiss(); updateUI() }
                            else { showToast("Edit failed.") }
                        }
                    }
                } else if (proceed && !dataChanged) {
                    Log.d("ProfileFragment", "Edit: Data not changed.")
                    // Optionally dismiss dialog even if no change
                    // dialog.dismiss()
                }
            }
        }
        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    } // End showCustomDialogBox

    fun showSellDialog() { // Add Product Dialog
        if (!isAdded || context == null) return
        Log.d("ProfileFragment", "showSellDialog called")
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_sell)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Find views
        val etName = dialog.findViewById<EditText>(R.id.et_name)
        val etPrice = dialog.findViewById<EditText>(R.id.et_price)
        val etDescription = dialog.findViewById<EditText>(R.id.et_description)
        val etQuantity = dialog.findViewById<EditText>(R.id.et_quantity)
        val productImageView = dialog.findViewById<ImageView>(R.id.product_img)
        val uploadButton = dialog.findViewById<Button>(R.id.upload_btn)
        val sellButton = dialog.findViewById<Button>(R.id.sell_button)
        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)

        if (etName == null || etPrice == null || etDescription == null || etQuantity == null ||
            productImageView == null || uploadButton == null || sellButton == null || cancelButton == null) {
            Log.e("ProfileFragment", "Error finding views in dialog_sell.xml")
            showToast("Error opening sell dialog."); return
        }

        // Reset state and load placeholder
        currentSellDialogImageFilePath = null
        currentSellDialogImageView = productImageView
        Glide.with(this).load(R.drawable.empty_img).centerCrop().into(productImageView)
        dialog.setOnDismissListener { currentSellDialogImageView = null }

        // Setup Listeners
        uploadButton.setOnClickListener {
            currentSellDialogImageView = productImageView
            imagePickerLauncher.launch("image/*")
        }
        cancelButton.setOnClickListener { dialog.dismiss() }

        sellButton.setOnClickListener {
            val name = etName.text.toString().trim()
            val priceText = etPrice.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val quantityText = etQuantity.text.toString().trim()
            val imagePath = currentSellDialogImageFilePath

            if (name.isEmpty() || priceText.isEmpty() || description.isEmpty() || quantityText.isEmpty() || imagePath.isNullOrBlank()) {
                showToast("Please fill all fields and upload an image!"); return@setOnClickListener
            }
            val price = priceText.toDoubleOrNull()
            val quantity = quantityText.toIntOrNull()
            if (price == null || price <= 0 || quantity == null || quantity <= 0) {
                showToast("Invalid price or quantity!"); return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val sellerEmail = withContext(Dispatchers.IO) { UserManager.getSignedIn()?.email }
                    if (sellerEmail == null) { showToast("Cannot list item: User error."); return@launch }

                    val product = Product(
                        id = UUID.randomUUID().toString(), name = name, price = price, description = description,
                        imageUrl = imagePath, // Checked not null above
                        sellerEmail = sellerEmail, quantity = quantity
                    )
                    Log.d("ProfileFragment", "Saving Product: $product")
                    withContext(Dispatchers.IO) {
                        // TODO: Move to ViewModel/Repository
                        AppDatabase.getDatabase(requireContext()).productDao().insert(product)
                    }
                    if(isAdded){ showToast("Product listed successfully!"); dialog.dismiss() }
                } catch (e: Exception) {
                    Log.e("ProfileFragment", "Error saving product", e)
                    if(isAdded) showToast("Failed to list product: ${e.localizedMessage}")
                }
            }
        }
        dialog.show()
    } // End showSellDialog

    // --- Utility ---
    private fun showToast(message: String) {
        // Ensure toast is shown on the main thread and fragment context is valid
        if (isAdded && context != null) {
            activity?.runOnUiThread { // Ensure on main thread
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        } else { Log.e("ProfileFragment", "Toast failed: context null or fragment not added. Msg: $message") }
    }

} // End Fragment