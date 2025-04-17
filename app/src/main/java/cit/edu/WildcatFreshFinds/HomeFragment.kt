package cit.edu.WildcatFreshFinds

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group // <-- Import Group
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager // <-- Import
import androidx.recyclerview.widget.RecyclerView // <-- Import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
// Import your Adapter and DatabaseManager/DAO
import cit.edu.WildcatFreshFinds.ProductAdapter // Adjust import if needed
import cit.edu.WildcatFreshFinds.DatabaseManager // Adjust import if needed


class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var uniformButton: Button
    private lateinit var bookButton: Button
    private lateinit var allButton: Button
    private var currentButton: Button? = null

    // --- RecyclerView related ---
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var groupNothingHere: Group // To control visibility of "nothing here" views
    // --- End RecyclerView related ---
    private lateinit var greetingNameTextView: TextView


    // onCreateView can remain the same or be removed if using Fragment constructor layout ID
    // override fun onCreateView(
    //     inflater: LayoutInflater,
    //     container: ViewGroup?,
    //     savedInstanceState: Bundle?
    // ): View? {
    //     return inflater.inflate(R.layout.fragment_home, container, false)
    // }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // val txt = view.findViewById<TextView>(R.id.no_items_txt) // Use groupNothingHere instead

        // --- Find Views ---
        greetingNameTextView = view.findViewById(R.id.full_name_label)
        allButton = view.findViewById(R.id.all_category_button)
        uniformButton = view.findViewById(R.id.uniform_category_button)
        bookButton = view.findViewById(R.id.book_category_button)
        productRecyclerView = view.findViewById(R.id.product_recycler_view)
        groupNothingHere = view.findViewById(R.id.group_nothing_here)
        // --- End Find Views ---

        // --- Setup RecyclerView ---
        setupRecyclerView()
        // --- End Setup RecyclerView ---


        // --- Load User Name ---
        viewLifecycleOwner.lifecycleScope.launch {
            var displayGreetingName = "Guest" // Default value
            try {
                // Fetch the user object which now has firstName and lastName
                val user = withContext(Dispatchers.IO) { UserManager.getSignedIn() }

                // Use firstName for the greeting, fallback if null or blank
                if (user != null && !user.firstName.isNullOrBlank()) {
                    displayGreetingName = user.firstName!! // Use !! because we checked isNullOrBlank
                } else if (user != null) {
                    // Optional: Fallback to LastName or generic 'User' if firstName is blank but user exists
                    displayGreetingName = user.lastName ?: "User"
                }
                // If user is null, it remains "Guest"
                Log.d("HomeFragment", "Setting greeting name to: $displayGreetingName")

            } catch (e: Exception) {
                Log.e("HomeFragment", "Error getting user name for greeting: ${e.message}", e)
                displayGreetingName = "User" // Fallback on error
            }
            // Set the text on the main thread
            greetingNameTextView.text = " $displayGreetingName" // Add space after "Hi" which is in XML
        }
        // --- End Updated Load User Name ---


        if (savedInstanceState == null) {
            currentButton = allButton
            setActiveButton(allButton)
        }

        // --- Button Click Listeners ---
        allButton.setOnClickListener {
            Log.d("HomeFragment", "All Category Button Clicked")
            // showToast("All Category Button") // Can disable toasts if list updates
            updateButtonState(allButton)
            // TODO: Implement filtering logic if needed, otherwise observer handles 'all'
        }

        uniformButton.setOnClickListener {
            Log.d("HomeFragment", "Uniform Category Button Clicked")
            // showToast("Uniform Category Button")
            updateButtonState(uniformButton)
            // TODO: Implement filtering logic based on category
        }

        bookButton.setOnClickListener {
            Log.d("HomeFragment", "Book Category Button Clicked")
            // showToast("Book Category Button")
            updateButtonState(bookButton)
            // TODO: Implement filtering logic based on category
        }
        // --- End Button Click Listeners ---


        // --- Observe Product Data ---
        observeProducts()
        // --- End Observe Product Data ---
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter()
        productAdapter.setOnItemClickListener { product ->
            Log.d("HomeFragment", "Product clicked: ${product.name}")
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product) // Pass Parcelable
            startActivity(intent)
        }

        productRecyclerView.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        Log.d("HomeFragment", "RecyclerView setup complete.")
    }



    private fun observeProducts() {
        try {
            // --- Use AppDatabase Singleton Directly ---
            val productDao = AppDatabase.getDatabase(requireContext()).productDao()

            productDao.getAllProducts().observe(viewLifecycleOwner) { products ->
                Log.d("HomeFragment", "Products observed: ${products?.size ?: 0} items")
                productAdapter.submitList(products)

                // Toggle visibility
                val isEmpty = products.isNullOrEmpty()
                productRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
                groupNothingHere.visibility = if (isEmpty) View.VISIBLE else View.GONE
                Log.d("HomeFragment", if(isEmpty) "Showing empty view" else "Showing product list")
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error observing products: ${e.message}", e)
            showToast("Error loading products.")
            productRecyclerView.visibility = View.GONE
            groupNothingHere.visibility = View.VISIBLE
        }
    }

    // Keep button state update functions
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