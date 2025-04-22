package cit.edu.WildcatFreshFinds.view_model

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.work.WorkManager
import cit.edu.WildcatFreshFinds.dao.AppDatabase
import cit.edu.WildcatFreshFinds.dao.OngoingTransactionDao
import cit.edu.WildcatFreshFinds.dao.ProductDao
import cit.edu.WildcatFreshFinds.dao.UserDao
import cit.edu.WildcatFreshFinds.data.OngoingTransaction
import cit.edu.WildcatFreshFinds.data.TransactionState
import cit.edu.WildcatFreshFinds.utility.Event
import cit.edu.WildcatFreshFinds.utility.UserManager

class OngoingTransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionDao: OngoingTransactionDao = AppDatabase.getDatabase(application).ongoingTransactionDao()
    private val userDao: UserDao = AppDatabase.getDatabase(application).userDao()
    private val productDao: ProductDao = AppDatabase.getDatabase(application).productDao()

    private val _currentUserEmail = MutableLiveData<String?>()
    val currentUserEmail: LiveData<String?> = _currentUserEmail

    val buyerTransactions: LiveData<List<OngoingTransaction>> = _currentUserEmail.switchMap { email ->
        if (email == null) {
            MutableLiveData()
        } else {
            transactionDao.getBuyerTransactionsByState(email,
                listOf(TransactionState.ONGOING, TransactionState.SELLER_CONFIRMED)
            )
        }
    }

    val sellerTransactions: LiveData<List<OngoingTransaction>> = _currentUserEmail.switchMap { email ->
        if (email == null) {
            MutableLiveData() // Empty
        } else {
            transactionDao.getSellerTransactionsByState(email,
                listOf(TransactionState.ONGOING, TransactionState.BUYER_CONFIRMED) // Add others if needed
            )
        }
    }

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

    // --- Buyer Actions ---
    fun confirmReceiptAsBuyer(transaction: OngoingTransaction) {
        if (transaction.buyerEmail != _currentUserEmail.value) {
            _toastMessage.postValue(Event("Error: You are not the buyer."))
            return
        }
        if (transaction.state != TransactionState.ONGOING && transaction.state != TransactionState.SELLER_CONFIRMED) {
            Log.w("OngoingTxViewModel", "Buyer cannot confirm receipt in state ${transaction.state}")
            _toastMessage.postValue(Event("Action not available in current state."))
            return
        }
        Log.d("OngoingTxViewModel", "Buyer confirming receipt for ${transaction.transactionId}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentTx = transactionDao.getTransactionById(transaction.transactionId)
                if (currentTx == null) {
                    Log.e("OngoingTxViewModel", "Transaction ${transaction.transactionId} not found for buyer confirm.")
                    _toastMessage.postValue(Event("Error: Transaction not found."))
                    return@launch
                }

                if (currentTx.state == TransactionState.SELLER_CONFIRMED) {
                    transactionDao.updateTransactionState(
                        transaction.transactionId,
                        TransactionState.COMPLETED,
                        completionTime = System.currentTimeMillis()
                    )
                    Log.i("OngoingTxViewModel", "Transaction ${transaction.transactionId} COMPLETED.")
                    _toastMessage.postValue(Event("Transaction Completed!"))
                    cancelTimeoutWorker(getApplication(), transaction.transactionId)
                } else if (currentTx.state == TransactionState.ONGOING) {
                    // Seller hasn't confirmed yet, just mark buyer confirmed
                    transactionDao.updateTransactionState(transaction.transactionId, TransactionState.BUYER_CONFIRMED)
                    Log.i("OngoingTxViewModel", "Transaction ${transaction.transactionId} marked as BUYER_CONFIRMED.")
                    _toastMessage.postValue(Event("Receipt confirmed. Waiting for seller."))
                } else {
                    Log.w("OngoingTxViewModel", "Transaction ${transaction.transactionId} state changed unexpectedly before buyer confirm. Current: ${currentTx.state}")
                    _toastMessage.postValue(Event("Transaction status changed. Please refresh."))
                }
            } catch (e: Exception) {
                Log.e("OngoingTxViewModel", "Error confirming as buyer: ${e.message}", e)
                _toastMessage.postValue(Event("Error confirming receipt."))
            }
        }
    }

    // --- Seller Actions ---
    fun confirmHandoverAsSeller(transaction: OngoingTransaction) {
        if (transaction.sellerEmail != _currentUserEmail.value) {
            _toastMessage.postValue(Event("Error: You are not the seller."))
            return
        }
        if (transaction.state != TransactionState.ONGOING && transaction.state != TransactionState.BUYER_CONFIRMED) {
            Log.w("OngoingTxViewModel", "Seller cannot confirm handover in state ${transaction.state}")
            _toastMessage.postValue(Event("Action not available in current state."))
            return
        }
        Log.d("OngoingTxViewModel", "Seller confirming handover for ${transaction.transactionId}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentTx = transactionDao.getTransactionById(transaction.transactionId)
                if (currentTx == null) { /* ... error handling ... */ return@launch }

                if (currentTx.state == TransactionState.BUYER_CONFIRMED) {
                    transactionDao.updateTransactionState(
                        transaction.transactionId,
                        TransactionState.COMPLETED,
                        completionTime = System.currentTimeMillis()
                    )
                    Log.i("OngoingTxViewModel", "Transaction ${transaction.transactionId} COMPLETED.")
                    _toastMessage.postValue(Event("Transaction Completed!"))
                    cancelTimeoutWorker(getApplication(), transaction.transactionId)
                } else if (currentTx.state == TransactionState.ONGOING){
                    // Buyer hasn't confirmed yet, just mark seller confirmed
                    transactionDao.updateTransactionState(transaction.transactionId, TransactionState.SELLER_CONFIRMED)
                    Log.i("OngoingTxViewModel", "Transaction ${transaction.transactionId} marked as SELLER_CONFIRMED.")
                    _toastMessage.postValue(Event("Handover confirmed. Waiting for buyer."))
                } else {
                    Log.w("OngoingTxViewModel", "Transaction ${transaction.transactionId} state changed unexpectedly before seller confirm. Current: ${currentTx.state}")
                    _toastMessage.postValue(Event("Transaction status changed. Please refresh."))
                }
            } catch (e: Exception) {
                Log.e("OngoingTxViewModel", "Error confirming as seller: ${e.message}", e)
                _toastMessage.postValue(Event("Error confirming handover."))
            }
        }
    }

    fun cancelOrReportTransaction(transaction: OngoingTransaction, initiatingUserEmail: String) {
        val isBuyerCancelling = initiatingUserEmail == transaction.buyerEmail
        val isSellerCancelling = initiatingUserEmail == transaction.sellerEmail

        if (!isBuyerCancelling && !isSellerCancelling) {
            Log.e("OngoingTxViewModel", "Initiator $initiatingUserEmail is not part of Tx ${transaction.transactionId}")
            _toastMessage.postValue(Event("Error: You are not part of this transaction."))
            return
        }

        val newState = if(isBuyerCancelling) TransactionState.CANCELLED_BY_BUYER else TransactionState.CANCELLED_BY_SELLER
        val otherPartyEmail = if (isBuyerCancelling) transaction.sellerEmail else transaction.buyerEmail

        Log.d("OngoingTxViewModel", "Processing Cancel/Report for Tx: ${transaction.transactionId} by $initiatingUserEmail. New state: $newState")

        if (otherPartyEmail.isBlank()) {
            Log.e("OngoingTxViewModel", "Cannot process cancel/report, other party email missing.")
            _toastMessage.postValue(Event("Error processing request: Participant info missing."))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentTx = transactionDao.getTransactionById(transaction.transactionId)
                if (currentTx == null || currentTx.state == TransactionState.COMPLETED ||
                    currentTx.state == TransactionState.CANCELLED_BY_BUYER ||
                    currentTx.state == TransactionState.CANCELLED_BY_SELLER ||
                    currentTx.state == TransactionState.EXPIRED) {
                    Log.w("OngoingTxViewModel", "Cannot cancel/report transaction ${transaction.transactionId}, already finalized or gone (State: ${currentTx?.state}).")
                    _toastMessage.postValue(Event("Transaction already finalized or cancelled."))
                    return@launch
                }

                cancelTimeoutWorker(getApplication(), transaction.transactionId)

                val rowsAffected = userDao.incrementReceivedCancellations(otherPartyEmail)
                if (rowsAffected > 0) Log.d("OngoingTxViewModel", "Incremented received cancellations for: $otherPartyEmail")
                else Log.w("OngoingTxViewModel", "Failed to increment cancellation count for: $otherPartyEmail")

                val qtyRestored = productDao.addQuantityToProduct(transaction.productId, transaction.quantityBought)
                if(qtyRestored > 0) Log.d("OngoingTxViewModel", "Restored quantity for product: ${transaction.productId}")
                else Log.w("OngoingTxViewModel", "Failed to restore quantity for product: ${transaction.productId}")

                transactionDao.updateTransactionState(
                    transaction.transactionId,
                    newState,
                    cancellationTime = System.currentTimeMillis()
                )
                Log.d("OngoingTxViewModel", "Updated transaction ${transaction.transactionId} state to $newState.")

                _toastMessage.postValue(Event("Transaction cancelled."))

            } catch (e: Exception) {
                Log.e("OngoingTxViewModel", "Error cancelling/reporting transaction: ${e.message}", e)
                _toastMessage.postValue(Event("Error cancelling transaction: ${e.localizedMessage}"))
            }
        }
    }
    private fun cancelTimeoutWorker(appContext: Context, transactionId: String) {
        val workName = "timeout_$transactionId"
        WorkManager.getInstance(appContext).cancelUniqueWork(workName)
        Log.d("ViewModel", "Attempted to cancel timeout worker with name: $workName")
    }
}