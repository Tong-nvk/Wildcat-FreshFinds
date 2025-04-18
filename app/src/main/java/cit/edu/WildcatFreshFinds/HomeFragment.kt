package cit.edu.WildcatFreshFinds

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.LiveData


class HomeFragment : Fragment(R.layout.fragment_home) {


    private lateinit var productRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var groupNothingHere: Group
    private lateinit var greetingNameTextView: TextView
    private lateinit var searchInput: EditText
    private var currentProductSource: LiveData<List<Product>>? = null
    private val productsLiveData = MediatorLiveData<List<Product>>().apply {
        value = emptyList()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        greetingNameTextView = view.findViewById(R.id.full_name_label)
        searchInput = view.findViewById(R.id.search_input) // <-- Find search input
        productRecyclerView = view.findViewById(R.id.product_recycler_view)
        groupNothingHere = view.findViewById(R.id.group_nothing_here)

        setupRecyclerView()
        loadUserName()
        setupSearch()
        observeProducts()
        loadProducts("")

    }
    private fun loadUserName(){
        viewLifecycleOwner.lifecycleScope.launch {
            var displayGreetingName = "Guest"
            try {
                val user = withContext(Dispatchers.IO) { UserManager.getSignedIn() }
                if (user != null && !user.firstName.isNullOrBlank()) {
                    displayGreetingName = user.firstName!!
                } else if (user != null) {
                    displayGreetingName = user.lastName ?: "User"
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error getting user name: ${e.message}", e)
                displayGreetingName = "User"
            }
            greetingNameTextView.text = " $displayGreetingName"
        }
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

    private fun setupSearch() {
        searchInput.addTextChangedListener { editable ->
            val query = editable?.toString()?.trim() ?: ""
            loadProducts(query)
        }
        Log.d("HomeFragment", "Search input listener setup.")
    }

    private fun observeProducts() {
        productsLiveData.observe(viewLifecycleOwner) { products ->

            Log.d("HomeFragment", "MediatorLiveData updated: ${products?.size ?: 0} items")
            productAdapter.submitList(products ?: emptyList()) // Handle potential null from mediator? (safer)

            // Toggle visibility - handles initial empty state correctly now
            val isEmpty = products.isNullOrEmpty()
            productRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            groupNothingHere.visibility = if (isEmpty) View.VISIBLE else View.GONE
            Log.d("HomeFragment", if (isEmpty) "Showing empty view" else "Showing product list")
        }
        Log.d("HomeFragment", "Observing MediatorLiveData for products.")
    }

    private fun loadProducts(query: String) {
        Log.d("HomeFragment", "Loading products for query: '$query'")
        try {
            val productDao = AppDatabase.getDatabase(requireContext()).productDao()

            currentProductSource?.let {
                productsLiveData.removeSource(it)
                Log.d("HomeFragment", "Removed previous LiveData source.")
            }

            val newSource: LiveData<List<Product>> = if (query.isBlank()) {
                productDao.getAllProducts()
            } else {
                val searchQuery = "%$query%"
                productDao.searchProducts(searchQuery)
            }

            currentProductSource = newSource
            Log.d("HomeFragment", "Adding new LiveData source.")

            productsLiveData.addSource(newSource) { productList ->
                productsLiveData.value = productList ?: emptyList()
                Log.d("HomeFragment", "LiveData source emitted ${productList?.size ?: "null"} items. Updated mediator.")
            }



        } catch (e: Exception) {
            Log.e("HomeFragment", "Error loading products for query '$query': ${e.message}", e)
            showToast("Error loading products.")
            currentProductSource?.let { productsLiveData.removeSource(it) } // Clean up source on error
            currentProductSource = null
            productsLiveData.value = emptyList() // Set to empty on error
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}