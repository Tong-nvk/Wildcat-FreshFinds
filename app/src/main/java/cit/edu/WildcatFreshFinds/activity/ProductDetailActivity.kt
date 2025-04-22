package cit.edu.WildcatFreshFinds.activity

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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import cit.edu.WildcatFreshFinds.data.Product
import cit.edu.WildcatFreshFinds.view_model.ProductDetailViewModel
import cit.edu.WildcatFreshFinds.R
import cit.edu.WildcatFreshFinds.utility.UserManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private val viewModel: ProductDetailViewModel by viewModels()

    companion object {
        const val EXTRA_PRODUCT = "EXTRA_PRODUCT"
    }

    private lateinit var productImageView: ImageView
    private lateinit var productNameTextView: TextView
    private lateinit var productPriceTextView: TextView
    private lateinit var productDescTextView: TextView
    private lateinit var productSellerTextView: TextView
    private lateinit var availableQuantityTextView: TextView
    private lateinit var selectedQuantityTextView: TextView
    private lateinit var decreaseQtyButton: Button
    private lateinit var increaseQtyButton: Button
    private lateinit var buyNowButton: Button
    private lateinit var backButton: ImageView

    private var currentProduct: Product? = null
    private var selectedQuantity: Int = 1
    private var maxQuantity: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

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

        if (intent.hasExtra(EXTRA_PRODUCT)) {
            currentProduct = IntentCompat.getParcelableExtra(intent, EXTRA_PRODUCT, Product::class.java)
        }


        if (currentProduct != null) {
            populateUi(currentProduct!!)
            setupQuantitySelector()
            viewModel.checkSellerStatus(currentProduct)
        } else {
            Log.e("ProductDetailActivity", "Product data not received via Intent.")
            showToast("Error loading product details.")
            finish()
            return
        }

        setupObservers()

        buyNowButton.setOnClickListener {
            handleBuyNowClick()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun populateUi(product: Product) {
        productNameTextView.text = product.name ?: "N/A"
        productDescTextView.text = product.description ?: "No description."
        // Display seller info
        productSellerTextView.text = "Seller: ${product.sellerEmail ?: "Unknown"}"

        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        productPriceTextView.text = product.price?.let { format.format(it) } ?: "N/A"

        maxQuantity = product.quantity
        availableQuantityTextView.text = "Available: $maxQuantity"

        // Load image
        product.imageUrl?.let { filePath ->
            Glide.with(this)
                .load(filePath)
                .placeholder(R.drawable.empty_img)
                .error(R.drawable.empty_img)
                .into(productImageView)
        } ?: productImageView.setImageResource(R.drawable.empty_img)
    }

    private fun setupQuantitySelector() {
        selectedQuantity = 1
        updateQuantityDisplay()
        decreaseQtyButton.setOnClickListener {
            if (selectedQuantity > 1) {
                selectedQuantity--
                updateQuantityDisplay()
            }
        }
        increaseQtyButton.setOnClickListener {
            if (selectedQuantity < maxQuantity) {
                selectedQuantity++
                updateQuantityDisplay()
            } else {
                showToast("Maximum available quantity reached.")
            }
        }
    }

    private fun updateQuantityDisplay() {
        selectedQuantityTextView.text = selectedQuantity.toString()
        decreaseQtyButton.isEnabled = selectedQuantity > 1
        increaseQtyButton.isEnabled = selectedQuantity < maxQuantity
    }

    private fun setupObservers() {
        viewModel.isCurrentUserSeller.observe(this, Observer { isSeller ->
            if (isSeller) {
                buyNowButton.isEnabled = false
                buyNowButton.alpha = 0.5f
                Log.d("ProductDetailActivity", "Observer: User IS the seller, button disabled.")
            } else {
                buyNowButton.isEnabled = true
                buyNowButton.alpha = 1.0f
                Log.d("ProductDetailActivity", "Observer: User is NOT the seller, button enabled.")
            }
        })

        viewModel.toastMessage.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                showToast(message)
            }
        })

        viewModel.purchaseInitiated.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    Log.d("ProductDetailActivity", "Purchase initiated successfully, finishing activity.")
                    finish()
                }
            }
        })
    }

    private fun handleBuyNowClick() {
        val product = currentProduct ?: return

        if (selectedQuantity <= 0) {
            showToast("Please select a quantity.")
            return
        }
        if (selectedQuantity > product.quantity) {
            showToast("Only ${product.quantity} available.")
            selectedQuantity = product.quantity
            updateQuantityDisplay()
            return
        }

        showPurchaseConfirmationDialog(product, selectedQuantity) { confirmed ->
            if (confirmed) {

                lifecycleScope.launch {
                    val buyer = withContext(Dispatchers.IO) { UserManager.getSignedIn() } // Get buyer info
                    if (buyer?.email == null) {
                        showToast("Could not confirm your identity. Please log in again.")
                    } else {
                        viewModel.initiatePurchase(product, selectedQuantity, buyer.email)
                    }
                }
            }
        }
    }


    private fun showPurchaseConfirmationDialog(product: Product, quantity: Int, onConfirm: (Boolean) -> Unit) {
        showCustomConfirmationDialog(
            context = this,
            message = "Are you sure you want to buy $quantity of ${product.name ?: "this item"}?",
            positiveButtonText = "Yes",
            negativeButtonText = "No",
            onPositiveClick = {
                onConfirm(true)
            },
            onNegativeClick = {
                onConfirm(false)
            }
        )
    }
    fun showCustomConfirmationDialog(
        context: Context,
        message: String,
        positiveButtonText: String = "Yes",
        negativeButtonText: String = "No",
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_confirmation)
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
            onPositiveClick()
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            onNegativeClick?.invoke()
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}