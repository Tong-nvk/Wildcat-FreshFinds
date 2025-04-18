package cit.edu.WildcatFreshFinds

import android.app.AlertDialog
import android.app.Dialog
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
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var userNameTextView: TextView
    private lateinit var emailTextView: TextView

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private var currentDialogImageUri: Uri? = null
    private var currentDialogImageView: ImageView? = null // To hold reference to dialog's ImageView
    private var currentDialogImageFilePath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                Log.d("ProfileFragment", "Image selected: $uri")

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val filePath = copyImageToInternalStorage(requireContext(), uri)
                    withContext(Dispatchers.Main) {
                        if (filePath != null) {
                            currentDialogImageFilePath = filePath
                            Log.d("ProfileFragment", "Stored file path: $currentDialogImageFilePath")

                            currentDialogImageView?.let { imageView ->
                                Glide.with(this@ProfileFragment)
                                    .load(filePath)
                                    .placeholder(R.drawable.empty_img)
                                    .error(R.drawable.empty_img)
                                    .centerCrop()
                                    .into(imageView)
                            } ?: Log.w("ProfileFragment", "Dialog ImageView ref null for preview update.")

                        } else {
                            Log.e("ProfileFragment", "Failed to copy image to internal storage.")
                            showToast("Failed to process selected image.")
                            currentDialogImageFilePath = null
                            currentDialogImageView?.let { Glide.with(this@ProfileFragment).load(R.drawable.empty_img).into(it) }
                        }
                    }
                }

            } else {
                Log.d("ProfileFragment", "Image selection cancelled")
                currentDialogImageFilePath = null
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editButton = view.findViewById<Button>(R.id.edit_button)
        editButton.bringToFront()

        userNameTextView = view.findViewById(R.id.user_name) // Make sure this ID exists in fragment_profile.xml
        emailTextView = view.findViewById(R.id.email_value)

        Log.d("ProfileFragment", "onViewCreated: Fragment created")


        updateUI()

        editButton.setOnClickListener {
            showCustomDialogBox()
        }

        val settingsContainer: CardView = view.findViewById(R.id.settings_container)

        settingsContainer.setOnClickListener {
            Log.e("ProfileFragment", "Settings Navigation Button Clicked")
            showToast("Settings")

            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
        val sellContainer: CardView = view.findViewById(R.id.sell_container)

        sellContainer.setOnClickListener {
            showSellDialog()
        }

    }

    private fun updateUI() {
        Log.d("ProfileFragment", "updateUI: Starting UI update")
        viewLifecycleOwner.lifecycleScope.launch {
            var displayName = "Guest"
            var displayEmail = ""

            try {
                val signedInUser = withContext(Dispatchers.IO) { UserManager.getSignedIn() }

                if (signedInUser != null) {
                    // --- Concatenate first and last name ---
                    displayName = "${signedInUser.firstName ?: ""} ${signedInUser.lastName ?: ""}".trim()
                    if (displayName.isBlank()) { displayName = "User" } // Fallback
                    displayEmail = signedInUser.email
                    Log.d("ProfileFragment", "updateUI: Fetched user ${signedInUser.email}")
                } else {
                    Log.w("ProfileFragment", "updateUI: signedInUser is null!")
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "updateUI: Error getting user data: ${e.message}", e)
                showToast("Error loading profile data.")
                displayName = "Error"
                displayEmail = "Error"
            }

            // Update UI
            userNameTextView.text = displayName
            emailTextView.text = displayEmail
        }
    }

    fun showCustomDialogBox() {
        Log.d("ProfileFragment", "showCustomDialogBox: Showing edit dialog")
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_edit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)
        val editDialogButton = dialog.findViewById<Button>(R.id.edit_button)

        val etFirstName = dialog.findViewById<EditText>(R.id.et_first_name)
        val etLastName = dialog.findViewById<EditText>(R.id.et_last_name)
        val etPassword = dialog.findViewById<EditText>(R.id.et_password)

        // Check if views exist (crucial after layout change)
        if (etFirstName == null || etLastName == null || etPassword == null) {
            Log.e("ProfileFragment", "Error finding EditText views in dialog_edit.xml. Check IDs!")
            showToast("Error displaying edit dialog.")
            return // Don't show dialog if views are missing
        }


        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val signedInUser = withContext(Dispatchers.IO) { UserManager.getSignedIn() }

                withContext(Dispatchers.Main) {
                    if (signedInUser != null) {
                        // --- Populate new fields ---
                        etFirstName.setText(signedInUser.firstName ?: "")
                        etLastName.setText(signedInUser.lastName ?: "")
                        etPassword.setText(signedInUser.password ?: "")
                        Log.d("ProfileFragment", "showCustomDialogBox: Edit text values set")
                    } else {
                        Log.e("ProfileFragment", "Cannot populate edit fields, user is null.")
                        showToast("Cannot load user data to edit.")
                        dialog.dismiss()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error getting user data for edit dialog", e)
                withContext(Dispatchers.Main){ showToast("Error loading user data.") }
                dialog.dismiss()
            }
        }

        editDialogButton.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val password = etPassword.text.toString()

            if (firstName.isBlank() || lastName.isBlank() || password.isBlank()) {
                showToast("All fields are required.")
                return@setOnClickListener
            }
            if (password.length < 8) {
                showToast("Password must be at least 8 characters.")
                return@setOnClickListener
            }

            // Check if data changed before calling editUser
            viewLifecycleOwner.lifecycleScope.launch {
                var dataChanged = true
                try {
                    val signedInUser = withContext(Dispatchers.IO) { UserManager.getSignedIn() } // Get fresh data
                    if (signedInUser != null) {
                        if (firstName == signedInUser.firstName &&
                            lastName == signedInUser.lastName &&
                            password == signedInUser.password) {
                            showToast("Data was not edited.")
                            dataChanged = false
                        }
                    } else {
                        showToast("Error verifying current data.")
                        dataChanged = false // Don't proceed if error
                    }
                } catch (e: Exception) {
                    Log.e("ProfileFragment", "Error checking if data changed", e)
                    showToast("Error checking current data.")
                    dataChanged = false // Don't proceed on error
                }

                if (dataChanged) {
                    Log.d("ProfileFragment", "Edit Button Clicked, data changed. Proceeding with edit.")
                    launch {
                        try {
                            val success = UserManager.editUser(firstName, lastName, password)
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    showToast("Edit Successful!")
                                    dialog.dismiss()
                                    updateUI()
                                } else {
                                    showToast("Edit failed. User not found or no changes needed?")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ProfileFragment", "Error calling UserManager.editUser", e)
                            withContext(Dispatchers.Main) {
                                showToast("Error saving changes.")
                            }
                        }
                    }
                } else {
                    Log.d("ProfileFragment", "Edit Button Clicked, but data not changed or error occurred.")
                }
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
        var newFilePath: String? = null
        try {
            val imageDir = File(context.filesDir, "images")
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }
            val fileName = "product_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(imageDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4k buffer
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                    newFilePath = destinationFile.absolutePath // Get the absolute path of the new file
                    Log.d("ProfileFragment", "Image copied successfully to: $newFilePath")
                }
            }
        } catch (e: IOException) {
            Log.e("ProfileFragment", "Error copying image", e)
        }
        return newFilePath
    }
    fun showSellDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_sell) // Your dialog layout
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)
        val sellButton = dialog.findViewById<Button>(R.id.sell_button)

        val etName = dialog.findViewById<EditText>(R.id.et_name)
        val etPrice = dialog.findViewById<EditText>(R.id.et_price)
        val etDescription = dialog.findViewById<EditText>(R.id.et_description)
        val uploadButton = dialog.findViewById<Button>(R.id.upload_btn) // Find upload button
        val etQuantity = dialog.findViewById<EditText>(R.id.et_quantity) // <-- Find quantity EditText
        val productImageView = dialog.findViewById<ImageView>(R.id.product_img) // Find ImageView

        currentDialogImageFilePath = null
        currentDialogImageView = productImageView
        Glide.with(this)
            .load(R.drawable.empty_img)
            .centerCrop()
            .into(productImageView)
        dialog.setOnDismissListener {
            currentDialogImageView = null
            Log.d("ProfileFragment", "Sell dialog dismissed, ImageView reference cleared.")
        }


        uploadButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }


        sellButton.setOnClickListener {
            val name = etName.text.toString().trim()
            val priceText = etPrice.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val quantityText = etQuantity.text.toString().trim() // <-- Get quantity text

            if (name.isEmpty() || priceText.isEmpty() || description.isEmpty() || quantityText.isEmpty()) { // <-- Check quantityText
                showToast("Please fill in all product details!")
                return@setOnClickListener
            }
            val price = priceText.toDoubleOrNull()
            val quantity = quantityText.toIntOrNull()
            if (price == null || price <= 0) {
                showToast("Invalid price format!")
                return@setOnClickListener
            }
            if (quantity == null || quantity <= 0) {
                showToast("Please enter a valid quantity (must be 1 or more)!")
                return@setOnClickListener
            }
            if (currentDialogImageFilePath == null) {
                showToast("Please upload and process a product image!")
                return@setOnClickListener
            }

            if (currentDialogImageFilePath == null) { // <-- Check file path now
                showToast("Please upload and process a product image!")
                return@setOnClickListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                runCatching {
                    val signedInUser = withContext(Dispatchers.IO) { UserManager.getSignedIn() }
                    val sellerEmail = signedInUser?.email
                    val id = UUID.randomUUID().toString()
                    val imagePathString = currentDialogImageFilePath ?: run {
                        Log.e("ProfileFragment", "Image path null after validation!")
                        withContext(Dispatchers.Main) { showToast("Image error.") }
                        return@runCatching
                    }

                    val product = Product(
                        id = id,
                        name = name,
                        price = price,
                        description = description,
                        imageUrl = imagePathString,
                        sellerEmail = sellerEmail,
                        quantity = quantity
                    )
                    Log.d("ProfileFragment", "Saving Product: $product")

                    withContext(Dispatchers.IO) {
                        AppDatabase.getDatabase(requireContext()).productDao().insert(product)
                        Log.d("ProfileFragment", "Product insertion successful (likely)")
                    }
                    withContext(Dispatchers.Main) {
                        showToast("Product added successfully!")
                        dialog.dismiss()
                    }

                }.onFailure { e ->
                    Log.e("ProfileFragment", "Error saving product: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        showToast("Failed to add product. ${e.localizedMessage}")
                    }
                }
            }
        }


        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    private fun clearAllProductsData() {
        lifecycleScope.launch { // Use lifecycleScope if in Fragment/Activity
            try {
                withContext(Dispatchers.IO) { // Run database operation on IO thread
                    val dao = DatabaseManager.productDao(requireContext())
                    Log.d("ClearData", "Attempting to delete all products...")
                    dao.deleteAllProducts()
                    Log.d("ClearData", "All products deleted successfully.")
                }
                withContext(Dispatchers.Main) {
                    showToast("All products cleared!")
                }
            } catch (e: Exception) {
                Log.e("ClearData", "Error clearing products table: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showToast("Error clearing data.")
                }
            }
        }
    }

    private fun showConfirmationDialog(onConfirm: (Boolean) -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete ALL product data? This cannot be undone.")
            .setPositiveButton("Delete All") { dialog, _ ->
                onConfirm(true)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                onConfirm(false)
                dialog.dismiss()
            }
            .show()
    }

}