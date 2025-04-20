package cit.edu.WildcatFreshFinds // Or viewmodel package

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File // For deleting image file

class MyListingsViewModel(application: Application) : AndroidViewModel(application) {

    private val productDao: ProductDao = AppDatabase.getDatabase(application).productDao()
    private val transactionDao: OngoingTransactionDao = AppDatabase.getDatabase(application).ongoingTransactionDao()

    private val _currentUserEmail = MutableLiveData<String?>()

    // LiveData for the user's product listings
    val userListings: LiveData<List<Product>> = _currentUserEmail.switchMap { email ->
        if (email == null) {
            MutableLiveData() // Empty
        } else {
            productDao.getProductsBySeller(email) // Observe products by seller
        }
    }

    // LiveData for feedback messages
    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

    init {
        loadCurrentUserEmail()
    }

    private fun loadCurrentUserEmail() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { UserManager.getSignedIn() }
            _currentUserEmail.postValue(user?.email)
        }
    }

    fun deleteListing(product: Product) {
        viewModelScope.launch { // Use viewModelScope
            try {
                // Check for active transactions first
                val activeTransactionCount = withContext(Dispatchers.IO) {
                    transactionDao.countActiveTransactionsForProduct(product.id)
                }
                Log.d("MyListingsViewModel", "Product ${product.id} has $activeTransactionCount active transactions.")

                if (activeTransactionCount > 0) {
                    // Cannot delete, show message
                    _toastMessage.postValue(Event("Cannot delete '${product.name}': Active transactions exist."))
                } else {
                    // No active transactions, proceed with deletion
                    val rowsDeleted = withContext(Dispatchers.IO) {
                        productDao.deleteProductById(product.id) // Use delete by ID
                        // Or productDao.delete(product) if using @Delete
                    }

                    if (rowsDeleted > 0) {
                        Log.i("MyListingsViewModel", "Deleted product ${product.id} from DB.")
                        // Also delete the associated image file from internal storage
                        deleteProductImageFile(product.imageUrl)
                        _toastMessage.postValue(Event("Deleted '${product.name}' successfully."))
                        // LiveData driving userListings will update automatically
                    } else {
                        Log.w("MyListingsViewModel", "Failed to delete product ${product.id} from DB (already deleted?).")
                        _toastMessage.postValue(Event("Could not delete '${product.name}'."))
                    }
                }
            } catch (e: Exception) {
                Log.e("MyListingsViewModel", "Error deleting product ${product.id}: ${e.message}", e)
                _toastMessage.postValue(Event("Error deleting product."))
            }
        }
    }

    // Helper to delete image file
    private fun deleteProductImageFile(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        viewModelScope.launch(Dispatchers.IO) { // IO for file operations
            try {
                val file = File(filePath)
                if (file.exists()) {
                    if (file.delete()) {
                        Log.d("MyListingsViewModel", "Deleted image file: $filePath")
                    } else {
                        Log.w("MyListingsViewModel", "Failed to delete image file: $filePath")
                    }
                }
            } catch (e: Exception) {
                Log.e("MyListingsViewModel", "Error deleting image file $filePath", e)
            }
        }
    }
}