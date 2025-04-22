package cit.edu.WildcatFreshFinds.view_model // Or 'viewmodel' package

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.work.*
import cit.edu.WildcatFreshFinds.dao.AppDatabase
import cit.edu.WildcatFreshFinds.dao.OngoingTransactionDao
import cit.edu.WildcatFreshFinds.dao.ProductDao
import cit.edu.WildcatFreshFinds.data.OngoingTransaction
import cit.edu.WildcatFreshFinds.data.Product
import cit.edu.WildcatFreshFinds.data.TransactionState
import cit.edu.WildcatFreshFinds.utility.Event
import cit.edu.WildcatFreshFinds.utility.TransactionTimeoutWorker
import cit.edu.WildcatFreshFinds.utility.UserManager
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
            _isCurrentUserSeller.value = false
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
                val currentDbQuantity = withContext(Dispatchers.IO) { productDao.getProductById(product.id)?.quantity ?: -1 }
                if (quantityBought <= 0 || quantityBought > currentDbQuantity) {
                    _toastMessage.postValue(Event("Quantity unavailable or invalid (currently $currentDbQuantity)."))
                    return@launch
                }
                if (product.sellerEmail == null || product.price == null) { /* ... error handling ... */ return@launch}

                val newProductQuantity = currentDbQuantity - quantityBought
                val updatedProduct = product.copy(quantity = newProductQuantity)

                val newTransaction = OngoingTransaction(
                    productId = product.id,
                    productName = product.name,
                    productImageUrl = product.imageUrl,
                    pricePerItem = product.price,
                    quantityBought = quantityBought,
                    totalPrice = product.price * quantityBought,
                    sellerEmail = product.sellerEmail, // Already checked not null
                    buyerEmail = buyerEmail,
                    state = TransactionState.ONGOING // <-- Set initial state
                )

                withContext(Dispatchers.IO) {
                    productDao.update(updatedProduct)
                    transactionDao.insertTransaction(newTransaction)
                    Log.d("ProductDetailViewModel", "DB updated: Product quantity decreased, Transaction inserted.")
                }

                _toastMessage.postValue(Event("Purchase initiated!"))
                _purchaseInitiated.postValue(Event(true))

            } catch (e: Exception) {
                Log.e("ProductDetailViewModel", "Error during purchase initiation: ${e.message}", e)
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
            ExistingWorkPolicy.REPLACE,
            timeoutWorkRequest
        )
        Log.i("ProductDetailViewModel", "Enqueued timeout worker for Tx ID: $transactionId with delay $delay ms")
    }
}
