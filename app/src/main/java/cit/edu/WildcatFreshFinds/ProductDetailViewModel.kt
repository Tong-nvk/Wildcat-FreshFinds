package cit.edu.WildcatFreshFinds // Or 'viewmodel' package

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.work.*
import java.util.concurrent.TimeUnit

class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val productDao: ProductDao = AppDatabase.getDatabase(application).productDao()
    private val transactionDao: OngoingTransactionDao = AppDatabase.getDatabase(application).ongoingTransactionDao()

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

    private val _purchaseInitiated = MutableLiveData<Event<Boolean>>()
    val purchaseInitiated: LiveData<Event<Boolean>> = _purchaseInitiated

    private val _isCurrentUserSeller = MutableLiveData<Boolean>()
    val isCurrentUserSeller: LiveData<Boolean> = _isCurrentUserSeller


    fun checkSellerStatus(product: Product?) {
        val sellerEmail = product?.sellerEmail
        if (sellerEmail == null) {
            _isCurrentUserSeller.value = false // Cannot be seller if product has no seller email
            return
        }
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { UserManager.getSignedIn() }
            _isCurrentUserSeller.postValue(user?.email != null && user.email == sellerEmail)
        }
    }

    fun initiatePurchase(product: Product, quantityBought: Int, buyerEmail: String) {
        Log.d("ProductDetailViewModel", "Initiating purchase VM logic for ${product.id}, quantity: $quantityBought")
        viewModelScope.launch {
            try {
                // Final checks before DB operations
                val currentProductState = withContext(Dispatchers.IO) { productDao.findById(product.id)?.quantity ?: -1 }
                if (quantityBought <= 0 || quantityBought > currentProductState) {
                    _toastMessage.postValue(Event("Quantity unavailable or invalid."))
                    return@launch
                }
                if (product.sellerEmail == null || product.price == null) {
                    _toastMessage.postValue(Event("Error: Incomplete product data."))
                    return@launch
                }

                val newProductQuantity = product.quantity - quantityBought
                val updatedProduct = product.copy(quantity = newProductQuantity)
                val newTransaction = OngoingTransaction(
                    productId = product.id,
                    productName = product.name,
                    productImageUrl = product.imageUrl,
                    pricePerItem = product.price,
                    quantityBought = quantityBought,
                    totalPrice = product.price * quantityBought,
                    sellerEmail = product.sellerEmail,
                    buyerEmail = buyerEmail
                )

                // Perform DB operations
                withContext(Dispatchers.IO) {
                    productDao.update(updatedProduct) // Update product quantity
                    transactionDao.insertTransaction(newTransaction) // Insert transaction record
                    Log.d("ProductDetailViewModel", "DB updated: Product quantity decreased, Transaction inserted.")
                    scheduleTimeoutWorker(getApplication(), newTransaction.transactionId, newTransaction.deadlineTimestamp)
                }

                _toastMessage.postValue(Event("Purchase initiated!"))
                _purchaseInitiated.postValue(Event(true))

            } catch (e: Exception) {
                Log.e("ProductDetailViewModel", "Error during purchase initiation (${e::class.java.simpleName}): ${e.message}", e)
                _toastMessage.postValue(Event("Purchase failed. Please try again. ${e.localizedMessage}"))
            }
        }
    }

    private fun scheduleTimeoutWorker(appContext: Context, transactionId: String, deadlineMillis: Long) {
        val currentTime = System.currentTimeMillis()
        val delay = deadlineMillis - currentTime
        Log.d("ViewModel", "Scheduling worker for $transactionId with delay ${delay}ms")

        if (delay <= 0) {
            Log.w("ViewModel", "Deadline for $transactionId already passed? Not scheduling.")
            return
        }

        val workerData = workDataOf("TRANSACTION_ID" to transactionId)
        val timeoutWorkRequest = OneTimeWorkRequestBuilder<TransactionTimeoutWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workerData)
            .addTag("transaction_timeout")
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            "timeout_$transactionId",
            ExistingWorkPolicy.REPLACE, // Replace if already scheduled for same Tx ID
            timeoutWorkRequest
        )
        Log.i("ProductDetailViewModel", "Enqueued timeout worker for Tx ID: $transactionId with delay $delay ms")
    }
}
