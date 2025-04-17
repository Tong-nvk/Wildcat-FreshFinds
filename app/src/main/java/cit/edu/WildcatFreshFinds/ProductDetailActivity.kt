package cit.edu.WildcatFreshFinds // Use your package name

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.IntentCompat // For getParcelableExtra API 33+
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    // Define keys for Intent extras (good practice)
    companion object {
        const val EXTRA_PRODUCT = "EXTRA_PRODUCT" // Use this if passing Parcelable
        // Define others if passing individually
        // const val EXTRA_PRODUCT_ID = "EXTRA_PRODUCT_ID"
        // const val EXTRA_PRODUCT_NAME = "EXTRA_PRODUCT_NAME"
        // ... etc.
    }

    private lateinit var productImageView: ImageView
    private lateinit var productNameTextView: TextView
    private lateinit var productPriceTextView: TextView
    private lateinit var productDescTextView: TextView
    private lateinit var productSellerTextView: TextView
    private lateinit var availableQuantityTextView: TextView
    // Add views for quantity selection (e.g., TextView, Buttons)
    private lateinit var selectedQuantityTextView: TextView
    private lateinit var decreaseQtyButton: Button
    private lateinit var increaseQtyButton: Button
    private lateinit var buyNowButton: Button
    private lateinit var backButton: ImageView

    private var currentProduct: Product? = null
    private var selectedQuantity: Int = 1 // Default to buying 1
    private var maxQuantity: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail) // Create this layout next

        // --- Find Views ---
        backButton = findViewById(R.id.back_icon);
        productImageView = findViewById(R.id.detail_product_image)
        productNameTextView = findViewById(R.id.detail_product_name)
        productPriceTextView = findViewById(R.id.detail_product_price)
        productDescTextView = findViewById(R.id.detail_product_description)
        productSellerTextView = findViewById(R.id.detail_product_seller)
        availableQuantityTextView = findViewById(R.id.detail_available_quantity)
        selectedQuantityTextView = findViewById(R.id.detail_selected_quantity)
        decreaseQtyButton = findViewById(R.id.detail_decrease_qty_button)
        increaseQtyButton = findViewById(R.id.detail_increase_qty_button)
        buyNowButton = findViewById(R.id.detail_buy_now_button)
        // --- End Find Views ---


        // --- Get Product Data from Intent ---
        if (intent.hasExtra(EXTRA_PRODUCT)) {
            currentProduct = IntentCompat.getParcelableExtra(intent, EXTRA_PRODUCT, Product::class.java)
        }
        // --- Alternative: Get individual extras if not using Parcelable ---
        // val productId = intent.getStringExtra(EXTRA_PRODUCT_ID)
        // val productName = intent.getStringExtra(EXTRA_PRODUCT_NAME)
        // ... etc ...
        // if (productId != null) {
        //     currentProduct = Product(id = productId, name = productName, ...) // Reconstruct object
        // }


        // --- Populate UI ---
        if (currentProduct != null) {
            populateUi(currentProduct!!)
            setupQuantitySelector()
        } else {
            // Handle error - product data not received
            Log.e("ProductDetailActivity", "Product data not received via Intent.")
            showToast("Error loading product details.")
            finish() // Close activity if no data
            return
        }
        // --- End Populate UI ---

        backButton.setOnClickListener {
            finish()
        }

        // --- Setup Buy Now Button ---
        buyNowButton.setOnClickListener {
            handleBuyNow()
        }
        // --- End Setup Buy Now Button ---
    }

    private fun populateUi(product: Product) {
        productNameTextView.text = product.name ?: "N/A"
        productDescTextView.text = product.description ?: "No description."
        productSellerTextView.text = "Sold by: ${product.seller ?: "Unknown"}"

        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        productPriceTextView.text = product.price?.let { format.format(it) } ?: "N/A"

        maxQuantity = product.quantity // Store max quantity
        availableQuantityTextView.text = "Available: $maxQuantity"

        // Load image
        product.imageUrl?.let { filePath ->
            Glide.with(this)
                .load(filePath) // Load from file path
                .placeholder(R.drawable.empty_img) // Use your placeholder
                .error(R.drawable.empty_img)     // Use your error drawable
                .into(productImageView)
        } ?: productImageView.setImageResource(R.drawable.empty_img) // Set placeholder if null

    }

    private fun setupQuantitySelector() {
        selectedQuantity = 1 // Start with 1 selected
        updateQuantityDisplay()

        decreaseQtyButton.setOnClickListener {
            if (selectedQuantity > 1) {
                selectedQuantity--
                updateQuantityDisplay()
            }
        }

        increaseQtyButton.setOnClickListener {
            if (selectedQuantity < maxQuantity) { // Can't select more than available
                selectedQuantity++
                updateQuantityDisplay()
            } else {
                showToast("Maximum available quantity reached.")
            }
        }
    }

    private fun updateQuantityDisplay() {
        selectedQuantityTextView.text = selectedQuantity.toString()
        // Enable/disable buttons based on quantity
        decreaseQtyButton.isEnabled = selectedQuantity > 1
        increaseQtyButton.isEnabled = selectedQuantity < maxQuantity
    }

    private fun handleBuyNow() {
        if (currentProduct == null) return

        val productName = currentProduct!!.name ?: "Product"
        showToast("Buying $selectedQuantity of $productName.")

        // TODO: Implement actual purchase logic:
        // 1. Check if selectedQuantity <= current available quantity again (race condition?)
        // 2. Perform payment processing (if applicable)
        // 3. Update the quantity in the database:
        //    - Calculate newQuantity = currentProduct!!.quantity - selectedQuantity
        //    - Create updatedProduct = currentProduct!!.copy(quantity = newQuantity)
        //    - Call a method (e.g., in a ViewModel/Repository) to update the product in Room DB using ProductDao's update method.
        //    - Handle success/failure of the database update.
        // 4. Navigate to a success/confirmation screen or back to the list.

        Log.d("ProductDetailActivity", "Buy Now clicked for ${currentProduct?.id}, quantity: $selectedQuantity")
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}